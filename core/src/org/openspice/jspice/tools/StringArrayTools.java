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
package org.openspice.jspice.tools;

import java.util.Map;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

public final class StringArrayTools {

	public static final Map argvToMap( final String[] argv ) {
		final Map properties = new Hashtable();
		for ( int i = 0; i < argv.length; i++ ) {
			try {
				final StringTokenizer parser = new StringTokenizer( argv[ i ], "=" );
				final String name = parser.nextToken().toString();
				final String value = parser.nextToken( "\"" ).toString().substring( 1 );
				properties.put( name, value );
			} catch ( final NoSuchElementException e ) {
				throw new RuntimeException( e );
			}
		}
		return properties;
	}

}
