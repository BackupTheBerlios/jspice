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
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.tools.BooleanTools;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.lib.AbsentLib;
import org.openspice.tools.IntegerTools;
import org.openspice.tools.IteratorTools;


import java.util.Iterator;

public final class ConstantExpr extends NullaryExpr implements OneResult {
	private Object value;

	private  ConstantExpr( final Object _value ) {
		super();
		this.value = _value;
	}

	public static Expr make( final int n ) {
		return make( new Integer( n ) );
	}

	public static Expr make( final Object obj ) {
		return new ConstantExpr( obj );
	}

	public Iterator getAllData() {
		return IteratorTools.make1( this.value );
	}

	public Arity arity() {
		return this.defaultArity();
	}

	public Object getValue() {
		return this.value;
	}

	public Object maybeConstantExpr() {
		return this.value;
	}

	public boolean isConstantExpr() {
		return true;
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitConstantExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return this;
	}

	public boolean isBooleanExpr() {
		return this.value instanceof Boolean;
	}

	public final static ConstantExpr TRUE_EXPR = new ConstantExpr( BooleanTools.TRUE );
	public final static ConstantExpr FALSE_EXPR = new ConstantExpr( BooleanTools.FALSE );
	public final static ConstantExpr ZERO_EXPR = new ConstantExpr( IntegerTools.ZERO );
	public final static ConstantExpr ONE_EXPR = new ConstantExpr( IntegerTools.ONE );
	public final static ConstantExpr ABSENT_EXPR = new ConstantExpr( AbsentLib.ABSENT );

}
