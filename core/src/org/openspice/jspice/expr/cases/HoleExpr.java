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
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;

public final class HoleExpr extends NullaryExpr {
	final int hole_num;

	private HoleExpr( final int _hole_num ) {
		this.hole_num = _hole_num;
	}

	public int getHoleNum() {
		return this.hole_num;
	}

	public static Expr make() {
		return make( -1 );
	}

	public static Expr make( final int _hole_num ) {
		return new HoleExpr( _hole_num );
	}

	public Arity arity() {
		//	Required by Constructor of CheckOneExpr.
		return Arity.ONE;
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitHoleExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return this;
	}
}
