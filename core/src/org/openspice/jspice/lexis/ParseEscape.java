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
import org.openspice.jspice.conf.JSpiceConf;

import java.util.LinkedList;

public abstract class ParseEscape {

	final JSpiceConf jspice_conf;	//	Permitted to be null - but disables entity parsing.

	public ParseEscape( final JSpiceConf jspice_conf ) {
		this.jspice_conf = jspice_conf;
	}

	public JSpiceConf getJSpiceConf() {
		return this.jspice_conf;
	}

	public abstract char readChar( final char default_char );
	public abstract char readCharNoEOF();

	public final Alert alert( final String msg ) {
		return this.alert( msg, null );
	}

	//	Override this one.
	public Alert alert( final String complaint, final String explanation ) {
		throw new Alert( complaint, explanation ).mishap();
	}

	private char parseAfterEscAmp() {
		final StringBuffer b = new StringBuffer();
		char ch = this.readCharNoEOF();
		char answer;
		if ( ch == '#' ) {
			int radix = 10;
			ch = this.readCharNoEOF();
			if ( ch == 'x' ) {
				//	  hex
				ch = this.readCharNoEOF();
				radix = 16;
			}
			for(;;) {
				int n = Character.digit( ch, radix );
				if ( n == -1 ) break;	// end of file
				b.append( ch );
				ch = this.readChar( ' ' );
			}
			answer = (char)Integer.parseInt( b.toString(), radix );
		} else {
			for(;;) {
				if ( !Character.isLetter( ch ) ) break;
				b.append( ch );
				ch = this.readCharNoEOF();
			}
			final String s = b.toString().intern();
			final Character tmp = this.jspice_conf != null ? this.jspice_conf.decode( s ) : null;
			if ( tmp == null ) {
				new Alert(
					"Unrecognized HTML entity in string",
					"Not all entities are recognized yet"
				).
				culprit( "entity name", s ).
				mishap( 'T' );
			}
			answer = tmp.charValue();
		}
		if ( ch != ';' ) {
			this.alert(
				"Unexpected HTML entity sequence in string",
				"Entities are a sequence of letters terminated by a semi-colon"
			).
			culprit( "entity", "&" + b.toString() + ";" ).mishap();
		}
		return answer;
	}

	final char parseNumber( final int radix, final int count ) {
		final StringBuffer b = new StringBuffer();
		for ( int i = 0; i < count; i++ ) {
			b.append( this.readCharNoEOF() );
		}
		final int nchar = Integer.parseInt( b.toString(), radix );
		return (char)nchar;
	}

	static private final String esc_in = "[]{}|*%?'\"`\\abnrstv";
	static private final String esc_ou = "[]{}|*%?'\"`\\\u0007\b\n\r \t\u000B";

	public final char parseEscape() throws ParseEscapeException {
		final char ch = this.readCharNoEOF();
		final int offset = esc_in.indexOf( ch );
		if ( offset >= 0 ) {
			return esc_ou.charAt( offset );
		} else if ( ch == '&' ) {
			//	Character entities.
			return this.parseAfterEscAmp();
		} else if ( ch == '^' ) {
			//	Control characters.
			final char nch = this.readCharNoEOF();
			if ( 64 <= nch && nch <= 95 ) {
				return (char)( nch - 64 );
			} else {
				throw this.alert( "Unexpected escape sequence in string" ).
				culprit( "sequence", "\\^" + new Character( nch ).toString() ).
				mishap();
			}
		} else if ( ch == '0' ) {
			final char nch = this.readCharNoEOF();
			if ( nch != 'x' ) {
				throw this.alert( "Unexpected escape sequence in string" ).
				culprit( "sequence", "\\0" + new Character( nch ).toString() ).
				mishap();
			}
			return this.parseNumber( 16, 2 );
		} else if ( ch == 'u' ) {
			return this.parseNumber( 16, 4 );
		} else if ( ch == '(' ) {
			final CharSequence cs = this.parseExpr();
			throw new ParseEscapeException( cs );
		} else {
			throw this.alert( "Unexpected escape sequence in string" ).
			culprit( "sequence", "\\" + new Character( ch ).toString() ).
			mishap();
		}
	}

	private final CharSequence parseExpr() {
		final StringBuffer buff = new StringBuffer();
		int state = 0;
		int paren_level = 0;
		final LinkedList nesting_quotes = new LinkedList();	//	List<Character>
		char quote = ' ';	//	doesn't matter
		for(;;) {
			final char ch = this.readCharNoEOF();
			if ( ch == '\n' || ch == '\r' ) new Alert( "Line break before end of string" ).mishap();
			switch ( state ) {
				case 0:			//	outer level
					switch ( ch ) {
						case '"':
						case '\'':
						case '`':
							state = 1;
							quote = ch;
							break;
						case ')':
							if ( paren_level == 0 ) {
								if ( nesting_quotes.isEmpty() ) {
									return buff.toString();
								} else {
									quote = ((Character)nesting_quotes.removeLast()).charValue();
									state = 1;
								}
							} else {
								paren_level -= 1;
							}
							break;
						case '(':
							paren_level += 1;
							break;
						default:
							break;
					}
					break;
				case 1:			//	string level
					if ( ch == quote ) {
						state = 0;
					} else if ( ch == '\\' ) {
						state = 2;
					}
					break;
				case 2:			//	after backquote in string
					int count = 0;		//	throw this many away - not very robust!
					switch ( ch ) {
						case '(':
							state = 0;
							nesting_quotes.addLast( new Character( quote ) );
							break;
						case '^':
							count = 1;	//	A
							break;
						case '0':
							count = 3;	//	xFF
							break;
						case 'u':
							count = 4;	//	FFFF
							break;
					}
					for ( int i = 0; i < count; i++ ) {
						final char x = this.readCharNoEOF();
						if ( !Character.isLetterOrDigit( ch ) ) {
							new Alert( "Unexpected backquote sequence" ).culprit( "char", x +"" ).warning();
						}
					}
					break;
				default:
					break;
			}
			buff.append( ch );
		}
	}

}