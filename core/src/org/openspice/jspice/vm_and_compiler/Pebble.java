/**
 *	JSpice, an Open Spice interpreter and library.
 *	Copyright (C) 2003, Stephen F. K. Leach
 *
 * 	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 * 	the Free Software Foundation; either version 2 of the License, or
 * 	(at your option) any later version.
 *
 * 	This program is distributed in the hope that it will be useful,
 * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 * 	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.openspice.jspice.vm_and_compiler;

import org.openspice.jspice.vm_and_compiler.VM;

public abstract class Pebble {
	abstract Object run( final Object tos, final VM vm );
	
	static class NoOp extends Pebble {
		Object run( final Object tos, final VM vm ) {
			return tos;
		}
	}
	
	final static NoOp NO_OP = new NoOp();

/*
	static abstract class Arity0to1 extends Proc {
		Arity inArity() { 
			return Arity.ZERO; 
		}
		
		Arity outArity() {
			return Arity.ONE;
		}
	}

	static abstract class Arity0to0 extends Proc {
		Arity inArity() { 
			return Arity.ZERO; 
		}
		
		Arity outArity() {
			return Arity.ZERO;
		}
	}

	static abstract class Arity0xto0x extends Proc {
		Arity inArity() { 
			return Arity.UNKNOWN; 
		}
		
		Arity outArity() {
			return Arity.UNKNOWN;
		}
	}
*/


}
