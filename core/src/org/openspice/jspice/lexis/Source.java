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

package org.openspice.jspice.lexis;

import org.openspice.jspice.alert.Alert;

import java.io.*;



public class Source extends SourceCore implements SourceIntf, org.openspice.jspice.tools.HasPrintName {
	private int[] buffer;
	private int count;

	Source( final String _origin, final Reader _reader, final String _prompt ) {
		super( _origin, _reader, _prompt );
		this.buffer = new int[ 0 ];
		this.count = 0;
	}

	public void pushInt( final int ich ) {
		if ( this.count >= this.buffer.length ) {
			final int[] new_buffer = new int[ 2 * this.buffer.length + 8 ];
			for ( int i = 0; i < this.count; i++ ) {
				new_buffer[ i ] = buffer[ i ];
			}
			this.buffer = new_buffer;
		}
		this.buffer[ this.count++ ] = ich;
	}

	public int readInt() {
		final int ich = this.count == 0 ? this.rawReadInt() : this.buffer[ --this.count ];
		return ich;
	}

	public char readCharNoEOF() {
		final int ch = readInt();
		if ( ch == -1 ) {
			new Alert( "Unexpected end of file" ).mishap( 'T' );
		}
		return (char)ch;
	}

	public char readChar( final char default_char ) {
		final int ch = readInt();
		return ch == -1 ? default_char : (char)ch;
	}

	private boolean tryRead( final String s, final int n ) {
		if ( n >= s.length() ) {
			return true;
		}
		final int ich = this.readInt();
		if ( ich == s.charAt( n ) && tryRead( s, n + 1 ) ) {
			return true;
		}
		this.pushInt( ich );
		return false;
	}

    public boolean tryRead( final String s ) {
        return tryRead( s, 0 );
    }
}
