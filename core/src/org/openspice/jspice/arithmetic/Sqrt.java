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

import org.openspice.jspice.lib.CastLib;

import java.math.BigInteger;
import org.openspice.tools.IntegerTools;


public class Sqrt extends Unary1ArithmeticOperator {

	private Number sqrtDouble( final double d ) {
		if ( d >= 0 ) return new Double( Math.sqrt( d ) );
		return Complex.make( IntegerTools.ZERO, new Double( Math.sqrt( -d ) ) );
	}

	public Object applyInteger( final Integer x ) {
		return sqrtDouble( x.doubleValue() );
	}

	public Object applyBigInteger( final BigInteger x ) {
		return sqrtDouble( x.doubleValue() );
	}

	public Object applyRational( final Rational x ) {
		return sqrtDouble( x.doubleValue() );
	}

	public Object applyDouble( final Double x ) {
		return sqrtDouble( x.doubleValue() );
	}

	public Object applyComplex( Complex x ) {
		return x.sqrt();
	}

	public static final Sqrt SQRT = new Sqrt();

	public static final Object doSqrt( final Object x ) {
		return SQRT.apply( CastLib.toNumber( x ) );
	}

}
