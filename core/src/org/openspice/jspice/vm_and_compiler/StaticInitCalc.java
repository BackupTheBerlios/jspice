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
package org.openspice.jspice.vm_and_compiler;

import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;

public final class StaticInitCalc extends ExprVisitor.DefaultUnreachable {

	public static Arity calc( final Expr e ) {
		return new StaticInitCalc().apply( e );
	}

	//	e is known to consume exactly N values from the stack 
	//	during initialization.
	public static Arity exact( final Expr e ) {
		final Arity a = calc( e );
		if ( a == null || a.isVariadic() ) return null;
		return a;
	}

	//	e must be given at least N values to avoid underflow 
	//	during initialization.
	public static Arity reserve( final Expr e ) {
		final Arity a = calc( e );
		if ( a == null ) return null;
		if ( a.isFixed() ) return a;           // efficiency
		return new Arity( a, false );
	}
	
	public Arity apply( final Expr e ) {
		return (Arity)e.visit( this );
	}

	public Object visitHelloExpr( final HelloExpr hello_expr, final Object _arg ) {
		return Arity.ONE;
	}
	
	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object _arg ) {
		return Arity.ONE;
	}

	public Object visitCommaExpr( final CommaExpr comma_expr, final Object _arg ) {
		final Arity a = this.apply( comma_expr.getLeft() );
		if ( a == null ) return null;
		final Arity b = this.apply( comma_expr.getRight() );
		if ( b == null ) return null;
		return a.add( b );
	}
	
	public Object visitSkipExpr( final SkipExpr skip_expr, final Object _arg ) {
		return Arity.ZERO;
	}

	public Object visitApplyExpr( final ApplyExpr apply_expr, final Object _arg ) {
		final Object obj = apply_expr.getFun().maybeConstantExpr();
		if ( ! ( obj instanceof Proc ) ) return null;
		final Proc inv = ((Proc)obj).inverse();
		if ( inv == null ) {
			new Alert(
				"Function used in initializer lacks inverse",
				"Initializing an expression inverts functions"
			).culprit( "function", obj ).mishap( 'G' );
		}
		return inv.inArity();
	}
}


