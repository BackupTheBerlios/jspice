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

import org.openspice.jspice.expr.markers.BadResults;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.datatypes.Arity;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public final class CompoundExpr extends ExprBase implements BadResults {
	private ArrayList list;

	private CompoundExpr( final ArrayList _list ) {
		this.list = _list;
	}

	public static Expr make( final List _list ) {
		final Factory f = new Factory();
		for ( Iterator it = _list.iterator(); it.hasNext(); ) {
			f.add( (Expr)(it.next()) );
		}
		return f.make();
	}

	public ExprIterator getAllKids() {
		return ExprIterator.makeFromIterator( this.list.iterator() );
	}

	public Arity arity() {
		return this.defaultArity();
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitCompoundExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		final Factory f = new Factory();
		while ( kids.hasNext() ) {
			f.add( kids.next() );
		}
		return f.make();
	}

	public int size() {
		return this.list.size();
	}

	static final class Factory {
		final ArrayList list = new ArrayList();

		void add( final Expr e ) {
			this.list.add( e );
		}

		CompoundExpr make() {
			this.list.trimToSize();
			return new CompoundExpr( list );
		}
	}
}
