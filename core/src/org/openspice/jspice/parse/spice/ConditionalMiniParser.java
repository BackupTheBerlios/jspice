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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.MiniParser;
import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.SkipExpr;
import org.openspice.jspice.expr.cases.If3Expr;

public final class ConditionalMiniParser extends Prefix {

	final String closing_keyword;

	public ConditionalMiniParser( final String _closing_keyword ) {
		this.closing_keyword = _closing_keyword.intern();

	}

	public Expr prefix( final String interned, final Parser parser ) {
		final Expr test = parser.readExprTo( "then" );
		final Expr ifso = parser.readStmnts();
		Expr ifnot = null;
		if ( parser.tryReadToken( "elseif" ) != null ) {
			ifnot = this.prefix( "if", parser );
		} else if ( parser.tryReadToken( "elseunless" ) != null ) {
			ifnot = this.prefix( "unless", parser );
		} else if ( parser.tryReadToken( "else" ) != null ) {
			ifnot = parser.readStmntsTo( this.closing_keyword );
		} else {
			parser.mustReadToken( this.closing_keyword );
			ifnot = SkipExpr.make();
		}
		if ( interned == "if" ) {
			return If3Expr.make( test, ifso, ifnot );
		} else {
			return If3Expr.make( test, ifnot, ifso );
		}
	}

}
