package tads2.jetty;

// console implementation of platform IO

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class GameTerminal implements PlatformIO {

	private BufferedReader _in;

	public GameTerminal() {
		_in = new BufferedReader( new InputStreamReader( System.in ) );
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
		System.out.print( s );
	}

	// prints an error message
	public void print_error( String s ) {
		System.err.println( "[" + s + "]" );
	}

	// ends the current line, shifts stuff up to accomodate a new line
	public void scroll_window() {
		System.out.println();
	}

	// tell the status line to have this string on it
	public void set_status_string( String s, boolean left ) {
		System.out.println( "[status " + ( left ? "left" : "right" ) + "] " + s );
	}

	// read a key from input
	public String read_key() {
		try {
			String txt = _in.readLine();
			if ( txt.equals( "" ) ) {
				return " ";
			} else {
				return txt.substring( 0, 1 );
			}
		} catch ( IOException ioe ) {
			return null;
		}
	}

	// note that this automatically calls scroll_window() after reading
	public String read_line() {
		try {
			return _in.readLine();
		} catch ( IOException ioe ) {
			return null;
		}
	}

	// returns true if the user hit space (and so it should scroll
	// a full page), false otherwise (so it should scroll a line)
	public boolean more_prompt( String prompt ) {
		System.out.println( prompt );
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
			System.out.println( "" );
		}
	}

	// set the formatting style of the main window font, to bold or italic
	public void set_style( int style, boolean turn_on ) {
		System.out.print( "[" );
		if ( style == BOLD ) {
			System.out.print( "bold" );
		} else if ( style == ITALIC ) {
			System.out.print( "italic" );
		} else {
			System.out.print( "??unknown style: " + style + "??" );
		}

		if ( turn_on ) {
			System.out.print( " on]" );
		} else {
			System.out.print( " off]" );
		}
	}
}
