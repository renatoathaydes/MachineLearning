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

	// see https://en.wikipedia.org/wiki/Levenshtein_distance
	@Memoized
	int stringDistance( String s1, String s2 ) {
		if ( s1.empty ) return s2.size()
		if ( s2.empty ) return s1.size()

		/* test if last characters of the strings match */
		def cost = ( s1[ -1 ] == s2[ -1 ] ) ? 0 : 1

		/* return minimum of delete char from s1, delete char from s2, and delete char from both */
		return Math.min(
				Math.min( stringDistance( s1.take( s1.size() - 1 ), s2 ) + 1,
						stringDistance( s1, s2.take( s2.size() - 1 ) ) + 1 ),
				stringDistance( s1.take( s1.size() - 1 ), s2.take( s2.size() - 1 ) ) + cost )
	}

}
