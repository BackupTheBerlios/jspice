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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.*;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.tools.ListTools;
import org.openspice.jspice.tools.CountingIteratorTools;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.lib.MapLib;
import org.openspice.tools.IntegerTools;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

/**	Missing then/else and finally.
	//	for <generator> ** ; while/until <condition> do
	//		<stmnts>
	//	endfor
*/
public class ForMiniParser extends Prefix {


	//	Now construct the answer from the inside-out.
	//	The for loop becomes
	//
	//	block
	//		val tmp1 = <iterator1>;
	//		val tmp2 = <iterator2>;
	//		...
	//		repeat
	//			&_while
	//				tmp1.hasNext() and
	//				tmp2.hasNext() and
	//				...
	//			then
	//				<finally>
	//			do
	//				val <match1> = tmp1.next();
	//				val <match2> = tmp2.next();
	//				...
	//			end_&_while
	//
	//			&_while <while> then <then> do () end_&_while
	//			&_while <while'> then <then'> do () end_&_while
	//			...
	//			&_while true then () do
	//				block
	//					<body>
	//				endblock
	//			end_&_while
	//		endrepeat
	//	endblock
	private Expr construct(
		final LinkedList tmpvars,
		final LinkedList matches,
		final LinkedList iterators,
		final LinkedList filters,
		final LinkedList conditions,
		final LinkedList results,
		final Expr body,
		final Expr finally_expr
	) {
		//	Construct the RepeatExpr first, then add the declarations & block.
		final RepeatExpr.Factory rep_factory = new RepeatExpr.Factory();

		//	Create the predicate: tmp1.hasNext() and tmp2.hasNext() and ...
		Expr predicate;
		if ( !tmpvars.isEmpty() ) {
			//	Take a copy of tmpvars, we don't want to trash it.
			final LinkedList tmps = new LinkedList( tmpvars );
			Expr tuple = (Expr)tmps.removeLast();
			while ( !tmps.isEmpty() ) {
				tuple = CommaExpr.make( (Expr)tmps.removeLast(), tuple );
			}
			predicate = (
				ApplyExpr.make(
					new Vary1Proc() {
						public Object call( Object tos, final VM vm, int nargs ) {
							boolean result = ((Iterator)tos).hasNext();
							while ( nargs-- > 1 ) {
								tos = vm.pop();
								result = result && ((Iterator)tos).hasNext();
							}
							return Boolean.valueOf( result );
						}
					},
					tuple
				)
			);
		} else {
			predicate = ConstantExpr.TRUE_EXPR;
		}

		//	Create the loop bindings
		//		val <match1> = tmp1.next();
		//		val <match2> = tmp2.next();
		//		...
		Expr loop_bindings = SkipExpr.make();
		{
			final LinkedList tmps = new LinkedList( tmpvars );
			final Proc next_proc = (
				new Unary1InvokeProc() {
					public Object invoke( final Object x ) {
						return ((Iterator)x).next();
					}
				}
			);
			while ( !matches.isEmpty() ) {
				final Expr iexpr = (
					InitExpr.make(
						null,
						Props.VAL,
						(Expr)matches.removeLast(),
						ApplyExpr.make(
							next_proc,
							(Expr)tmps.removeLast()
						)
					)
				);
				loop_bindings = CommaExpr.make( iexpr, loop_bindings );
			}
		}
		assert matches.isEmpty();

		//	Add the first guarded clause.
		//		&_while tmp1.hasNext() and tmp2.hasNext() and ...
		//				...
		//		then <finally>
		//		do
		//			val <match1> = tmp1.next();
		//			val <match2> = tmp2.next();
		//			...
		//		end_&_while
		rep_factory.add(
			predicate,
			finally_expr,
			loop_bindings,
			true                              //return
		);

		//	Add a break condition for each filter.
		{
			for ( Iterator fit = filters.iterator(); fit.hasNext(); ) {
				final Expr filter_expr = (Expr)fit.next();
				rep_factory.add( filter_expr, SkipExpr.make(), SkipExpr.make(), false );
			}
		}

		//	Now add a guarded clause for each side-condition.
		//		&_while <while> then <then> do () end_&_while
		//		&_while <while'> then <then'> do () end_&_while
		//		...
		{
			final Iterator cs = conditions.iterator();
			final Iterator rs = results.iterator();
			while ( cs.hasNext() && rs.hasNext() ) {
				rep_factory.add(
					(Expr)cs.next(),
					(Expr)rs.next(),
					SkipExpr.make(),
					true				//	return
				);
			}
			assert !cs.hasNext() && !rs.hasNext();
		}

		//	Next we need an unguarded clause for the main body.
		rep_factory.add(
			ConstantExpr.TRUE_EXPR,
			SkipExpr.SKIP_EXPR,
			body,
			true	//	return
		);

		//	This completes the construction of the repeat loop.
		Expr e = rep_factory.make();

		//	Now add the initialization of the tmpvars.
		while ( !tmpvars.isEmpty() ) {
			final Expr iexpr = (
				InitExpr.make(
					null,
					Props.VAL,
					(NameExpr)tmpvars.removeLast(),
					(Expr)iterators.removeLast()
				)
			);
			e = CommaExpr.make( iexpr, e );
		}
		assert tmpvars.isEmpty() && iterators.isEmpty();

		//	Then add the enclosing Block.
		return BlockExpr.make( e );
	}

