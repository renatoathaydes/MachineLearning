package com.athaydes.ml.algorithms

import groovy.transform.CompileStatic

/**
 *
 * User: Renato
 */
class KMeans {
	private final int k
	final List<Cluster> clusters = [ ]
	ClusterStore store = new MemoryClusterStore()
	def enableLog = false

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

	@CompileStatic
	void classifyAll( Collection<Sample> samples ) {
		for ( Sample sample in samples ) {
			nearestCluster( sample.value ).leftShift( sample )
		}
		//samples.each { sample -> nearestCluster( sample.value ) << sample }
		reclassifyAfterChanging()
	}

	@CompileStatic
	private Cluster nearestCluster( BigDecimal value ) {
		Cluster min = null
		double minDist = Double.MAX_VALUE
		for ( Cluster c in clusters ) {
			if ( c.mean == value ) return c
			else if ( c.mean == null ) return c
			double d
			if ( ( d = distance( c, value ) ) < minDist ) {
				min = c
				minDist = d
			}
		}
		return min
	}

	@CompileStatic
	private void reclassifyAfterChanging( ) {
		def needReclassify = false
		for ( cluster in clusters ) {
			def samples = store.getSamples( cluster.id )
			needReclassify = reassessClusterSamples( cluster, samples )
			if ( needReclassify ) break;
		}
		if ( needReclassify ) {
			if ( enableLog ) {
				println( clusters )
			}
			reclassifyAfterChanging()
		}
	}

	@CompileStatic
	private boolean reassessClusterSamples( Cluster cluster, List<? extends Sample> samples ) {
		for ( sample in samples ) {
			if ( sample.value == null ) throw new RuntimeException( ' sample value was null' )
			Cluster nearest = nearestCluster( sample.value )
			if ( cluster != nearest && nearest.mean != null ) {
				if ( enableLog )
					println "Looks like sample $sample.value is in cluster with mean $cluster.mean" +
							" should be in cluster with mean $nearest.mean"
				cluster.minus sample
				nearest.leftShift sample
				return true
			}
		}
		return false
	}

	@CompileStatic
	private static double distance( Cluster cluster, BigDecimal value ) {
		if ( cluster.mean == null ) 0.0 else Math.abs( cluster.mean.minus( value ).doubleValue() )
	}

	@CompileStatic
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
			if ( enableLog )
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
	BigDecimal getValue( )
}

@CompileStatic
class SimpleSample implements Sample {
	final BigDecimal value

	SimpleSample( BigDecimal value ) {
		this.value = value
	}

	String toString( ) {
		"[SimpleSample: $value]"
	}
}

@CompileStatic
interface ClusterStore {
	void add( String name, Sample sample )

	void remove( String name )

	List<? extends Sample> getSamples( String name )

	final static Helper helper = new Helper()

	static class Helper {
		static BigDecimal avg( ClusterStore store, String name ) {
			double total = 0.0
			List<? extends Sample> samples = store.getSamples( name )
			for ( Sample s in samples ) {
				total += s.value.doubleValue()
			}
			return new BigDecimal( total / samples.size() )
		}
	}
}

@CompileStatic
class MemoryClusterStore implements ClusterStore {

	final Map<String, List<Sample>> map = [ : ]

	void add( String name, Sample sample ) {
		List<? extends Sample> res = map[ name ]
		if ( !res )
			map[ name ] = res = [ ]
		res.add sample

	}

	List<? extends Sample> getSamples( String name ) {
		List<? extends Sample> res = map[ name ]
		if ( !res )
			map[ name ] = res = [ ]
		res
	}

	void remove( String name ) {
		map.remove( name )
	}
}
