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

import org.openspice.jspice.datatypes.proc.Unary1BoolInvokeProc;
import org.openspice.jspice.lib.IsLib;

public class IsMapletProc extends Unary1BoolInvokeProc {

	{
		setDescription(
			"isMaplet",
			"%p( item ) -> bool",
			"return true if item is a maplet, otherwise false"
		);
	}

	public Object invoke( final Object x ) {
		return Boolean.valueOf( IsLib.isMapEntry( x ) );
	}

	public static final IsMapletProc IS_MAPLET_PROC= new IsMapletProc();


}
