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
package org.openspice.jspice.boxes;

import tads2.jetty.PlatformIO;
import tads2.jetty.OutputFormatter;
import tads2.jetty.Jetty;

import java.io.*;

import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VFile;

public class GameBox extends CmdBox implements PlatformIO {

	final VFile game_file;

	public GameBox( final VFile game_file ) {
		this.game_file = game_file;
	}

	public void goBuddyGo() {
		final VFile file = this.game_file;
		final Jetty j = new Jetty( this, file.inputStreamContents() );
		if ( j.load() ) {
			j.run();
		}
	}

	private final String input_line() {
		return this.getLine();
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
		this.print( s );
	}

	// prints an error message
	public void print_error( String s ) {
		this.println( "[" + s + "]" );
	}

	// ends the current line, shifts stuff up to accomodate a new line
	public void scroll_window() {
		this.println();
	}

	// tell the status line to have this string on it
	public void set_status_string( String s, boolean left ) {
		this.println( "[status " + ( left ? "left" : "right" ) + "] " + s );
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
		this.println( prompt );
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
			this.println( "" );
		}
	}

	// set the formatting style of the main window font, to bold or italic
	public void set_style( int style, boolean turn_on ) {
		this.print( "[" );
		if ( style == BOLD ) {
			this.print( "bold" );
		} else if ( style == ITALIC ) {
			this.print( "italic" );
		} else {
			this.print( "??unknown style: " + style + "??" );
		}

		if ( turn_on ) {
			this.print( " on]" );
		} else {
			this.print( " off]" );
		}
	}

}

