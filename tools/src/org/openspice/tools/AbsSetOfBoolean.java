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

import java.util.AbstractSet;
import java.util.Iterator;

public abstract class AbsSetOfBoolean extends AbstractSet implements SetOfBoolean {

	protected abstract int getState();

	public final boolean isEmpty() {
		return this.getState() == 0;
	}

	public final boolean contains( Object o ) {
		if ( ! ( o instanceof Boolean ) ) return false;
		final boolean b = ((Boolean)o).booleanValue();
		return ( this.getState() & ( b ? 2 : 1 ) ) != 0;
	}

	public final Iterator iterator() {
		switch ( this.getState() ) {
			case 0: return IteratorTools.make0();
			case 1: return IteratorTools.make1( Boolean.FALSE );
			case 2: return IteratorTools.make1( Boolean.TRUE );
			default: return IteratorTools.make2( Boolean.FALSE, Boolean.TRUE );
		}
	}

	public final int size() {
		switch ( this.getState() ) {
			case 0: return 0;
			case 3: return 2;
			default: return 1;
		}
	}

	public final boolean contains( boolean flag ) {
		return flag ? ( this.getState() & 2 ) != 0 : ( this.getState() & 1 ) != 0;
	}

	public boolean isFull() {
		return this.getState() == 3;
	}

}
