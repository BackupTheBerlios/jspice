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
package org.openspice.jspice.built_in.comparisons;

import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;
import org.openspice.jspice.datatypes.Deferred;
import org.openspice.jspice.arithmetic.NumEqual;
import org.openspice.jspice.tools.CharSequenceTools;

public class EqualProc extends Binary1InvokeProc {

	public Object invoke( Object x, Object y ) {
		x = Deferred.deref(  x );
		y = Deferred.deref( y );
		try {
			if ( x == null ) {
				return Boolean.valueOf( y == null );
			} else if ( x instanceof Number ) {
				return NumEqual.NUM_EQUAL.apply( (Number)x, (Number)y );
			} else if ( x instanceof CharSequence ) {
				if ( y instanceof CharSequence ) {
					return Boolean.valueOf( CharSequenceTools.equals( (CharSequence)x, (CharSequence)y ) );
				} else {
					return Boolean.FALSE;
				}
			} else {
				return Boolean.valueOf( x.equals( y ) );
			}
		} catch ( final ClassCastException _ ) {
			return Boolean.FALSE;
		}
	}

	public static final EqualProc EQUAL_PROC = new EqualProc();

}
