package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.expr.iterators.ExprIterator;

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


abstract class TrinaryExpr extends ExprBase {
	protected Expr first;
	protected Expr second;
	protected Expr third;

	TrinaryExpr( final Expr _first, final Expr _second, final Expr _third ) {
		this.first = _first;
		this.second = _second;
		this.third = _third;
	}

	public Expr getFirst() {
		return this.first;
	}

	public Expr getSecond() {
		return this.second;
	}

	public Expr getThird() {
		return this.third;
	}

	public ExprIterator getAllKids() {
		return ExprIterator.make3( this.first, this.second, this.third );
	}

}
