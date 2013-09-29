package com.athaydes.ml.algorithms

import org.junit.Test

/**
 *
 * User: Renato
 */
class KMeansTest {

	@Test
	void "clusters always have different IDs"( ) {
		def clustersCount = 10_000
		def algorithm = new KMeans( clustersCount )
		def clusterIds = algorithm.clusters*.id as Set
		assert clusterIds.size() == clustersCount
	}

	@Test
	void "first samples classified as exact match or empty cluster"( ) {
		def algorithm = new KMeans( 3 )

		def sample1 = new SimpleSample( 2 )
		def c1 = algorithm.classify( sample1 )
		def c2 = algorithm.classify( sample1 )
		assert c1 == c2
		assert c1.mean == 2

		def sample2 = new SimpleSample( 5 )
		def c3 = algorithm.classify( sample2 )
		def c4 = algorithm.classify( sample2 )
		assert c3 == c4
		assert c1 != c3
		assert c3.mean == 5

		def sample3 = new SimpleSample( 10 )
		def c5 = algorithm.classify( sample3 )
		assert c5 != c1 && c5 != c3
		assert c5.mean == 10
	}

	@Test
	void "classifyAll should have the same result as classifying samples one by one"( ) {
		def oneByOneAlgorithm = new KMeans( 4 )
		def batchAlgorithm = new KMeans( 4 )

		def rand = new Random()
		def items = ( 50..100 ).collect { new SimpleSample( it * rand.nextInt( 10000 ) ) }
		items.each { oneByOneAlgorithm.classify it }
		batchAlgorithm.classifyAll items

		def oneByOneValues = oneByOneAlgorithm.clusters.collect {
			( oneByOneAlgorithm.store.getSamples( it.id )*.value ).sort()
		} as Set

		def batchValues = batchAlgorithm.clusters.collect {
			( batchAlgorithm.store.getSamples( it.id )*.value ).sort()
		} as Set

		assert oneByOneValues.size() == 4
		assert batchValues.size() == 4

		assert oneByOneValues == batchValues
	}

	@Test
	void "samples can have any type"( ) {
		def algorithm = new KMeans( 5 )

		def sample1 = new SimpleSample( 2.5 )
		def c1 = algorithm.classify( sample1 )
		def sample2 = new SimpleSample( 2.6 )
		def c2 = algorithm.classify( sample2 )
		def sample3 = new SimpleSample( 2.7 )
		def c3 = algorithm.classify( sample3 )
		def sample4 = new SimpleSample( 2.8 )
		def c4 = algorithm.classify( sample4 )
		def sample5 = new SimpleSample( 2.9 )
		def c5 = algorithm.classify( sample5 )

		assert [ c1, c2, c3, c4, c5 ].unique().size() == 5
		assert c1.mean == 2.5
		assert c1.sampleCount == 1
		assert c2.mean == 2.6
		assert c2.sampleCount == 1
		assert c3.mean == 2.7
		assert c3.sampleCount == 1
		assert c4.mean == 2.8
		assert c4.sampleCount == 1
		assert c5.mean == 2.9
		assert c5.sampleCount == 1

		def sample6 = new SimpleSample( 1000.0 )
		def c6 = algorithm.classify( sample6 )

		assert c6.sampleCount == 1
		assert c6.mean == 1000.0

	}

	@Test
	void "samples go on nearest cluster when all clusters have samples"( ) {
		def algorithm = new KMeans( 3 )

		def sample1 = new SimpleSample( 2 )
		def c1 = algorithm.classify( sample1 )
		def sample2 = new SimpleSample( 5 )
		def c2 = algorithm.classify( sample2 )
		def sample3 = new SimpleSample( 10 )
		def c3 = algorithm.classify( sample3 )
		assert c1 != c2 && c2 != c3 && c1 != c3

		def sample4 = new SimpleSample( 1 )
		def c4 = algorithm.classify( sample4 )
		assert c4 == c1
		assert c4.mean == 1.5

		def sample5 = new SimpleSample( 7 )
		def c5 = algorithm.classify( sample5 )
		assert c5 == c2
		assert c5.mean == 6

		def sample6 = new SimpleSample( 9 )
		def c6 = algorithm.classify( sample6 )
		assert c6 == c3
		assert c6.mean == 9.5
	}

	@Test
	void "clusters change when needed"( ) {
		def algorithm = new KMeans( 2 )

		( 1..10 ).each { algorithm.classify( new SimpleSample( it ) ) }
		( 91..100 ).each { algorithm.classify( new SimpleSample( it ) ) }

		def clusters = algorithm.clusters
		assert clusters.size() == 2
		def means = clusters*.mean
		assert means.contains( ( 1..10 ).sum() / 10 )
		assert means.contains( ( 91..100 ).sum() / 10 )
	}

	@Test
	void "clusters change when needed - randomized"( ) {
		def algorithm = new KMeans( 2 )

		def sampleValues = ( ( 1..10 ) + ( 91..100 ) ) as List
		Collections.shuffle( sampleValues )

		sampleValues.each { algorithm.classify( new SimpleSample( it ) ) }
		def clusters = algorithm.clusters
		assert clusters.size() == 2
		def means = clusters*.mean
		assert means.contains( ( 1..10 ).sum() / 10 )
		assert means.contains( ( 91..100 ).sum() / 10 )
	}

	@Test
	void performanceTest( ) {
		def getSamples = { order ->
			def sampleValues = ( ( 1..( 1 * order ) ) +
					( ( 2 * order + 1 )..( 3 * order ) ) +
					( ( 4 * order + 1 )..( 5 * order ) ) ) as List

			Collections.shuffle( sampleValues )
			sampleValues.collect { new SimpleSample( it ) }
		}

		def sizes = [ 2000, 1500, 1000, 500 ]
		def times = ( 1..5 ).collect {
			sizes.collect { order ->
				def samples = getSamples( order )
				def algorithm = new KMeans( 3 )
				runWithTimer {
					algorithm.classifyAll( samples )
				}
			}
		}

		def timesBySize = [ : ]
		println "${'Cluster size'.center( 20 )} | ${'Time taken (ms)'.center( 20 ) }"
		times.each { t ->
			sizes.eachWithIndex { s, i ->
				timesBySize.get( s, [ ] ) << t[ i ]
				println "${( s as String ).center( 20 )} | ${( t[ i ] as String ).center( 20 )}"
			}
		}
		timesBySize.each { s, ts ->
			println "Avg time for size $s: ${ts.sum() / ts.size()}"
		}

	}

	static runWithTimer( Closure cls ) {
		def startT = System.currentTimeMillis()
		cls()
		System.currentTimeMillis() - startT
	}

}