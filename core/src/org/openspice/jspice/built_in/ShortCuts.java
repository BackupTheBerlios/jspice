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

import org.openspice.jspice.datatypes.proc.*;
import org.openspice.jspice.built_in.arithmetic.*;
import org.openspice.jspice.built_in.comparisons.*;

import java.util.Map;
import java.util.HashMap;


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

	public final static Proc addProc = new AddProc();
	public final static Proc subProc = new SubProc();
	public final static Proc addImagProc = AddImagProc.ADD_IMAG_PROC;
	public static final Proc subImagProc = SubImagProc.SUB_IMAG_PROC;
	public final static Proc unaryAddProc = PositeProc.POSITE_PROC;
	public final static Proc unarySubProc = NegateProc.NEGATE_PROC;
	public final static Proc mulProc = new MulProc();
	public final static Proc divProc = new DivProc();
	public static final Proc divideProc = new DivideProc();
	public final static Proc modProc = new ModProc();
	public final static Proc mapletProc = MapletProc.MAPLET_PROC;
	public final static Proc listProc = ListProc.LIST_PROC;
	public final static Proc appendProc = new AppendProc();

	public final static Proc lteProc = new LTEProc();
	public final static Proc ltProc = new LTProc();
	public final static Proc gteProc = new GTEProc();
	public final static Proc gtProc = new GTProc();
	public final static Proc eqProc = new EQProc();
	public final static Proc neqProc = new NEQProc();
	public final static Proc equalProc = new EqualProc();
	public final static Proc notEqualProc = new NotEqualProc();

	public final static Proc explodeProc = InvListProc.INV_LIST_PROC;
	public final static Proc noneProc = NoneProc.noneProc;

	public final static Proc indexProc = IndexProc.INDEX_PROC;
	public final static Proc lengthProc = LengthProc.LENGTH_PROC;

	//	Initialization.
	static {
		//System.out.println( "Running initialization" );
		put( "+", addProc );
		put( "unary_+", unaryAddProc );
		put( "+:", addImagProc );
		put( "-", subProc );
		put( "unary_-", unarySubProc );
		put( "-:", subImagProc );
		put( "*", mulProc );
		put( "DIV", divProc );
		put( "MOD", modProc );
		put( "/", divideProc );
		//putOne( "//", DivRatProc.proc );
		put( "==>", mapletProc );
		put( "{", listProc );
		put( "++", appendProc );

		put( "<=", lteProc );
		put( "<", ltProc );
		put( ">=", gteProc );
		put( ">", gtProc );
		put( "==", eqProc );
		put( "/==", neqProc );
		put( "=", equalProc );
		put( "/=", notEqualProc );

		put( "explode", explodeProc );		//	maybe I should call this "..."?  Or both?
		put( "none", noneProc );
	}

}
