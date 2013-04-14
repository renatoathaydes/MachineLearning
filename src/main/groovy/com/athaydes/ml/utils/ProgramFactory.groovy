package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Instr
import com.athaydes.ml.algorithms.Program
import com.athaydes.ml.algorithms.Specification

/**
 *
 * User: Renato
 */
class ProgramFactory {

	Program create( List<Instr> instrs, Specification specification,
	                boolean simplifyCode = false ) {
		new Program( specification: specification, code: simplifyCode ?
			new Coder().simplify( instrs ) : instrs )
	}

}
