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

public class IntStack {
	private int used;
	private int capacity;
	private int[] data;

	public IntStack() {
		this.used = 0;
		this.capacity = 8;
		this.data = new int[ this.capacity ];
	}

	public int pop() {
		return this.data[ --this.used ];
	}

	public IntStack push( final int x ) {
		if ( this.used >= this.capacity ) {
			final int new_capacity = 2 * java.lang.Math.max( this.capacity, 8 );
			final int[] new_data = new int[ new_capacity ];
			java.lang.System.arraycopy( data, 0, new_data, 0, this.used );
			this.data = new_data;
			this.capacity = new_capacity;
		}
		assert this.used < this.capacity;
		this.data[ this.used++ ] = x;
		return this;
	}

	public boolean isEmpty() {
		return this.used == 0;
	}

	public int peek() {
		return this.data[ this.used - 1 ];
	}

	public int size() {
		return this.used;
	}

	public int[] toIntArray() {
		final int[] answer = new int[ this.used ];
		System.arraycopy( this.data, 0, answer, 0, this.used );
		return answer;
	}

}

