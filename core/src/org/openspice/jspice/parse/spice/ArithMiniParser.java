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
import org.openspice.jspice.parse.miniparser.Bothfix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.ApplyExpr;
import org.openspice.jspice.alert.Alert;

public class ArithMiniParser extends Bothfix {

	public Expr postfix( final String sym, final int prec, final Expr lhs, final Parser parser ) {
		final Expr rhs = parser.readExprPrec( prec );
		return ApplyExpr.make1( sym, lhs, rhs );
	}

	public Expr prefix( final String sym, final Parser parser ) {
		if ( sym.equals( "+" ) ) {
			return ApplyExpr.make( "unary_+", parser.readExpr() );
		} else if ( sym.equals( "-" ) ) {
			return ApplyExpr.make( "unary_-", parser.readExpr() );
		} else {
			new Alert(
				"Infix operator missing left hand argument"
			).culprit( "operator", sym ).mishap( 'P' );
			return null;	//	sop
		}
	}

}
