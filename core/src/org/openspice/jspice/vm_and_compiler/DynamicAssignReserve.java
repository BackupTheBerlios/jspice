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
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.alert.Alert;

//
//	This could all be done in integer arithmetic with a little
//	bit more work.  We aren't interested in the variadicity so
//	we could be a lot more efficient.
//
public final class DynamicAssignReserve extends ExprVisitor.DefaultUnreachable {

	static Pebble dy_reserve( final Petrifier p, final Expr e ) {
		return new DynamicAssignReserve().apply( e, p );
	}
	
	Pebble apply( final Expr e, final Petrifier p ) {
		return (Pebble)e.visit( this, p );
	}
	
	public Object visitHelloExpr( final HelloExpr hello_expr, final Object pet ) {
		return this.visitNameExpr( hello_expr.getNameExpr(), pet );
	}
	
	public Object visitHelloExpr( final NameExpr name_expr, final Object pet ) {
		return ArityPebble.ONE;
	}
	
	public Object visitSkipExpr( final SkipExpr _, final Object pet ) {
		return ArityPebble.ZERO;
	}
	
	public Object visitCommaExpr( final CommaExpr comma_expr, final Object pet ) {
		final Pebble a_pebble = this.apply( comma_expr.getLeft(), (Petrifier)pet );
		final Pebble b_pebble = this.apply( comma_expr.getRight(), (Petrifier)pet );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = a_pebble.run( tos, vm );
					tos = b_pebble.run( tos, vm );
					final Arity a = (Arity)vm.pop();
					return a.add( (Arity)tos );
				}
			}
		);
	}

	public Object visitApplyExpr( final ApplyExpr apply_expr, final Object pet ) {
		final Expr fun_expr = apply_expr.getFun();
		final Pebble fun_pebble = ((Petrifier)pet).petrify( fun_expr );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = fun_pebble.run( tos, vm );
					if ( ! ( tos instanceof Proc ) ) {
						new Alert(
							"Trying to get updater of non-procedure"
						).hint( "e.g. f(x) := E, but f not a procedure" ).culprit( "value", tos ).mishap( 'E' );
					}
					return ((Proc)tos).valsUArity();
				}
			}
		);
	}

	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object pet ) {
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					vm.push( tos );
					return Arity.ONE;
				}
			}
		);
	}
}

