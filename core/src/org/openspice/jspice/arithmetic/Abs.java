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

public class Abs extends Unary1ArithmeticOperator {

	public Object applyInteger( final Integer x ) {
		return new Integer( Math.abs( x.intValue() ) );
	}

	public Object applyBigInteger( final BigInteger x ) {
		return x.abs();
	}

	public Object applyRational( final Rational x ) {
		return x.abs();
	}

	public Object applyDouble( final Double x ) {
		return new Double( Math.abs( x.doubleValue() ) );
	}

	public Object applyComplex( Complex x ) {
		return x.abs();
	}

	public static final Abs ABS = new Abs();

	public static final Object doAbs( final Object x ) {
		return ABS.apply( CastLib.toNumber( x ) );
	}

}
