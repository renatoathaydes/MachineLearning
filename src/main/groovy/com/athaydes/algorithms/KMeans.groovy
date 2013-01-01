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
		String id
		while (true) {
			id = UUID.randomUUID().toString()
			if (!(clusters*.id).contains(id)) break;
		}
		return id
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

		println "Sample.value=$sample.value, " + clusters
		reclassifyAfterChanging()
		return cluster
	}

	private Cluster nearestCluster( BigDecimal value ) {
		def spotOnClusters = clusters.grep{ it.mean == value }
		if (spotOnClusters) {
			return spotOnClusters[0]
		}
		def nullMeanClusters = clusters.grep{ it.mean == null }
		if (nullMeanClusters) {
			return nullMeanClusters[0]
		}
		return clusters.sort {
			c1, c2 ->
				distance( c1, value ) - distance( c2, value ) as int
		}[0]
	}

	private void reclassifyAfterChanging() {
		def needReclassify = false
		for (cluster in clusters) {
			def samples = store.getSamples( cluster.id )
			needReclassify = reassessClusterSamples( cluster, samples )
			if (needReclassify) break;
		}
		if (needReclassify) {
			println ( clusters )
			println ( "Store: " + store.map )
			reclassifyAfterChanging()
		}
	}

	private boolean reassessClusterSamples( Cluster cluster, List<Sample> samples ) {
		for ( sample in samples ) {
			Cluster nearest = nearestCluster( sample.value )
			if ( cluster != nearest && nearest.mean != null ) {
				println "Looks like sample $sample.value is in cluster with mean $cluster.mean should be in cluster with mean $nearest.mean"
				cluster - sample; nearest << sample
				return true
			}
		}
		return false
	}

	private static distance( Cluster cluster, BigDecimal value) {
		if (cluster.mean) Math.abs( cluster.mean - value )
		else 0.0
	}

	class Cluster {
		final String id
		BigDecimal mean
		long sampleCount

		Cluster(String id) {
			this.id = id
		}

		Cluster leftShift( Sample sample ) {
			store.add( id, sample )
			mean = store.helper.avg( store, id )
			sampleCount++
			return this
		}

		Cluster minus( sample ) {
			println ( "Removing $sample.value from cluster $id")
			--sampleCount
			if (sampleCount > 0) {
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

class Sample {
	final BigDecimal value
	Sample(value) {
		this.value = value
	}

	String toString( ) {
		"[Sample: $value]"
	}
}

interface ClusterStore {
	void add( name, Sample sample )
	void remove( name )
	List<Sample> getSamples( name )
	final static Helper helper = new Helper()

	static class Helper {
		static BigDecimal avg( ClusterStore store, name ) {
			def values = store.getSamples( name )*.value
			return values.sum() / values.size()
		}
	}
}

class MemoryClusterStore implements ClusterStore {

	final map = [:]

	void add( name, Sample sample ) {
		map.get(name, []) << sample
	}

	List<Sample> getSamples( name ) {
		map.get(name, [])
	}

	void remove( name ) {
		map.remove( name )
	}
}
