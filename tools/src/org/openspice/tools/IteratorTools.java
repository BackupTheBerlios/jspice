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

public class IteratorTools {

	static abstract class SimpleAbstractIterator implements Iterator {

		public abstract boolean hasNext();
		public abstract Object next();

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	static class ZeroShotIterator extends SimpleAbstractIterator {
		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new IllegalStateException();
		}
	}

	static class OneShotIterator extends SimpleAbstractIterator {
		private boolean has_next = true;
		private Object object;

		OneShotIterator( final Object _object ) {
			this.object = _object;
		}

		public boolean hasNext() {
			return this.has_next;
		}

		//	We null out the fields that have been used to permit early garbage collection.
		public Object next() {
			final Object ans = this.object;
			this.object = null;
			this.has_next = false;
			return ans;
		}
	}

	static class TwoShotIterator extends SimpleAbstractIterator {

		private int n = 0;
		private Object x;
		private Object y;

		public TwoShotIterator( final Object x, final Object y ) {
			this.x = x;
			this.y = y;
		}

		public boolean hasNext() {
			return n < 2;
		}

		public Object next() {
			if ( n == 0 ) {
				final Object z = x;
				this.x = null;
				n += 1;
				return z;
			} else if ( n == 1 ) {
				final Object z = y;
				this.y = null;
				n += 1;
				return z;
			} else {
				return null;	//	or throw an error? todo:
			}
		}

	}

	public static final ZeroShotIterator ZERO_SHOT_ITERATOR = new ZeroShotIterator();

	public static Iterator make0() {
		return ZERO_SHOT_ITERATOR;
	}

	public static Iterator make1( final Object x ) {
		return new OneShotIterator( x );
	}

	public static Iterator make2( final Object x, final Object y ) {
		return new TwoShotIterator( x, y );
	}

	static class MultiIterator implements Iterator {

		final Iterator itit;
		Iterator it = ZERO_SHOT_ITERATOR;

		public MultiIterator( final Iterator itit ) {
			this.itit = itit;
		}

		public boolean hasNext() {
			if ( this.it.hasNext() ) return true;
			if ( !this.itit.hasNext() ) return false;
			this.it = (Iterator)itit.next();
			return this.hasNext();
		}

		public Object next() {
			return this.it.next();
		}

		public void remove() {
			this.it.remove();
		}

	}

}