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
import org.openspice.jspice.lib.CastLib;

public final class DynamicAssign extends ExprVisitor.DefaultUnreachable {
	//
	//	Generates the code for the initialization expression e 
	//	provided that the vm_and_compiler.n_args register is correctly set (and saved).
	//
	public static Pebble dynamic_assign( final Petrifier p, final Expr e ) {
		return new DynamicAssign().apply( e, p );
	}
	
	Pebble apply( final Expr e, final Petrifier p ) {
		return (Pebble)e.visit( this, p );
	}

	public Object visitHelloExpr( final HelloExpr hello_expr, final Object pet ) {
		return this.visitNameExpr( hello_expr.getNameExpr(), pet );
	}
	
	public Object visitNameExpr( final NameExpr name_expr, final Object pet ) {
		final Pebble p = PetrifyInitialize.petrify( name_expr, Pebble.NO_OP );
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					if ( vm.v_args <= 0 ) {
						new Alert(
							"Underflow during assignment",
							"Too few inputs were available to assign to variable"
						).
						culprit( "variable", name_expr ).
						mishap( 'E' );
					}
					vm.n_args -= 1;
					return p.run( tos, vm );
				}
			}
		);
	}
	
	public Object visitSkipExpr( final SkipExpr _, final Object pet ) {
		return Pebble.NO_OP;
	}
	
	public Object visitCommaExpr( final CommaExpr comma_expr, final Object pet ) {
		final Expr a = comma_expr.getLeft();
		final Expr b = comma_expr.getRight();
		final Pebble a_dy_reserve_pebble = DynamicAssignReserve.dy_reserve( (Petrifier)pet, a );
		final Pebble a_pebble = this.apply( a, (Petrifier)pet );
		final Pebble b_pebble = this.apply( b, (Petrifier)pet );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = a_dy_reserve_pebble.run( tos, vm );
					final int a_reserve = ((Arity)tos).getCount();
					tos = vm.pop();
					if ( a_reserve > vm.v_args ) {
						new Alert(
							"Underflow during assignment",
							"Too few inputs to initialize both expressions"
						).culprit( "expr1", a ).culprit( "expr2", b ).mishap( 'E' );
					}
					//System.out.println( "Prepare to run B: nargs = " + vm_and_compiler.n_args + "; reserve = " + a_reserve );
					//vm_and_compiler.showAll();
					vm.v_args -= a_reserve;
					tos = b_pebble.run( tos, vm );
					//System.out.println( "Prepare to run A: nargs = " + vm_and_compiler.n_args + "; reserve = " + a_reserve );
					//vm_and_compiler.showAll();
					vm.v_args += a_reserve;
					tos = a_pebble.run( tos, vm );
					//System.out.println( "done" );
					//vm_and_compiler.showAll();
					return tos;
				}
			}
		);
	}

	public Object visitApplyExpr( final ApplyExpr apply_expr, final Object pet ) {
		final Pebble fun_pebble = ((Petrifier)pet).petrify( apply_expr.getFun() );
		final Pebble keys_pebble = ((Petrifier)pet).petrify( apply_expr.getArg() );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = fun_pebble.run( tos, vm );
					if ( ! ( tos instanceof Proc ) ) {
						new Alert(
							"Trying to get updater of non-procedure"
						).hint( "e.g. f(x) := E, f not a procedure" ).culprit( "value", tos ).mishap( 'E' );
					}
					//
					//	We now need to compute the number of args to permit this
					//  to access.
					//
					final Proc proc = CastLib.toProc( tos );
					final Arity vals_arity = proc.valsUArity();
//					System.err.println( "vals_arity = " + vals_arity + "; vm.n_args = " + vm.n_args + "; vm.v_args = " + vm.v_args);
					if ( vals_arity.getCount() > vm.v_args ) {
						new Alert( "Underflow detected during initialization" ).mishap( 'E' );
					}
					final int wants = vals_arity.isFixed() ? vals_arity.getCount() : vm.v_args;
					final int reserved = vm.v_args - wants;

					
					tos = vm.pop();
					final int before = vm.length();
					tos = keys_pebble.run( tos, vm );
					final int kargs = vm.length() - before;
					tos = proc.ucall( tos, vm, wants, kargs );
					vm.v_args = reserved;
					
					return tos;
					
				}
			}
		);
	}

	/*public Object visitCheckOneExpr( final Expr.CheckOneExpr expr, final Object pet ) {
		return this.apply( expr.getFirst(), (Petrifier)pet );
	}*/
}
