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

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.NameExpr;
import org.openspice.jspice.expr.cases.LambdaExpr;
import org.openspice.jspice.expr.cases.ApplyExpr;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.parse.Parser;

final class Decurrier {

	private ApplyExpr app;
	private Expr body;

	public ApplyExpr getApp() {
		return app;
	}

	public Expr getBody() {
		return body;
	}

	Decurrier( final Expr _header, final Expr _body ) {
		if ( _header instanceof ApplyExpr ) {
			this.app = (ApplyExpr)_header;
		} else {
			this.app = ApplyExpr.makeAnonApply( _header );
		}
		this.body = _body;
	}

	Decurrier canonize() {
		while ( app.getFun() instanceof ApplyExpr ) {
			body = LambdaExpr.make( ApplyExpr.makeAnonApply( app.getArg() ), body );
			app = (ApplyExpr)app.getFun();
		}
		if ( ! ( app.getFun() instanceof NameExpr ) ) {
			new Alert( "Invalid procedure header" ).culprit( "expr", app.getFun() ).mishap( 'P' );
		}
		return this;
	}

	public static Decurrier parse( final Parser parser, final String closing_keyword ) {
		final Expr head = parser.readDefineHead();
		if ( parser.tryReadToken( "=>" ) == null && parser.tryReadToken( "as" ) == null ) {
			new Alert( "Expecting '=>' or 'as' before the body of this definition" ).culprit( "token", parser.peekToken() ).mishap();
		}
		final Expr body = parser.readStmntsTo( closing_keyword );
		return new Decurrier( head, body ).canonize();
	}

}
