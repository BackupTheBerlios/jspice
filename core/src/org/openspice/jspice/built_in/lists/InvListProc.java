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

import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.tools.ListTools;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Unary1PlusFastProc;

import java.util.Iterator;
import java.util.List;

public class InvListProc extends Unary1PlusFastProc {

	final static public org.openspice.jspice.built_in.lists.InvListProc INV_LIST_PROC = new org.openspice.jspice.built_in.lists.InvListProc();

	public Proc inverse() {
		return ListProc.LIST_PROC;
	}

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		for( final Iterator it = ListTools.iterator( tos ); it.hasNext(); ) {
			vm.push( it.next() );
		}
		return vm.pop();
	}

	public Object ucall( final Object tos, final VM vm, final int vargs, final int kargs ) {
		this.keysUArity().check( kargs );
		final List list = CastLib.toList( tos );
		try {
			list.clear();
			for ( int i = vargs - 1; i >= 0; i-- ) {
				list.add( vm.get( i ) );
			}
		} catch ( final UnsupportedOperationException exn ) {
			new Alert( "Trying to update an immutable list" ).culprit( "list", list ).mishap( 'E' );
		}
		vm.drop( vargs );
		return vm.pop();
	}
}