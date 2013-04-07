package com.athaydes.ml.algorithms

import com.athaydes.ml.utils.Assembly
import groovy.transform.Immutable

/**
 *
 * User: Renato
 */
class LinearGP {

	Object[] args
	def expected
	final algorithm = new GPAlgorithm()

	List<Program> programs

	static LinearGP get( ) { new LinearGP() }

	def withInputs( Object... args ) {
		this.args = args
		return this
	}

	def resultIs( expected ) {
		this.expected = expected
		return this
	}

	def code( ) {
		init()
		programs[ 0 ].code
	}

	def call( ) {
		init()
		programs[ 0 ].eval()
	}

	List<Program> getPrograms( ) {
		init()
		programs
	}

	private void init( ) {
		if ( !programs ) {
			programs = algorithm.evolve( this )
		}
		programs
	}

}

class GPAlgorithm {

	static final Random rand = new Random( System.currentTimeMillis() )
	static final MAX_PROGRAM_SIZE = 10

	def populationSize = 20

	List<Program> evolve( LinearGP gp ) {
		def population = randomPopulation( gp )
		rank( population, gp.expected )
	}

	List<Program> rank( List<Program> programs, expected ) {
		programs.sort { p1, p2 ->
			def p1Val = p1.eval()
			def p2Val = p2.eval()
			if ( !p1Val ) return p2Val ? 1 : 0
			if ( !p2Val ) return p1Val ? -1 : 0
			def v1 = Math.abs( expected - p1Val )
			def v2 = Math.abs( expected - p2.eval() )
			v1 > v2 ? 1 : v1 < v2 ? -1 : p1.code.size() - p2.code.size()
		}
	}

	List<Program> randomPopulation( LinearGP gp ) {
		( 1..populationSize ).collect {
			new Program( code:
					( 1..( 1 + rand.nextInt( MAX_PROGRAM_SIZE ) ) ).collect {
						randomInstr gp
					} )
		}
	}

	Instr randomInstr( LinearGP gp ) {
		def functs = rand.nextFloat() < 0.4f ? Instr.F0_NAMES : Instr.F1_NAMES
		def name = functs[ rand.nextInt( functs.size() ) ]
		def params = ( functs == Instr.F0_NAMES || gp.args.size() == 0 ) ?
			[ ] : gp.args[ [ rand.nextInt( gp.args.size() ) ] ]
		new Instr( name: name, params: params )
	}

}

@Immutable
class Instr {

	static final F0_NAMES = [ 'add', 'sub', 'out' ]
	static final F1_NAMES = [ 'ld' ]

	String name
	List params

	@Override
	String toString( ) { name + ( params ? "($params)" : "" ) }

}

class Program {

	List<Instr> code
	private isEval = false
	private res

	def eval( ) {
		if ( isEval ) return res
		Assembly.clr()
		code.each { Instr instr ->
			Assembly."$instr.name"( * instr.params )
		}
		isEval = true
		res = Assembly.out()
	}

}