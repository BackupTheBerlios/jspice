package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.OneResult;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.datatypes.Arity;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

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


public final class RelOpChainExpr extends ExprBase implements OneResult {
	final Expr start;
	final List symbols = new LinkedList();
	final List expressions = new LinkedList();

	public Arity arity() { return this.defaultArity(); }

	//	Exceptionally, the constructor for RelOpChainExpr is exposed because
	//	it is a 'natural' variadic constructor.
	public RelOpChainExpr( final Expr _start ) {
		//this.start = Expr.CheckOneExpr.make( _start );
		this.start = _start;
	}

	public Expr getStart() {
		return this.start;
	}

	public Iterator getSymbolIterator() {
		return this.symbols.iterator();
	}

	public Iterator getExpressionIterator() {
		return this.expressions.iterator();
	}

	public void add( final String sym, final Expr e ) {
		this.symbols.add( sym );
		//this.expressions.add( Expr.CheckOneExpr.make( e ) );
		this.expressions.add( e );
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitRelOpChainExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		final RelOpChainExpr dup = new RelOpChainExpr( kids.next() );
		final Iterator syms = this.symbols.iterator();
		while ( syms.hasNext() ) {
			dup.add( (String)syms.next(), kids.next() );
		}
		return dup;
	}

	public ExprIterator getAllKids() {
		final LinkedList kids = new LinkedList( this.expressions );
		kids.addFirst( this.start );
		return ExprIterator.makeFromIterator( kids.iterator() );
	}

	public boolean isBooleanExpr() {
		return true;
	}

}
