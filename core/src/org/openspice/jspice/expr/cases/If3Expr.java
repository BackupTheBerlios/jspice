
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
import org.openspice.jspice.expr.cases.TrinaryExpr;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;

public final class If3Expr extends TrinaryExpr {
	
	private  If3Expr( final Expr _test, final Expr _ifso, final Expr _ifnot ) {
		super( CheckBooleanExpr.make(_test), _ifso, _ifnot );
	}

	public static Expr make( final Expr _test, final Expr _ifso, final Expr _ifnot ) {
		return new If3Expr( _test, _ifso, _ifnot );
	}

	public Arity arity() {
		return Arity.ZERO_OR_MORE;
	}

	public Object visit( final ExprVisitor visitor, final Object arg ) {
		return visitor.visitIf3Expr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( kids.next(), kids.next(), kids.next() );
	}

}
