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
package org.openspice.jspice.datatypes;

import java.util.regex.Matcher;

public class Binding extends ImmutableList {

	static private final String[] groups( final Matcher m ) {
		final int n = m.groupCount() + 1;
		final String[] g = new String[ n ];
		for ( int i = 0; i < n; i++ ) {
			g[ i ] = m.group( i );
		}
		return g;
	}

	final int[] starts;
	final int[] ends;

	public Binding( final Matcher m ) {
		super( groups( m ) );

		final int n = m.groupCount() + 1;
		this.starts = new int[ n ];
		this.ends = new int[ n ];
		for ( int i = 0; i < n; i++ ) {
			this.starts[ i ] = m.start( i );
			this.ends[ i ] = m.end( i );
		}

	}

}
