package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.OneResult;
import org.openspice.jspice.expr.cases.AnonExpr;
import org.openspice.jspice.expr.cases.NamedExpr;
import org.openspice.jspice.expr.TmpName;
import org.openspice.jspice.namespace.NameExprVisitor;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.datatypes.Arity;

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


public abstract class NameExpr extends NullaryExpr implements OneResult {
	public abstract NameExpr modNickname( final String intn );
	public abstract boolean isConstant();
	public abstract String getTitle();
	public abstract Object visit( final NameExprVisitor nev, final Object arg );
	public abstract boolean isNamedExpr();

	public boolean isType3() {
		return false;
	}

	public Arity arity() {
		return this.defaultArity();
	}

	public static NameExpr make( final String _sym ) {
		if ( _sym.equals( "_" ) ) {
			return new AnonExpr();
		} else {
			return new NamedExpr( _sym );
		}
	}

	public static NameExpr make( final Integer _sym ) {
		return new NamedExpr( _sym );
	}

	public static NameExpr make( final TmpName _sym ) {
		return new NamedExpr( _sym );
	}

	public static NameExpr make( final Var.Local local ) {
		final NamedExpr nmde = new NamedExpr( local.getTitle() );
		nmde.setVar( local );
		return nmde;
	}
}
