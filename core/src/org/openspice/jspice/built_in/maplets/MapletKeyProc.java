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

import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.lib.CastLib;

public class MapletKeyProc extends Unary1InvokeProc {

	{
		setDescription(
			"mapletKey",
			"%p( k ==> v ) -> k",
			"returns the key (left hand side) of a maplet"
		);
	}

	public Object invoke( final Object x ) {
		return CastLib.toMaplet( x ).getKey();
	}

	public static final MapletKeyProc MAPLET_KEY_PROC = new MapletKeyProc();

}
