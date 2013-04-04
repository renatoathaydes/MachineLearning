package com.athaydes.algorithms

/**
 *
 * User: Renato
 */
class KMeans {
	private final int k
	final List<Cluster> clusters = [ ]
	ClusterStore store = new MemoryClusterStore()

	KMeans( int k ) {
		this.k = k
		k.times { this.@clusters << new Cluster( generateClusterId() ) }
	}

	private String generateClusterId( ) {
		String id = UUID.randomUUID().toString()
		( clusters*.id ).contains( id ) ? generateClusterId() : id
	}

	/**
	 * @return read-only view of the clusters
	 */
	List<Cluster> getClusters( ) {
		Collections.unmodifiableList( clusters )
	}

	def Cluster classify( Sample sample ) {
		Cluster cluster = nearestCluster( sample.value )
		cluster << sample
		reclassifyAfterChanging()
		return cluster
	}

	void classifyAll( Iterable<Sample> samples ) {
		samples.each { sample -> nearestCluster( sample.value ) << sample }
		reclassifyAfterChanging()
	}

	private Cluster nearestCluster( BigDecimal value ) {
		def spotOnClusters = clusters.grep { it.mean == value }
		if ( spotOnClusters ) {
			return spotOnClusters[ 0 ]
		}
		def nullMeanClusters = clusters.grep { it.mean == null }
		if ( nullMeanClusters ) {
			return nullMeanClusters[ 0 ]
		}
		return clusters.min {
			c1, c2 ->
				distance( c1, value ) - distance( c2, value ) as int
		}
	}

	private void reclassifyAfterChanging( ) {
		def needReclassify = false
		for ( cluster in clusters ) {
			def samples = store.getSamples( cluster.id )
			needReclassify = reassessClusterSamples( cluster, samples )
			if ( needReclassify ) break;
		}
		if ( needReclassify ) {
			println( clusters )
			println( "Store: " + store.map )
			reclassifyAfterChanging()
		}
	}

	private boolean reassessClusterSamples( Cluster cluster, List<Sample> samples ) {
		for ( Sample sample in samples ) {
			Cluster nearest = nearestCluster( sample.value )
			if ( cluster != nearest && nearest.mean != null ) {
				println "Looks like sample $sample.value is in cluster with mean $cluster.mean should be in cluster with mean $nearest.mean"
				cluster - sample; nearest << sample
				return true
			}
		}
		return false
	}

	private static distance( Cluster cluster, BigDecimal value ) {
		cluster.mean ? Math.abs( cluster.mean - value ) : 0.0
	}

	class Cluster {
		final String id
		BigDecimal mean
		long sampleCount

		Cluster( String id ) {
			this.id = id
		}

		Cluster leftShift( Sample sample ) {
			store.add( id, sample )
			mean = store.helper.avg( store, id )
			sampleCount++
			return this
		}

		Cluster minus( Sample sample ) {
			println( "Removing $sample.value from cluster $id" )
			--sampleCount
			if ( sampleCount > 0 ) {
				store.getSamples( id ).remove( sample )
				mean = store.helper.avg( store, id )
			} else {
				mean = null
				store.remove( id )
			}
			return this
		}

		String toString( ) {
			"[Cluster mean: $mean, samples: $sampleCount]"
		}
	}

}

interface Sample {
	final BigDecimal value
}

class SimpleSample implements Sample {
	final BigDecimal value

	SimpleSample( value ) {
		this.value = value
	}

	String toString( ) {
		"[SimpleSample: $value]"
	}
}

interface ClusterStore {
	void add( name, sample )

	void remove( name )

	List getSamples( name )

	final static Helper helper = new Helper()

	static class Helper {
		static BigDecimal avg( ClusterStore store, name ) {
			def values = store.getSamples( name )*.value
			return values.sum() / values.size()
		}
	}
}

class MemoryClusterStore implements ClusterStore {

	final map = [ : ]

	void add( name, sample ) {
		map.get( name, [ ] ) << sample
	}

	List getSamples( name ) {
		map.get( name, [ ] )
	}

	void remove( name ) {
		map.remove( name )
	}
}
