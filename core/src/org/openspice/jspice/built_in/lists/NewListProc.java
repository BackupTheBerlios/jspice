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
package org.openspice.jspice.built_in.lists;

import org.openspice.jspice.datatypes.proc.FastProc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.vm_and_compiler.VM;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class NewListProc extends FastProc {

	{
		this.setDescription(
			"newList",
			"%p( A1, ..., An ) -> list",
			"returns a mutable list whose members are the values A1 to An"
 		);
	}

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		if ( nargs == 0 ) {
			vm.push( tos );
			return new ArrayList();
		} else if ( nargs == 1 ) {
			final List answer = new ArrayList();
			answer.add( tos );
			return answer;
		} else {
			vm.push( tos );
			return Arrays.asList( vm.popArray( nargs ) );
		}
	}

	public Arity inArity() {
		return Arity.ZERO_OR_MORE;
	}

	public Arity outArity() {
		return Arity.ONE;
	}

	public static final NewListProc NEW_LIST_PROC = new NewListProc();

}
