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

import org.openspice.jspice.boxes.CubbyHole;
import org.openspice.jspice.boxes.CommandBox;

import java.io.PrintWriter;
import java.io.StringWriter;

abstract class AbsCmdBox implements CommandBox {

	final CubbyHole incoming = new CubbyHole();
	final CubbyHole outgoing = new CubbyHole();
	final StringBuffer buffer;
	final PrintWriter print_writer;

	public AbsCmdBox() {
		final StringWriter w = new StringWriter();
		this.buffer = w.getBuffer();
		this.print_writer = new PrintWriter( w, true );
	}


	boolean is_shutdown = false;

	public void shutdown() {
		this.sync();
		this.is_shutdown = true;
	}

	public boolean isShutdown() {
		return this.is_shutdown;
	}

	protected final void sync() {
		this.outgoing.put( this.buffer.toString() );
		this.buffer.setLength( 0 );
	}

	public String getLine() {
		this.sync();
		return (String)incoming.get();
	}

	public final PrintWriter printWriter() {
		return this.print_writer;
	}

	//	----

	public void flush() {
		this.print_writer.flush();
	}

	public void close() {
		this.print_writer.close();
	}

	public boolean checkError() {
		return this.print_writer.checkError();
	}

	public void write( int c ) {
		this.print_writer.write( c );
	}

	public void write( char buf[], int off, int len ) {
		this.print_writer.write( buf, off, len );
	}

	public void write( char buf[] ) {
		this.print_writer.write( buf );
	}

	public void write( String s, int off, int len ) {
		this.print_writer.write( s, off, len );
	}

	public void write( String s ) {
		this.print_writer.write( s );
	}

	public void print( boolean b ) {
		this.print_writer.print( b );
	}

	public void print( char c ) {
		this.print_writer.print( c );
	}

	public void print( int i ) {
		this.print_writer.print( i );
	}

	public void print( long l ) {
		this.print_writer.print( l );
	}

	public void print( float f ) {
		this.print_writer.print( f );
	}

	public void print( double d ) {
		this.print_writer.print( d );
	}

	public void print( char s[] ) {
		this.print_writer.print( s );
	}

	public void print( String s ) {
		this.print_writer.print( s );
	}

	public void print( Object obj ) {
		this.print_writer.print( obj );
	}

	public void println() {
		this.print_writer.println();
	}

	public void println( boolean x ) {
		this.print_writer.println( x );
	}

	public void println( char x ) {
		this.print_writer.println( x );
	}

	public void println( int x ) {
		this.print_writer.println( x );
	}

	public void println( long x ) {
		this.print_writer.println( x );
	}

	public void println( float x ) {
		this.print_writer.println( x );
	}

	public void println( double x ) {
		this.print_writer.println( x );
	}

	public void println( char x[] ) {
		this.print_writer.println( x );
	}

	public void println( String x ) {
		this.print_writer.println( x );
	}

	public void println( Object x ) {
		this.print_writer.println( x );
	}
}
