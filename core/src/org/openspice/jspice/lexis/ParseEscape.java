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

public abstract class ParseEscape {

	final JSpiceConf jspice_conf;

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
			final Character tmp = this.jspice_conf.decode( s );
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

	static private final String esc_in = "[]{}|*%?'\"`\\abnrstv";
	static private final String esc_ou = "[]{}|*%?'\"`\\\u0007\b\n\r \t\u000B";

	public final char parseEscape() {
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
		} else {
			throw this.alert( "Unexpected escape sequence in string" ).
			culprit( "sequence", "\\" + new Character( ch ).toString() ).
			mishap();
		}
	}

}