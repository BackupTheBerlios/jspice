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
package tads2;

import tads2.jetty.PlatformIO;
import tads2.jetty.OutputFormatter;

import java.io.*;

public class GameTerminalBox implements PlatformIO {

	final private BufferedReader _in;
	final private PrintStream _out;
	final private PrintStream _err;

	public GameTerminalBox( final InputStream sysin, final PrintStream sysout, final PrintStream syserr ) {
		_in = new BufferedReader( new InputStreamReader( sysin ) );
		_out = sysout;
		_err = syserr;
	}

	private final String input_line() {
		try {
			return this._in.readLine();
		} catch ( final IOException e ) {
			return null;					//	This cannot be correct: just copying original code.
		}
	}

	public void set_out( OutputFormatter out ) {
		// give it a size right off, then we're done with it
		out.resize( 78, 50 );
	}

	public int size_text( char c ) {
		return 1;
	}

	public int size_text( String s ) {
		return s.length();
	}

	// prints the string (advancing the print cursor as necessary, but not
	// line-wrapping)
	public void print_text( String s ) {
		this._out.print( s );
	}

	// prints an error message
	public void print_error( String s ) {
		this._err.println( "[" + s + "]" );
	}

	// ends the current line, shifts stuff up to accomodate a new line
	public void scroll_window() {
		this._out.println();
	}

	// tell the status line to have this string on it
	public void set_status_string( String s, boolean left ) {
		this._out.println( "[status " + ( left ? "left" : "right" ) + "] " + s );
	}

	// read a key from input
	public String read_key() {
		final String txt = this.input_line();
		if ( txt == null || txt.equals( "" ) ) {
			return " ";
		} else {
			return txt.substring( 0, 1 );
		}
	}

	// note that this automatically calls scroll_window() after reading
	public String read_line() {
		return this.input_line();
	}

	// returns true if the user hit space (and so it should scroll
	// a full page), false otherwise (so it should scroll a line)
	public boolean more_prompt( String prompt ) {
		this._out.println( prompt );
		String k = null;
		while ( k == null ) {
			k = read_key();

			// it's impossible to read a return in terminal mode, sorry
			if ( !k.equals( " " ) ) {
				k = null;
			}
		}

		return true;
	}

	public void clear_screen() {
		for ( int i = 0; i < 20; i++ ) {
			this._out.println( "" );
		}
	}

	// set the formatting style of the main window font, to bold or italic
	public void set_style( int style, boolean turn_on ) {
		this._out.print( "[" );
		if ( style == BOLD ) {
			this._out.print( "bold" );
		} else if ( style == ITALIC ) {
			this._out.print( "italic" );
		} else {
			this._out.print( "??unknown style: " + style + "??" );
		}

		if ( turn_on ) {
			this._out.print( " on]" );
		} else {
			this._out.print( " off]" );
		}
	}

}

