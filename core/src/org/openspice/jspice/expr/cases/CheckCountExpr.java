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

import org.openspice.jspice.expr.markers.OneResult;
import org.openspice.jspice.expr.cases.UnaryExpr;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.alert.Alert;

public final class CheckCountExpr extends UnaryExpr implements OneResult {
	final int count;

	private CheckCountExpr( final int _count, final Expr _val ) {
		super( _val );
		this.count = _count;
	}

	public int getCount() {
		return this.count;
	}

	public static Expr make( final int k, final Expr e ) {
		final Arity n = e.arity();
		if ( n.hasFixedCount( k ) ) {
			return e;
		} else if ( n.isVariadic() ) {
			return new CheckCountExpr( k, e );
		} else {
			throw(
				new Alert(
					"Arity mismatch (" + n + " results)",
					"This expression must return " + k + " result" + ( k == 1 ? "" : "s" )
				).culprit( "expression", e ).mishap( 'C' )
			);
		}
	}

	public Arity arity() {
		return this.defaultArity();
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitCheckCountExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( this.count, kids.next() );
	}
}
