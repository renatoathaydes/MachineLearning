package com.athaydes.ml.algorithms

import com.athaydes.ml.algorithms.KMeans.Cluster
import org.junit.Test

/**
 *
 * User: Renato
 */
class KMeansTest {

	@Test
	void clustersHaveDifferentIds( ) {
		def clustersCount = 10000
		KMeans algorithm = new KMeans( clustersCount )
		def clusterIds = algorithm.clusters*.id as Set
		assert clusterIds.size() == clustersCount
	}

	@Test
	void firstSamplesClassifiedAsExactMatchOrEmptyCluster( ) {
		KMeans algorithm = new KMeans( 3 )

		SimpleSample sample1 = new SimpleSample( 2 )
		Cluster c1 = algorithm.classify( sample1 )
		Cluster c2 = algorithm.classify( sample1 )
		assert c1 == c2
		assert c1.mean == 2

		SimpleSample sample2 = new SimpleSample( 5 )
		Cluster c3 = algorithm.classify( sample2 )
		Cluster c4 = algorithm.classify( sample2 )
		assert c3 == c4
		assert c1 != c3
		assert c3.mean == 5

		SimpleSample sample3 = new SimpleSample( 10 )
		Cluster c5 = algorithm.classify( sample3 )
		assert c5 != c1 && c5 != c3
		assert c5.mean == 10
	}

	@Test
	void testClassifyAll( ) {
		KMeans oneByOneAlgorithm = new KMeans( 2 )
		KMeans batchAlgorithm = new KMeans( 2 )

		def rand = new Random()
		def items = ( 50..100 ).collect { new SimpleSample( it * rand.nextInt( 10000 ) ) }
		items.each { oneByOneAlgorithm.classify it }
		batchAlgorithm.classifyAll items

		def oneByOneValues = [ ]
		oneByOneAlgorithm.clusters.each {
			oneByOneValues << ( oneByOneAlgorithm.store.getSamples( it.id )*.value ).sort()
		}

		def batchValues = [ ]
		batchAlgorithm.clusters.each {
			batchValues << ( batchAlgorithm.store.getSamples( it.id )*.value ).sort()
		}

		assert oneByOneValues.size() == 2
		assert batchValues.size() == 2

		if ( oneByOneValues[ 0 ].size() == batchValues[ 0 ].size() ) {
			assert oneByOneValues[ 0 ] == batchValues[ 0 ]
			assert oneByOneValues[ 1 ] == batchValues[ 1 ]
		} else {
			assert oneByOneValues[ 0 ] == batchValues[ 1 ]
			assert oneByOneValues[ 1 ] == batchValues[ 0 ]
		}


	}

	@Test
	void samplesCanHaveAnyType( ) {
		KMeans algorithm = new KMeans( 5 )

		SimpleSample sample1 = new SimpleSample( 2.5 )
		Cluster c1 = algorithm.classify( sample1 )
		SimpleSample sample2 = new SimpleSample( 2.6 )
		Cluster c2 = algorithm.classify( sample2 )
		SimpleSample sample3 = new SimpleSample( 2.7 )
		Cluster c3 = algorithm.classify( sample3 )
		SimpleSample sample4 = new SimpleSample( 2.8 )
		Cluster c4 = algorithm.classify( sample4 )
		SimpleSample sample5 = new SimpleSample( 2.9 )
		Cluster c5 = algorithm.classify( sample5 )

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

		SimpleSample sample6 = new SimpleSample( 1000.0 )
		Cluster c6 = algorithm.classify( sample6 )

		assert c6.sampleCount == 1
		assert c6.mean == 1000.0

	}

	@Test
	void samplesGoOnNearestClusterWhenAllClustersHaveSamples( ) {
		KMeans algorithm = new KMeans( 3 )

		SimpleSample sample1 = new SimpleSample( 2 )
		Cluster c1 = algorithm.classify( sample1 )
		SimpleSample sample2 = new SimpleSample( 5 )
		Cluster c2 = algorithm.classify( sample2 )
		SimpleSample sample3 = new SimpleSample( 10 )
		Cluster c3 = algorithm.classify( sample3 )
		assert c1 != c2 && c2 != c3 && c1 != c3

		SimpleSample sample4 = new SimpleSample( 1 )
		Cluster c4 = algorithm.classify( sample4 )
		assert c4 == c1
		assert c4.mean == 1.5

		SimpleSample sample5 = new SimpleSample( 7 )
		Cluster c5 = algorithm.classify( sample5 )
		assert c5 == c2
		assert c5.mean == 6

		SimpleSample sample6 = new SimpleSample( 9 )
		Cluster c6 = algorithm.classify( sample6 )
		assert c6 == c3
		assert c6.mean == 9.5
	}

	@Test
	void clustersChangeWhenNeeded( ) {
		KMeans algorithm = new KMeans( 2 )

		( 1..10 ).each { algorithm.classify( new SimpleSample( it ) ) }
		( 91..100 ).each { algorithm.classify( new SimpleSample( it ) ) }

		def clusters = algorithm.clusters
		assert clusters.size() == 2
		def means = clusters*.mean
		assert means.contains( ( 1..10 ).sum() / 10 )
		assert means.contains( ( 91..100 ).sum() / 10 )
	}

	@Test
	void clustersChangeWhenNeededRandomized( ) {
		KMeans algorithm = new KMeans( 2 )

		List sampleValues = ( ( 1..10 ) + ( 91..100 ) ) as List
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
			List sampleValues = ( ( 1..( 1 * order ) ) +
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

	def runWithTimer( Closure cls ) {
		def startT = System.currentTimeMillis()
		cls()
		System.currentTimeMillis() - startT
	}

}