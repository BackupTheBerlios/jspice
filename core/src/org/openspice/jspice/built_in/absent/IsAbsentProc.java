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
package org.openspice.jspice.built_in.absent;

import org.openspice.jspice.datatypes.proc.Unary1BoolInvokeProc;
import org.openspice.jspice.lib.IsLib;

public class IsAbsentProc extends Unary1BoolInvokeProc {

	{
		setDescription(
			"isAbsent",
			"%p( item ) -> bool",
			"returns true if x is absent, otherwise false"
		);
	}

	public Object invoke( Object x ) {
		return Boolean.valueOf( IsLib.isAbsent( x ) );
	}

	public static final IsAbsentProc IS_ABSENT_PROC = new IsAbsentProc();

}
