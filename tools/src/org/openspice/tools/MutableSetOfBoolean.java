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

import java.util.Collection;

public class MutableSetOfBoolean extends AbsSetOfBoolean implements SetOfBoolean {

	private int state;

	private MutableSetOfBoolean( final int state ) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	protected Object clone() throws CloneNotSupportedException {
		return new MutableSetOfBoolean( this.state );
	}

	public boolean removeAll( final Collection c ) {
		final int before = this.state;
		if ( c.contains( Boolean.FALSE ) ) {
			this.state &= 2;
		}
		if ( c.contains( Boolean.TRUE ) ) {
			this.state &= 1;
		}
		return this.state != before;
	}

	public boolean add( final Object o ) {
		if ( o instanceof Boolean ) {
			final int before = this.state;
			this.state |= ( ((Boolean)o).booleanValue() ? 2 : 1 );
			return before != this.state;
		} else {
			throw new UnsupportedOperationException();
		}

	}

	public void clear() {
		this.state = 0;
	}

	public boolean retainAll( Collection c ) {
		int after = 0;
		final int before = this.state;
		if ( c.contains( Boolean.FALSE ) ) {
			after |= 1;
		}
		if ( c.contains( Boolean.TRUE ) ) {
			after |= 2;
		}
		this.state = after;
		return before != after;
	}

	public boolean remove( final Object o ) {
		if ( ! ( o instanceof Boolean ) ) return false;
		final boolean b = ((Boolean)o).booleanValue();
		final int before = this.state;
		this.state &= b ? 1 : 2;
		return this.state != before;
	}

}
