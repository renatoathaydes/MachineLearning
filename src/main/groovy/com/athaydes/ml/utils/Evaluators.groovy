package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Program

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
		v1 > v2 ? 1 : v1 < v2 ? -1 : p1.code.size() - p2.code.size()
	}

	final Closure stringEvaluator = { Program p1, Program p2 ->
		String p1Val = p1.eval()
		String p2Val = p2.eval()
		println "Comparing $p1Val and $p2Val"
		if ( !p1Val ) return p2Val ? 1 : 0
		if ( !p2Val ) return p1Val ? -1 : 0
		def v1 = p1.specification.out.first() - p1Val
		def v2 = p2.specification.out.first() - p2Val
		v1 > v2 ? 1 : v1 < v2 ? -1 : p1.code.size() - p2.code.size()
	}

	int stringDistance( String s1, String s2 ) {
		//TODO
		s1.hashCode() - s2.hashCode()
	}

}
