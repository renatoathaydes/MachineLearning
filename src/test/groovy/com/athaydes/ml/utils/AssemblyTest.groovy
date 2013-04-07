package com.athaydes.ml.utils

import org.junit.Test

import static com.athaydes.ml.utils.Assembly.*

/**
 *
 * User: Renato
 */
class AssemblyTest {

	@Test
	void testLd( ) {
		assert out() == null
		ld 0
		assert out() == 0
		ld 1
		ld 2
		assert out() == 2
		assert out() == 1

		ld 'hello'
		ld true
		assert out() == true
		assert out() == 'hello'
	}

	@Test
	void testSum( ) {
		assert out() == null
		ld 1
		ld 1
		add()
		assert out() == 2

		ld 2
		ld 4
		ld 8
		ld 16
		add()
		add()
		add()
		assert out() == 2 + 4 + 8 + 16

		ld 100
		add()
		assert out() == 100
	}

	@Test
	void testSub( ) {
		assert out() == null
		ld 4
		ld 10
		sub()
		assert out() == 6

		ld 2
		ld 4
		ld 8
		ld 16
		sub()
		sub()
		sub()
		assert out() == 16 - 8 - 4 - 2

		ld 'hi'
		ld 'hi there'
		sub()
		assert out() == ' there'

		ld false
		sub()
		assert out() == false
	}

	@Test
	void testClr( ) {
		assert out() == null
		ld 1
		clr()
		assert out() == null
	}
}
