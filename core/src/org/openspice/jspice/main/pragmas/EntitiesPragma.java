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

import org.openspice.jspice.conf.JSpiceConf;

import java.util.List;
import java.util.Iterator;

public class EntitiesPragma {

	final JSpiceConf jspice_conf;

	public EntitiesPragma( JSpiceConf jspice_conf ) {
		this.jspice_conf = jspice_conf;
	}

	public void list( final List args ) {
		final List list = this.jspice_conf.listEntities( args.isEmpty() ? ".*" : (String)args.get( 0 ) );
		int count = 0;
		for ( Iterator it = list.iterator(); it.hasNext(); count++ ) {
			final String ent = (String)it.next();
			final StringBuffer b = new StringBuffer();
			b.append( count );
			b.append( '.' );
			while ( b.length() < 4 ) {
				b.append( ' ' );
			}
			b.append( ent );
			System.out.println( b );
		}
	}

}
