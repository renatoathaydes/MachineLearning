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
	void testSimplify() {
		// non-simplify-able examples
		assert coder.simplify( coder.writeCode {} ) ==
				coder.writeCode {}
		assert coder.simplify( coder.writeCode { ld 'hi' } ) ==
				coder.writeCode { ld 'hi' }
		assert coder.simplify( coder.writeCode { ld 10; ld 20; add() } ) ==
				coder.writeCode { ld 10; ld 20; add() }
		assert coder.simplify( coder.writeCode { ld 10; ld 20; out(); ld 20; ld 20; add(); sub() } ) ==
				coder.writeCode { ld 10; ld 20; out(); ld 20; ld 20; add(); sub() }

		// simplify-able examples
		assert coder.simplify( coder.writeCode { add() } ) ==
				coder.writeCode {}
		assert coder.simplify( coder.writeCode { add(); out(); sub() } ) ==
				coder.writeCode {}
		assert coder.simplify( coder.writeCode { ld 10; ld 20; add(); add() } ) ==
				coder.writeCode { ld 10; ld 20; add() }
		assert coder.simplify( coder.writeCode { out(); ld 10; out(); out(); ld 20; ld 20; add(); sub() } ) ==
				coder.writeCode { ld 10; out(); ld 20; ld 20; add() }
		assert coder.simplify( coder.writeCode { ld 10; ld 20 } ) ==
				coder.writeCode { ld 20 }
		assert coder.simplify( coder.writeCode { ld 10; ld 20; out(); add(); ld 30 } ) ==
				coder.writeCode { ld 20; out(); ld 30 }

	}

	@Test
	void testWriteCode() {
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

	@Test
	void testWriteCodeWithPropertyLikeAccess() {
		List<Instr> code = coder.writeCode {
			ld 10
			ld 5
			add
			out
		}
		assert code == [ new Instr( 'ld', [ 10 ] ), new Instr( 'ld', [ 5 ] ),
		                 new Instr( 'add', [ ] ), new Instr( 'out', [ ] ) ]
	}

}
