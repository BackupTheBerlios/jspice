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
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.built_in.LocationContProc;
import org.openspice.jspice.built_in.PartApply;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.built_in.LocationContProc;

import java.util.*;

/*
	In the lift phase we have two jobs to do.  Firstly, we will
	remove all "outers" via the lift transformation.  Secondly,
	all lifted _var_s will be promoted to type-3. 
	
	The lift transformation involves replacing a LambdaExpr
	by a call to PartApply of a Lambda with some extra
	arguments.
	
	Promotion is handled explicitly.  Firstly the hellos
	are wrapped with the InvNewLocationProc, causing the
	value to be wrapped with a Location by the initialization
	unwind code.  Secondly, all references are augmented by
	calls to "Location.getValue()" (which is InvNewLocationProc).
	
	Lifting runs after NameResolution because it is only after
	name resolution that we know what the outers are and 
	which variables are type-3.
*/

public final class Lift extends ExprTransform {
	
	public static Expr lift( final Expr e ) {
		return new Lift().transform( e );
	}

	private Expr liftHelloExpr( final HelloExpr hello_expr ) {
		final NameExpr nme = hello_expr.getNameExpr();
		if ( nme.isType3() ) {
			return ApplyExpr.make( LocationContProc.LOCATION_CONT_PROC, hello_expr );
		} else {
			return hello_expr;
		}
	}
	
	private Expr liftNameExpr( final NameExpr name_expr ) {
		if ( name_expr.isType3() ) {
			return ApplyExpr.make( LocationContProc.LOCATION_CONT_PROC, name_expr );
		} else {
			return name_expr;
		}
	}
	
	private Expr liftLambdaExpr( final LambdaExprForLifting lambda_expr ) {
		final LambdaExpr new_lambda_expr = lambda_expr.lift( this );
		if ( lambda_expr.hasOuters() ) {
			Expr arg = SkipExpr.SKIP_EXPR;
			for ( Iterator it = lambda_expr.outsideIterator(); it.hasNext(); ) {
				final Var.Local local = (Var.Local)it.next();
				arg = CommaExpr.make( arg, NameExpr.make( local ) );
			}
			
			return( 
				ApplyExpr.make(
					PartApply.PART_APPLY,
					CommaExpr.make( new_lambda_expr, arg )
				)
			);
		} else {
			return new_lambda_expr;
		}
	}
	
	public Expr transform( final Expr expr ) {
		//System.out.println( "lifting: " + expr );
		if ( expr instanceof HelloExpr ) {
			return this.liftHelloExpr( (HelloExpr)expr );
		} else if ( expr instanceof NameExpr ) {
			//	Order matters!  This must come AFTER the processing of a hello.
			return this.liftNameExpr( (NameExpr)expr );
		} else if ( expr instanceof LambdaExprForLifting ) {
			return this.liftLambdaExpr( (LambdaExprForLifting)expr );
		} else {
			return this.map( expr );
		}
	}
}
