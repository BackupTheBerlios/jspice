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

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

public class ImmutableSetOfBoolean extends AbsSetOfBoolean implements SetOfBoolean {

	private final int state;

	public ImmutableSetOfBoolean( final int state ) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	protected final Object clone() throws CloneNotSupportedException {
		return new ImmutableSetOfBoolean( this.getState() );
	}


	public static final ImmutableSetOfBoolean NEITHER = new ImmutableSetOfBoolean( 0 );
	public static final ImmutableSetOfBoolean ONLY_FALSE = new ImmutableSetOfBoolean( 1 );
	public static final ImmutableSetOfBoolean ONLY_TRUE = new ImmutableSetOfBoolean( 2 );
	public static final ImmutableSetOfBoolean EITHER = new ImmutableSetOfBoolean( 3 );

}
