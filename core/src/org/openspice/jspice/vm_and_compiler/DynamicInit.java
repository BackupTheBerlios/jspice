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
import org.openspice.jspice.alert.Alert;

public final class DynamicInit extends ExprVisitor.DefaultUnreachable {
	final Petrifier petrifier;
	
	DynamicInit( final Petrifier _petrifier ) {
		this.petrifier = _petrifier;
	}

	/**
	 * Generates the code for the initialization expression e 
	 *	provided that the vm_and_compiler.n_args register is correctly set (and saved).
	 */
	static Pebble dynamic_init( final Petrifier p, final Expr e ) {
		return new DynamicInit( p ).apply( e );
	}
	
	Pebble apply( final Expr e ) {
		return (Pebble)e.visit( this );
	}

	public Object visitHelloExpr( final HelloExpr hello_expr, final Object _arg ) {
		final Pebble p = PetrifyInitialize.petrify( hello_expr.getNameExpr(), Pebble.NO_OP );
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					if ( vm.n_args <= 0 ) {
						new Alert(
							"Underflow during initialization",
							"Too few inputs were available to assign to variable"
						).culprit( "variable", hello_expr.getNameExpr() ).mishap( 'E' );
					}
					vm.n_args -= 1;
					return p.run( tos, vm );
				}
			}
		);
	}
	
	public Object visitAnonExpr( final AnonExpr anon_expr, final Object _arg ) {
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					if ( vm.n_args <= 0 ) {
						new Alert(
							"Underflow during initialization",
							"Too few inputs were available to assign to anonymous variable"
						).mishap( 'E' );
					}
					vm.n_args -= 1;
					return vm.pop();
				}
			}
		);
	}
	
	public Object visitSkipExpr( final SkipExpr _, final Object _arg ) {
		return Pebble.NO_OP;
	}
	
	public Object visitCommaExpr( final CommaExpr comma_expr, final Object _arg ) {
		final Expr a = comma_expr.getLeft();
		final Expr b = comma_expr.getRight();
		final Pebble a_dy_reserve_pebble = DynamicInitReserve.dy_reserve( this.petrifier, a );
		final Pebble a_pebble = this.apply( a );
		final Pebble b_pebble = this.apply( b );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = a_dy_reserve_pebble.run( tos, vm );
					final int a_reserve = ((Arity)tos).getCount();
					tos = vm.pop();
					if ( a_reserve > vm.n_args ) {
						new Alert(
							"Underflow during initialization",
							"Too few inputs to initialize both expressions"
						).culprit( "expr1", a ).culprit( "expr2", b ).mishap( 'E' );
					}
					//System.out.println( "Prepare to run B: nargs = " + vm_and_compiler.n_args + "; reserve = " + a_reserve );
					//vm_and_compiler.showAll();
					vm.n_args -= a_reserve;
					tos = b_pebble.run( tos, vm );
					//System.out.println( "Prepare to run A: nargs = " + vm_and_compiler.n_args + "; reserve = " + a_reserve );
					//vm_and_compiler.showAll();
					vm.n_args += a_reserve;
					tos = a_pebble.run( tos, vm );
					//System.out.println( "done" );
					//vm_and_compiler.showAll();
					return tos;
				}
			}
		);
	}

	public Object visitApplyExpr( final ApplyExpr apply_expr, final Object _arg ) {
		final Pebble fun_pebble = this.petrifier.petrify( apply_expr.getFun() );
		final Pebble arg_pebble = this.apply( apply_expr.getArg() );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = fun_pebble.run( tos, vm );
					if ( ! ( tos instanceof Proc ) ) {
						new Alert(
							"Trying to invert non-procedure"
						).hint( "e.g. val f(x) = E, but f not a procedure" ).culprit( "value", tos ).mishap( 'E' );
					}
					final Proc inv = ((Proc)tos).inverse();
					if ( inv == null ) {
						new Alert(
							"Procedure lacks inverse"
						).hint( "e.g. val f(x) = E, f cannot be run backward" ).culprit( "value", tos ).mishap( 'E' );
					}
					//
					//	We now need to compute the number of args to permit this
					//  to access.
					//
					final Arity inv_in_arity = inv.inArity();
					if ( inv_in_arity.getCount() > vm.n_args ) {
						new Alert( "Underflow detected during initialization" ).mishap( 'E' );
					}
					final int wants = inv_in_arity.isFixed() ? inv_in_arity.getCount() : vm.n_args;
					final int reserved = vm.n_args - wants;
					
					//System.out.println( "Before running " + inv );
					//System.out.println( "vm_and_compiler.n_args = " + vm_and_compiler.n_args );
					//System.out.println( "wants = " + wants );
					//System.out.println( "vm_and_compiler.length() = " + vm_and_compiler.length() );
					
					tos = vm.pop();
					final int before = vm.length();
					tos = inv.call( tos, vm, wants );
					
					//	Now we have to run the arg_pebble with however many results the call
					//	to the inverse function dynamically generated.
					//System.out.println( "After running " + inv );
					//System.out.println( "vm_and_compiler.length() = " + vm_and_compiler.length() );
					//System.out.println( "num results = " + ( vm_and_compiler.length() - before + wants ) );
					vm.n_args = vm.length() - before + wants;
					tos = arg_pebble.run( tos, vm );
					
					//	At this point vm_and_compiler.n_args should indicate that all the outputs have
					//	been consumed.
					if ( vm.n_args != 0 ) {
						new Alert( "Unused inputs during initialization" ).mishap( 'E' );
					}
					
					vm.n_args = reserved;
					return tos;
					
				}
			}
		);
	}

	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object _arg ) {
		return this.apply( expr.getFirst() );
	}
}
