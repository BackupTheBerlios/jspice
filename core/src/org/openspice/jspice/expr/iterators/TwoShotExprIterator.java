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

public class TwoShotExprIterator extends ExprIterator {
	private int count = 0;
	private Expr x;
	private Expr y;

	TwoShotExprIterator( final Expr _x, final Expr _y ) {
		this.x = _x;
		this.y = _y;
	}

	//	We null out the fields that have been used to permit early garbage collection.
	public Expr next() {
		this.count += 1;
		if ( count == 1 ) {
			final Expr ans = this.x;
			this.x = null;
			return ans;
		} else {
			final Expr ans = this.y;
			this.y = null;
			return ans;
		}
	}

	public boolean hasNext() {
		return this.count < 2;
	}
}
