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
package org.openspice.jspice.datatypes.proc;


import org.openspice.jspice.vm_and_compiler.*;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.alert.AlertException;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.built_in.*;

import java.util.*;



public abstract class Proc {

	public abstract Arity inArity();
	public abstract Arity outArity();
	public abstract Object call( final Object tos, final VM vm, final int nargs );

	//	Make this abstract when we are about to embark on hacking the summaries.
	public String summary() {
		return this.defaultSummary();
	}

	protected final String defaultSummary() {
		return "Sorry, a summary for this procedure is unavailable";
	}

	public Proc inverse() {
		return null;
	}
	
	public Arity keysUArity() {
		return this.inArity();
	}
	
	public Arity valsUArity() {
		return this.outArity();
	}

	public final AlertException fail_updater( final String complaint, final String explanation, final Object tos, final VM vm, final int vargs, final int kargs ) {
		vm.push( tos );

		final LinkedList keys = new LinkedList();
		for ( int i = 0; i < kargs; i++ ) {
			keys.addFirst( vm.pop() );
		}

		final LinkedList values = new LinkedList();
		for ( int i = 0; i < vargs; i++ ) {
			values.addFirst( vm.pop() );
		}

		final Alert alert = new Alert( complaint, explanation );

		alert.culprit( "procedure", this );
		{
			int k = 0;
			for ( Iterator it = keys.iterator(); it.hasNext(); k++ ) {
				alert.culprit( "key" + k, it.next() );
			}
		}
		{
			int v = 0;
			for ( Iterator it = values.iterator(); it.hasNext(); v++ ) {
				alert.culprit( "value" + v, it.next() );
			}
		}

		return alert.mishap( 'E' );
	}

	public Object ucall( final Object tos, final VM vm, final int vargs, final int kargs ) {
		throw this.fail_updater( "Invalid updater", "A procedure is being called in update mode that has no updater", tos, vm, vargs, kargs );
	}


//	//	----------------------------------------------------
//
//	private static Hashtable table = new Hashtable();
//
//	public static Object lookup( final String s ) {
//		final Object answer = table.get( s );
//		if ( answer == null ) {
//			//System.out.println( "table size = " + table.size() );
//			throw new RuntimeException( s );
//		}
//		return answer;
//	}
//
//	public static Proc lookupProc( final String s ) {
//		return (Proc)lookup( s );
//	}
//
//	public static void putOne( final String s, final Proc p ) {
//		assert s != null : 0;
//		assert p != null : 1;
//		assert table != null : 2;
//		//System.out.println( "putOne '" + s +"' into table" );
//		table.putOne( s, p );
//	}
//
//	public final static Proc addProc = new AddProc();
//	public final static Proc subProc = new SubProc();
//	public final static Proc unaryAddProc = UnaryAddProc.UNARY_ADD_PROC;
//	public final static Proc unarySubProc = UnarySubProc.UNARY_SUB_PROC;
//	public final static Proc mulProc = new MulProc();
//	public final static Proc divProc = new DivProc();
//	public final static Proc modProc = new ModProc();
//	public final static Proc mapletProc = MapletProc.MAPLET_PROC;
//	public final static Proc listProc = ListProc.LIST_PROC;
//	public final static Proc appendProc = new AppendProc();
//
//	public final static Proc lteProc = new LTEProc();
//	public final static Proc ltProc = new LTProc();
//	public final static Proc gteProc = new GTEProc();
//	public final static Proc gtProc = new GTProc();
//	public final static Proc eqProc = new EQProc();
//	public final static Proc neqProc = new NEQProc();
//	public final static Proc equalProc = new EqualProc();
//	public final static Proc notEqualProc = new NotEqualProc();
//
//	public final static Proc explodeProc = InvListProc.INV_LIST_PROC;
//	public final static Proc noneProc = new NoneProc();
//
//	public final static Proc indexProc = IndexProc.INDEX_PROC;
//	public final static Proc lengthProc = LengthProc.LENGTH_PROC;
//
//	//	Initialization.
//	static {
//		//System.out.println( "Running initialization" );
//		putOne( "+", addProc );
//		putOne( "unary_+", unaryAddProc );
//		putOne( "-", subProc );
//		putOne( "unary_-", unarySubProc );
//		putOne( "*", mulProc );
//		putOne( "DIV", divProc );
//		putOne( "MOD", modProc );
//		//putOne( "/", DivFloProc.proc );
//		//putOne( "//", DivRatProc.proc );
//		putOne( "==>", mapletProc );
//		putOne( "{", listProc );
//		putOne( "++", appendProc );
//
//		putOne( "<=", lteProc );
//		putOne( "<", ltProc );
//		putOne( ">=", gteProc );
//		putOne( ">", gtProc );
//		putOne( "==", eqProc );
//		putOne( "/==", neqProc );
//		putOne( "=", equalProc );
//		putOne( "/=", notEqualProc );
//
//		putOne( "explode", explodeProc );		//	maybe I should call this "..."?  Or both?
//		putOne( "none", noneProc );
//	}

}


