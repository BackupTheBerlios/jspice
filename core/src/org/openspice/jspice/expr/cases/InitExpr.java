package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.ZeroResults;
import org.openspice.jspice.expr.cases.BinaryExpr;
import org.openspice.jspice.expr.cases.CommaExpr;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.lift.LiftFactory;

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


public final class InitExpr extends BinaryExpr implements ZeroResults {

	public Arity arity() { return this.defaultArity(); }

	private InitExpr( final Expr _init_expr, final Expr _src_expr ) {
		super( _init_expr, _src_expr );
	}

	public Expr getInitExpr() {
		return this.getFirst();
	}

	public Expr getSourceExpr() {
		return this.getSecond();
	}

	public static InitExpr make( final FacetSet _facets, final Props _props, final Expr _init_expr, final Expr _src_expr ) {
		final LiftFactory f = new LiftFactory( _facets, _props, true, null );
		Expr ie = (Expr)_init_expr.visit( f );
		ie = CommaExpr.make( f.getLifted(), ie );
		return new InitExpr( ie, _src_expr );
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitInitExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return new InitExpr( kids.next(), kids.next() );
	}
}
