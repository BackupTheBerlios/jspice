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
import org.openspice.jspice.parse.SpiceParser;
import org.openspice.jspice.parse.Prec;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.InitExpr;
import org.openspice.jspice.expr.cases.ConstantExpr;

public class InitMiniParser extends Prefix {

	final Props props;

	public InitMiniParser( final boolean _constant ) {
		this.props = new Props().modConstant( _constant );
	}

	public Expr prefix( final String interned, final Parser parser ) {
		final Expr init_expr = parser.readExprPrec( Prec.up_to_relop );
		Expr src_expr = ConstantExpr.ABSENT_EXPR;
		if ( parser.canReadToken( "=" ) ) {
			src_expr = parser.readExpr();
		}
		return InitExpr.make( null, this.props, init_expr, src_expr );
	}

}
