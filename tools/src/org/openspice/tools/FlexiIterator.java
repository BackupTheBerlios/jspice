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

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

public abstract class FlexiIterator extends ImmutableIterator {

	public abstract void expand( final List list );

	final LinkedList list = new LinkedList();

	protected FlexiIterator( final List list ) {
		this.addList( list );
	}

	public void addList( final List x ) {
		this.list.add( x );
	}

	public void addIterator( final Iterator x ) {
		this.list.add( x );
	}

	public void addObject( final Object x ) {
		this.list.add( IteratorTools.make1( x ) );
	}

	public Object next() {
		final Object x = list.get( 0 );
		if ( x instanceof Iterator ) {
			return ((Iterator)x).next();
		} else {
			this.expand( (List)x );
			return this.next();
		}
	}

	public boolean hasNext() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

}
