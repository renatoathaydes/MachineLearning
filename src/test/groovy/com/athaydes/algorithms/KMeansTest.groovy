package test.groovy.com.athaydes.algorithms

import main.groovy.com.athaydes.algorithms.KMeans
import main.groovy.com.athaydes.algorithms.KMeans.Cluster
import main.groovy.com.athaydes.algorithms.Sample
import org.junit.Test

/**
 * 
 * User: Renato
 */
class KMeansTest  {

	@Test
	void clustersHaveDifferentIds( ) {
		def clustersCount = 10000
		KMeans algorithm = new KMeans(clustersCount)
		def clusterIds = algorithm.clusters*.id as Set
		assert clusterIds.size() == clustersCount
	}

	@Test
	void firstSamplesClassifiedAsExactMatchOrEmptyCluster( ) {
		KMeans algorithm = new KMeans(3)

		Sample sample1 = new Sample(2)
		Cluster c1 = algorithm.classify( sample1 )
		Cluster c2 = algorithm.classify( sample1 )
		assert c1 == c2
		assert c1.mean == 2

		Sample sample2 = new Sample(5)
		Cluster c3 = algorithm.classify( sample2 )
		Cluster c4 = algorithm.classify( sample2 )
		assert c3 == c4
		assert c1 != c3
		assert c3.mean == 5

		Sample sample3 = new Sample(10)
		Cluster c5 = algorithm.classify( sample3 )
		assert c5 != c1 && c5 != c3
		assert c5.mean == 10
	}

	@Test
	void samplesCanHaveAnyType( ) {
		KMeans algorithm = new KMeans(5)

		Sample sample1 = new Sample(2.5)
		Cluster c1 = algorithm.classify( sample1 )
		Sample sample2 = new Sample(2.6)
		Cluster c2 = algorithm.classify( sample2 )
		Sample sample3 = new Sample(2.7)
		Cluster c3 = algorithm.classify( sample3 )
		Sample sample4 = new Sample(2.8)
		Cluster c4 = algorithm.classify( sample4 )
		Sample sample5 = new Sample(2.9)
		Cluster c5 = algorithm.classify( sample5 )

		assert [c1, c2, c3, c4, c5].unique().size() == 5
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

		Sample sample6 = new Sample(1000.0)
		Cluster c6 = algorithm.classify( sample6 )

		assert c6.sampleCount == 1
		assert c6.mean == 1000.0

	}

	@Test
	void samplesGoOnNearestClusterWhenAllClustersHaveSamples( ) {
		KMeans algorithm = new KMeans(3)

		Sample sample1 = new Sample(2)
		Cluster c1 = algorithm.classify( sample1 )
		Sample sample2 = new Sample(5)
		Cluster c2 = algorithm.classify( sample2 )
		Sample sample3 = new Sample(10)
		Cluster c3 = algorithm.classify( sample3 )
		assert c1 != c2 && c2 != c3 && c1 != c3

		Sample sample4 = new Sample(1)
		Cluster c4 = algorithm.classify( sample4 )
		assert c4 == c1
		assert c4.mean == 1.5

		Sample sample5 = new Sample(7)
		Cluster c5 = algorithm.classify( sample5 )
		assert c5 == c2
		assert c5.mean == 6

		Sample sample6 = new Sample(9)
		Cluster c6 = algorithm.classify( sample6 )
		assert c6 == c3
		assert c6.mean == 9.5
	}

	@Test
	void clustersChangeWhenNeeded( ) {
		KMeans algorithm = new KMeans(2)

		(1..10).each { algorithm.classify( new Sample(it) ) }
		(91..100).each { algorithm.classify( new Sample(it) ) }

		def clusters = algorithm.clusters
		assert clusters.size() == 2
		def means = clusters*.mean
		assert means.contains((1..10).sum() / 10)
		assert means.contains((91..100).sum() / 10)
	}

	@Test
	void clustersChangeWhenNeededRandomized( ) {
		KMeans algorithm = new KMeans(2)

		List sampleValues = ((1..10) + (91..100)) as List
		Collections.shuffle(sampleValues)
		println sampleValues

		sampleValues.each { algorithm.classify( new Sample(it) ) }
		def clusters = algorithm.clusters
		assert clusters.size() == 2
		def means = clusters*.mean
		assert means.contains((1..10).sum() / 10)
		assert means.contains((91..100).sum() / 10)
	}

}