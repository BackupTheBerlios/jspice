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
package org.openspice.jspice.datatypes.maps;

import org.openspice.jspice.lib.AbsentLib;
import org.openspice.jspice.datatypes.Maplet;

import java.util.List;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

public final class ListAsMap extends PseudoMap {
	private final List list;

	public ListAsMap( final List _list ) {
		this.list = _list;
	}

	public Object getObject() {
		return this.list;
	}

	public Set entrySet() {
		return new ListAsMapSet( this.list );
	}

	public Object get( final Object key ) {
		try {
			return this.list.get( ((Integer)key).intValue() );
		} catch ( final ClassCastException exn ) {
			return AbsentLib.ABSENT;
		}
	}

	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public int size() {
		return this.list.size();
	}

	static final class ListAsMapSet extends AbstractSet {
		final List list;

		ListAsMapSet( final List _list ) {
			this.list = _list;
		}

		public int size() {
			return this.list.size();
		}

		public Iterator iterator() {
			final Iterator it = this.list.iterator();

			return(
				new Iterator() {
					int n = 0;

					public boolean hasNext() {
						return it.hasNext();
					}

					public Object next() {
						return (
							new Maplet(
								new Integer( ++this.n ),	//	Spice is 1-indexed.
								it.next()
							)
						);
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				}
			);
		}
	}
}
