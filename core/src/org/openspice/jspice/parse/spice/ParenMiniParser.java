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

import org.openspice.jspice.parse.miniparser.Bothfix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.ThunkDeferred;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.alert.Alert;

public final class ParenMiniParser extends Bothfix {

	public Expr postfix( final String sym, final int prec, final Expr lhs, final Parser parser ) {
		return ApplyExpr.make( lhs, parser.readStmntsTo( ")" ) );
	}

	public Expr prefix( final String sym, final Parser parser ) {
		if ( parser.tryReadToken( "=|" ) != null ) {
			return this.defer( parser );
		} else {
			final Expr lhs = parser.readStmnts();
			if ( parser.tryReadToken( "=>" ) != null || parser.tryReadToken( "as" ) != null ) {
				/*
				 * Note: with the addition of structure patterns you cannot allow optional
				 * function names - they get confused with single atrgument functions.
				 */
				final Expr rhs = parser.readStmntsTo( ")" );
				final Decurrier d = new Decurrier( ApplyExpr.make( new AnonExpr(), lhs ), rhs ).canonize();
				return FunMiniParser.make( d );
			} else {
				parser.mustReadToken( ")" );
				return lhs;
			}
		}
	}

	private Expr defer( final Parser parser ) {
		final Expr e = parser.readStmntsTo( "|=" );
		parser.mustReadToken( ")" );
		final Expr thunk_expr = LambdaExpr.make( SkipExpr.make(), CheckOneExpr.make( e ) );
		return make_defer( thunk_expr );
	}

	private Expr make_defer( final Expr thunk_expr ) {
		return (
			ApplyExpr.make(
				new Unary1InvokeProc() {
					public Object invoke( final Object _ ) {
						throw Alert.unreachable();
					}

					public Object fastCall( final Object tos, final VM vm, final int nargs ) {
						return new ThunkDeferred( (Proc)tos, vm );	//	Guaranteed to be a Proc.
					}
				},
				thunk_expr
			)
		);
	}

}
