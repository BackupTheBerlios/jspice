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

import org.openspice.jspice.alert.Alert;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.regex.Matcher;

public final class BindingList extends AbstractList {

	private final Binding head;
	private Matcher matcher;				//	null = matcher has been used.
	private BindingList tail = null;

	private boolean hasTail() {
//		System.err.println( "hasTail" );
		if ( this.matcher == null ) {
			return this.tail != null;
		} else {
			if ( this.matcher.find() ) {
				this.tail = new BindingList( this.matcher );
			}
			this.matcher = null;	//	null out matcher.
			return this.tail != null;
		}
	}

	public Object get( final int k ) {
//		System.err.println( "get" );
		BindingList list = this;
		int i = 0;
		while ( i < k ) {
			if ( list.hasTail() ) {
				list = list.tail;
				i += 1;
			} else {
				throw new Alert( "Index out of range" ).culprit( "index", new Integer( k ) ).mishap();
			}
		}
		return this.head;
	}

	public int size() {
//		System.err.println( "size" );
		BindingList list = this;
		int sz = 1;
		while ( list.hasTail() ) {
			sz += 1;
			list = this.tail;
		}
		return sz;
	}

	public Iterator iterator() {
//		System.err.println( "iterator" );
		return (
			new Iterator() {
				private BindingList list = BindingList.this;

				public boolean hasNext() {
					return list != null;
				}

				public Object next() {
					final Binding b = list.head;
					list = list.hasTail() ? list.tail : null;
					return b;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			}
		);
	}

	public BindingList( final Matcher m ) {
		this.matcher = m;
		this.head = new Binding( m );
//		System.err.println( "new" );
	}
}
