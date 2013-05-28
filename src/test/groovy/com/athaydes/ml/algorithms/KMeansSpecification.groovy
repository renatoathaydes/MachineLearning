package com.athaydes.ml.algorithms

/**
 *
 * User: Renato
 */
class KmeansSpecification extends spock.lang.Specification {

	def "K-mean clusters must always have unique IDs"( ) {
		given:
		"Clusters of size $size"
		def algorithm = new KMeans( size )

		expect:
		"The ID of each cluster should be different"
		( algorithm.clusters*.id ).unique().size() == size

		where:
		size << [ 1, 2, 3, 10, 10000 ]
	}

	def """A new sample goes into an empty cluster unless its value matches
           a non-empty cluster's mean exactly"""( ) {
		given:
		"$k clusters and samples $samples"
		def algorithm = new KMeans( k )
		def simpleSamples = samples.collect { new SimpleSample( it ) }
		algorithm.classifyAll simpleSamples

		expect:
		"The cluster's means should be: $expectedMeans"
		def means = algorithm.clusters.collect { it.mean }
		assert means.sort() == expectedMeans

		where:
		k | samples     | expectedMeans
		2 | [ 5 ]       | [ null, 5 ]
		2 | [ 5, 5 ]    | [ null, 5 ]
		2 | [ 5, 10 ]   | [ 5, 10 ]
		3 | [ 1 ]       | [ null, null, 1 ]
		3 | [ 1, 2 ]    | [ null, 1, 2 ]
		3 | [ 1, 2, 3 ] | [ 1, 2, 3 ]
		3 | [ 1, 1 ]    | [ null, null, 1 ]
		3 | [ 1, 1, 2 ] | [ null, 1, 2 ]

	}

}
