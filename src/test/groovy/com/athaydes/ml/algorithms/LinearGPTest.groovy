package com.athaydes.ml.algorithms

import org.junit.Test

/**
 *
 * User: Renato
 */
class LinearGPTest {

	@Test
	void testRanking( ) {
		Program p1 = new Program( code: [
				new Instr( 'ld', [ 1 ] ),
				new Instr( 'ld', [ 2 ] ),
				new Instr( 'add', [ ] )
		] )
		Program p2 = new Program( code: [
				new Instr( 'ld', [ 10 ] ),
				new Instr( 'ld', [ 2 ] ),
				new Instr( 'add', [ ] )
		] )
		assert LinearGP.get().algorithm.rank( [ p1, p2 ], 0 ) == [ p1, p2 ]
		assert LinearGP.get().algorithm.rank( [ p2, p1 ], 0 ) == [ p1, p2 ]
		assert LinearGP.get().algorithm.rank( [ p1, p2 ], 10 ) == [ p2, p1 ]
		assert LinearGP.get().algorithm.rank( [ p2, p1 ], 10 ) == [ p2, p1 ]
		assert LinearGP.get().algorithm.rank( [ p1, p2 ], 3 ) == [ p1, p2 ]
		assert LinearGP.get().algorithm.rank( [ p1, p2 ], 12 ) == [ p2, p1 ]

		Program p3 = new Program( code: [
				new Instr( 'ld', [ 10 ] ),
				new Instr( 'ld', [ 1 ] ),
				new Instr( 'ld', [ 1 ] ),
				new Instr( 'add', [ ] ),
				new Instr( 'add', [ ] )
		] )
		assert LinearGP.get().algorithm.rank( [ p2, p3 ], 0 ) == [ p2, p3 ]
		assert LinearGP.get().algorithm.rank( [ p3, p2 ], 0 ) == [ p2, p3 ]

		Program p4 = new Program( code: [ ] )
		assert LinearGP.get().algorithm.rank( [ p3, p4 ], 0 ) == [ p3, p4 ]
		assert LinearGP.get().algorithm.rank( [ p4, p3 ], 0 ) == [ p3, p4 ]



	}

	@Test
	void testSimpleProgram( ) {
		def gp = LinearGP.get()

		gp.withInputs( 1, 2, 3, 4 ).resultIs( 5 )

		gp.programs.each {
			println "Resuls ${it.eval()} from : $it.code"
		}

	}

}
