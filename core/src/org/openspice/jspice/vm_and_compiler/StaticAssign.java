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
import org.openspice.jspice.main.FailException;

final class StaticAssign extends ExprVisitor.DefaultUnreachable implements Cloneable {
	final Petrifier petrifier;
	int count;
	Pebble do_first_pebble;
	
	//	Use this when the number of inputs is known at compile-time.
	public StaticAssign apply( final Expr ass_expr ) {
		return (StaticAssign)ass_expr.visit( this );
	}
	
	StaticAssign( final Petrifier _petrifier, final int _count, final Pebble _do_first_pebble ) {
		this.petrifier = _petrifier;
		this.count = _count;
		this.do_first_pebble = _do_first_pebble;
	}
		
	StaticAssign make( final int _count, final Pebble _do_first_pebble ) {
		return new StaticAssign( this.petrifier, _count, _do_first_pebble );
	}
	
	StaticAssign make( final int _count ) {
		return new StaticAssign( this.petrifier, _count, this.do_first_pebble );
	}
	
	Pebble result() {
		if ( this.count == 0 ) return this.do_first_pebble;
		throw (
			new Alert( "Assignment does not match source expression" ).
			hint( this.count > 0 ? "Too many input values" : "Too few input values" ).
			culprit( "Excess", new Integer( this.count ) ).
			mishap( 'G' )
		);
	}
	
	public Object visitNameExpr( final NameExpr name_expr, final Object _arg ) {
		if ( this.count >= 1 ) {
			return(
				this.make( 
					this.count - 1,
					PetrifyInitialize.petrify( name_expr, this.do_first_pebble )
				)
			);
		} else {
			new Alert(
				"Insufficient inputs to initialization",
				"A simple count shows an imbalance"
			).mishap( 'G' );
			return null;
		}
	}

	public Object visitInitExpr( final InitExpr expr, final Object _arg ) {
		return this.visit( expr.getInitExpr(), _arg );
	}

	public Object visitHelloExpr( final HelloExpr hello_expr, final Object _arg ) {
		return this.visitNameExpr( hello_expr.getNameExpr(), _arg );
	}
	
	public Object visitSkipExpr( final SkipExpr _, final Object _arg ) {
		return this;
	}
	
	public Object visitCommaExpr( final CommaExpr expr, final Object _arg  ) {
		final Expr a = expr.getLeft();
		final Expr b = expr.getRight();
		final Arity b_arity = StaticAssignCalc.exact( expr.getRight() );
		if ( b_arity != null ) {
			return this.apply( b ).apply( a );
		} else {
			final Arity a_arity = StaticAssignCalc.reserve( a );
			if ( a_arity != null ) {
				//
				//	Not sure about this.  My hand-written notes suggest this should be
				//	guarded by a_arity.isFixed() as well.  But that seems too aggressive.
				//
				final int a_reserve = a_arity.getCount();
				final StaticAssign st = this.make( this.count - a_reserve ).apply( b );
				return st.make( st.count + a_reserve ).apply( a );
			} else {		
				return Alert.unreachable();
			}
		}
	}
	
	private Proc findProc( final ApplyExpr expr ) {
		try {
			return (Proc)expr.getFun().maybeConstantExpr();
		} catch ( final Exception exn ) {
			return null;
		}
	}
	
	public Object visitApplyExpr( final ApplyExpr expr, final Object _arg  ) {
		final Proc proc = this.findProc( expr );
		if ( proc != null && proc.keysUArity().isFixed() ) {
			//
			//	It should eat as many as it likes up to a maximum of this.n (and
			//	a minimum of count).
			//
			final Arity vals_u_arity = proc.valsUArity();
			final int vargs = vals_u_arity.isVariadic() ? this.count : vals_u_arity.getCount();
			final Arity keys_u_arity = proc.keysUArity();
			final Pebble keys_pebble = this.petrifier.petrify( expr.getArg() );
			final int kargs = keys_u_arity.getCount();
			return(
				this.make(
					this.count - vargs,
					new Pebble() {
						Object run( Object tos, final VM vm ) {
							tos = do_first_pebble.run( tos, vm );
							tos = keys_pebble.run( tos, vm );
							tos = proc.ucall( tos, vm, vargs, kargs );
							return tos;
						}
					}
				)
			);
		} else {
			throw FailException.failException;
		}
	}
	
	/*Pebble dynamicPebble( final Expr expr ) {
		//
		//	Unsure about this.  Does dynamic_assign _promise_ not to underflow v_args?
		//
		final Pebble dynamic_pebble = DynamicAssign.dynamic_assign( this.petrifier, expr );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm_and_compiler ) {
					vm_and_compiler.intpush( vm_and_compiler.v_args );
					vm_and_compiler.v_args = count;
					tos = dynamic_pebble.run( tos, vm_and_compiler );
					if ( vm_and_compiler.v_args != 0 ) {
						Alert.mishap(
							"Wrong number of inputs for assignment",
							Alert.EVAL
						);
					}
					vm_and_compiler.v_args = vm_and_compiler.intpop();
					return tos;
				}
			}
		);
	}*/
	
	/*public Object visitCheckOneExpr( final Expr.CheckOneExpr expr, final Object _arg  ) {
		if ( this.count >= 1 ) {
			return this.make( this.count - 1, this.make( 1 ).apply( expr.getFirst() ).result() );
		} else {
			Alert.mishap( 
				"Insufficient inputs to assignments",
				"A simple count shows an imbalance",
				Alert.COMP
			);
			return null;
		}
	}*/
	
}