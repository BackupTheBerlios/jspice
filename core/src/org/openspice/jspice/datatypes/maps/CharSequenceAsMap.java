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

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

public final class CharSequenceAsMap extends PseudoMap {

	private final CharSequence charseq;

	public CharSequenceAsMap( final CharSequence s ) {
		this.charseq = s;
	}

	public Object getObject() {
		return this.charseq;
	}

	public Set entrySet() {
		return new CharSequenceAsMapSet( this.charseq );
	}

	public Object get( final Object key ) {
		try {
			return new Character( this.charseq.charAt( ((Integer)key).intValue() ) );
		} catch ( final ClassCastException exn ) {
			return AbsentLib.ABSENT;
		}
	}

	public boolean isEmpty() {
		return this.charseq.length() == 0;
	}

	public int size() {
		return this.charseq.length();
	}

	static final class CharSequenceAsMapSet extends AbstractSet {
		final CharSequence charseq;

		CharSequenceAsMapSet( final CharSequence s ) {
			this.charseq = s;
		}

		public int size() {
			return this.charseq.length();
		}

		public Iterator iterator() {
			return(
				new Iterator() {
					private int n = 0;

					public boolean hasNext() {
						return this.n < charseq.length();
					}

					public Object next() {
						final Maplet ans = (
							new Maplet(
								new Integer( this.n + 1 ),					//	 Spice is 1-indexed.
								new Character( charseq.charAt( this.n ) )
							)
						);
						this.n += 1;
						return ans;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				}
			);
		}
	}
}
