/*
 *  BFI
 *  Copyright (C) 2003 Thomas Cort
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
/**
 * Modified by Stephen Leach, June 2004 for inclusion within the JSpice
 * interpreter.
 *
 * All code must adhere to the JSpice coding standards!  This includes the
 * following intrusive changes:
 * 	1.	using final declarations where appropriate.
 * 	2.	always the JSpice error reporting conventions.
 * 	3.	laying out code properly.
 * 	4.	eliminate unused variables.
 * 	5.	REMOVE public access to should should be private methods.
 * 	6.	Putting "this." in front of all instance method calls.
 *
 * In addition, I generalized the stream handling to deal with more than
 * just stdin/stdout.
 */

package BFI;

import org.openspice.tools.FileTools;

import java.io.*;
import java.util.Arrays;

/**
 *  BFI - Compliant with The ENSI BrainFuck Language Specification v1.3, and
 *  also compliant with The Portable Brainfuck Specification.
 *  @author Thomas Cort (<A HREF="mailto:tom@tomcort.com">tom@tomcort.com</A>)
 *  @version 1.1 2003-03-16
 *  @since   1.0
 */
public final class BFI {

	final private int MAX_DATA_SIZE = 32767;
	final private byte[] x;

	private char[] c;
	private int p, pc, l;

	private final Reader input_stream;
	private final Writer output_stream;
	private final Writer error_stream;

	public BFI() {
		this( new InputStreamReader( System.in ), new OutputStreamWriter( System.out ), new OutputStreamWriter( System.err ) );
	}

	public BFI( final Reader in, final Writer out, final Writer err ) {
		this.input_stream = in;
		this.output_stream = out;
		this.error_stream = err;
		this.x = new byte[ MAX_DATA_SIZE + 1 ];
	}

	public void execute( final String s ) {
		this.c = s.toCharArray();
		Arrays.fill( this.x, (byte)0 );
		p = l = pc = 0;
		this.checkSyntax();
		this.interpret();
		try {
			this.output_stream.flush();
		} catch ( final IOException ex ) {
			this.errBF( "Cannot process file" );
		}
	}

	public void execute( final File file ) {
		this.execute( FileTools.fileAsString( file ) );
	}

	/**
	 *  Evaluates the current character in the Brainfuck program
	 *  @since 1.0
	 */
	private void interpret() {
		for ( pc = 0; pc < c.length; pc++ ) {
			switch ( c[ pc ] ) {
				case '>':
					this.incrementPointer();
					break;
				case '<':
					this.decrementPointer();
					break;
				case '+':
					this.incrementByteAtPointer();
					break;
				case '-':
					this.decrementByteAtPointer();
					break;
				case '.':
					this.output();
					break;
				case ',':
					this.input();
					break;
				case '[':
					this.startJump();
					break;
				case ']':
					this.endJump();
			}
//			if ( c[ pc ] == '>' ) {
//				this.incrementPointer();
//			} else if ( c[ pc ] == '<' ) {
//				this.decrementPointer();
//			} else if ( c[ pc ] == '+' ) {
//				this.incrementByteAtPointer();
//			} else if ( c[ pc ] == '-' ) {
//				this.decrementByteAtPointer();
//			} else if ( c[ pc ] == '.' ) {
//				this.output();
//			} else if ( c[ pc ] == ',' ) {
//				this.input();
//			} else if ( c[ pc ] == '[' ) {
//				this.startJump();
//			} else if ( c[ pc ] == ']' ) {
//				this.endJump();
//			}
		}
	}

	/**
	 *  Checks sytanx. Must be called explicitly before you call interpret() if
	 *  you want to check the syntax.
	 *  @since 1.0
	 */
	private void checkSyntax() {
		int ob = 0;
		int cb = 0;

		for ( int t = 0; t < c.length; t++ ) {
			if ( c[ t ] == '[' ) {
				ob++;
			} else if ( c[ t ] == ']' ) {
				cb++;
			}
		}

		if ( ob != cb ) {
			this.errBF( ob > cb ? "Missing closing bracket(s)" : "Missing opening bracket(s)" );
		}
	}

	/**
	 *  '>' Increment pointer.
	 *  @since 1.0
	 */
	private void incrementPointer() {
		if ( ++p > MAX_DATA_SIZE ) {
			this.errBF( "Pointer moved beyond MAX_DATA_SIZE: " + MAX_DATA_SIZE );
		}
	}

	/**
	 *  '<' Decrement pointer.
	 *  @since 1.0
	 */
	private void decrementPointer() {
		if ( --p < 0 ) {
			this.errBF( "Pointer moved to the left of the starting position" );
		}
	}

	/**
	 *  '+' Increments the value at the pointer.
	 *  @since 1.0
	 */
	private void incrementByteAtPointer() {
		if ( x[ p ] == 127 ) {
			this.errBF( "Byte incremented beyond capacity" );
		}
		++x[ p ];
	}

	/**
	 *  '-' Dencrements the value at the pointer.
	 *  @since 1.0
	 */
	private void decrementByteAtPointer() {
		if ( x[ p ] == -128 ) {
			this.errBF( "Byte decremented below capacity" );
		}
		--x[ p ];
	}

	/**
	 *  '[' Start of loop code block.
	 *  @since 1.0
	 */
	private void startJump() {
		if ( x[ p ] == 0 ) {
			pc++;
			while ( l > 0 || c[ pc ] != ']' ) {
				if ( c[ pc ] == '[' ) l++;
				if ( c[ pc ] == ']' ) l--;
				pc++;
			}
		}
	}

	/**
	 *  ']' End of loop code block.
	 *  @since 1.0
	 */
	private void endJump() {
		pc--;
		while ( l > 0 || c[ pc ] != '[' ) {
			if ( c[ pc ] == ']' ) l++;
			if ( c[ pc ] == '[' ) l--;
			pc--;
		}
		pc--;
	}

	/**
	 *  '.' converts current byte to ascii & prints it to STDOUT
	 *  @since 1.0
	 */
	private void output() {
		try {
			this.output_stream.write( x[ p ] );
		} catch ( final IOException e ) {
			this.errBF( "Cannot write to output stream" );
		}
	}

	/**
	 *  ',' reads a character from STDIN, and stores it at the current location
	 *  @since 1.0
	 */
	private void input() {
		try {
			x[ p ] = (byte)this.input_stream.read();
		} catch ( final IOException ex ) {
			this.errBF( "Input Parse Error" );
		}
	}

	/**
	 *  Error Handler
	 *  @param str - error message
	 *  @since 1.0
	 */
	private void errBF( final String str ) {
		try {
			this.error_stream.write( str );
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final void main( final String[] args ) {
		final BFI bfi = new BFI();
		bfi.execute( new File( "triangle.bf" ) );
	}

}