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

import java.util.Map;
import java.util.TreeMap;

public final class QuotedToken extends WordLikeToken {

	private final char flavour;
	private final Map opt_interpolation_map;

	public QuotedToken( final boolean _hadWhiteSpaceStart, final String _word, final char _flavour, final Map interpolation_map ) {
		super( _hadWhiteSpaceStart, _word );
		this.flavour = _flavour;
		this.opt_interpolation_map = interpolation_map != null ? new TreeMap( interpolation_map ) : null;
	}

	public boolean isSymbolToken() {
		return this.flavour == '`';
	}

	public boolean isCharsToken() {
		return this.flavour == '\'';
	}

	public boolean isRegexpToken() {
		return this.flavour == '/';
	}

	public boolean isStringToken() {
		return this.flavour == '"';
	}


	public String kind() {
		switch ( this.flavour ) {
		case '`':
			return "symbol";
		case '\'':
			return "chars";
		case '/':
			return "regexp";
		}
		return "string";
	}

	public Expr parse( final org.openspice.jspice.parse.Parser parser, final int prec, final Expr lhs, final TokenParser token_parser ) {
		return token_parser.parseQuoted( this, prec, lhs, parser );
	}

	public Map getOptInterpolationMap() {
		return this.opt_interpolation_map;
	}

}
