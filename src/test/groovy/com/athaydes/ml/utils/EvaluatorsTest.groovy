package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Instr
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

	def "Number evaluator can sort programs when Specification uses ints"() {
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
		codes | inputs | out | expectedRanking
		[
				coder.writeCode { ld 1; ld 2; add },
				coder.writeCode { ld 10; ld 2; add } ]            | [ 1, 2, 10 ] | 0  | [ 0, 1 ]
		[
				coder.writeCode { ld 1; ld 2; add },
				coder.writeCode { ld 10; ld 2; add } ]            | [ 1, 2, 10 ] | 10 | [ 1, 0 ]
		[
				coder.writeCode { ld 1; ld 2; add },
				coder.writeCode { ld 10; ld 2; add } ]            | [ 1, 2, 10 ] | 3  | [ 0, 1 ]
		[
				coder.writeCode { ld 1; ld 2; add },
				coder.writeCode { ld 10; ld 2; add } ]            | [ 1, 2, 10 ] | 12 | [ 1, 0 ]
		[
				coder.writeCode { ld 10; ld 2; add },
				coder.writeCode { ld 10; ld 1; ld 1; add; add } ] | [ 1, 2, 10 ] | 12 | [ 0, 1 ]
		[
				coder.writeCode {},
				coder.writeCode { ld 10 } ]                       | [ 10 ]       | 0  | [ 1, 0 ]

	}

	def "Number evaluator can sort programs when Specification uses doubles"() {
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
		codes | inputs | out | expectedRanking
		[
				coder.writeCode { ld 0.1; ld 0.2; add },
				coder.writeCode { ld 1.0; ld 0.2; add } ]              | [ 0.1, 0.2, 1.0 ] | 0.0 | [ 0, 1 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add },
				coder.writeCode { ld 1.0; ld 0.2; add } ]              | [ 0.1, 0.2, 1.0 ] | 1.0 | [ 1, 0 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add },
				coder.writeCode { ld 1.0; ld 0.2; add } ]              | [ 0.1, 0.2, 1.0 ] | 0.3 | [ 0, 1 ]
		[
				coder.writeCode { ld 0.1; ld 0.2; add },
				coder.writeCode { ld 1.0; ld 0.2; add } ]              | [ 0.1, 0.2, 1.0 ] | 1.2 | [ 1, 0 ]
		[
				coder.writeCode { ld 1.0; ld 0.2; add },
				coder.writeCode { ld 1.0; ld 0.1; ld 0.1; add; add } ] | [ 0.1, 0.2, 1.0 ] | 1.2 | [ 0, 1 ]
		[
				coder.writeCode {},
				coder.writeCode { ld 1.0 } ]                           | [ 1.0 ]           | 0.0 | [ 1, 0 ]

	}

	def "String Evaluator can rank programs"() {
		given:
		"A Specification with the given <inputs> and <out>"
		def spec = new Specification( inputs: inputs, out: [ out ] )

		and:
		"Programs using that Specification and the given <codes>"
		def programs = codes.collect { code -> new Program( specification: spec, code: code ) }

		when:
		"The programs are ranked using a string evaluator"
		def sortedList = programs.sort( false, evaluators.stringEvaluator )

		then:
		"The programs will have the <expectedRanking> starting from 0"
		def indexOfProgramsInSortedList = programs.collect { sortedList.indexOf( it ) }
		indexOfProgramsInSortedList == expectedRanking

		where:
		codes | inputs | out | expectedRanking
		[
				coder.writeCode { ld 'a'; ld 'b'; add },
				coder.writeCode { ld 'a'; ld 'c'; add },
				coder.writeCode {} ]                       | [ 'a', 'b', 'c' ] | 'ba' | [ 0, 1, 2 ]
		[
				coder.writeCode { ld 'a'; ld 'c'; add },
				coder.writeCode { ld 'a'; ld 'b'; add },
				coder.writeCode {} ]                       | [ 'a', 'b', 'c' ] | 'ba' | [ 1, 0, 2 ]
		[
				coder.writeCode {},
				coder.writeCode { ld 'a'; ld 'c'; add },
				coder.writeCode { ld 'a'; ld 'b'; add }, ] | [ 'a', 'b', 'c' ] | 'ba' | [ 2, 1, 0 ]
		[
				coder.writeCode { ld 'b'; ld 'a'; add },
				coder.writeCode { ld 'z'; ld 'a'; add } ]  | [ 'a', 'b', 'z' ] | 'az' | [ 1, 0 ]
		[
				coder.writeCode { ld 'z'; ld 'a'; add },
				coder.writeCode { ld 'b'; ld 'a'; add } ]  | [ 'a', 'b', 'z' ] | 'az' | [ 0, 1 ]

	}

	def "String Evaluator ranks shorter programs higher given the same result"() {
		given:
		"A simple specification"
		def sp = new Specification( inputs: [ 'a', 'b', 'c' ], out: [ 'ba' ] )

		and:
		"Two programs of different length which output the same result"
		def p1 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 'a' ] ),
				new Instr( 'ld', [ 'c' ] ),
				new Instr( 'add', [ ] )
		] )
		def p2 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 'a' ] ),
				new Instr( 'ld', [ 'a' ] ),
				new Instr( 'ld', [ 'c' ] ),
				new Instr( 'add', [ ] )
		] )

		and:
		"A String Evaluator"
		def evaluator = evaluators.stringEvaluator

		expect:
		"The shorter program to be ranked higher than the longer one"
		[ p1, p2 ].sort( evaluator ) == [ p1, p2 ]
		[ p2, p1 ].sort( evaluator ) == [ p1, p2 ]
	}

	def "The stringDistance method can find the distance between Strings"() {
		expect:
		"The distance between <s1> and <s2> to be the <expected>"
		evaluators.stringDistance( s1, s2 ) == expected

		where:
		s1 | s2 | expected
		''       | ''       | 0
		'a'      | 'a'      | 0
		'abcdef' | 'abcdef' | 0
		'a'      | 'z'      | 1
		'a'      | 'az'     | 1
		'hello'  | 'helo'   | 1
		'hi'     | 'hello'  | 4

	}

}
