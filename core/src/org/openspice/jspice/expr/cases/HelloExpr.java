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

package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.OneResult;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.datatypes.Arity;

public final class HelloExpr extends ExprBase implements OneResult {
	FacetSet facets;		//	Assigned to during name resolution.
	final Props props;
	final NameExpr name_expr;

	public Arity arity() {
		return this.defaultArity();
	}

	private HelloExpr( final FacetSet _facets, final Props _props, final NameExpr _name_expr ) {
		this.facets = _facets;
		this.props = _props;
		this.name_expr = _name_expr;
	}

	public FacetSet getFacets() {
		return this.facets;
	}

	public void setFacets( final FacetSet _facets ) {
		this.facets = _facets;
	}

	public Props getProps() {
		return this.props;
	}

	public NameExpr getNameExpr() {
		return this.name_expr;
	}

	public static HelloExpr make( final FacetSet _facets, final Props _props, final NameExpr _name_expr ) {
		return new HelloExpr( _facets, _props, _name_expr );
	}

	public static HelloExpr make( final NameExpr _name_expr ) {
		return new HelloExpr( null, Props.VAR, _name_expr );
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitHelloExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return make( this.facets, this.props, (NameExpr)kids.next() );
	}

	public ExprIterator getAllKids() {
		return ExprIterator.make1( this.name_expr );
	}
}
