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

import org.openspice.jspice.expr.cases.HelloExpr;
import org.openspice.jspice.expr.cases.NameExpr;
import org.openspice.jspice.expr.cases.NamedExpr;
import org.openspice.jspice.expr.TmpName;
import org.openspice.jspice.namespace.*;
import org.openspice.jspice.alert.Alert;

import java.util.TreeMap;

final class GlobalEnv extends Env {

	//	This is either very clever or insane.  Sadly I
	//	don't know which.
	private final TreeMap global_tmps = new TreeMap();

	void hello( final HelloExpr hello_expr, final NameSpace name_space ) {
		//System.out.println( "Global hello for " + hello_expr.getNameExpr() );
		final NameExpr name = hello_expr.getNameExpr();
		if ( name instanceof NamedExpr ) {
            Var.Perm p;
			final NamedExpr named = (NamedExpr)name;
			if ( named.isTmp() ) {
				p = (Var.Perm)global_tmps.get( named.getSymbol() );
				if ( p == null ) {
					final String id = named.getSymbol().toString();
					p = new Var.Perm( Props.VAL, FacetSet.NONE, id, new Location() );
					global_tmps.put( named.getSymbol(), p );
				}
			} else {
				final String intn = (String)named.getSymbol();
				assert intn == intn.intern();
				//	Top-level.
				p = (
					name_space.declarePerm(
						hello_expr.getFacets(),
						intn,
						hello_expr.getProps()
					)
				);
			}
			assert p != null;
			named.setVar( p );
		}
	}

	void bind( final NamedExpr nme, final NameSpace name_space ) {
		Var.Perm p;
		if ( nme.isTmp() ) {
			final TmpName tmp_name = (TmpName)nme.getSymbol();
			//System.out.println( tmp_name + " compared to itself! = " + tmp_name.compareTo( tmp_name ) );
			p = (Var.Perm)global_tmps.get( tmp_name );
			if ( p == null ) {
				/*System.out.println( "Looking up " + tmp_name );
				System.out.println( " compared to itself! = " + tmp_name.compareTo( tmp_name ) );
				System.out.println( "table" );
				for ( Iterator it = global_tmps.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry me = (Map.Entry)it.next();
					System.out.println( "key = " + me.getKey() + ", value = " + me.getValue() );
				}
				System.out.println( "/table" );*/
				Alert.unreachable();
			}
		} else {
			final String intn = (String)nme.getSymbol();
			final String nn = nme.getNickname();
			p = name_space.fetchPerm( nn, intn );
		}
		assert p != null;
		nme.setVar( p );
	}

	boolean isGlobal() {
		return true;
	}

}

