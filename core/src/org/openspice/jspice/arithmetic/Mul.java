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

public final class Mul extends BinaryArithmeticOperator {

	public Object applyIntegers( final Integer x, final Integer y ) {
		final long r = x.longValue() * y.longValue();
		if ( Integer.MIN_VALUE <= r && r <= Integer.MAX_VALUE ) {
			return new Integer( (int)r );
		} else {
			return BigInteger.valueOf( r );
		}
	}

	public Object applyBigIntegers( final BigInteger x, final BigInteger y ) {
		return x.multiply( y );
	}

	public Object applyDoubles( final Double x, final Double y ) {
		return new Double( x.doubleValue() * y.doubleValue() );
	}

	public Object applyRationals( final Rational x, final Rational y ) {
		final BigInteger nx = x.getNumerator();
		final BigInteger dx = x.getDenominator();
		final BigInteger ny = y.getNumerator();
		final BigInteger dy = y.getDenominator();

//		nx * ny
//		-------
//		dx * dy

		return Rational.make( nx.multiply( ny ), dx.multiply( dy ) );
	}

	public Object applyComplexes( final Complex x, final Complex y ) {
		final Number rx = x.getRealPart();
		final Number ix = x.getImagPart();
		final Number ry = y.getRealPart();
		final Number iy = y.getImagPart();
		return(
			Complex.make(
				(Number)Sub.SUB.apply( (Number)this.apply( rx, ry ), (Number)this.apply( ix, iy ) ),
				(Number)Add.doAdd( (Number)this.apply( rx, iy ), (Number)this.apply( ix, ry ) )
			)
		);
	}

	public static final Mul MUL = new Mul();

	public static final Number doMul( final Number x, final Number y ) {
		return (Number)MUL.apply( x, y );
	}



}
