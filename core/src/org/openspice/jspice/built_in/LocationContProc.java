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

import org.openspice.jspice.namespace.Location;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.built_in.InvLocationContProc;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.datatypes.proc.Proc;

public class LocationContProc extends Unary1InvokeProc {
	final static public org.openspice.jspice.built_in.LocationContProc LOCATION_CONT_PROC = new org.openspice.jspice.built_in.LocationContProc();

	public Object invoke( final Object locn ) {
		try {
			return ((Location)locn).getValue();
		} catch ( final Exception exn ) {
			Alert.unreachable();
			return null; 	//	sop
		}
	}

	public Proc inverse() {
		return InvLocationContProc.INV_LOCATION_CONT_PROC;
	}

	public Object ucall( final Object tos, final VM vm, final int vargs, final int kargs ) {
		//	Not too sure about this stuff - hacked out in a hurry!
		this.keysUArity().check( kargs );
		this.valsUArity().check( vargs );
		final Location locn = (Location)tos;
		final Object value = vm.pop();
		locn.setValue( value );
		return vm.pop();
	}
}
