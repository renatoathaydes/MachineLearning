package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Instr
import org.junit.Test

/**
 *
 * User: Renato
 */
class CoderTest {

	final coder = new Coder()

	@Test
	void testSimplify( ) {
		assert coder.simplify( coder.writeCode { ld 10; ld 20; add() } ) ==
				coder.writeCode { ld 10; ld 20; add() }
		assert coder.simplify( coder.writeCode { ld 10; ld 20; add(); add() } ) ==
				coder.writeCode { ld 10; ld 20; add() }
	}

	@Test
	void testWriteCode( ) {
		List<Instr> code = coder.writeCode {
			ld 10
			ld 5
			add()
		}
		assert code == [ new Instr( 'ld', [ 10 ] ), new Instr( 'ld', [ 5 ] ), new Instr( 'add', [ ] ) ]

		assert coder.writeCode {} == [ ]
		assert coder.writeCode { hello 'world' } == [ new Instr( 'hello', [ 'world' ] ) ]
		assert coder.writeCode { ( 'a'..'z' ).each { s -> "$s"() } } ==
				( 'a'..'z' ).collect { new Instr( it, [ ] ) }

	}

}
