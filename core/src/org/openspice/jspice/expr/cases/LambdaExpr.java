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

package org.openspice.jspice.expr.cases;

import org.openspice.jspice.expr.markers.OneResult;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprTransform;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.ExprBase;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.lift.LiftFactory;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.main.Print;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;




public final class LambdaExpr extends ExprBase implements OneResult, LambdaExprForResolver, LambdaExprForPetrification, LambdaExprForLifting, Cloneable {

	public String toString() {
		return "lambda";
	}

	static private final class Outer {
		public final Var.Local inside;
		public final Var.Local outside;

		Outer( final Var.Local _inside, final Var.Local _outside ) {
			this.inside = _inside;
			this.outside = _outside;
		}

		public String toString() {
			return "{" + this.inside + "," + this.outside + "}";
		}
	}

	public static final class SlotCounter {
		private int slot_count = 0;

		public int nextSlot() {
			return this.slot_count++;
		}

		public int getCount() {
			return this.slot_count;
		}
	}

	private Expr init;
	private List hellos;
	private Expr body;

	private List args = new ArrayList();
	private List lvars = new ArrayList();
	private List outers = new ArrayList();	//	list of Outers

	private SlotCounter slot_counter = new SlotCounter();

	/*
		This is a very crafty routine, unfortunately.  Firstly, it
		adds Hellos which will not (and must not) be visited by the
		lifter.  In addition, it relies on the fact that the resulting
		Lambda does utilize any of hellos, args, lvars, outers, so
		there is no need to keep the -init- slot in sync with these.
	*/
	public LambdaExpr lift( final ExprTransform t ) {
		LambdaExpr lambda = null;
		try {
			lambda = (LambdaExpr)this.clone();
		} catch ( CloneNotSupportedException exn ) {
			Alert.unreachable();
		}
		lambda.init = t.transform( this.init );
		lambda.body = t.transform( this.body );

		for ( Iterator it = this.outers.iterator(); it.hasNext(); ) {
			final Var.Local inside_local = ((Outer)it.next()).inside;
			lambda.init = (
				CommaExpr.make(
					lambda.init,
					HelloExpr.make( NameExpr.make( inside_local ) )
				)
			);
		}
		lambda.outers = new ArrayList();

		return lambda;
	}

	private LambdaExpr( Expr _init, Expr _body ) {
		//	Canonise.
		if ( ! ( _init instanceof ApplyExpr ) ) {
			_init = ApplyExpr.make( NameExpr.make( "_" ), _init );
		}

		//	Decurry.
		while ( ((ApplyExpr)_init).getFun() instanceof ApplyExpr ) {
			_body = (
				new LambdaExpr(
					ApplyExpr.make( NameExpr.make( "_" ), ((ApplyExpr)_init).getArg() ), 	//	Canonical form - VITAL.
					_body
				)
			);
			_init = ((ApplyExpr)_init).getFun();
		}

		//	Extract.
		this.hellos = new ArrayList();
		final LiftFactory f = new LiftFactory( null, Props.VAL, false, this.hellos );
		this.init = (Expr)((ApplyExpr)_init).getArg().visit( f );
		this.body = _body;
	}


	//
	//	For the args & lvars of the lambda that are marked as
	//	type-3 we should promote them to being a location.
	//	This returns a list of Var.Locals that falls into that
	//	category.
	//
	private final void scanForType3( final List locvars, final List dst ) {
		for ( Iterator it = locvars.iterator(); it.hasNext(); ) {
			final Var.LocalOrAnon lvar = (Var.LocalOrAnon)it.next();
			if ( lvar.isType3() ) {
				dst.add( lvar );
			}
		}
	}

	public List fetchType3Promotions() {
		final ArrayList list = new ArrayList();
		this.scanForType3( this.args, list );
		//this.scanForType3( this.lvars, list );
		return list;
	}

	public void addArg( final Var.LocalOrAnon local ) {
		if ( !this.args.contains( local ) ) {
			this.args.add( local );
		}
	}

	public void addLvar( final Var.Local local ) {
		if ( !this.lvars.contains( local ) ) {
			this.lvars.add( local );
		}
	}

	public void addOuter( final Var.Local inside_lvar, final Var.Local outside_lvar ) {
		if ( !inside_lvar.isOuter() ) {
			inside_lvar.setIsOuter();
			outside_lvar.setIsOuter();
			this.outers.add( new Outer( inside_lvar, outside_lvar ) );
		}
	}

	public Iterator outsideIterator() {
		final Iterator it = this.outers.iterator();
		return(
			new Iterator() {
				public boolean hasNext() { return it.hasNext(); }
				public Object next() { return ((Outer)it.next()).outside; }
				public void remove() { throw new UnsupportedOperationException(); }
			}
		);
	}

	public boolean hasOuters() {
		return !this.outers.isEmpty();
	}

	public void allocatePositions() {
		if ( Print.wouldPrint( Print.PARSE ) ) {
			Print.println( "allocatePositions" );
			Print.println( "this slot count = " + this.slot_counter.getCount() );
		}
		for ( Iterator it = this.lvars.iterator(); it.hasNext(); ) {
			final Var.Local lv = (Var.Local)it.next();
			lv.setSlot( this.slot_counter );
		}
		//this.lvar_count = this.slot_counter.getCount();

		for ( Iterator it = this.args.iterator(); it.hasNext(); ) {
			final Var.LocalOrAnon lv = (Var.LocalOrAnon)it.next();
			if ( lv instanceof Var.Local ) {
				((Var.Local)lv).setSlot( this.slot_counter );
			}
		}
		//this.arg_count = this.slot_counter.getCount();
		for ( Iterator it = this.outers.iterator(); it.hasNext(); ) {
			final Var.Local lvar = ((Outer)it.next()).inside;
			lvar.setSlot( this.slot_counter );
		}
		if ( Print.wouldPrint( Print.PARSE ) ) {
			Print.println( "this.lvars = " + this.lvars );
			Print.println( "this.args = " + this.args );
			Print.println( "this.outers = " + this.outers );
			Print.println( "this slot count = " + this.slot_counter.getCount() );
		}
	}


	public Iterator getNamedExprIterator() {
		final ArrayList list = new ArrayList();
		for ( Iterator it = this.hellos.iterator(); it.hasNext(); ) {
			final NameExpr nme = ((HelloExpr)it.next()).getNameExpr();
			if ( nme instanceof NamedExpr ) {
				list.add( nme );
			}
		}
		return list.iterator();
	}

	public int getSlotCount() {
		return this.slot_counter.getCount();
	}

	public static LambdaExpr make( final Expr _arglist, final Expr _body ) {
		return new LambdaExpr( _arglist, _body );
	}

	static LambdaExpr makeEmpty() {
		return make( SkipExpr.SKIP_EXPR );
	}

	static LambdaExpr make( final Expr _body ) {
		return new LambdaExpr( ApplyExpr.make( NameExpr.make( "_" ), SkipExpr.SKIP_EXPR ), _body );
	}

	public Expr getBody() {
		return this.body;
	}

	public Expr getInitExpr() {
		return this.init;
	}

	public Arity arity() { return this.defaultArity(); }

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitLambdaExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		throw Alert.unreachable();
	}

	public ExprIterator getAllKids() {
		return ExprIterator.make2( this.init, this.body );
	}
}
