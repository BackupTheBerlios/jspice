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
package org.openspice.jspice.resolve;

import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Print;

import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


final class LocalEnv extends Env {

	final boolean is_lexical_block;
	final Env parent;							//	A link to the outer scope.
	final TreeMap locals = new TreeMap( new CompareObjects() );		//	String -> Var.Local
	final LambdaExprForResolver lambda;

	private LocalEnv(
		final boolean _is_lexical_block,
		final Env _parent,
		final LambdaExprForResolver _lambda
	) {
		assert _parent != null;
		assert _lambda != null;
		this.is_lexical_block = _is_lexical_block;
		this.parent = _parent;
		this.lambda = _lambda;
		assert _lambda.getSlotCount() == 0;

		//	Introduce local arglist.
		if ( !this.is_lexical_block ) {
			for ( Iterator it = _lambda.getNamedExprIterator(); it.hasNext(); ) {
				final NameExpr nme = (NameExpr)it.next();
				if ( nme instanceof AnonExpr ) {
					_lambda.addArg( new Var.Anon( Props.VAL ) );
				} else if ( nme instanceof NamedExpr ) {
					final NamedExpr named = (NamedExpr)nme;
					_lambda.addArg( LocalEnv.this.addLocal( Props.VAL, named ) );
				} else {
					Alert.unreachable();
				}
			}
		}
	}

	LocalEnv( final Env _parent, final LambdaExprForResolver _lambda ) {
		this( false, _parent, _lambda );
	}

	LocalEnv( final Env _parent ) {
		this( true, _parent, ((LocalEnv)_parent).lambda );
	}

	boolean isGlobal() {
		return false;
	}

	private Var.Local lookup( final Object s ) {
		return (Var.Local)this.locals.get( s );
	}

	private Var.Local lookup( final NamedExpr var ) {
		return this.lookup( var.getSymbol() );
	}

	Var.Local addLocal( final Props props, final NamedExpr nme ) {
		final String title = nme.getTitle();
		final Var.Local lvar = new Var.Local( props, title, (LambdaExpr)this.lambda );
		this.locals.put( nme.getSymbol(), lvar );
		nme.setVar( lvar );
		return lvar;
	}

	void hello( final HelloExpr hello_expr, final NameSpace name_space ) {
		final NameExpr name = hello_expr.getNameExpr();
		if ( name instanceof NamedExpr ) {
			final NamedExpr named = (NamedExpr)name;
			final Var.Local loc = this.lookup( named.getSymbol() );
			if ( loc == null ) {
				this.lambda.addLvar( this.addLocal( hello_expr.getProps(), named ) );
			} else {
				if ( loc.getProps().isForwardVersion( hello_expr.getProps() ) ) {
					loc.setProps( hello_expr.getProps() );
				} else {
					new Alert(
						"Already declared in same scope"
					).culprit( "name", named.getTitle() ).mishap( 'R' );
				}
			}
		}
	}

	static final class Transition {
		public final LocalEnv inner;
		public final LocalEnv outer;

		Transition( final LocalEnv _inner, final LocalEnv _outer ) {
			this.inner = _inner;
			this.outer = _outer;
		}
	}

	//	Compute the list of transitions between lambdas.  Returns a
	//	list of pairs of LocalEnvs.  These must be reverse ordered i.e.
	//	from OUT to IN.
	private List transitions( final LocalEnv here, final LocalEnv there ) {
		final LinkedList distinct = new LinkedList();
		LocalEnv start = here;
		for(;;) {
			if ( !start.is_lexical_block ) {
				distinct.addFirst( start );		//	This does reversal here.
			}
			if ( start == there ) break;
			start = (LocalEnv)start.parent;
		}

		assert !distinct.isEmpty();

		final LinkedList answer = new LinkedList();
		final Iterator it = distinct.iterator();
		LocalEnv prev = (LocalEnv)it.next();
		while ( it.hasNext() ) {
			LocalEnv next = (LocalEnv)it.next();
			answer.addLast( new Transition( next, prev ) );
			prev = next;
		}

		return answer;
	}

	void bind( final NamedExpr nme, final NameSpace name_space ) {
		//System.out.println( "trying to bind " + nme.getTypeSymbol() );
		LocalEnv loc = this;
		do {
			final Var.Local lvar = loc.lookup( nme );
			if ( lvar != null ) {
				if ( loc.lambda == this.lambda ) {
					nme.setVar( lvar );
				} else {
					if ( Print.wouldPrint( Print.PARSE ) ) {
						Print.println( "outer detected (" + nme.getTitle() + ")" );
					}
					final List trans = this.transitions( this, loc );
					for ( Iterator it = trans.iterator(); it.hasNext(); ) {
						final Transition t = (Transition)it.next();
						//	nb. The 'var' field of -nme- gets assigned to
						//	repeatedly.  The last assignment is the innermost
						//	and that's the one we want.
						if ( Print.wouldPrint( Print.PARSE ) ) {
							Print.println( "TRANSITION for " + nme.getTitle() + " from " + t.inner.lambda + " to " + t.outer.lambda );
						}
						final Var.Local olvar = t.outer.lookup( nme );
						t.inner.lambda.addOuter( t.inner.addLocal( olvar.getProps(), nme ), olvar );
					}
				}
				return;
			}
			if ( ! ( loc.parent instanceof LocalEnv ) ) break;
			loc = (LocalEnv)loc.parent;
		} while( true );
		//	Otherwise it is global.
		//System.out.println( "looking in global space " + nme.getTypeSymbol() );
		Env.GLOBAL.bind( nme, name_space );
	}


}

