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

public class Negate extends Unary1ArithmeticOperator {

	public Object applyInteger( final Integer x ) {
		final int n = x.intValue();
		if ( n == Integer.MIN_VALUE ) {
			return BigInteger.valueOf( -((long)n) );
		} else {
			return new Integer( -n );
		}
	}

	public Object applyBigInteger( final BigInteger x ) {
		return x.negate();
	}

	public Object applyRational( final Rational x ) {
		//	We can avoid the cost of Rational.make here.
		return new Rational( x.getNumerator().negate(), x.getDenominator() );
	}

	public Object applyDouble( final Double x ) {
		return new Double( -x.doubleValue() );
	}

	public Object applyComplex( Complex x ) {
		return Complex.make( (Number)this.apply( x.getRealPart() ), x.getImagPart() );
	}

	public static final Negate NEGATE = new Negate();

	public static final Number doNegate( final Number x ) {
		return (Number)Negate.NEGATE.apply( x );
	}

}
