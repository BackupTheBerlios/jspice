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
import org.openspice.tools.IntegerTools;
import org.openspice.tools.BigIntegerTools;

/**
 * The tower of exact numbers is fairly complicated in Java.
 * 		Byte -> Short -> Integer -> Long -> BigInteger
 * And inexacts look like this
 * 		Float -> Double -> BigDecimal
 *
 * In order to map this into Spice's number system we will
 * utilize Integer and BigInteger for exact numbers, coercing
 * up as required, and Double for inexact numbers.  Then we
 * will create Rationals for exacts.  And lastly Complex.
 */
public abstract class BinaryArithmeticOperator {

	public abstract Object applyIntegers( Integer x, Integer y );
	public abstract Object applyBigIntegers( BigInteger x, BigInteger y );
	public abstract Object applyDoubles( Double x, Double y );
	public abstract Object applyRationals( Rational x, Rational y );
	public abstract Object applyComplexes( Complex x, Complex y );

	private final Object unify( final Number x, final Number y ) {
		if ( x instanceof Integer ) {
			if ( y instanceof BigInteger ) {
				return this.apply( BigInteger.valueOf( x.longValue() ), y );
			} else if ( y instanceof Double ) {
				return this.apply( new Double( x.doubleValue() ), y );
			} else if ( y instanceof Rational ) {
				return this.apply( new Rational( (Integer)x, IntegerTools.ONE ), y );
			} else if ( y instanceof Complex ) {
				return this.apply( new Complex( x, IntegerTools.ZERO ), y );
			} else {
				throw Alert.unreachable();
			}
		} else if ( x instanceof BigInteger ) {
			if ( y instanceof Integer ) {
				return this.apply( x, BigInteger.valueOf( y.longValue() ) );
			} else if ( y instanceof Double ) {
				return this.apply( new Double( x.doubleValue() ), y );
			} else if ( y instanceof Rational ) {
				return this.apply( new Rational( (BigInteger)x, BigIntegerTools.ONE ), y );
			} else if ( y instanceof Complex ) {
				return this.apply( new Complex( x, IntegerTools.ZERO ), y );
			} else {
				throw Alert.unreachable();
			}
		} else if ( x instanceof Double ) {
			if ( y instanceof Integer ) {
				return this.apply( x, new Double( y.doubleValue() ) );
			} else if ( y instanceof BigInteger ) {
				return this.apply( x, new Double( y.doubleValue() ) );
			} else if ( y instanceof Rational ) {
				return this.apply( x, new Double( y.doubleValue() ) );
			} else if ( y instanceof Complex ) {
				return this.apply( new Complex( x, IntegerTools.ZERO ), y );
			} else {
				throw Alert.unreachable();
			}
		} else if ( x instanceof Rational ) {
			if ( y instanceof Integer ) {
				return this.apply( x, new Rational( (Integer)y, IntegerTools.ONE ) );
			} else if ( y instanceof BigInteger ) {
				return this.apply( x, new Rational( (BigInteger)y, BigIntegerTools.ONE ) );
			} else if ( y instanceof Double ) {
				return this.apply( new Double( x.doubleValue() ), y );
			} else if ( y instanceof Complex ) {
				return this.apply( new Complex( x, IntegerTools.ZERO ), y );
			} else {
				throw Alert.unreachable();
			}
		} else if ( x instanceof Complex ) {
			if ( y instanceof Integer ) {
				return this.apply( x, new Complex( y, IntegerTools.ZERO ) );
			} else if ( y instanceof BigInteger ) {
				return this.apply( x, new Complex( y, IntegerTools.ZERO ) );
			} else if ( y instanceof Double ) {
				return this.apply( x, new Complex( y, IntegerTools.ZERO ) );
			} else if ( y instanceof Complex ) {
				return this.apply( x, new Complex( y, IntegerTools.ZERO ) );
			} else {
				throw Alert.unreachable();
			}
		} else {
			throw Alert.unreachable();
		}
	}

	public final Object apply( final Number x, final Number y ) {
		final Class cx = x.getClass();
		final Class cy = y.getClass();
		if ( cx == cy ) {
			if ( cx == Integer.class ) {
				return this.applyIntegers( (Integer)x, (Integer)y );
			} else if ( cx == BigInteger.class ) {
				return this.applyBigIntegers( (BigInteger)x, (BigInteger)y );
			} else if ( cx == Double.class ) {
				return this.applyDoubles( (Double)x, (Double)y );
			} else if ( cx == Rational.class ) {
				return this.applyRationals( (Rational)x, (Rational)y );
			} else if ( cx == Complex.class ) {
				return this.applyComplexes( (Complex)x, (Complex)y );
			} else {
				//	They may be equal classes but they ain't supported.
				return this.apply( Simplify.simplify( x ), Simplify.simplify( y ) );
			}
		} else {
			final Number x1 = Simplify.simplify( x );
			final Number y1 = Simplify.simplify( y );
			if ( x == x1 && y == y1 ) {
				//	Simplification means that unification will work.
				return this.unify( x1, y1 );
			} else {
				//	Just run thru again, simplification made a difference.
				return this.apply( x1, y1 );
			}
		}
	}

}
