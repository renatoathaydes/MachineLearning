package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Program
import com.athaydes.ml.algorithms.Specification
import spock.lang.Shared

/**
 *
 * User: Renato
 */
class EvaluatorsTest extends spock.lang.Specification {

	@Shared
	def evaluators = new Evaluators()
	@Shared
	def coder = new Coder()

	def "Number evaluator can sort programs when Specification uses ints"( ) {
		given:
		"A Specification with the given <inputs> and <out>"
		def spec = new Specification( inputs: inputs, out: [ out ] )

		and:
		"Programs using that Specification and the given <codes>"
		def programs = codes.collect { code -> new Program( specification: spec, code: code ) }

		when:
		"The programs are ranked using a number evaluator"
		def sortedList = programs.sort( false, evaluators.numberEvaluator )

		then:
		"The programs will have the <expectedRanking> starting from 0"
		def indexOfProgramsInSortedList = programs.collect { sortedList.indexOf( it ) }
		indexOfProgramsInSortedList == expectedRanking

		where:
		codes                                                         | inputs       | out | expectedRanking
		[
				coder.writeCode { ld 1; ld 2; add() },
				coder.writeCode { ld 10; ld 2; add() } ]              | [ 1, 2, 10 ] | 0   | [ 0, 1 ]
		[
				coder.writeCode { ld 1; ld 2; add() },
				coder.writeCode { ld 10; ld 2; add() } ]              | [ 1, 2, 10 ] | 10  | [ 1, 0 ]
		[
				coder.writeCode { ld 1; ld 2; add() },
				coder.writeCode { ld 10; ld 2; add() } ]              | [ 1, 2, 10 ] | 3   | [ 0, 1 ]
		[
				coder.writeCode { ld 1; ld 2; add() },
				coder.writeCode { ld 10; ld 2; add() } ]              | [ 1, 2, 10 ] | 12  | [ 1, 0 ]
		[
				coder.writeCode { ld 10; ld 2; add() },
				coder.writeCode { ld 10; ld 1; ld 1; add(); add() } ] | [ 1, 2, 10 ] | 12  | [ 0, 1 ]
		[
				coder.writeCode {},
				coder.writeCode { ld 10 } ]                           | [ 10 ]       | 0   | [ 1, 0 ]

	}

	def "Number evaluator can sort programs when Specification uses doubles"( ) {
		given:
		"A Specification with the given <inputs> and <out>"
		def spec = new Specification( inputs: inputs, out: [ out ] )

		and:
		"Programs using that Specification and the given <codes>"
		def programs = codes.collect { code -> new Program( specification: spec, code: code ) }

		when:
		"The programs are ranked using a number evaluator"
		def sortedList = programs.sort( false, evaluators.numberEvaluator )

		then:
		"The programs will have the <expectedRanking> starting from 0"
		def indexOfProgramsInSortedList = programs.collect { sortedList.indexOf( it ) }
		indexOfProgramsInSortedList == expectedRanking

		where:
		codes                                                              | inputs            | out | expectedRanking
		[
				coder.writeCode { ld 0.1; ld 0.2; add() },
				coder.writeCode { ld 1.0; ld 0.2; add() } ]                | [ 0.1, 0.2, 1.0 ] | 0.0 | [ 0, 1 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add() },
				coder.writeCode { ld 1.0; ld 0.2; add() } ]                | [ 0.1, 0.2, 1.0 ] | 1.0 | [ 1, 0 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add() },
				coder.writeCode { ld 1.0; ld 0.2; add() } ]                | [ 0.1, 0.2, 1.0 ] | 0.3 | [ 0, 1 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add() },
				coder.writeCode { ld 1.0; ld 0.2; add() } ]                | [ 0.1, 0.2, 1.0 ] | 1.2 | [ 1, 0 ]
		[
				coder.writeCode { ld 1.0; ld 0.2; add() },
				coder.writeCode { ld 1.0; ld 0.1; ld 0.1; add(); add() } ] | [ 0.1, 0.2, 1.0 ] | 1.2 | [ 0, 1 ]
		[
				coder.writeCode {},
				coder.writeCode { ld 1.0 } ]                               | [ 1.0 ]           | 0.0 | [ 1, 0 ]

	}
//
//	@Test
//	void testStringEvaluator( ) {
//		Specification sp = new Specification( inputs: [ 'a', 'b', 'c' ], out: [ 'ba' ] )
//
//		Program p1 = new Program( specification: sp, code: [
//				new Instr( 'ld', [ 'a' ] ),
//				new Instr( 'ld', [ 'b' ] ),
//				new Instr( 'add', [ ] )
//		] )
//		Program p2 = new Program( specification: sp, code: [
//				new Instr( 'ld', [ 'a' ] ),
//				new Instr( 'ld', [ 'c' ] ),
//				new Instr( 'add', [ ] )
//		] )
//
//		def evaluator = evaluators.stringEvaluator
//
//		assert [ p1, p2 ].sort( evaluator ) == [ p1, p2 ]
//		assert [ p2, p1 ].sort( evaluator ) == [ p1, p2 ]
//
//		sp = new Specification( inputs: [ 'a', 'b', 'c' ], out: [ 'ab' ] )
//		p1 = new Program( specification: sp, code: p1.code )
//		p2 = new Program( specification: sp, code: p2.code )
//		assert [ p1, p2 ].sort( evaluator ) == [ p2, p1 ]
//		assert [ p2, p1 ].sort( evaluator ) == [ p2, p1 ]
//
//		sp = new Specification( inputs: [ 0.1, 0.2, 1.0 ], out: [ 0.3 ] )
//		p1 = new Program( specification: sp, code: p1.code )
//		p2 = new Program( specification: sp, code: p2.code )
//		assert [ p1, p2 ].sort( evaluator ) == [ p1, p2 ]
//
//		sp = new Specification( inputs: [ 0.1, 0.2, 1.0 ], out: [ 1.2 ] )
//		p1 = new Program( specification: sp, code: p1.code )
//		p2 = new Program( specification: sp, code: p2.code )
//		assert [ p1, p2 ].sort( evaluator ) == [ p2, p1 ]
//
//		Program p3 = new Program( specification: sp, code: [
//				new Instr( 'ld', [ 1.0 ] ),
//				new Instr( 'ld', [ 0.1 ] ),
//				new Instr( 'ld', [ 0.1 ] ),
//				new Instr( 'add', [ ] ),
//				new Instr( 'add', [ ] )
//		] )
//
//		// p2 and p3 get the same result but p2 is shorter so should be ranked higher
//		assert [ p2, p3 ].sort( evaluator ) == [ p2, p3 ]
//		assert [ p3, p2 ].sort( evaluator ) == [ p2, p3 ]
//
//		// p4 has no code, so it just returns null, so it should be ranked lowest
//		Program p4 = new Program( specification: sp, code: [ ] )
//		assert [ p1, p2, p3, p4 ].sort( evaluator ).last() == p4
//		assert [ p4, p3, p2, p1 ].sort( evaluator ).last() == p4
//	}

	def "the stringDistance method can find the distance between Strings"( ) {
		expect:
		"The distance between <s1> and <s2> to be the <expected>"
		evaluators.stringDistance( s1, s2 ) == 0

		where:
		s1       | s2       | expected
		''       | ''       | 0
		'a'      | 'a'      | 0
		'abcdef' | 'abcdef' | 0
		//'a'      | 'z'      | 1

	}

}
