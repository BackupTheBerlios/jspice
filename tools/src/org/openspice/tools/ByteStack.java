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

public class ByteStack {
	
	private int used;
	private int capacity;
	private byte[] data;

	public ByteStack () {
		this.used = 0;
		this.capacity = 8;
		this.data = new byte[ this.capacity ];
	}

	public byte pop() {
		return this.data[ --this.used ];
	}

	public ByteStack push( final byte x ) {
		if ( this.used >= this.capacity ) {
			final int new_capacity = 2 * java.lang.Math.max( this.capacity, 8 );
			final byte[] new_data = new byte[ new_capacity ];
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

	public byte peek() {
		return this.data[ this.used - 1 ];
	}

	public int size() {
		return this.used;
	}

	public byte[] toByteArray() {
		final byte[] answer = new byte[ this.used ];
		System.arraycopy( this.data, 0, answer, 0, this.used );
		return answer;
	}

}


