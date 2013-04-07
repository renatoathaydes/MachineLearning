package com.athaydes.ml.utils

/**
 *
 * User: Renato
 */
class Assembly {

	private static final acc = [ ] as Stack

	static void ld( val ) {
		acc << val
	}

	static void add( ) {
		if ( acc.size() > 1 ) acc << acc.pop() + acc.pop()
	}

	static void sub( ) {
		if ( acc.size() > 1 ) acc << acc.pop() - acc.pop()
	}

	static void clr( ) {
		acc.clear()
	}

	static out( ) {
		if ( !acc.isEmpty() ) acc.pop()
	}

}
