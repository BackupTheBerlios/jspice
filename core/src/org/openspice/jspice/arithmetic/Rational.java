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
import org.openspice.jspice.tools.Showable;
import org.openspice.jspice.tools.Consumer;

import java.math.BigInteger;

public class Rational extends Number implements Showable {

	private final BigInteger numerator;
	private final BigInteger denominator;

	//	Danger!  Danger!  Will Robinson!
	public Rational( final BigInteger n, final BigInteger d  ) {
		this.numerator = n;
		this.denominator = d;
	}

	//	Danger!  Danger!  Will Robinson!
	public Rational( final Integer a, final Integer b ) {
		this( BigInteger.valueOf( a.longValue() ), BigInteger.valueOf( b.longValue() ) );
	}

	public static Number make( Integer a, Integer b ) {
		return make( BigInteger.valueOf( a.longValue() ), BigInteger.valueOf( b.longValue() ) );
	}

	public static Number make( BigInteger n, BigInteger d  ) {
		final int dsign = d.compareTo( BigInteger.ZERO );

		//	Sort out signs.
		if ( dsign == 0 ) {
			new Alert( "Divide by zero", "Rational numbers must have non-zero denominator" ).mishap();
		} else if ( dsign < 0 ) {
			n = n.negate();
			d = d.negate();
		}

		final BigInteger c = n.gcd( d );
		if ( c.compareTo( BigInteger.ONE ) > 0 ) {
			n = n.divide( c );
			d = d.divide( c );
		}
		if ( d.compareTo( BigInteger.ONE ) == 0 ) {
			return n;
		} else {
			return new Rational( n, d );
		}
	}

	public BigInteger getNumerator() {
		return numerator;
	}

	public BigInteger getDenominator() {
		return denominator;
	}

	public static final int determinant( final Rational x, final Rational y) {
		final BigInteger nx = x.getNumerator();
		final BigInteger dx = x.getDenominator();
		final BigInteger ny = y.getNumerator();
		final BigInteger dy = y.getDenominator();

//		 nx     ny         nx * dy - ny * dx
//		---- - ----  ;     -----------------
//		 dx     dy              dx * dy

		final BigInteger nxdy = nx.multiply( dy );
		final BigInteger nydx = ny.multiply( dx );
		final int sign_dxdy = dx.signum() * dy.signum();
		final int nxdy_nydx = nxdy.compareTo( nydx );
		return sign_dxdy * nxdy_nydx;
	}

	//	---oooOOOooo---

	public int intValue() {
		return (int)( this.numerator.doubleValue() / this.denominator.doubleValue() );
	}

	public long longValue() {
		return (long)( this.numerator.doubleValue() / this.denominator.doubleValue() );
	}

	public float floatValue() {
		return (float)( this.numerator.doubleValue() / this.denominator.doubleValue() );
	}

	public double doubleValue() {
		return this.numerator.doubleValue() / this.denominator.doubleValue();
	}

	//	---oooOOOooo---

	public Rational abs() {
		return new Rational( this.numerator.abs(), this.denominator );
	}

	//	---oooOOOooo---

	public void showTo( Consumer cuchar ) {
		cuchar.outObject( this.numerator );
		cuchar.out( '/' );
		cuchar.outObject( this.denominator );
	}

}
