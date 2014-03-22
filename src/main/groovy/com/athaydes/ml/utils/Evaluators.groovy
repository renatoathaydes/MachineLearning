package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Program
import groovy.transform.Memoized

/**
 *
 * User: Renato
 */
class Evaluators {

	final Closure numberEvaluator = { Program p1, Program p2 ->
		def p1Val = p1.eval()
		def p2Val = p2.eval()
		if ( !p1Val ) return p2Val ? 1 : 0
		if ( !p2Val ) return p1Val ? -1 : 0
		def v1 = Math.abs( p1.specification.out.first() - p1Val )
		def v2 = Math.abs( p2.specification.out.first() - p2Val )
		v1 != v2 ? toInt( v1 - v2 ) : p1.code.size() - p2.code.size()
	}

	final Closure stringEvaluator = { Program p1, Program p2 ->
		String p1Val = p1.eval()
		String p2Val = p2.eval()
		if ( !p1Val ) return p2Val ? 1 : 0
		if ( !p2Val ) return p1Val ? -1 : 0
		def v1 = stringDistance p1.specification.out.first(), p1Val
		def v2 = stringDistance p2.specification.out.first(), p2Val
		v1 != v2 ? v1 - v2 : p1.code.size() - p2.code.size()
	}

	int toInt( value ) {
		if ( value > 0 ) Math.ceil( value )
		else Math.floor( value )
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
