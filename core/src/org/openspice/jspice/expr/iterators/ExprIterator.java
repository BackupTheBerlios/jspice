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

package org.openspice.jspice.expr.iterators;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprTransform;

import java.util.*;

public abstract class ExprIterator implements ExprIteratorIntf {

	abstract public boolean hasNext();
	abstract public Expr next();

	public ExprIterator map( final ExprTransform t ) {
		final ExprIterator that = this;
		return(
			new ExprIterator() {
				public boolean hasNext() { return that.hasNext(); }
				public Expr next() { return t.transform( that.next() ); }
			}
		);
	}

	public static ExprIterator theZeroShotIterator;
	
	public static synchronized ExprIterator make0() {
		if ( theZeroShotIterator == null ) {
			theZeroShotIterator = new ZeroShotExprIterator();
		}
		return theZeroShotIterator;
	}

	public static ExprIterator make1( final Expr x ) {
		return new OneShotExprIterator( x );
	}
	
	public static ExprIterator make2( final Expr x, final Expr y ) {
		return new TwoShotExprIterator( x, y );
	}

	public static ExprIterator make3( final Expr x, final Expr y, final Expr z ) {
		return new ArrayExprIterator( false, new Expr[] { x, y, z } );
	}
	
	public static ExprIterator makeFromIterator( final Iterator it ) {
		return new IteratorExprIterator( it );
	}
}

