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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.MiniParser;
import org.openspice.jspice.parse.miniparser.Infix;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.ShortCircuitExpr;

public final class ShortCircuitMiniParser extends Infix {

	final boolean and_oriented;
	final boolean bool_oriented;

	public ShortCircuitMiniParser( final boolean _and_oriented, final boolean _bool_oriented ) {
		this.and_oriented = _and_oriented;
		this.bool_oriented = _bool_oriented;
	}

	public Expr infix( final Expr lhs, final Expr rhs ) {
		return(
			ShortCircuitExpr.make(
				this.and_oriented,
				this.bool_oriented,
				lhs,
				rhs
			)
		);
	}

}
