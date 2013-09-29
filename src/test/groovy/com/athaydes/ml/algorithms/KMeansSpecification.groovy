package com.athaydes.ml.algorithms

/**
 *
 * User: Renato
 */
class KmeansSpecification extends spock.lang.Specification {

	def "K-mean clusters must always have unique IDs"( ) {
		given:
		"Clusters of <size>"
		def algorithm = new KMeans( size )

		expect:
		"The ID of each cluster should be unique"
		( algorithm.clusters*.id ).unique().size() == size

		where:
		size << [ 1, 2, 3, 10, 10000 ]
	}

	def """A new sample goes into an empty cluster unless its value matches
           a non-empty cluster's mean exactly"""( ) {
		given:
		"<k> clusters and <samples>"
		def algorithm = new KMeans( k )

		when:
		"All the samples are classified"
		def simpleSamples = samples.collect { new SimpleSample( it ) }
		algorithm.classifyAll simpleSamples

		then:
		"The cluster's means should be <expectedMeans>"
		def means = algorithm.clusters.collect { it.mean }
		means.sort() == expectedMeans

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
