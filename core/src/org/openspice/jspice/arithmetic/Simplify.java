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
package org.openspice.jspice.arithmetic;

import org.openspice.jspice.alert.Alert;

import java.math.BigInteger;
import java.math.BigDecimal;

public final class Simplify {

	public static Number simplify( final Number x ) {
		if ( x instanceof Integer || x instanceof Double || x instanceof BigInteger || x instanceof Rational || x instanceof Complex ) {
			//	Standard representation.
			return x;
		} else if ( x instanceof Byte || x instanceof Short ) {
			return new Integer( x.intValue() );
		} else if ( x instanceof Long || x instanceof BigDecimal ) {
			return new Double( x.doubleValue() );
		} else {
			//	I'm not entirely sure this is the way to handle this problem ....
			new Alert( "Converting unrecognized number type to Double" ).culprit( "number", x ).warning();
			return new Double( x.doubleValue() );
		}
	}

}
