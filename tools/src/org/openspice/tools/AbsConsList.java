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
package org.openspice.tools;

import java.util.Iterator;
import java.util.List;
import java.util.AbstractList;

abstract class AbsConsList extends AbstractList {

	public static Iterator iterator( final List list ) {
		return(
			new FlexiIterator( list ) {
				public void expand( final List list ) {
					if ( list instanceof ConsToFrontList ) {
						final ConsToFrontList c = (ConsToFrontList)list;
						this.addObject( c.head );
						this.addList( c.tail );
					} else if ( list instanceof ConsToBackList ) {
						final ConsToBackList c = (ConsToBackList)list;
						this.addList( c.allbutfoot );
						this.addObject( c.foot );
					} else {
						this.addIterator( list.iterator() );
					}
				}
			}
		);
	}

}
