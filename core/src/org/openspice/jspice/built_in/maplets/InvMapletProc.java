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
package org.openspice.jspice.built_in.maplets;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.UnaryFastProc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.maplets.NewMapletProc;

import java.util.Map;

public class InvMapletProc extends UnaryFastProc {

	final static public org.openspice.jspice.built_in.maplets.InvMapletProc INV_MAPLET_PROC = new org.openspice.jspice.built_in.maplets.InvMapletProc();

	public Proc inverse() {
		return NewMapletProc.NEW_MAPLET_PROC;
	}

	public Arity outArity() { return Arity.TWO; }

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		try {
			final Map.Entry me = (Map.Entry)tos;
			vm.push( me.getKey() );
			return me.getValue();
		} catch ( final ClassCastException exn ) {
			return new Alert( "Maplet needed" ).culprit( "object", tos ).mishap();
		}
	}
}
