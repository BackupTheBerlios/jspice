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
import org.openspice.jspice.lib.AbsentLib;
import org.openspice.jspice.lib.CastLib;
import org.openspice.tools.BooleanTools;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.datatypes.Symbol;
import org.openspice.jspice.datatypes.XmlElement;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.vm_and_compiler.*;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.Location;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.FailException;
import org.openspice.jspice.built_in.ShortCuts;

import java.util.*;


public class Petrifier extends ExprVisitor.DefaultUnimplemented {

	public Pebble petrify( final Expr e ) {
		return (Pebble)e.visit( this );
	}

	public Pebble petrifyCheckOne( final Expr e ) {
		final Pebble subproc = this.petrify( e );
		final Arity n = e.arity();
		if ( n.hasFixedCount( 1 ) ) {
			return subproc;
		} else if ( n.isVariadic() ) {
			return(
				new Pebble() {
					Object run( Object tos, final VM vm ) {
						final int before = vm.length();
						tos = subproc.run( tos, vm );
						if ( vm.length() - before != 1 ) {
							//  Absolutely stupid error message.
							new Alert( "Check 1 result failed" ).mishap( 'G' );
						}
						return tos;
					}
				}
			);
		} else {
			new Alert(
				"Arity mismatch (" + n + " results)",
				"This expression must return one result"
			).culprit( "expression", e ).mishap( 'G' );
			return null;
		}
	}
	
