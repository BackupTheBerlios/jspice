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

package org.openspice.jspice.lift;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.Props;

import java.util.*;

public final class LiftFactory extends ExprVisitor {
	Expr lifted = SkipExpr.SKIP_EXPR;
	final FacetSet facet_set;
	final Props props;
	final boolean lifting_allowed;
	final List hello_list;
	final TmpVarGenerator g = new TmpVarGenerator();
	
	Expr apply( final Expr e ) {
		//final Object obj = e.visit( this );
		//System.out.println( "got back: " + obj );
		//return (Expr)obj;
		return (Expr)e.visit( this );
	}
	
	public LiftFactory( final FacetSet _facet_set, final Props _props, final boolean _lifting_allowed, final List _hello_list ) {
		this.hello_list = _hello_list;
		this.lifting_allowed = _lifting_allowed;
		this.facet_set = _facet_set;
		this.props = _props;
	}
	
	Expr useFactory( final Expr e ) {
		final Expr r = (Expr)e.visit( this );
		return CommaExpr.make( this.lifted, r );
	}
	
	public Expr getLifted() {
		return this.lifted;
	}
	
	public Object visitExpr( final Expr e, final Object arg ) {
		new Alert(
			"Unsuitable target for initialization"
		).culprit( "target", e ).mishap( 'G' );
		return null;
	}
	
	public Object visitNameExpr( final NameExpr e, final Object _arg  ) {
		if ( e instanceof NamedExpr ) {
			final HelloExpr hello_expr = HelloExpr.make( this.facet_set, this.props, (NamedExpr)e );
			if ( this.hello_list != null ) {
				this.hello_list.add( hello_expr );
			}
			return hello_expr;
		}
		return e;
	}
	
	public Object visitSkipExpr( final SkipExpr e, final Object _arg  ) {
		return e;
	}
	
	public Object visitCommaExpr( final CommaExpr e, final Object _arg  ) {
		return(
			CommaExpr.make(
				this.apply( e.getLeft() ),
				this.apply( e.getRight() )
			)
		);
	}
	
	public Object visitApplyExpr( final ApplyExpr expr, final Object _arg  ) {
		final Expr e1 = expr.getFirst();
		final Expr e2 = this.apply( expr.getSecond() );
		if ( e1 instanceof ConstantExpr || e1 instanceof NameExpr ) {
			return( ApplyExpr.make( e1, e2 ) );
		} else if ( this.lifting_allowed ) {
			final NameExpr tmp = g.newTmpVar();
			this.lifted = CommaExpr.make( this.lifted, InitExpr.make( null, Props.VAL, tmp, e1 ) );
			return ApplyExpr.make( tmp, e2 );
		} else {
			return new Alert( "Invalid formal parameters" ).mishap( 'G' );
		}
	}
	
	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object _arg  ) {
		return CheckOneExpr.make( this.apply( expr.getFirst() ) );
	}
}

