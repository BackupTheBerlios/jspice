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

import java.util.*;

public class ImmutableList extends AbstractList {

    private Object[] array;
	
	private final static Object[] nullArray = new Object[ 0 ];

    public ImmutableList( final Object[] data ) {
        this.array = data;
    }

    public Object get( final int index ) {
        return this.array[ index ];
    }

    public int size() {
        return this.array.length;
    }
	
	public static class Maker {
		private int n = 0;
		private LinkedList list = new LinkedList();
		
		public Maker addLast( final Object object ) {
			this.n += 1;
			this.list.addLast( object );
			return this;
		}
		
		public Maker addFirst( final Object object ) {
			this.n += 1;
			this.list.addFirst( object );
			return this;
		}
		
		public ImmutableList make() {
			if ( this.n == 0 ) {
				return EMPTY_LIST;
			} else {
				return new ImmutableList( this.list.toArray() );
			}
		}
	}

	public static final ImmutableList EMPTY_LIST = new ImmutableList( nullArray );

}
