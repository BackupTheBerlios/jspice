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

public final class CheckBooleanExpr extends UnaryExpr implements OneResult {

	private CheckBooleanExpr( final Expr _val ) {
		super( _val );
	}

	public static Expr make( final Expr e ) {
		final Arity ne = e.arity();
		if ( e.isBooleanExpr() ) {
			return e;
		} else if ( ne.hasFixedCount( 1 ) || ne.isVariadic() ) {
			return new CheckBooleanExpr( e );
		} else {
			throw(
				new Alert(
					"Mismatch (" + ne.toNiceString() + " results)",
					"This expression must return a single Boolean result"
				).culprit( "expression", e ).mishap( 'E' )
			);
		}
	}

	public Arity arity() {
		return this.defaultArity();
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitCheckBooleanExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( kids.next() );
	}

	public boolean isBooleanExpr() {
		return true;
	}

}
