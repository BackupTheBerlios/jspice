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
package org.openspice.jspice.main.jline_stuff;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;

public class PrefixFilterAccumulator {

	final String prefix;
	final List acc_list;

	public PrefixFilterAccumulator( final String prefix, final List acc_list ) {
		this.prefix = prefix;
		this.acc_list = acc_list;
	}

	public void add( final String candidate ) {
//		System.err.println( "does '" + candidate + "' start with '" + this.prefix + "' = " +  candidate.startsWith( this.prefix ) );
		if ( candidate.startsWith( this.prefix ) ) {
			this.acc_list.add( candidate );
		}
	}

	public void addAll( final Collection collection ) {
		for ( Iterator it = collection.iterator(); it.hasNext(); ) {
			final String c = (String)it.next();
			this.add( c );
		}
	}

}
