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

import org.openspice.jspice.vm_and_compiler.VM;

public abstract class SlotProc extends Unary1InvokeProc {

	public abstract void invoke_updater( final Object key, final Object value );

	public Object ucall( final Object tos, final VM vm, final int vargs, final int kargs ) {
		if ( kargs == 1 && vargs == 1 ) {
			this.invoke_updater( tos, vm.pop() );
			return vm.pop();
		} else {
			//	Wrong number of parameters.
			throw this.fail_updater( "Wrong number of parameters for slot update", "Should be one key and one value", tos, vm, vargs, kargs );
		}
	}

}
