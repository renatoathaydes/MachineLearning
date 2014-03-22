package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Instr

/**
 *
 * User: Renato
 */
class Coder {

	static final enum KeyType {
		OPERANDS, STACK_CHANGE
	}
	static final validInstrs = [
			'ld' : [ ( KeyType.OPERANDS ): 0, ( KeyType.STACK_CHANGE ): 1 ],
			'out': [ ( KeyType.OPERANDS ): 1, ( KeyType.STACK_CHANGE ): -1 ],
			'add': [ ( KeyType.OPERANDS ): 2, ( KeyType.STACK_CHANGE ): -1 ],
			'sub': [ ( KeyType.OPERANDS ): 2, ( KeyType.STACK_CHANGE ): -1 ],
			'clr': [ ( KeyType.OPERANDS ): 0, ( KeyType.STACK_CHANGE ): Integer.MIN_VALUE ],
	].asImmutable()

	List<Instr> simplify( List<Instr> code ) {
		def stackCount = 0
		( code.collect { instr ->
			if ( instr.name in validInstrs ) {
				def info = validInstrs[ instr.name ]
				if ( stackCount >= info[ KeyType.OPERANDS ] ) {
					stackCount = Math.max( 0, stackCount + info[ KeyType.STACK_CHANGE ] )
					return instr
				} else {
					return null
				}
			}
		} - null ).dropWhile { instr ->
			if ( stackCount > 0 ) {
				def info = validInstrs[ instr.name ]
				stackCount -= info[ KeyType.STACK_CHANGE ]
			}
			stackCount > 0
		}
	}

	List<Instr> writeCode( Closure cls ) {
		cls.delegate = new CoderWriter()
		cls()
		cls.delegate.code
	}

	static class CoderWriter {

		final List<Instr> code = [ ]

		def invokeMethod( String name, Object args ) {
			code << new Instr( name, args as List )
		}

		def getProperty( String name ) {
			if ( name in Instr.F0_NAMES ) {
				code << new Instr( name, [ ] )
				return null
			} else metaClass.getMetaProperty( name ).getProperty( this )
		}

	}

}