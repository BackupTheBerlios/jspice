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
package org.openspice.jspice.built_in;

import org.openspice.jspice.datatypes.proc.Nullary0FastProc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.vm_and_compiler.VM;

import java.util.Iterator;

public class LastResultsProc extends Nullary0FastProc {

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		Arity.ZERO.check( nargs );
		vm.push( tos );
		for ( Iterator it = vm.getAllResults().iterator(); it.hasNext(); ) {
			vm.push( it.next() );
		}
		return vm.pop();
	}

	public static final LastResultsProc LAST_RESULTS_PROC = new LastResultsProc();

}
