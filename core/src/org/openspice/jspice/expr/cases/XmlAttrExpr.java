package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.BadResults;
import org.openspice.jspice.expr.cases.BinaryExpr;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.iterators.ExprIterator;
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


public final class XmlAttrExpr extends BinaryExpr implements BadResults {
	public Arity arity() {
		return this.defaultArity();
	}

	private XmlAttrExpr( final Expr _name, final Expr _value ) {
		//super( Expr.CheckOneExpr.make( _name ), Expr.CheckOneExpr.make( _value ) );
		super( _name, _value );
	}

	public static Expr make( final Expr _name, final Expr _value ) {
		return new XmlAttrExpr( _name, _value );
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitXmlAttrExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( kids.next(), kids.next() );
	}
}
