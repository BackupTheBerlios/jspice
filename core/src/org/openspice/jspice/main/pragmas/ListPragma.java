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
package org.openspice.jspice.main.pragmas;

import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.Location;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.conf.FixedConf;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class ListPragma {

	//	This is all fairly yuck.  I should allow comments to be
	//	associated with variables - not just values.  Otherwise I
	//	cannot give a comment to a variable like environment_variable
	//	because it is an ordinary Map - or any assignable variable.

	private static final int mxlen = 64;

	private void summarize( final String name, final Object x ) {
		if ( x instanceof SpiceObject ) {
			System.out.println( ((SpiceObject)x).summary( name ) );
		} else {
			//	Otherwise we have a vanilla Java value.  Do our best.
			String s = PrintTools.showToString( x );
			if ( s.length() > mxlen ) {
				s = s.substring( 0, mxlen - 4 ) + " ...";
			}
			System.out.println( name + " = " + s );
		}
	}

	/**
	 * The arguments to this pragma should denote a package name
	 *
	 * @param current the current namespace
	 * @param args the package to list
	 */
	public void list( final NameSpace current, final List args ) {
		final String nsn = args.isEmpty() ? FixedConf.STD_LIB : (String)args.get( 0 );
		final NameSpace ns = current.getNameSpaceManager().get( nsn );

		//	Map< String, Var.Perm >
		int count = 1;
		for ( Iterator it = new TreeMap( ns.getLocalBindings() ).entrySet().iterator(); it.hasNext(); count++ ) {
			final Map.Entry me = (Map.Entry)it.next();
			final String name = (String)me.getKey();
			final Var.Perm perm = (Var.Perm)me.getValue();

			final Location loc = perm.getLocation();
			final Object value = loc.getValue();

			final String count_str = count + "";
			final int npad = 3 - count_str.length();
			System.out.print( count + "." );
			for ( int i = 0; i < npad; i++ ) {
				System.out.print( ' ' );
			}
			this.summarize( name, value );
		}
	}

}
