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
package org.openspice.vfs.files;

import org.openspice.tools.CharArrayTools;

/**
 * todo: Must cope with / - that's forbidden in UNIX.  Might have to forbid lots of stuff.
 */
public class FileNameTools {

	public static final char esc_char = '%';
	public static final char forbidden_char = '/';

	//	%UUUU -> hex

	public static final String decode( final String s ) {
		if ( s.indexOf( esc_char ) == -1 ) {
			return s;
		}
		final StringBuffer buff = new StringBuffer();
		for ( int i = 0; i < s.length(); i++ ) {
			final char ch = s.charAt( i );
			if ( ch != esc_char ) {
				final char[] h = new char[ 4 ];
				for ( int j = 0; j < 4; j++ ) {
					h[ j ] = s.charAt( ++i );
				}
				buff.append( CharArrayTools.fromHex( h ) );
			}
		}
		return buff.toString();
	}

	/**
	 * Slightly inefficient.
	 * @param s the string to be encoded
	 * @param separator the separator character to be protected
	 * @return the encoded (partial) file name
	 */
	public static final String encode( final String s, final char separator ) {
		if ( s.indexOf( separator ) == -1 && s.indexOf( esc_char ) == -1 && s.indexOf( forbidden_char ) == -1 ) {
			return s;
		}
		final StringBuffer buff = new StringBuffer();
		for ( int i = 0; i < s.length(); i++ ) {
			final char ch = s.charAt( i );
			if ( ch == esc_char || ch == separator || ch == forbidden_char ) {
				final char[] hex = CharArrayTools.toHex( ch );
				buff.append( esc_char );
				buff.append( hex, 0, 4 );
			} else {
				buff.append( ch );
			}
		}
		return buff.toString();
	}

	public static final String makeFileName( final String nam, final char sep, final String ext ) {
		if ( ext == null ) {
			return encode( nam, sep );
		} else {
			return encode( nam, sep ) + sep + encode( ext, sep );
		}
	}

	public static final int find_separator( final String name, final char sep ) {
		return name.indexOf( sep );
	}

	public static final String extractNam( final String name, final char sep ) {
		final int n = find_separator( name, sep );
		return n == -1 ? name : name.substring( 0, n );
	}

	public static final String extractExt( final String name, final char sep ) {
		final int n = find_separator( name, sep );
		return n == -1 ? null : name.substring( n + 1 );
	}

}
