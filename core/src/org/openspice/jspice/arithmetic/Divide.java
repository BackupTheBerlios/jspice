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

public class Divide extends BinaryArithmeticOperator {

	public Object applyIntegers( final Integer x, final Integer y ) {
		return Rational.make( x, y );
	}

	public Object applyBigIntegers( final BigInteger x, final BigInteger y ) {
		return new Double( x.doubleValue() / y.doubleValue() );
	}

	public Object applyDoubles( final Double x, final Double y ) {
		return new Double( x.doubleValue() / y.doubleValue() );
	}

	public Object applyRationals( final Rational x, final Rational y ) {
		final BigInteger nx = x.getNumerator();
		final BigInteger dx = x.getDenominator();
		final BigInteger ny = y.getNumerator();
		final BigInteger dy = y.getDenominator();

//		nx * dy
//		-------
//		dx * ny

		return Rational.make( nx.multiply( dy ), dx.multiply( ny ) );
	}

	public Object applyComplexes( final Complex x, final Complex y ) {
		return Mul.MUL.apply( x, y.reciprocal() );
	}



	public static final Divide DIVIDE = new Divide();

}
