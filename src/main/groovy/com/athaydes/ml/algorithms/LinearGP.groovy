package com.athaydes.ml.algorithms

import com.athaydes.ml.utils.Assembly
import com.athaydes.ml.utils.ProgramFactory
import groovy.transform.Immutable
import groovy.transform.Memoized

/**
 *
 * User: Renato
 */
class LinearGP {

	int populationSize = 20
	int generations = 25
	int keepOverNextGen = 5
	int maxProgramSize = 10
	int simplifyCodeThresholdSize = 7
	float mutationP = 0.1f
	float f0P = 0.4f
	final List<Program> programs = [ ]
	final List<Specification> specifications = [ ]
	def pFactory = new ProgramFactory()
	Closure evaluator

	Closure programEqChecker = { Program p1, Program p2 ->
		p1.code.size() != p2.code.size() ? 1 :
				p1.code.collect { it.name + it.params.toString() }.toString() <=>
						p2.code.collect { it.name + it.params.toString() }.toString()
	}

	void setMutationP( float p ) {
		if ( p < 0f || p > 1f )
			throw new IllegalArgumentException( "P must be between 0.0 and 1.0" )
		this.mutationP = p
	}

	SpecificationBuilder withInputs( Object... inputs ) {
		new SpecificationBuilder( inputs: inputs )
	}

	List<Instr> getCode() {
		init()
		programs[ 0 ].code
	}

	def call() {
		init()
		programs[ 0 ].eval()
	}

	List<Program> getPrograms() {
		init()
		programs
	}

	private void init() {
		if ( !programs ) {
			programs.addAll GPAlgorithm.instance.evolve( this )
		}
		programs
	}

	class SpecificationBuilder {

		Object[] inputs

		LinearGP resultIs( expected ) {
			specifications << new Specification( inputs: inputs, out: [ expected ] )
			return LinearGP.this
		}

	}

}

@Singleton
class GPAlgorithm {

	static final Random rand = new Random( System.currentTimeMillis() )
	def pEvolver = new ProbabilityEvolver()
	final evolvingFields = ( [ 'mutationP' ] * 5 ) + ( [ 'f0P' ] * 5 )
	int evolvingFieldIndex = 0

	List<Program> evolve( LinearGP gp ) {
		( 1..gp.generations ).inject( randomPopulation( gp ) ) {
			parents, _ -> newGeneration( parents, gp )
		}.collect { Program program ->
			gp.pFactory.create( program.code, program.specification, true )
		}.sort( gp.evaluator )
	}

	private List<Program> newGeneration( List<Program> parents, LinearGP gp ) {
		def nextGen = ensureSize( ( parents[ 0..<gp.keepOverNextGen ] + offspring( parents, gp ) )
				.unique( gp.programEqChecker ), gp )
				.sort( gp.evaluator )
		pEvolver.evolveP( nextEvolvingField(), gp, parents, nextGen )
	}

	String nextEvolvingField() {
		if ( evolvingFields.empty ) return null
		if ( evolvingFieldIndex < evolvingFields.size() )
			return evolvingFields[ evolvingFieldIndex++ ]
		evolvingFieldIndex = 0
		nextEvolvingField()
	}

	List<Program> ensureSize( List<Program> population, LinearGP gp ) {
		population.size() >= gp.populationSize ?
				population.subList( 0, gp.populationSize ) :
				population + randomPopulation( gp, gp.populationSize - population.size() )
	}

	List<Program> offspring( List<Program> parents, LinearGP gp ) {
		match( parents ).collectMany { List<Program> couple ->
			copulate( couple[ 0 ], couple[ 1 ], gp.pFactory ).collect { Program child ->
				def simplify = child.code.size() >= gp.simplifyCodeThresholdSize
				gp.pFactory.create( mutate( gp.mutationP, gp.f0P, child.code,
						couple.first().specification.inputs ),
						couple.first().specification, simplify )
			}
		}
	}

