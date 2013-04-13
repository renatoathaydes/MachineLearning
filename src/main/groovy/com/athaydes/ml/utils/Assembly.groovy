package com.athaydes.ml.utils

/**
 *
 * User: Renato
 */
class Assembly {

	private final acc = [ ] as Stack

	void ld( val ) {
		acc << val
	}

	void add( ) {
		if ( acc.size() > 1 ) acc << acc.pop() + acc.pop()
	}

	void sub( ) {
		if ( acc.size() > 1 ) acc << acc.pop() - acc.pop()
	}

	void clr( ) {
		acc.clear()
	}

	def out( ) {
		if ( !acc.isEmpty() ) acc.pop()
	}

}
