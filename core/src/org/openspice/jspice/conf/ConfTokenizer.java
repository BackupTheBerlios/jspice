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

import org.openspice.jspice.lexis.ParseEscape;
import org.openspice.jspice.lexis.ParseEscapeException;
import org.openspice.jspice.alert.Alert;

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
public class ConfTokenizer extends ParseEscape {

	private final StringBuffer buffer = new StringBuffer();
	private final Reader reader;

	public ConfTokenizer( final Reader reader ) {
		super( null );			//	disables entity parsing.
		this.reader = reader;
	}

	private static final int MIDDLE_OF_LINE = 0;
	private static final int END_OF_LINE = 1;
	private static final int END_OF_FILE = 2;

	public char readChar( char default_char ) {
		try {
			final int ich = this.reader.read();
			return ich == -1 ? default_char : (char)ich;
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public char readCharNoEOF() {
		try {
			final int ich = this.reader.read();
			if ( ich == -1 ) {
				throw new Alert( "Unexpected end of input" ).mishap();
			}
			return (char)ich;
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * May add a single String to the input list and returns
	 * the exit conditions.
	 * @param list the list to add to
	 * @return the exit condition
	 * @throws IOException
	 */
	private int doNext( final List list ) throws IOException {
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

		//	When/if you have got here, buffer.length == 1.
		assert buffer.length() == 1;

		final char quote_char = this.buffer.charAt( 0 );
		final boolean quoted = quote_char == '"';
		if ( quoted ) {
			this.buffer.setLength( 0 );
		}
		for (;;) {
			final int ich = this.reader.read();
			if ( ich == -1 ) {
				list.add( this.buffer.toString() );
				return END_OF_FILE;
			}
			char ch = (char)ich;
			if ( ch == '\\' ) {
				try {
					this.buffer.append( this.parseEscape() );
				} catch ( ParseEscapeException e ) {
					throw new Alert( "Attempting to use forbidden escape sequence '\\('" ).mishap();
				}
			} else if ( quoted && ch == quote_char || !quoted && Character.isWhitespace( ch ) ) {
				list.add( this.buffer.toString() );
				return ch == '\n' ? END_OF_LINE : MIDDLE_OF_LINE;
			} else {
				this.buffer.append( ch );
			}
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