	private void readGenerator( final Parser parser, final List matchers, final List iterators ) {
		//	<InitExpr> in <Expr>
		//	<InitExpr> from <Expr>
		//	<InitExpr> on <Expr>
		//final Expr.NameExpr id = parser.readNameExpr();
		final Expr match_expr = parser.readExpr();
		if ( parser.canReadToken( "in" ) ) {
			//	The way this should work is that the generator should
			//	have an ITERATOR expression on the RHS and a INIT
			//	expression on the LHS.  Because ITERATORs are not part
			//	of the language we would have to use a special
			//	Proc or built-in operator.
			final Expr e = parser.readExpr();
			final Proc p = (
				new Unary1InvokeProc() {
					public Object invoke( final Object tos ) {
						return ListTools.convertTo( tos ).iterator();
					}
				}
			);
			final Expr app = ApplyExpr.make( p, e );
			matchers.add( match_expr );
			iterators.add( app );
			//return new Generator( id, app );
		} else if ( parser.canReadToken( "on" ) ) {
			final Expr e = parser.readExpr();
			final Proc p = (
				new Unary1InvokeProc() {
					public Object invoke( final Object tos ) {
						return MapLib.convertTo( tos ).entrySet().iterator();
					}
				}
			);
			final Expr app = ApplyExpr.make( p, e );
			matchers.add( match_expr );
			iterators.add( app );
		} else if ( parser.canPeekToken( "from" ) || parser.canPeekToken( "by" ) || parser.canPeekToken( "to" ) ) {
			final Expr from_expr = parser.canReadToken( "from" ) ? parser.readExpr() : ConstantExpr.make( IntegerTools.ONE );
			final Expr by_expr = parser.canReadToken( "by" ) ? parser.readExpr() : ConstantExpr.make( IntegerTools.ONE );
			final Expr to_expr = parser.canReadToken( "to" ) ? parser.readExpr() : null;
			Expr app;
			if ( to_expr == null ) {
				//	Unbounded iteration.
				app = (
					ApplyExpr.make1(
						new Binary1InvokeProc() {
							public Object invoke( final Object from_value, final Object by_value ) {
								return (
									CountingIteratorTools.fromBy(
										CastLib.toNumber( from_value ),
										CastLib.toNumber( by_value )
									)
								);
							}
						},
						from_expr,
						by_expr
					)
				);
			} else {
				//	Bounded iteration.
				app = (
					ApplyExpr.make1(
						new Trinary1InvokeProc() {
							public Object invoke( final Object from_value, final Object by_value, final Object to_value ) {
								return (
									CountingIteratorTools.fromByTo(
										CastLib.toNumber( from_value ),
										CastLib.toNumber( by_value ),
										CastLib.toNumber( to_value )
									)
								);
							}
						},
						from_expr,
						by_expr,
						to_expr
					)
				);
			}
			assert app != null;
			//return new Generator( id, app );
			matchers.add( match_expr );
			iterators.add( app );
		} else {
			new Alert( "Bad for loop expression" ).mishap( 'P' );
		}
	}

	public Expr prefix( final String interned, final Parser parser ) {
		parser.saveTmp();

		final LinkedList tmpvars = new LinkedList();
		final LinkedList matches = new LinkedList();
		final LinkedList iterators = new LinkedList();
		final LinkedList filters = new LinkedList();
		final LinkedList conditions = new LinkedList();
		final LinkedList results = new LinkedList();

		if ( parser.tryReadToken( "do" ) == null ) {
			for (;;) {
				final Token t = parser.peekToken();
				if ( t.hasName( "suchthat" ) ) {
					parser.dropToken();
					final Expr filter = parser.readExpr();
					filters.add( filter );
				} else if ( t.hasName( "while" ) || t.hasName( "until" ) ) {
					parser.dropToken();
					Expr cond = parser.readExpr();
					if ( t.hasName( "until" ) ) {
						cond = ApplyExpr.make( NameExpr.make( "not" ), cond );
					}
					conditions.add( cond );
					if ( parser.canReadToken( "then" ) || parser.canReadToken( "else" ) ) {
						results.add( parser.readExpr() );
					} else {
						results.add( SkipExpr.make() );
					}
				} else if ( parser.tryReadToken( "until" ) != null ) {
					conditions.add( ApplyExpr.make( NameExpr.make( "not" ), parser.readExpr() ) );
				} else {
					tmpvars.add( parser.newTmpNameExpr() );
					readGenerator( parser, matches, iterators );
					final Token nexttok = parser.peekToken();
					if ( nexttok == null || !nexttok.hasName( "while" ) && !nexttok.hasName( "until" ) && !nexttok.hasName( "suchthat" ) && !nexttok.hasName( "do" ) && !nexttok.hasName( ";" ) ) {
						new Alert( "Unexpected token following iterator" ).culprit( "token", nexttok ).mishap();
					}
				}
				parser.tryReadToken( ";" );		//	dispose of optional semi-colon
				if ( parser.tryReadToken( "do" ) != null ) break;
			}
		}
		assert tmpvars.size() == matches.size();

		final Expr body = parser.readStmnts();
		Expr finally_expr = SkipExpr.make();
		if ( parser.canReadToken( "finally" ) ) {
			finally_expr = parser.readStmntsTo( "endfor" );
		} else {
			parser.mustReadToken( "endfor" );
		}

		final Expr answer = (
			this.construct(
				tmpvars,
				matches,
				iterators,
				filters,
				conditions,
				results,
				body,
				finally_expr
			)
		);

		parser.restoreTmp();

		return answer;
		//return Expr.ForExpr.make( generators, conditions, Expr.BlockExpr.make( body ) );
	}

}
