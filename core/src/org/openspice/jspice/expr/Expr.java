package org.openspice.jspice.expr;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.expr.iterators.ExprIterator;
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

public abstract class Expr implements ExprIntf {

	public abstract Arity arity();

	public abstract boolean isBooleanExpr();

	public abstract ExprIterator getAllKids();

	public abstract Expr copy( ExprIterator kids );

	public abstract Object visit( ExprVisitor visitor, Object arg );

	public abstract Object visit( ExprVisitor v );

	protected abstract Arity defaultArity();

	public abstract Expr bottomUp( ExprTransform t );

	public abstract Iterator getAllData();

	public abstract void showTree( int nspaces );

	protected abstract Object value();

	public abstract Expr simplify();

	protected abstract Expr simplify1();

	public abstract void showTree();

	public abstract Object maybeConstantExpr();

	public abstract boolean isConstantExpr();

}
