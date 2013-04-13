package com.athaydes.ml.utils

import com.athaydes.ml.algorithms.Instr
import com.athaydes.ml.algorithms.Program
import com.athaydes.ml.algorithms.Specification

/**
 *
 * User: Renato
 */
class ProgramFactory {

	boolean simplifyCode = true

	Program create( List<Instr> instrs, Specification specification ) {
		new Program( specification: specification, code: simplifyCode ?
			new Coder().simplify( instrs ) : instrs )
	}

}
