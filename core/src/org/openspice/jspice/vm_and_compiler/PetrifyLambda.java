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

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.LambdaExprForPetrification;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.main.Print;

class Closure extends Proc {
	final Proc proc;
	final Object[] args;
	
	Closure( final Proc _proc, final Object[] _args ) {
		this.proc = _proc;
		this.args = _args;
	}
	
	public Arity inArity() { return Arity.ZERO_OR_MORE; }
	
	public Arity outArity() { return Arity.ZERO_OR_MORE; }
	
	public Object call( Object tos, final VM vm, final int nargs ) {
		Print.println( Print.PARSE, "Closure running: nargs = " + nargs );
		vm.push( tos );
		for ( int i = 0; i < this.args.length; i++ ) {
			vm.push( this.args[ i ] );
		}
		Print.println( Print.PARSE, "Outers acquired" );
		return this.proc.call( vm.pop(), vm, nargs + this.args.length );
	}
}
	
public class PetrifyLambda {

	static abstract class CallManager {
		abstract void intro( VM vm, int nargs, int frame_size );
		abstract void outtro( VM vm, int nargs, int frame_size );
		
		static final class Static extends CallManager {
			final int count;
			
			Static( final int _count ) { this.count = _count; }
			
			void intro( final VM vm, final int nargs, int frame_size ) {
				//System.out.println( "static call manager intro: nargs = " + nargs + ", frame_size = " + frame_size + ", count = " + count );
				vm.callpush( frame_size );
				if ( this.count == nargs ) return;
				Arity.failCheck( this.count, nargs );
			}
			
			void outtro( final VM vm, final int nargs, int frame_size ) {
				vm.calldrop( frame_size );
			}
		}
		
		static final class Dynamic extends CallManager {
			void intro( final VM vm, final int nargs, int frame_size ) {
				//System.out.println( "dynamic call manager intro" );
				vm.callpush( frame_size );
				vm.intpush( vm.n_args );
				vm.n_args = nargs;
			}
			
			void outtro( final VM vm, final int nargs, int frame_size ) {
				vm.calldrop( frame_size );
				vm.n_args = vm.intpop();
			}
		}
		
	}
	
	static private Proc makeProc( final Petrifier petrifier, final LambdaExprForPetrification lambda_expr ) {
		final Expr init_expr = lambda_expr.getInitExpr();
		final Expr body = lambda_expr.getBody();
		
		Pebble ipeb;
		CallManager cman;
		
		Arity init_arity = StaticInitCalc.calc( init_expr );
		//final Arity init_arity = StaticInitCalc.exact( init_expr );
		if ( init_arity == null ) {
			new Alert(
				"Arity of procedure cannot be properly determined",
				"One of the arguments cannot be statically resolved"
			).
			hint( "The compiler knows too little to generate good code" ).culprit( "procedure", lambda_expr ).
			warning( 'G' );
			init_arity = Arity.ZERO_OR_MORE;
		}
		if ( init_arity.isFixed() ) {
			final StaticInit st_init = new StaticInit( petrifier, init_arity.getCount(), Pebble.NO_OP );
			ipeb = st_init.apply( init_expr ).result();
			cman = new CallManager.Static( init_arity.getCount() );
		} else {
			ipeb  = DynamicInit.dynamic_init( petrifier, init_expr );
			cman = new CallManager.Dynamic();
		}
		final Pebble init_pebble = ipeb;
		final CallManager call_manager = cman;	//CallManager.Type3.make( cman, lambda_expr );

		final Pebble body_pebble = petrifier.petrify( body );
		final int call_frame_size = lambda_expr.getSlotCount();
		final Arity ia = init_arity;
		final Arity oa = body.arity();
		return( 		
			new Proc() {
				public Arity inArity() { return ia; }
				public Arity outArity() { return oa; }				//	Wrong
				public Object call( Object tos, final VM vm, final int nargs ) {
					//System.out.println( "entering proc - run call manager: call_frame_size = " + call_frame_size );
					call_manager.intro( vm, nargs, call_frame_size );
					//System.out.println( "intro done - run initialization" );
					tos = init_pebble.run( tos, vm );
					//System.out.println( "initialization done - run body" );
					tos = body_pebble.run( tos, vm );
					//System.out.println( "body done - run outtro" );
					call_manager.outtro( vm, nargs, call_frame_size );
					//System.out.println( "done" );
					return tos;
				}
			}
		);
	}

	
	public static Pebble petrifyLambda( final Petrifier petrifier, final LambdaExprForPetrification e ) {
		final Proc proc = makeProc( petrifier, e );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					vm.push( tos );
					return proc;
				}
			}
		);
		/*if ( e.hasOuters() ) {
			final ArrayList offsets = new ArrayList();
			for ( Iterator it = e.outsideIterator(); it.hasNext(); ) {
				final Var.Local local = (Var.Local)it.next();
				assert local.isOuter();
				if ( !local.isOuter() ) {
					Alert.unreachable();
				}
				offsets.add( new Integer( local.getOffset() ) );
			}
			final int[] int_offsets = new int[ offsets.size() ];
			for ( int i = 0; i < int_offsets.length; i++ ) {
				int_offsets[ i ] = ((Integer)offsets.get( i )).intValue();
			}
			PrintTools.debugln( "Constructed offsets = " + offsets );
			return(
				new Pebble() {
					Object run( Object tos, final VM vm_and_compiler ) {
						vm_and_compiler.push( tos );
						final Object[] args = new Object[ int_offsets.length ];
						for ( int i = 0; i < int_offsets.length; i++ ) {
							args[ i ] = vm_and_compiler.load( int_offsets[ i ] );
						}
						PrintTools.debugln( "Constructed args = " + Arrays.asList( args ) );
						return new Closure( proc, args );
					}
				}
			);
		} else {
			return(
				new Pebble() {
					Object run( Object tos, final VM vm_and_compiler ) {
						vm_and_compiler.push( tos );
						return proc;
					}
				}
			);
		}*/
	}
}
