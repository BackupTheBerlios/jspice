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
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.parse.TokenParser;
import org.openspice.jspice.alert.Alert;

import java.util.*;


public abstract class Token {
    private final boolean hadWhiteSpaceStart;
    protected final String word;
	public abstract Expr parse( final Parser parser, final int prec, final Expr lhs, final TokenParser token_parser );

    Token( final boolean _hadWhiteSpaceStart, final String _word ) {
        this.hadWhiteSpaceStart = _hadWhiteSpaceStart;
        this.word = _word;
    }

	public boolean isNameToken() {
		return false;
	}

	public boolean matches( final Token that ) {
		return this.word.equals( that.word );
	}

    public boolean hadWhiteSpaceStart() {
        return this.hadWhiteSpaceStart;
    }

    public String getWord() {
        return this.word;
    }

    public boolean hasName( final String sym ) {
        return false;
    }

}

