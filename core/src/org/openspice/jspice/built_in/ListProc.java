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

import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.datatypes.ImmutableList;
import org.openspice.jspice.datatypes.proc.Vary1Proc;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.built_in.InvListProc;

class ListProc extends Vary1Proc {
	final static public org.openspice.jspice.built_in.ListProc LIST_PROC = new org.openspice.jspice.built_in.ListProc();

	public Proc inverse() {
		return InvListProc.INV_LIST_PROC;
	}

	public Object call( final Object tos, final VM vm, final int nargs ) {
		switch ( nargs ) {
			case 0: {
				vm.push( tos );
				return new ImmutableList.Maker().make();
			}
			case 1: {
				return new ImmutableList.Maker().addLast( tos ).make();
			}
			default: {
				final ImmutableList.Maker maker = new ImmutableList.Maker();
				maker.addFirst( tos );
				for ( int i = 1; i < nargs; i++ ) {
					maker.addFirst( vm.pop() );
				}
				return maker.make();
			}
		}
	}
}
