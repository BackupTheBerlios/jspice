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

import java.math.BigInteger;
import org.openspice.tools.IntegerTools;


public class GreaterThanOrEqual extends BinaryArithmeticOperator {
	
	public Object applyIntegers( final Integer x, final Integer y ) {
		return Boolean.valueOf( x.intValue() >= y.intValue() );
	}

	public Object applyBigIntegers( final BigInteger x, final BigInteger y ) {
		return Boolean.valueOf( x.compareTo( y ) >= 0 );
	}

	public Object applyDoubles( final Double x, final Double y ) {
		return Boolean.valueOf( x.doubleValue() >= y.doubleValue() );
	}

	public Object applyRationals( final Rational x, final Rational y ) {
		return Boolean.valueOf( Rational.determinant( x, y ) >= 0 );
	}

	public Object applyComplexes( Complex x, Complex y ) {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public static boolean gtez( final Number n ) {
		final Boolean r = (Boolean)GREATER_THAN_OR_EQUAL.apply( n, IntegerTools.ZERO );
		return r.booleanValue();
	}

	public static boolean gte( final Number a, final Number b ) {
		final Boolean r = (Boolean)GREATER_THAN_OR_EQUAL.apply( a, b );
		return r.booleanValue();
	}

	public static final GreaterThanOrEqual GREATER_THAN_OR_EQUAL = new GreaterThanOrEqual();

}
