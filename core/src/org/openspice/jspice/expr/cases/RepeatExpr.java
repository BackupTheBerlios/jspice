package org.openspice.jspice.expr.cases;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.expr.iterators.IteratorExprIterator;
import java.util.ArrayList;

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


public final class RepeatExpr extends ExprBase {


	static final class Triple {
		final Expr first;
		final Expr second;
		final Expr third;
		final boolean fourth;

		Triple( final Expr _first, final Expr _second, final Expr _third, final boolean _fourth ) {
			this.first = _first;
			this.second = _second;
			this.third = _third;
			this.fourth = _fourth;
		}
	}

	private final Triple[] body;
	//final Expr.CompoundExpr body;

	private RepeatExpr( final Triple[] _body ) {
		this.body = _body;
	}

	/*public Expr.CompoundExpr getCompoundExpr() {
		return this.body;
	}*/

	/*public Expr[][] getBody() {
		return this.body;
	}*/

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitRepeatExpr( this, arg );
	}

	public int size() {
		return this.body.length;
	}

	public Expr getTest( final int i ) {
		return this.body[ i ].first;
	}

	public Expr getResult( final int i ) {
		return this.body[ i ].second;
	}

	public Expr getDo( final int i ) {
		return this.body[ i ].third;
	}

	public boolean getReturnOrBreak( final int i ) {
		return this.body[ i ].fourth;
	}

	public ExprIterator getAllKids() {
		final ArrayList list = new ArrayList();
		for ( int i = 0; i < this.body.length; i++ ) {
			final Triple t = this.body[ i ];
			list.add( t.first );
			list.add( t.second );
			list.add( t.third );
			list.add( ConstantExpr.make( Boolean.valueOf( t.fourth ) ) );	//	Yuck.
		}
		return ExprIterator.makeFromIterator( list.iterator() );
	}

	public Expr copy( final ExprIterator kids ) {
		final Factory f = new Factory();
		while ( kids.hasNext() ) {
			f.add( kids.next(), kids.next(), kids.next(), ((Boolean)(((ConstantExpr)kids.next()).getValue())).booleanValue() );
		}
		return f.make();
	}

	public Arity arity() { return this.defaultArity(); }

	public static final class Factory {
		final ArrayList list = new ArrayList();

		public void add( final Expr _test, final Expr _result, final Expr _body, final boolean ret_or_brk ) {
			this.list.add( new Triple( CheckBooleanExpr.make( _test ), _result, _body, ret_or_brk ) );
		}

		public RepeatExpr make() {
			final Triple[] data = new Triple[ list.size() ];
			System.arraycopy( list.toArray(), 0, data, 0, data.length );
			return new RepeatExpr( data );
		}
	}
}
