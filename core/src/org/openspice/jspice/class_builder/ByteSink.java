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
package org.openspice.jspice.class_builder;

import java.io.ByteArrayOutputStream;

public final class ByteSink extends ByteArrayOutputStream {

	static class Handle {

		final ByteSink ios;
		final int origin;

		public Handle( final ByteSink ios ) {
			this.ios = ios;
			this.origin = ios.count;
		}

		public final void copyBytes( final ByteSink b ) {
//			System.err.println( "copying " + b.count + " bytes into position " + this.origin );
//			System.err.println( "byte[0] " + b.buf[0] );
//			System.err.println( "byte[1] " + b.buf[1] );
//			System.err.println( "byte[2] " + b.buf[2] );
//			System.err.println( "byte[3] " + b.buf[3] );
			System.arraycopy( b.buf, 0, ios.buf, this.origin, b.count );
//			System.err.println( "byte[" + ( this.origin + 0 ) + "] " + ios.buf[this.origin + 0] );
//			System.err.println( "byte[" + (this.origin + 1) + "] " + ios.buf[this.origin + 1] );
//			System.err.println( "byte[" + (this.origin + 2) + "] " + ios.buf[this.origin + 2] );
//			System.err.println( "byte[" + (this.origin + 3) + "] " + ios.buf[this.origin + 3] );
		}

	}

	public Handle newHandle() {
		return new Handle( this );
	}

}


