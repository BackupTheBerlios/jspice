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

package org.openspice.jspice.expr.cases;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.built_in.ShortCuts;

public final class ApplyExpr extends BinaryExpr {

	private ApplyExpr( final Expr _fun, final Expr _args ) {
		//super( CheckOneExpr.make( _fun ), _args );
		super( _fun, _args );
	}

	public Arity arity() {
		final Object obj = this.lhs.maybeConstantExpr();
		if ( obj instanceof Proc ) {
			return ((Proc)obj).outArity();
		} else {
			return Arity.ZERO_OR_MORE;
		}
	}

	public Expr getFun() {
		return this.lhs;
	}

	public Expr getArg() {
		return this.rhs;
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitApplyExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( kids.next(), kids.next() );
	}

	public static ApplyExpr makeAnonApply( final Expr arg ) {
		return new ApplyExpr( NameExpr.make( "_" ), arg );
	}

	public static Expr make( final Expr f, final Expr a ) {
		return new ApplyExpr( f, a );
	}

	public static Expr make( final Proc p, final Expr a ) {
		return ApplyExpr.make( ConstantExpr.make( p ), a );
	}

	public static Expr make( final String s, final Expr a ) {
		return ApplyExpr.make( ShortCuts.lookupProc( s ), a );
	}

	/*public static Expr make1( final Proc p, final Expr a ) {
		return Expr.ApplyExpr.make( ConstantExpr.make( p ), CheckOneExpr.make( a ) );
	}*/

	public static Expr make1( final Proc p, final Expr a, final Expr b ) {
		return (
			ApplyExpr.make(
				ConstantExpr.make( p ),
				CommaExpr.make( CheckOneExpr.make( a ), CheckOneExpr.make( b ) )
			)
		);
	}

	public static Expr make1( final String s, final Expr a, final Expr b ) {
		return make1( ShortCuts.lookupProc( s ), a, b );
	}

	/*public static Expr make1( final String s, final Expr a ) {
		return make1( Proc.lookupProc( s ), a );
	}*/

	public static Expr make1( final Proc p, final Expr a, final Expr b, final Expr c ) {
		return (
			ApplyExpr.make(
				ConstantExpr.make( p ),
				CommaExpr.make(
					CheckOneExpr.make( a ),
					CommaExpr.make(
						CheckOneExpr.make( b ),
						CheckOneExpr.make( c )
					)
				)
			)
		);
	}

}
