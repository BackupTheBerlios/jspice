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
import org.openspice.jspice.expr.cases.BinaryExpr;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;

public final class CommaExpr extends BinaryExpr {
	public Arity arity() {
		return this.getFirst().arity().add( this.getSecond().arity() );
	}

	private CommaExpr( final Expr _lhs, final Expr _rhs ) {
		super( _lhs, _rhs );
	}

	protected Expr simplify1() {
		return(
			this.lhs instanceof SkipExpr ? this.rhs :
			this.rhs instanceof SkipExpr ? this.lhs :
			this
		);
	}

	public static Expr make( final Expr lhs, final Expr rhs ) {
		return new CommaExpr( lhs, rhs ).simplify();
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitCommaExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( kids.next(), kids.next() );
	}
}
