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
import org.openspice.jspice.parse.Parser;

public final class NameToken extends WordLikeToken {

	public NameToken( final boolean _hadWhiteSpaceStart, final String _word ) {
		super( _hadWhiteSpaceStart, _word.intern() );
	}

	public final boolean isNameToken() {
		return true;
	}

	public String getInterned() {
		return this.word;
	}

	String kind() { return "name"; }

	public boolean hasName( final String sym ) {
		return sym == this.word;				// 	this.word is intern'd
	}

	public Expr parse( final Parser parser, final int prec, final Expr lhs, final TokenParser token_parser ) {
		assert parser != null;
		assert token_parser != null;
		//System.out.println( "parser = " + parser + "; token_parser = " + token_parser );
		return token_parser.parseName( this, prec, lhs, parser );
	}

}
