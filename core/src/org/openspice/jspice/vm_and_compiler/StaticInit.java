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

final class StaticInit extends ExprVisitor.DefaultUnreachable implements Cloneable {
	final Petrifier petrifier;
	int count;
	Pebble do_first_pebble;
	
	//	Use this when the number of inputs is known at compile-time.
	public StaticInit apply( final Expr init_expr ) {
		return (StaticInit)init_expr.visit( this );
	}
	
	StaticInit( final Petrifier _petrifier, final int _count, final Pebble _do_first_pebble ) {
		this.petrifier = _petrifier;
		this.count = _count;
		this.do_first_pebble = _do_first_pebble;
	}
		
	StaticInit make( final int _count, final Pebble _do_first_pebble ) {
		return new StaticInit( this.petrifier, _count, _do_first_pebble );
	}
	
	StaticInit make( final int _count ) {
		return new StaticInit( this.petrifier, _count, this.do_first_pebble );
	}
	
	Pebble result() {
		if ( this.count == 0 ) return this.do_first_pebble;

		new Alert( "Initialization does not match source expression" ).
		hint( this.count > 0 ? "Too many input values" : "Too few input values" ).
		culprit( "Excess", new Integer( this.count ) ).
		mishap( 'G' );

		return null;	//	sop
	}
	
	public Object visitHelloExpr( final HelloExpr hello_expr, final Object _arg ) {
		if ( this.count >= 1 ) {
			return(
				this.make( 
					this.count - 1,
					PetrifyInitialize.petrify( hello_expr.getNameExpr(), this.do_first_pebble )
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
	
	public Object visitAnonExpr(  final AnonExpr anon_expr, final Object _arg ) {
		if ( this.count >= 1 ) {
			return(
				this.make( 
					this.count - 1,
					PetrifyInitialize.petrify( anon_expr, this.do_first_pebble )
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
	
	public Object visitSkipExpr( final SkipExpr _, final Object _arg ) {
		return this;
	}
	
	public Object visitCommaExpr( final CommaExpr expr, final Object _arg  ) {
		final Expr a = expr.getLeft();
		final Expr b = expr.getRight();
		final Arity b_arity = StaticInitCalc.exact( expr.getRight() );
		if ( b_arity != null ) {
			return this.apply( b ).apply( a );
		} else {
			final Arity a_arity = StaticInitCalc.reserve( a );
			if ( a_arity != null ) {
				//	Not sure about this.  My hand-written notes suggest this should be
				//	guarded by a_arity.isFixed() as well.  But that seems too aggressive.
				final int a_reserve = a_arity.getCount();
				final StaticInit st = this.make( this.count - a_reserve ).apply( b );
				return st.make( st.count + a_reserve ).apply( a );
			} else {
				return Alert.unreachable();
			}
		}
	}
	
	private Proc findInverse( final ApplyExpr expr ) {
		try {
			return ((Proc)expr.getFun().maybeConstantExpr()).inverse();
		} catch ( final Exception exn ) {
			return null;
		}
	}
	
	public Object visitApplyExpr( final ApplyExpr expr, final Object _arg  ) {
		final Proc inv = this.findInverse( expr );
		if ( inv != null && inv.outArity().isFixed() ) {
			//	It should eat as many as it likes up to a maximum of this.n (and
			//	a minimum of count).
			final Arity inv_in_arity = inv.inArity();
			final int nargs = inv_in_arity.isVariadic() ? this.count : inv_in_arity.getCount();
			final StaticInit st = (
				this.make(
					inv.outArity().getCount(),
					new Pebble() {
						Object run( Object tos, final VM vm ) {
							tos = do_first_pebble.run( tos, vm );
							tos = inv.call( tos, vm, nargs );
							return tos;
						}
					}
				).apply( expr.getArg() )
			);
			return this.make( this.count - nargs, st.result() );
		} else {
			return Alert.unreachable();
		}
	}
	
	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object _arg  ) {
		if ( this.count >= 1 ) {
			return this.make( this.count - 1, this.make( 1 ).apply( expr.getFirst() ).result() );
		} else {
			new Alert(
				"Insufficient inputs to initialization",
				"A simple count shows an imbalance"
			).mishap( 'G' );
			return null;
		}
	}
}