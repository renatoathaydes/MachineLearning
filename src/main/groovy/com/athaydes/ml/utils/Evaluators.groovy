package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Program
import groovy.transform.Memoized

/**
 *
 * User: Renato
 */
class Evaluators {

	final Closure numberEvaluator = { Program p ->
		def p1Val = p.eval()
		if ( !p1Val ) return Integer.MAX_VALUE
		def diff = Math.abs( p.specification.out.first() - p1Val )
		p.code.size() + 1000 * diff
	}

	final Closure stringEvaluator = { Program p ->
		String p1Val = p.eval()
		if ( !p1Val ) return Integer.MAX_VALUE
		def distance = stringDistance p.specification.out.first(), p1Val
		p.code.size() + 1000 * distance
	}

	// based on https://en.wikipedia.org/wiki/Levenshtein_distance
	@Memoized
	int stringDistance( String s1, String s2 ) {
		if ( s1.empty ) return 5 * s2.size()
		if ( s2.empty ) return 5 * s1.size()

		/* test if last characters of the strings match */
		def cost = ( s1[ -1 ] == s2[ -1 ] ) ? 0 : 1

		/* return minimum of delete char from s1, delete char from s2, and delete char from both */
		use( HasInit ) {
			5 * Math.abs( s1.size() - s2.size() ) + Math.min(
					Math.min( stringDistance( s1.init(), s2 ) + 1,
							stringDistance( s1, s2.init() ) + 1 ),
					stringDistance( s1.init(), s2.init() ) + cost )
		}

	}

	@Category( String )
	class HasInit {
		String init() {
			if ( this ) this.take( this.size() - 1 ) else this
		}
	}

}
