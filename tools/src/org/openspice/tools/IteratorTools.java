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

	public static Iterator make0() {
		return new ZeroShotIterator();
	}

	public static Iterator make1( final Object x ) {
		return new OneShotIterator( x );
	}

}