	public Object visitApplyExpr( final ApplyExpr e, final Object arg  ) {
		final Pebble fun = this.petrifyCheckOne( e.getFun() );
		final Pebble args = this.petrify( e.getArg() );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final int before = vm.length();
					tos = args.run( tos, vm );
					final int after = vm.length();
					final Proc p = CastLib.toProc( fun.run( tos, vm ) );
					assert vm.length() == after + 1;
					return p.call( vm.pop(), vm, after - before );
				}
			}
		);
	}
	
	private Pebble petrifyAssign( final NameExpr nme, final Pebble pebble ) {
		return PetrifyAssign.petrify( nme, pebble );
	}
	
	public Object visitBlockExpr( final BlockExpr e, final Object arg  ) {
		return e.getFirst().visit( this );
	}
	
	public Object visitCheckCountExpr( final CheckCountExpr e, final Object arg  ) {
		final Pebble subproc = this.petrify( e.getFirst() );
		final int count = e.getCount();
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final int before = vm.length();
					tos = subproc.run( tos, vm );
					if ( vm.length() - before != count ) {
						//  Absolutely stupid error message.
						new Alert(
							"Dynamic check of the number of results failed"
						).
						culprit( "Wanted", new Integer( count ) ).
						culprit( "Found", new Integer( vm.length() - before ) ).
						mishap( 'E' );
					}
					return tos;
				}
			}
		);
	}
	
	public Object visitCheckOneExpr( final CheckOneExpr e, final Object arg  ) {
		final Pebble subproc = this.petrify( e.getFirst() );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final int before = vm.length();
					tos = subproc.run( tos, vm );
					if ( vm.length() - before != 1 ) {
						//  Absolutely stupid error message.
						new Alert( "Check 1 result failed" ).mishap( 'E' );
					}
					return tos;
				}
			}
		);
	}
	
	public Object visitConstantExpr( final ConstantExpr e, final Object arg  ) {
		final Object value = e.getValue();
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					vm.push( tos );
					return value;
				}
			}
		);
	}
	
	public Object visitCommaExpr( final CommaExpr e, final Object arg  ) {
		final Pebble lhs = this.petrify( e.getLeft() );
		final Pebble rhs = this.petrify( e.getRight() );
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					return rhs.run( lhs.run( tos, vm ), vm );
				}
			}
		);
		
	}

	/*public Object visitExplodeExpr( final Expr.ExplodeExpr expr, final Object arg  ) {
		return (
			this.petrify(
				Expr.ApplyExpr.make(
					Expr.ConstantExpr.make( Proc.explodeProc ),
					expr.getFirst()
				)
			)
		);
	}*/
	

	public Object visitHoleExpr( final HoleExpr expr, final Object arg  ) {
		return (
			new Alert(
				"Misplaced hole found",
				"Holes must have the right kind of parent expression"
			).mishap( 'G' )
		);
	}
			
	public Object visitIf3Expr( final If3Expr e, final Object arg  ) {
		final Pebble testp = this.petrify( e.getFirst() );
		final Pebble ifsop = this.petrify( e.getSecond() );
		final Pebble ifnotp = this.petrify( e.getThird() );
		return(
			new Pebble() {
				Object run( final Object tos, final VM vm ) {
					final Boolean testv = (Boolean)testp.run( tos, vm );
					if ( testv.booleanValue() ) {
						return ifsop.run( vm.pop(), vm );
					} else {
						return ifnotp.run( vm.pop(), vm );
					}
				}
			}
		);
	}

	public Object visitImportExpr( final ImportExpr import_expr, final Object arg ) {
		return skipPebble;
	}

	private Pebble dynamicTotalAssignExpr( final Expr ass_expr, final Expr source_expr ) {
		final Pebble source_pebble = this.petrify( source_expr );
		final Pebble dynamic_pebble = DynamicAssign.dynamic_assign( this, ass_expr );
		return (
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final int before = vm.length();
					tos = source_pebble.run( tos, vm );
					vm.intpush( vm.v_args );
					vm.v_args = vm.length() - before;
					
					tos = dynamic_pebble.run( tos, vm );
					
					if ( vm.v_args != 0 ) {
						new Alert( "Assignment failed" ).mishap( 'E' );
					}
					vm.v_args = vm.intpop();
					return tos;
				}
			}
		);
	}
	
	public Object visitTotalAssignExpr( final TotalAssignExpr e, final Object arg  ) {
		final Expr ass_expr = e.getFirst();
		Expr source_expr = e.getSecond();
		
		//	Is it possible to impose an arity on the source?  
		Arity source_arity = source_expr.arity();
		if ( source_arity.isVariadic() ) {
			//	Try to exploit the target initArity.
			final Arity ass_arity = StaticAssignCalc.exact( ass_expr );
			if ( ass_arity != null ) {
				source_arity = ass_arity;
				source_expr = CheckCountExpr.make( source_arity.getCount(), source_expr );
			}
		}

		try {
			if ( source_arity.isFixed() ) throw FailException.failException;
			final Pebble source_pebble = this.petrify( source_expr );
			final StaticAssign st_ass = new StaticAssign( this, source_arity.getCount(), source_pebble );
			return st_ass.apply( ass_expr ).result();
		} catch ( FailException _ ) {
			return dynamicTotalAssignExpr( ass_expr, source_expr );
		}
	}
	
	private Pebble dynamicInitPebble( final Expr init_expr, final Expr source_expr ) {
		final Pebble source_pebble = this.petrify( source_expr );
		final Pebble dynamic_pebble = DynamicInit.dynamic_init( this, init_expr );
		return (
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final int before = vm.length();
					tos = source_pebble.run( tos, vm );
					vm.intpush( vm.n_args );
					vm.n_args = vm.length() - before;
					
					tos = dynamic_pebble.run( tos, vm );
					
					if ( vm.n_args != 0 ) {
						new Alert( "Initialization failed" ).mishap( 'E' );
					}
					vm.n_args = vm.intpop();
					return tos;
				}
			}
		);
	}

	
	public Object visitInitExpr( final InitExpr e, final Object arg  ) {
		Expr init_expr = e.getInitExpr();
		Expr source_expr = e.getSourceExpr();
		
		//	Unwrap any outer constant function calls.
		for (;;) {
			if ( ! ( init_expr instanceof ApplyExpr ) ) break;
			final Object const_fun = ((ApplyExpr)init_expr).getFun().maybeConstantExpr();
			if ( ! ( const_fun instanceof Proc ) ) break;	//	defensive
			final Proc inv = ((Proc)const_fun).inverse();
			if ( inv == null ) {
				new Alert(
					"Invalid initialization - no inverse",
					"Function calls inside initializations must be invertible"
				).culprit( "function", const_fun ).mishap( 'G' );
			}
			init_expr = ((ApplyExpr)init_expr).getArg();
			source_expr = ApplyExpr.make( inv, source_expr );
		}
		
		//	Is it possible to impose an arity on the source?  
		Arity source_arity = source_expr.arity();
		if ( source_arity.isVariadic() ) {
			//	Try to exploit the target initArity.
			final Arity init_arity = StaticInitCalc.exact( init_expr );
			if ( init_arity != null ) {
				source_arity = init_arity;
				source_expr = CheckCountExpr.make( source_arity.getCount(), source_expr );
			}
		}

		if ( source_arity.isFixed() ) {
			final Pebble source_pebble = this.petrify( source_expr );
			final StaticInit st_init = new StaticInit( this, source_arity.getCount(), source_pebble );
			return st_init.apply( init_expr ).result();
		} else {
			return this.dynamicInitPebble( init_expr, source_expr );
		}
	}
	
	public Object visitLambdaExpr( final LambdaExpr e, final Object arg  ) {
		return PetrifyLambda.petrifyLambda( this, e );
	}
	
	public Object visitAnonExpr( final AnonExpr e, final Object arg  ) {
		return ConstantExpr.ABSENT_EXPR.visit( this );
	}

	public Object visitNamedExpr( final NamedExpr e, final Object arg  ) {
		if ( e.isGlobal() ) {
			final Var.Perm p = e.getPerm();
			final Location r = p.getLocation();
			if ( r.referenceCheck() ) {
				return(
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							vm.push( tos );
							return r.getValue();
						}
					}
				);
			} else {
				return(
					new Pebble() {
						boolean do_check = true;
						Object run( final Object tos, final VM vm ) {
							if ( this.do_check ) {
								r.useCheck();
								this.do_check = false;
							}
							vm.push( tos );
							return r.getValue();
						}
					}
				);
			}
		} else {
			final int offset = e.getOffset();
			if ( e.isType1() ) {
				return( 
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							vm.push( tos );
							return vm.load( offset );
						}
					}
				);
				
			} else {
				//	isOuterLocal
				assert e.isType3();
				return( 
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							vm.push( tos );
							return ((Ref)vm.load( offset )).cont;
						}
					}
				);
			}
		}
	}
	
	//
	//	Note that the simplification phase is responsible for
	//	changing RelOpChain's into ordinary ApplyExprs where
	//	possible - giving rise to further optimization opportunities.
	//
	//	Also note that there is no dynamic arity checking.  CHECK
	//	THAT THE ARITY IS ENFORCED BY THE REL_OP_CHAIN_EXPR
	//	CONSTRUCTOR!!!!
	//
	public Object visitRelOpChainExpr( final RelOpChainExpr e, final Object arg  ) {
		final Pebble startp = this.petrifyCheckOne( e.getStart() );
		final LinkedList proc_list = new LinkedList();
		final LinkedList peb_list = new LinkedList();
		for ( Iterator it = e.getSymbolIterator(); it.hasNext(); ) {
			proc_list.addLast( ShortCuts.lookupProc( (String)it.next() ) );
		}
		for ( Iterator it = e.getExpressionIterator(); it.hasNext(); ) {
			peb_list.addLast( this.petrifyCheckOne( (Expr)it.next() ) );
		}
		//System.out.println( "proc_list.toArray(): " + proc_list.toArray() );
		final Object[] oprocs = proc_list.toArray();
		final Object[] opebbles = peb_list.toArray();
		assert oprocs.length == opebbles.length;
		final int n = oprocs.length;
		final Proc[] procs = new Proc[ n ];
		final Pebble[] pebbles = new Pebble[ n ];
		System.arraycopy( oprocs, 0, procs, 0, n );
		System.arraycopy( opebbles, 0, pebbles, 0, n );
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					tos = startp.run( tos, vm );
					for ( int i = 0; i < n; i++ ) {
						tos = pebbles[ i ].run( tos, vm );
						final Boolean b = (Boolean)procs[ i ].call( tos, vm, 2 );
						if ( !b.booleanValue() ) {
							return b;
						}
					}
					return BooleanTools.TRUE;
				}
			}
		);
	}
	
	static private Pebble[] createPebbleArray( final List list ) {
		final Pebble[] pebbles = new Pebble[ list.size() ];
		int n = 0;
		for ( Iterator it = list.iterator(); it.hasNext(); n++ ) {
			pebbles[ n ] = (Pebble)it.next();
		}
		return pebbles;
	}
	
	public Object visitRepeatExpr( final RepeatExpr repeat_expr, final Object arg  ) {
		final ArrayList test_list = new ArrayList();
		final ArrayList result_list = new ArrayList();
		final ArrayList do_list = new ArrayList();
		final ExprIterator it = repeat_expr.getAllKids();
		while ( it.hasNext() ) {
			test_list.add( this.petrify( it.next() ) );
			result_list.add( this.petrify( it.next() ) );
			do_list.add( this.petrify( it.next() ) );
		}
		final Pebble[] test_array = createPebbleArray( test_list );
		final Pebble[] result_array = createPebbleArray( result_list );
		final Pebble[] do_array = createPebbleArray( do_list );
		assert test_array.length == result_array.length;
		assert test_array.length == do_array.length;
		final int count = test_array.length;
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					for (;;) {
						for ( int i = 0; i < count; i++ ) {
							tos = test_array[ i ].run( tos, vm );
							if ( !((Boolean)tos).booleanValue() ) {
								return result_array[ i ].run( vm.pop(), vm );
							}
							tos = do_array[ i ].run( vm.pop(), vm );
						}
					}
				}
			}
		);
	}
	
	public Object visitShortCircuitExpr( final ShortCircuitExpr e, final Object arg  ) {
		final Pebble lhp = this.petrifyCheckOne( e.getLeft() );
		final Pebble rhp = this.petrifyCheckOne( e.getRight() );
		if ( e.bool_oriented ) {
			if ( e.and_oriented ) {
				return(
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							final Boolean lhv = (Boolean)lhp.run( tos, vm );
							if ( lhv.booleanValue() ) {
								return (Boolean)rhp.run( vm.pop(), vm );
							} else {
								return BooleanTools.FALSE;
							}
						}
					}
				);
			} else {
				return(
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							final Boolean lhv = (Boolean)lhp.run( tos, vm );
							if ( lhv.booleanValue() ) return BooleanTools.TRUE;
							return (Boolean)rhp.run( vm.pop(), vm );
						}
					}
				);
			}
		} else {
			if ( e.and_oriented ) {
				return(
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							final Object lhv = lhp.run( tos, vm );
							if ( lhv == AbsentLib.ABSENT ) return AbsentLib.ABSENT;
							return rhp.run( vm.pop(), vm );
						}
					}
				);
			} else {
				return(
					new Pebble() {
						Object run( final Object tos, final VM vm ) {
							final Object lhv = lhp.run( tos, vm );
							if ( lhv != AbsentLib.ABSENT ) return lhv;
							return rhp.run( vm.pop(), vm );
						}
					}
				);
			}
		}
	}
	
	final static private Pebble skipPebble = (
		new Pebble() {
			Object run( final Object tos, final VM vm ) { return tos; }
		}
	);
	
	public Object visitSkipExpr( final SkipExpr e, final Object arg  ) {
		return skipPebble;
	}
	
	public Object visitXmlElementExpr( final XmlElementExpr e, final Object arg  ) {
		final Pebble namep = this.petrifyCheckOne( e.getFirst() );
		final Pebble bodyp = this.petrify( e.getThird() );
		
		final ArrayList attr_names = new ArrayList();
		final ArrayList attr_values = new ArrayList();
		int n = 0;
		for ( ExprIterator it = e.getSecond().getAllKids(); it.hasNext(); n++ ) {
			final XmlAttrExpr xattr = (XmlAttrExpr)it.next();
			attr_names.add( this.petrifyCheckOne( xattr.getFirst() ) );
			attr_values.add( this.petrifyCheckOne( xattr.getSecond() ) );
		}
		final int attr_count = n;
		
		final Pebble[] attr_names_p = new Pebble[ attr_count ];
		final Pebble[] attr_values_p = new Pebble[ attr_count ];
		for ( int i = 0; i < n; i++ ) {
			attr_names_p[ i ] = (Pebble)attr_names.get( i );
			attr_values_p[ i ] = (Pebble)attr_values.get( i );
		}
		
		return(
			new Pebble() {
				Object run( Object tos, final VM vm ) {
					final Symbol name = (Symbol)namep.run( tos, vm );
					
					final TreeMap map = new TreeMap();
					for ( int i = 0; i < attr_count; i++ ) {
						final Object x = attr_names_p[ i ].run( vm.pop(), vm );
						final Object y = attr_values_p[ i ].run( vm.pop(), vm );
						try {
							map.put( x, y );
						} catch ( ClassCastException ex ) {
							new Alert(
								"Attribute name not a symbol",
								"Names of XML element attributes must be symbolic"
							).culprit( "attribute name", x ).mishap( 'E' );
						}
					}
					
					tos = vm.pop();
					final int before = vm.length();
					tos = bodyp.run( tos, vm );
					final int num_stack = vm.length() - before;
					vm.push( tos );
					final Object[] kids = new Object[ num_stack ];
					for ( int i = num_stack - 1; i >= 0; i-- ) {
						kids[ i ] = vm.pop();
					}
					
					return new XmlElement( name, map, kids );
				}
			}
		);
	}
	
}