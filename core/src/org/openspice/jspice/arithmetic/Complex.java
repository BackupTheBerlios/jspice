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
import org.openspice.tools.IntegerTools;


public class Complex extends Number implements Showable {

	private final Number real_part;
	private final Number imag_part;

	//	Danger!  Danger!
	Complex( final Number r, final Number i  ) {
		this.real_part = r;
		this.imag_part = i;
	}

	public static Number make( final Number r, final Number i  ) {
		if ( NumEqual.equal( i, IntegerTools.ZERO ) ) {
			return r;
		} else {
			return new Complex( r, i );
		}
	}

	public Number getRealPart() {
		return real_part;
	}

	public Number getImagPart() {
		return imag_part;
	}

	public Complex conjugate() {
		return new Complex( this.real_part, Negate.doNegate( this.imag_part ) );
	}

	public Double abs() {
		final double r = this.real_part.doubleValue();
		final double i = this.imag_part.doubleValue();
		return new Double( Math.sqrt( r * r + i * i ) );
	}

	public Double phase() {
		final double r = this.real_part.doubleValue();
		final double i = this.imag_part.doubleValue();
		return new Double( Math.atan2( i, r ) );
	}

	public Number reciprocal() {
		final double r = this.real_part.doubleValue();
		final double i = this.imag_part.doubleValue();
		final double abs2 = r * r + i * i;
		return make( new Double( r / abs2 ), new Double( -i/abs2 ) );
	}

	public Number sqrt() {
		final double r = this.real_part.doubleValue();
		final double i = this.imag_part.doubleValue();
		final double theta = Math.atan2( i, r ) / 2.0;
		final double mag = Math.sqrt( Math.sqrt( r * r + i * i ) );
		return make( new Double( mag * Math.cos( theta ) ), new Double( mag * Math.sin( theta ) ) );
	}

	//	---oooOOOooo---

	public int intValue() {
		return this.abs().intValue();
	}

	public long longValue() {
		return this.abs().longValue();
	}

	public float floatValue() {
		return this.abs().floatValue();
	}

	public double doubleValue() {
		return this.abs().doubleValue();
	}

	//	---oooOOOooo---

	public void showTo( Consumer cuchar ) {
		cuchar.outObject( this.real_part );
		cuchar.out( '+' );
		cuchar.outObject( this.imag_part );
		cuchar.out( 'i' );
	}

	public static final Complex I = new Complex( IntegerTools.ZERO, IntegerTools.ONE );
}
