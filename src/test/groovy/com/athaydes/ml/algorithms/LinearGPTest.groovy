package com.athaydes.ml.algorithms

import com.athaydes.ml.utils.Evaluators
import com.athaydes.ml.utils.ProgramFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 *
 * User: Renato
 */
class LinearGPTest {


	static final callArgs = [ ]
	final evaluators = new Evaluators()

	@Before
	void setup( ) { callArgs.clear() }

	@Test
	void testEquivalenceChecker( ) {
		LinearGP gp = new LinearGP()
		Specification sp = new Specification( inputs: [ 1, 2 ], out: [ 0 ] )

		Program p1 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 1 ] ),
				new Instr( 'ld', [ 2 ] ),
				new Instr( 'add', [ ] )
		] )
		Program p2 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 10 ] ),
				new Instr( 'ld', [ 2 ] ),
				new Instr( 'add', [ ] )
		] )
		Program p3 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 10 ] ),
				new Instr( 'ld', [ 2 ] ),
				new Instr( 'add', [ ] )
		] )
		Program p4 = new Program( specification: sp, code: [ ] )

		assert [ p1, p2 ].unique( gp.programEqChecker ) == [ p1, p2 ]
		assert [ p1, p4 ].unique( gp.programEqChecker ) == [ p1, p4 ]
		assert [ p2, p3 ].unique( gp.programEqChecker ) == [ p2 ]
		assert [ p1, p2, p3, p4 ].unique( gp.programEqChecker ) == [ p1, p2, p4 ]
		assert [ ].unique( gp.programEqChecker ) == [ ]
	}

	@Test
	void testMatch( ) {
		Specification sp1 = new Specification( inputs: [ ], out: [ ] )
		Specification sp2 = new Specification( inputs: [ ], out: [ ] )
		def p1 = new Program( code: [ ], specification: sp1 )
		def p2 = new Program( code: [ ], specification: sp1 )
		def p3 = new Program( code: [ ], specification: sp2 )
		def p4 = new Program( code: [ ], specification: sp2 )

		assert GPAlgorithm.instance.match( [ ] ) == [ ]
		assert GPAlgorithm.instance.match( [ p1 ] ) == [ ]
		assert GPAlgorithm.instance.match( [ p1, p2 ] ) == [ [ p1, p2 ] ]
		assert GPAlgorithm.instance.match( [ p1, p2, p3 ] ) == [ [ p1, p2 ] ]
		assert GPAlgorithm.instance.match( [ p1, p2, p3, p4 ] ) == [ [ p1, p2 ], [ p3, p4 ] ]
		assert GPAlgorithm.instance.match( [ p1, p3 ] ) == [ ]

	}

	@Category( GPAlgorithm )
	class RandomInstrMock {
		Instr randomInstr( float f0P, Object[] inputs ) {
			LinearGPTest.callArgs << [ f0P, inputs ]
			new Instr( name: 'z', params: [ ] )
		}
	}

	@Test
	void testMutate( ) {
		def inputs = [ 1, 2 ] as Object[]
		def code = ( 'a'..'f' ).collect { new Instr( name: it, params: [ ] ) }

		use( RandomInstrMock ) {
			def mutatedFNames = ( 1..100 ).collect {
				GPAlgorithm.instance.mutate( 0.25f, 0.9f, code, inputs )*.name
			}
			assert callArgs.size() > 85
			assert callArgs.every { it[ 0 ] == 0.9f }

			def randomPs = mutatedFNames.collect { instrs ->
				instrs.count { it == 'z' } / instrs.size()
			}
			def avgRandomP = randomPs.sum() / randomPs.size()

			// mutations can occur in 3 ways:
			// - a gene is replaced by a random gene ( 60% of all mutations  )
			// - a gene is removed ( 20% )
			// - a random gene is added ( 20% )
			// So actually random genes are going to be around 80% of the mutations
			Assert.assertEquals 0.25 * 0.8, avgRandomP, 0.15

			mutatedFNames = ( 1..100 ).collect {
				GPAlgorithm.instance.mutate( 0.75f, 0.9f, code, inputs )*.name
			}

			randomPs = mutatedFNames.collect { instrs ->
				instrs.count { it == 'z' } / instrs.size()
			}
			avgRandomP = randomPs.sum() / randomPs.size()

			Assert.assertEquals 0.75 * 0.8, avgRandomP, 0.15

		}

	}

	@Test
	void testRandomInstr( ) {
		def inputs = [ ] as Object[]
		def instrs = ( 1..100 ).collect { GPAlgorithm.instance.randomInstr( 0.25f, inputs ) }

		assert Instr.F0_NAMES.containsAll( instrs.name )

		inputs = [ 'a' ] as Object[]
		instrs = ( 1..100 ).collect { GPAlgorithm.instance.randomInstr( 0.25f, inputs ) }

		def f0s = instrs.grep { it.name in Instr.F0_NAMES }
		def f1s = instrs.grep { it.name in Instr.F1_NAMES }

		Assert.assertEquals 25, f0s.size(), 15
		Assert.assertEquals 75, f1s.size(), 15

		assert f0s.params.every { it.size() == 0 }
		assert f1s.params.every { it == inputs }

		inputs = [ 20, 30 ] as Object[]
		instrs = ( 1..100 ).collect { GPAlgorithm.instance.randomInstr( 0.15f, inputs ) }

		f0s = instrs.grep { it.name in Instr.F0_NAMES }
		f1s = instrs.grep { it.name in Instr.F1_NAMES }

		Assert.assertEquals 10, f0s.size(), 15
		Assert.assertEquals 90, f1s.size(), 15

		assert f0s.params.every { it.size() == 0 }
		assert f1s.params.every { it.size() == 1 }
		assert f1s.params.every { it[ 0 ] in inputs }
	}

	@Category( GPAlgorithm )
	class CrossOverMock {
		List<Instr> crossOver( List<Instr> c1, List<Instr> c2 ) {
			LinearGPTest.callArgs << [ c1, c2 ]
			c1
		}
	}

	@Test
	void testCopulate( ) {
		Specification sp = new Specification( inputs: [ 1, 2 ], out: [ ] )
		Program p1 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 1 ] )
		] )
		Program p2 = new Program( specification: sp, code: [
				new Instr( 'ld', [ 2 ] )
		] )

		def pf = new ProgramFactory()
		use( CrossOverMock ) {
			def children = GPAlgorithm.instance.copulate( p1, p2, pf )

			assert callArgs == [ [ p1.code, p2.code ], [ p2.code, p1.code ] ]
			assert children.size() == 2
			assert children[ 0 ] == p1
			assert children[ 1 ] == p2
		}
	}

	@Test
	void testCrossOver( ) {
		List<Instr> c1 = [
				new Instr( 'ld', [ 'a' ] ),
				new Instr( 'ld', [ 'b' ] ),
				new Instr( 'add', [ ] ),
				new Instr( 'ld', [ 'abcde' ] ),
				new Instr( 'sub', [ ] )
		]
		List<Instr> c2 = [
				new Instr( 'ld', [ 'fg' ] ),
				new Instr( 'ld', [ 'g' ] ),
				new Instr( 'sub', [ ] ),
				new Instr( 'ld', [ 'hij' ] ),
				new Instr( 'add', [ ] )
		]

		assert GPAlgorithm.instance.crossOver( c1, c2 ) == [
				new Instr( 'ld', [ 'a' ] ),
				new Instr( 'ld', [ 'b' ] ),
				new Instr( 'sub', [ ] ),
				new Instr( 'ld', [ 'hij' ] ),
				new Instr( 'add', [ ] )
		]

		assert GPAlgorithm.instance.crossOver( [ ], [ ] ) == [ ]
		assert GPAlgorithm.instance.crossOver( [ new Instr( 'out', [ ] ) ], [ ] ) == [ ]
		assert GPAlgorithm.instance.crossOver( [ ], [ new Instr( 'out', [ ] ) ] ) ==
				[ new Instr( 'out', [ ] ) ]
		assert GPAlgorithm.instance.crossOver( [ new Instr( 'add', [ ] ) ], [ new Instr( 'out', [ ] ) ] ) ==
				[ new Instr( 'out', [ ] ) ]
	}

	@Category( GPAlgorithm )
	class MutateAndCopulateMock {
		List<Program> copulate( Program p1, Program p2, ProgramFactory pf ) {
			LinearGPTest.callArgs << [ 'copulate': [ p1, p2 ] ]
			[ p1 ]
		}

		List<Instr> mutate( float mutationP, float f0P, List<Instr> code, Object[] inputs ) {
			LinearGPTest.callArgs << [ 'mutate': [ mutationP, f0P, code, inputs ] ]
			code
		}
	}

	@Test
	void testOffspring( ) {
		/* This test depends on the GPAlgorithm.instance.match(..) method working */

		Specification sp1 = new Specification( inputs: [ 1 ], out: [ ] )
		Specification sp2 = new Specification( inputs: [ 2 ], out: [ ] )
		def p1 = new Program( code: [ new Instr( name: 'ld' ) ], specification: sp1 )
		def p2 = new Program( code: [ ], specification: sp1 )
		def p3 = new Program( code: [ new Instr( name: 'out' ) ], specification: sp2 )
		def p4 = new Program( code: [ ], specification: sp2 )

		def gp = new LinearGP( f0P: 0.15f, mutationP: 0.25f )

		def offspring = [ ]
		use( MutateAndCopulateMock ) {
			offspring = GPAlgorithm.instance.offspring( [ p1, p2, p3, p4 ], gp )
		}

		def copulateArgs = callArgs.collect { it[ 'copulate' ] } - null
		def mutateArgs = callArgs.collect { it[ 'mutate' ] } - null

		assert copulateArgs == [ [ p1, p2 ], [ p3, p4 ] ]
		assert mutateArgs == [ [ 0.25f, 0.15f, p1.code, sp1.inputs ], [ 0.25f, 0.15f, p3.code, sp2.inputs ] ]

		assert offspring.size() == 2
		assert offspring[ 0 ] == p1
		assert offspring[ 1 ] == p3

		use( MutateAndCopulateMock ) {
			assert GPAlgorithm.instance.offspring( [ ], gp ).isEmpty()
			assert GPAlgorithm.instance.offspring( [ p1 ], gp ).isEmpty()
			assert GPAlgorithm.instance.offspring( [ p1, p3 ], gp ).isEmpty()
			assert GPAlgorithm.instance.offspring( [ p1, p2 ], gp ) == [ p1 ]
		}

	}

	@Test
	void testRandomPopulation( ) {
		LinearGP gp = new LinearGP( populationSize: 100,
				maxProgramSize: 5, f0P: 0.3f, evaluator: evaluators.numberEvaluator )
		gp.withInputs( 10, 20 ).resultIs( 1 ).withInputs( 30, 40 ).resultIs( 2 )

		def population = [ ]
		use( RandomInstrMock ) {
			population = GPAlgorithm.instance.randomPopulation( gp )
		}

		assert population.size() == gp.specifications.size() * gp.populationSize
		assert callArgs.size() > population.size()
		assert callArgs.every { it[ 0 ] == 0.3f }

		def inputs0reqs = callArgs.takeWhile { it[ 1 ] == gp.specifications[ 0 ].inputs }
		def inputs1reqs = callArgs.drop( inputs0reqs.size() )
				.takeWhile { it[ 1 ] == gp.specifications[ 1 ].inputs }
		assert inputs0reqs.size() > population.size()
		Assert.assertEquals inputs1reqs.size(), inputs0reqs.size(), inputs0reqs.size() * 0.15

	}

	@Test
	void testSimplePrograms( ) {
		def gp = new LinearGP( populationSize: 28, generations: 10,
				mutationP: 0.25f, evaluator: evaluators.numberEvaluator )
				.withInputs( 2, 3 ).resultIs( 5 )

		assert gp.programs.size() == 28

		// with such simple inputs, we should always find the optimal solution
		assert gp.programs[ 0 ].eval() == 5
		assert gp.programs[ 0 ].code.size() == 3

		gp = new LinearGP( populationSize: 28, generations: 10,
				mutationP: 0.25f, evaluator: evaluators.numberEvaluator )
				.withInputs( 5, 5 ).resultIs( 10 )

		assert gp.programs.size() == 28
		assert gp.programs[ 0 ].eval() == 10
		assert gp.programs[ 0 ].code.size() == 3

		gp = new LinearGP( populationSize: 28, generations: 10,
				mutationP: 0.25f, evaluator: evaluators.numberEvaluator )
				.withInputs( 10, 11 ).resultIs( -1 )

		assert gp.programs.size() == 28
		assert gp.programs[ 0 ].eval() == -1
		assert gp.programs[ 0 ].code.size() == 3

	}

	@Test
	void testNonTrivialPrograms( ) {
		def gp = new LinearGP( populationSize: 250, generations: 100,
				mutationP: 0.15f, maxProgramSize: 100, evaluator: evaluators.stringEvaluator )
				.withInputs( 'h', 'u', 'o', 'e', 's' ).resultIs( 'house' )

		//assert gp.programs.size() == 28
		gp.programs.each { println it.code + " : " + it.eval() }

		//assert gp.programs[ 0 ].eval() == 5
		//assert gp.programs[ 0 ].code.size() == 3

	}

}
