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

import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.SkipExpr;
import org.openspice.jspice.expr.cases.CommaExpr;
import org.openspice.jspice.expr.cases.ConstantExpr;
import org.openspice.jspice.expr.cases.ApplyExpr;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NumberToken;
import org.openspice.jspice.tokens.QuotedToken;
import org.openspice.jspice.tokens.WordLikeToken;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.Symbol;
import org.openspice.jspice.built_in.lists.InvListProc;
import org.openspice.jspice.built_in.lists.NewListProc;

import java.util.regex.Pattern;

public class QuasiQuoteMiniParser extends Prefix {

	public Expr prefix( final String interned, final Parser parser ) {
		return this.readQQ( parser, ">>" );
	}

	public Expr readQQ( final Parser parser, final String terminator ) {
		Expr sofar = SkipExpr.make();
		while ( parser.tryReadToken( terminator ) == null ) {
			if ( parser.tryReadToken( ">>" ) != null ) {
				new Alert( "Unmatched close bracket" ).culprit( "bracket", ">>" ).mishap();
			} else if ( parser.tryReadToken( "{" ) != null ) {
				sofar = CommaExpr.make( sofar, ApplyExpr.make( NewListProc.NEW_LIST_PROC, this.readQQ( parser, "}" ) ) );
			} else if ( parser.tryReadToken( "^" ) != null ) {
				if ( parser.tryReadToken( "(" ) != null ) {
					sofar = CommaExpr.make( sofar, parser.readStmntsTo( ")" ) );
				} else {
					sofar = CommaExpr.make( sofar, parser.readNameExpr() );
				}
			} else if ( parser.tryReadToken( "^^" ) != null ) {
				if ( parser.tryReadToken( "(" ) != null ) {
					sofar = CommaExpr.make( sofar, ApplyExpr.make( InvListProc.INV_LIST_PROC, parser.readStmntsTo( ")" ) ) );
				} else {
					sofar = CommaExpr.make( sofar, ApplyExpr.make( InvListProc.INV_LIST_PROC, parser.readNameExpr() ) );
				}
			} else {
				final Token t = parser.readToken();
				if ( t == null ) {
					new Alert( "Unexpected end of input inside quasiquotes" ).mishap();
				}
				if ( t instanceof NumberToken ) {
					sofar = CommaExpr.make( sofar, ConstantExpr.make( ((NumberToken)t).getNumber() ) );
				} else if ( t instanceof QuotedToken ) {
					final QuotedToken qt = (QuotedToken)t;
					final String w = t.getWord();
					if ( qt.isStringToken() ) {
						sofar = CommaExpr.make( sofar, ConstantExpr.make( w ) );
					} else if ( qt.isSymbolToken() ) {
						sofar = CommaExpr.make( sofar, ConstantExpr.make( Symbol.make( w ) ) );
					} else if ( qt.isCharsToken() ) {
						for ( int i = 0; i < w.length(); i++ ) {
							sofar = CommaExpr.make( sofar, ConstantExpr.make( new Character( w.charAt( i ) ) ) );
						}
					} else if ( qt.isRegexpToken() ) {
						sofar = CommaExpr.make( sofar, ConstantExpr.make( Pattern.compile( w ) ) );
					} else {
						Alert.unreachable();
					}
				} else if ( t instanceof WordLikeToken ) {
					sofar = CommaExpr.make( sofar, ConstantExpr.make( Symbol.make( t.getWord() ) ) );
				} else {
					Alert.unreachable();
				}
			}
		}
		return sofar;
	}

}
