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
package org.openspice.jspice.tokens;

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.parse.TokenParser;
import org.openspice.jspice.alert.Alert;
import org.openspice.tools.IntegerTools;

import java.math.BigInteger;

public final class NumberToken extends Token {
	private Number number;

	public static final Number parseNumber( final String num ) {
		try {
			return IntegerTools.make( Integer.parseInt( num ) );
		} catch ( final NumberFormatException ex1 ) {
			try {
				return new BigInteger( num );
			} catch ( final NumberFormatException ex2 ) {
				try {
					return new Double( Double.parseDouble( num ) );
				} catch ( final NumberFormatException ex3 ) {
					throw new Alert( "Cannot understand this number token" ).culprit( "number", num ).mishap();
				}
			}
		}
	}

	public NumberToken( final boolean _hadWhiteSpaceStart, final String _word ) {
		super( _hadWhiteSpaceStart, _word );
		this.number = parseNumber( _word );

	}

	public Number getNumber() {
		return this.number;
	}

	public String toString() {
		return "<number " + this.number + ">";
	}

	boolean isNegative() {
		return this.number.doubleValue() < 0.0;
	}

	public Expr parse( final org.openspice.jspice.parse.Parser parser, final int prec, final Expr lhs, final TokenParser token_parser ) {
		return token_parser.parseNumber( this, prec, lhs, parser );
	}

}
