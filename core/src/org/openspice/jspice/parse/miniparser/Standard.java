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
package org.openspice.jspice.parse.miniparser;

import java.util.Hashtable;
import java.util.List;
import java.util.Enumeration;

public class Standard extends Table {
	private final Hashtable table = new Hashtable();
	private MiniParser default_mini_parser = null;

	public Standard( final MiniParser _default_mini_parser ) {
		this.default_mini_parser = _default_mini_parser;
	}

	public MiniParser get( final String key ) {
		return (MiniParser)this.table.get( key );
	}

	public MiniParser lookup( final String key ) {
		final MiniParser m = (MiniParser)this.table.get( key );
		return m == null ? this.default_mini_parser : m;
	}

	public void put( final String key, final MiniParser value ) {
		this.table.put( key, value );
	}

	public void findCompletions( final String prefix, final List list ) {
		for ( Enumeration it = this.table.keys(); it.hasMoreElements(); ) {
			final String key = (String)it.nextElement();
			if ( key.startsWith( prefix ) ) {
//				System.err.println( "key = " + key );
				list.add( key );
			}
		}
	}

}