	List<List<Program>> match( List<Program> parents ) {
		if ( parents.size() < 2 ) return [ ]
		( 1..<parents.size() ).collect { int i ->
			def M = parents[ i - 1 ]
			def F = parents[ i ]
			( i % 2 && M.specification.is( F.specification ) ) ? [ M, F ] : null
		} - null
	}

	List<Instr> mutate( float mutationP, float f0P, List<Instr> code, Object[] inputs ) {
		code.collectMany {
			def r = rand.nextFloat()
			if ( r < mutationP ) {
				def thirdChance = mutationP / 3.0f
				if ( r < thirdChance ) return [ randomInstr( f0P, inputs ) ]
				if ( r < thirdChance * 2.0f ) return [ it, randomInstr( f0P, inputs ) ]
				else return [ ]
			} else {
				return [ it ]
			}
		}
	}

	List<Program> copulate( Program p1, Program p2, ProgramFactory pFactory ) {
		[
				pFactory.create( crossOver( p1.code, p2.code ), p1.specification ),
				pFactory.create( crossOver( p2.code, p1.code ), p2.specification )
		]
	}

	List<Instr> crossOver( List<Instr> c1, List<Instr> c2 ) {
		c1.take( c1.size().intdiv( 2 ) ) + c2.drop( c2.size().intdiv( 2 ) )
	}

	List<Program> randomPopulation( LinearGP gp, int maxSize = -1 ) {
		int populationSize = maxSize < 0 ? gp.populationSize : maxSize
		def population = gp.specifications.collect { sp ->
			( 1..populationSize ).collect {
				gp.pFactory.create(
						( 1..( 1 + rand.nextInt( gp.maxProgramSize ) ) ).collect {
							randomInstr gp.f0P, sp.inputs
						}, sp )
			}
		}.flatten()
		Collections.shuffle( population )
		population
	}

	Instr randomInstr( float f0P, Object[] inputs ) {
		def functNames = ( !inputs || rand.nextFloat() < f0P ) ? Instr.F0_NAMES : Instr.F1_NAMES
		def name = functNames[ rand.nextInt( functNames.size() ) ]
		def params = ( functNames.is( Instr.F0_NAMES ) || inputs.size() == 0 ) ?
				[ ] : inputs[ [ rand.nextInt( inputs.size() ) ] ]
		new Instr( name: name, params: params )
	}

}

class ProbabilityEvolver {

	enum ProbabilityChange {
		INCREASE( 0.01f ), DECREASE( -0.01f )

		final float value;

		ProbabilityChange( float value ) {
			this.value = value;
		}

		ProbabilityChange swap() {
			if ( this == INCREASE ) DECREASE else INCREASE
		}
	}

	def pChange = ProbabilityChange.INCREASE

	List<Program> evolveP( String variable, LinearGP algorithm, List<Program> parents, List<Program> nextGen ) {
		if ( !variable ) return nextGen
		def scoreCalculator = { acc, p -> acc - algorithm.evaluator( p ) }
		def parentsScore = parents.inject( 0, scoreCalculator )
		def nextGenScore = nextGen.inject( 0, scoreCalculator )
		if ( parentsScore > nextGenScore ) {
			pChange = pChange.swap()
		}
		algorithm."$variable" = boundedP( algorithm."$variable", pChange.value )
		nextGen
	}

	float boundedP( float p, float delta ) {
		Math.min( Math.max( p + delta, 0f ), 1f )
	}

}

@Immutable
class Specification {
	Object[] inputs
	Object[] out
}

@Immutable
class Instr {

	static final F0_NAMES = [ 'add', 'sub', 'out' ]
	static final F1_NAMES = [ 'ld' ]

	String name
	List params

	@Override
	String toString() { name + ( params ? "(${params.toString()[ 1..-2 ]})" : "" ) }

}

@Immutable
class Program {

	Specification specification
	List<Instr> code

	@Memoized
	def eval() {
		def machine = new Assembly()
		code.each { Instr instr ->
			machine."$instr.name"( *instr.params )
		}
		machine.out()
	}

}