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

package org.openspice.jspice.namespace;

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.NamedExpr;
import org.openspice.jspice.expr.cases.AnonExpr;

public abstract class NameExprVisitor {

	abstract public Object visitNamedExpr( final NamedExpr e, final Object arg );
	abstract public Object visitAnonExpr( final AnonExpr e, final Object arg );

	static abstract class VoidVisitor extends NameExprVisitor {
		abstract public void voidVisitNamedExpr( final NamedExpr e, final Object arg );
		abstract public void voidVisitAnonExpr( final AnonExpr e, final Object arg );
		
		public Object visitAnonExpr( final AnonExpr e, final Object arg ) {
			voidVisitAnonExpr( e, arg );
			return null;
		}

		public Object visitNamedExpr( final NamedExpr e, final Object arg ) {
			voidVisitNamedExpr( e, arg );
			return null;
		}
	}

	static abstract class NamedOnlyVoidVisitor extends VoidVisitor {
		public void voidVisitAnonExpr( final AnonExpr e, final Object arg ) {}
	}

}