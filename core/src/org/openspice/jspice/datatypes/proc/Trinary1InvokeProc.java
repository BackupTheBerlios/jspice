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
package org.openspice.jspice.datatypes.proc;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.vm_and_compiler.VM;

public abstract class Trinary1InvokeProc extends FastProc {

	public abstract Object invoke( Object x, Object y, Object z );

	public Arity inArity() {
		return Arity.THREE;
	}

	public Arity outArity() {
		return Arity.ONE;
	}

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		final Object z = tos;
		final Object y = vm.pop();
		final Object x = vm.pop();
		return this.invoke( x, y, z );
	}
}
