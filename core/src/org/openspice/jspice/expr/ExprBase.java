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

package org.openspice.jspice.expr;

import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.expr.markers.*;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.ConstantExpr;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.tools.IteratorTools;

import java.util.*;

//	Marker interfaces.


//	Marker / near-Marker interfaces.


public abstract class ExprBase extends Expr {

	public final Object visit( final ExprVisitor v ) {
		return this.visit( v, null );
	}

	/**
	 * The idea behind defaultArity is to exploit marker interfaces
	 * to specify the Arity.
	 * @return Arity specified by marker interface (e.g. ZeroResults -> 0)
	 */
	protected final Arity defaultArity() {
		if ( this instanceof BadResults ) {
			Alert.unreachable( this + "" );
		}
		return(
			this instanceof ZeroResults ? Arity.ZERO :
			this instanceof OneResult ? Arity.ONE :
			Arity.ZERO_OR_MORE
		);
	}

	public Expr bottomUp( final ExprTransform t ) {
		final ExprIterator kids = this.getAllKids();
		final Expr e = this.copy( kids.map( t ) );
		return t.transform( e );
	}

	public Iterator getAllData() {
		return IteratorTools.make0();
	}

	public void showTree( final int nspaces ) {
		PrintTools.spaces( nspaces );
		PrintTools.println( this.getClass().getName() );

		final int next_level = nspaces + 2;

		for ( final Iterator it = this.getAllData(); it.hasNext(); ) {
			PrintTools.spaces( next_level );
			PrintTools.println( it.next() );
		}
		for ( final ExprIterator it = this.getAllKids(); it.hasNext(); ) {
			it.next().showTree( next_level );
		}
	}

	protected Object value() {
		return ((ConstantExpr)this).getValue();
	}

	public Expr simplify() {
		try {
			return this.simplify1();
		} catch ( Exception e ) {
			return this;
		}
	}

	protected Expr simplify1() {
		return this;
	}

	public void showTree() {
		showTree( 0 );
	}

	public Object maybeConstantExpr() {
		return null;
	}

	public boolean isConstantExpr() {
		return false;
	}

	public boolean isBooleanExpr() {
		return false;
	}


}
