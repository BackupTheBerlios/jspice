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
package org.openspice.jspice.built_in;

import org.openspice.jspice.built_in.lists.AppendProc;
import org.openspice.jspice.built_in.lists.NewImmutableListProc;
import org.openspice.jspice.built_in.lists.InvListProc;
import org.openspice.jspice.built_in.arithmetic.*;
import org.openspice.jspice.built_in.comparisons.*;
import org.openspice.jspice.built_in.maplets.NewMapletProc;
import org.openspice.jspice.built_in.maps.LengthProc;
import org.openspice.jspice.built_in.maps.IndexProc;
import org.openspice.jspice.datatypes.proc.Proc;

import java.util.Map;
import java.util.HashMap;

//import org.openspice.jspice.datatypes.proc.*;
//import org.openspice.jspice.built_in.arithmetic.*;
//import org.openspice.jspice.built_in.comparisons.*;
//import org.openspice.jspice.built_in.lists.AppendProc;
//import org.openspice.jspice.built_in.lists.InvListProc;
//import org.openspice.jspice.built_in.lists.NewImmutableListProc;
//
//import java.util.Map;
//import java.util.HashMap;


public final class ShortCuts {

	private static Map table = new HashMap();

	public static Object lookup( final String s ) {
		final Object answer = table.get( s );
		if ( answer == null ) {
			//System.out.println( "table size = " + table.size() );
			throw new RuntimeException( s );
		}
		return answer;
	}

	public static Proc lookupProc( final String s ) {
		return (Proc)lookup( s );
	}

	public static void put( final String s, final Proc p ) {
		assert s != null : 0;
		assert p != null : 1;
		assert table != null : 2;
		table.put( s, p );
	}

	//	Initialization.
	static {
		//System.out.println( "Running initialization" );
		put( "+", AddProc.ADD_PROC );
		put( "unary_+", PositeProc.POSITE_PROC );
		put( "+:", AddImagProc.ADD_IMAG_PROC );
		put( "-", SubProc.SUB_PROC );
		put( "unary_-", NegateProc.NEGATE_PROC );
		put( "-:", SubImagProc.SUB_IMAG_PROC );
		put( "*", MulProc.MUL_PROC );
		put( "div", DivProc.DIV_PROC );
		put( "mod", ModProc.MOD_PROC );
		put( "/", DivideProc.DIVIDE_PROC );
		put( "**", PowProc.POW_PROC );
		//putOne( "//", DivRatProc.proc );
		put( "==>", NewMapletProc.NEW_MAPLET_PROC );
		put( "{", NewImmutableListProc.NEW_IMMUTABLE_LIST_PROC );
		put( "++", AppendProc.APPEND_PROC );

		put( "<=", LTEProc.LTE_PROC );
		put( "<", LTProc.LT_PROC );
		put( ">=", GTEProc.GTE_PROC );
		put( ">", GTProc.GT_PROC );
		put( "==", EQProc.EQ_PROC );
		put( "/==", NEQProc.NEQ_PROC );
		put( "=", EqualProc.EQUAL_PROC );
		put( "/=", NotEqualProc.NOT_EQUAL_PROC );

		put( "explode", InvListProc.INV_LIST_PROC );		//	maybe I should call this "..."?  Or both?
		put( "none", NoneProc.NONE_PROC );
	}

}
