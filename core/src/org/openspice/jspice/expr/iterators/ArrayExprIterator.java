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

public class ArrayExprIterator extends ExprIterator {
	private int count = 0;
	final Expr[] exprs;

	ArrayExprIterator( final boolean copy_flag, final Expr[] _exprs ) {
		this.exprs = copy_flag ? (Expr[])_exprs.clone() : _exprs;
	}

	ArrayExprIterator( final Expr[] _exprs ) {
		this( true, _exprs );
	}

	public boolean hasNext() {
		return this.count < this.exprs.length;
	}

	public Expr next() {
		return this.exprs[ this.count++ ];
	}
}
