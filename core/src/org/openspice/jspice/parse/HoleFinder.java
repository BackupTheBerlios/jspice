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


package org.openspice.jspice.parse;

import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.alert.Alert;

import java.util.*;

class HoleTracker extends ExprVisitor.DefaultReturn {
	private List arglist = new LinkedList();
	
	private int next_default_hole_num = 0;	//	used for incrementing anon holes.	
	private boolean used_anon = false;	//	set true if anonymous hole "?" used.
	private boolean used_numd = false;	//	set true if numbered hole "?N" used.
	
	Expr getArglist() {
		Expr e = SkipExpr.SKIP_EXPR;
		for ( Iterator it = this.arglist.iterator(); it.hasNext(); ) {
			e = CommaExpr.make( e, (NameExpr)it.next() );
		}
		return e;
	}
		
	void check() {
		if ( this.used_anon && this.used_numd ) {
			new Alert(
				"Numbered and anonymous holes mixed",
				"All the holes should be of the same type"
			).mishap( 'G' );
		}
	}
	
	boolean hasHoles() {
		return this.used_anon || this.used_numd;
	}

	public Object visitCheckOneExpr( final CheckOneExpr e, final Object _arg ) {
		final Expr e1 = (Expr)e.getFirst().visit( this );
		return CheckOneExpr.make( e1 );
	}

	public Object visitCommaExpr( final CommaExpr e, final Object _arg ) {
		final Expr e1 = (Expr)e.getLeft().visit( this );
		final Expr e2 = (Expr)e.getRight().visit( this );
		return CommaExpr.make( e1, e2 );
	}
	
	public Object visitHoleExpr( final HoleExpr e, final Object _arg ) {
		int hole_num = e.getHoleNum();
		if ( hole_num < 0 ) {					//	defensive, could be == -1.
			hole_num = this.next_default_hole_num++;
			this.used_anon = true;
		} else {
			this.next_default_hole_num = hole_num + 1;
			this.used_numd = true;
		}
		final NameExpr named = NameExpr.make( new Integer( hole_num ) );
		this.arglist.add( named );
		return named;
	}
}

public class HoleFinder extends ExprVisitor {

	public Object visitExpr( final Expr e, final Object _arg ) {
		return e;
	}

	public Object visitApplyExpr( final ApplyExpr e, final Object _arg ) {
		final HoleTracker tracker = new HoleTracker();
		final Expr f = (Expr)e.getFun().visit( tracker, null );
		final Expr a = (Expr)e.getArg().visit( tracker, null );

		if ( tracker.hasHoles() ) {
			return LambdaExpr.make( tracker.getArglist(), ApplyExpr.make( f, a ) );
		} else {
			return e;
		}
	}
	
	public static ExprTransform makeTransform() {
		final HoleFinder hole_finder = new HoleFinder();
		return(
			new ExprTransform() {
				public Expr transform( final Expr e ) {
					return (Expr)e.visit( hole_finder );
				}
			}.bottomUp()
		);
	}

}

