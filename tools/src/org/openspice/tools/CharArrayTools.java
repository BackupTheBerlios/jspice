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

public class CharArrayTools {

	public static final char[] hex_digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static final char[] toHex( final char ch ) {
		final char[] h = new char[ 4 ];
		h[0] = hex_digits[ ch & 0xF ];
		h[1] = hex_digits[ ( ch >> 8 ) & 0xF ];
		h[2] = hex_digits[ ( ch >> 16 ) & 0xF ];
		h[3] = hex_digits[ ( ch >> 24 ) & 0xF ];
		return h;
	}

	public static final char fromHex( final char[] h ) {
		return (char)(
			( h[ 0 ] - '0' ) +
			( ( h[ 1 ] - '0' ) << 8 ) +
			( ( h[ 1 ] - '0' ) << 16 ) +
			( ( h[ 1 ] - '0' ) << 24 )
		);
	}

	private static char[] CBUF = null;

	public static final char[] tmpBuffer() {
		if ( CBUF == null ) {
			CBUF = new char[ 1 << 13 ];
		}
		return CBUF;
	}

}
