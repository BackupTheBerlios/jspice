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
package org.openspice.jspice.conf;

import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * This is a special purpose tokenizer for parsing Apache-like
 * configuration lines.  Its key qualities are that it supports end
 * of line comments and skips blank lines.
 *
 * The algorithm relies heavily on the fact that Reader permits
 * repeated calls after the end of stream, returning -1.
 */
public class ConfTokenizer {

	final StringBuffer buffer = new StringBuffer();
	final Reader reader;

	public ConfTokenizer( final Reader reader ) {
		this.reader = reader;
	}

	public static final int MIDDLE_OF_LINE = 0;
	public static final int END_OF_LINE = 1;
	public static final int END_OF_FILE = 2;

	/**
	 * May add a single String to the input list and returns
	 * the exit conditions.
	 * @param list the list to add to
	 * @return the exit condition
	 * @throws IOException
	 */
	public int doNext( final List list ) throws IOException {
		this.buffer.setLength( 0 );

		//	Skip whitespace.
		for (;;) {
			final int ich = this.reader.read();
			if ( ich == -1 ) return END_OF_FILE;
			final char ch = (char)ich;
			if ( ch == '#' ) {
				for (;;) {
					final int ich2 = this.reader.read();
					if ( ich2 == -1 ) return END_OF_FILE;
					if ( ich2 == '\n' ) return END_OF_LINE;
				}
			} else if ( ch == '\n' ) {
				return END_OF_LINE;
			} else if ( ! Character.isWhitespace( ch ) ) {
				this.buffer.append( ch );
				break;
			}
		}

		//	If you have got here, buffer.length >= 1.
		for (;;) {
			final int ich = this.reader.read();
			if ( ich == -1 ) {
				list.add( this.buffer.toString() );
				return END_OF_FILE;
			}
			final char ch = (char)ich;
			if ( Character.isWhitespace( ch ) ) {
				list.add( this.buffer.toString() );
				return ch == '\n' ? END_OF_LINE : MIDDLE_OF_LINE;
			}
			this.buffer.append( ch );
		}
	}

	//	EITHER returns null OR adds one or more items to the list.
	public List next( final List list ) {
		final int before = list.size();
		try {
			for (;;) {
				final int condition = this.doNext( list );
				if ( condition != MIDDLE_OF_LINE ) {
					if ( list.size() > before ) return list;
					if ( condition == END_OF_FILE ) {
						return null;
					}
				}
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public List next() {
		return this.next( new ArrayList() );
	}

}
