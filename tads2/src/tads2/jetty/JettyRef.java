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
package tads2.jetty;

import java.util.Vector;

/**
 * Because nearly all classes need access to the Jetty objects (because it
 * acts like an environment) they almost all inherit from JettyRef.  For
 * the convenience of altering Dan's original code, I have arranged that
 * there are lots of delegate methods - so that JettyRef's can do i/o
 * without having to do getIn() or getOut() first.
 */
public class JettyRef {

	final private Jetty jetty;

	public JettyRef( Jetty jetty ) {
		this.jetty = jetty;
	}

	public Jetty getJetty() {
		return jetty;
	}

	//	---oooOOOooo---
	//	Allocate new TValue without having to explicitly pass in a Jetty object.

	public TValue newTValue( final int type, final int value ) {
		return new TValue( this.jetty, type, value );
	}

	public TValue newTValue( final int type, final Vector value ) {
		return new TValue( this.jetty, type, value );
	}

	public TValue newTValue( final int type, final String value ) {
		return new TValue( this.jetty, type, value );
	}

	public TValue newTValue( final int type, final byte[] bs ) {
		return new TValue( this.jetty, type, bs );
	}


	public TValue newTValue( final int type, final byte[] bs, int x, int y ) {
		return new TValue( this.jetty, type, bs, x, y );
	}

	//	---oooOOOooo---

	// just helper thingys to let you call eval and call without a prebuilt
	// array. probably I should just provide overloaded versions of those,
	// but that seems a bit less clear.

	public TValue[] arg_array() {
		return new TValue[ 0 ];
	}

	public TValue[] arg_array( final TValue t1 ) {
		TValue[] args = new TValue[ 1 ];
		args[ 0 ] = ( t1 == null ) ? this.newTValue( TValue.NIL, 0 ) : t1;
		return args;
	}

	public TValue[] arg_array( final TValue t1, final TValue t2 ) {
		TValue[] args = new TValue[ 2 ];
		args[ 0 ] = ( t1 == null ) ? this.newTValue( TValue.NIL, 0 ) : t1;
		args[ 1 ] = ( t2 == null ) ? this.newTValue( TValue.NIL, 0 ) : t2;
		return args;
	}

	public TValue[] arg_array( final TValue t1, final TValue t2, final TValue t3 ) {
		TValue[] args = new TValue[ 3 ];
		args[ 0 ] = ( t1 == null ) ? this.newTValue( TValue.NIL, 0 ) : t1;
		args[ 1 ] = ( t2 == null ) ? this.newTValue( TValue.NIL, 0 ) : t2;
		args[ 2 ] = ( t3 == null ) ? this.newTValue( TValue.NIL, 0 ) : t3;
		return args;
	}

	public TValue[] arg_array( final TValue t1, final TValue t2, final TValue t3, final TValue t4 ) {
		TValue[] args = new TValue[ 4 ];
		args[ 0 ] = ( t1 == null ) ? this.newTValue( TValue.NIL, 0 ) : t1;
		args[ 1 ] = ( t2 == null ) ? this.newTValue( TValue.NIL, 0 ) : t2;
		args[ 2 ] = ( t3 == null ) ? this.newTValue( TValue.NIL, 0 ) : t3;
		args[ 3 ] = ( t4 == null ) ? this.newTValue( TValue.NIL, 0 ) : t4;
		return args;
	}

	public TValue[] arg_array( final TValue t1, final TValue t2, final TValue t3, final TValue t4, final TValue t5 ) {
		TValue[] args = new TValue[ 5 ];
		args[ 0 ] = ( t1 == null ) ? this.newTValue( TValue.NIL, 0 ) : t1;
		args[ 1 ] = ( t2 == null ) ? this.newTValue( TValue.NIL, 0 ) : t2;
		args[ 2 ] = ( t3 == null ) ? this.newTValue( TValue.NIL, 0 ) : t3;
		args[ 3 ] = ( t4 == null ) ? this.newTValue( TValue.NIL, 0 ) : t4;
		args[ 4 ] = ( t5 == null ) ? this.newTValue( TValue.NIL, 0 ) : t5;
		return args;
	}

	//	---oooOOOooo---
	//	Create a new ObjectMatch without having to explicitly pass in a Jetty.


	public ObjectMatch newObjectMatch( final int size ) {
		return new ObjectMatch( this.jetty, size, 1, null );
	}

	public ObjectMatch newObjectMatch( final int size, final int count, final Vector objects ) {
		return new ObjectMatch( this.jetty, size, count, objects );
	}


	//	---oooOOOOoo---
	//	Shortcuts for accessing the various components of the delegated Jetty.

	public GameState getState() {
		return jetty.getState();
	}

	public OutputFormatter getOut() {
		return jetty.getOut();
	}

	public InputHandler getIn() {
		return jetty.getIn();
	}

	public Parser getParser() {
		return jetty.getParser();
	}

	public Simulator getSimulator() {
		return jetty.getSimulator();
	}

	public CodeRunner getRunner() {
		return jetty.getRunner();
	}

	public ParserError getPerror() {
		return jetty.getPerror();
	}


	//	---oooOOOooo---
	//	Delegate methods for debug level

	public int get_debug_level() {
		return this.jetty._debug_level;
	}

	public void set_debug_level( final int d ) {
		this.jetty._debug_level = d;
	}

	//	---oooOOOooo---
	//	Delegate methods for i/o

	public void print( final String text ) throws ParseException, ReparseException, HaltTurnException, GameOverException {
		this.getOut().print( text );
	}

	// switch into or out of statusline-printing mode
	// (and when we switch out, actually send the updated status line to the
	// screen)
	public void print_statusline( final boolean start, final boolean left ) {
		this.getOut().print_statusline( start, left );
	}

	// note that it can be relatively expensive to construct the string argument
	// to this function, even if it's not printed. For higher debug levels
	// (3 and 4) you probably want to make sure this function is never
	// called, not just that it doesn't do anything. so wrap the call in an
	// actual if statement.
	public void print_error( final String text, final int level ) {
		this.getOut().print_error( text, level );
	}

	// wrap if necessary and print out what we've got so far
	public void flush() {
		this.getOut().flush();
	}

	public int hide_output() {
		return this.getOut().hide_output();
	}

	public boolean unhide_output( final int x ) {
		return this.getOut().unhide_output( x );
	}

	public void clear_screen() {
		this.getOut().clear_screen();
	}

	public int capture_output() {
		return this.getOut().capture_output();
	}

	public String uncapture_output( final int x ) {
		return this.getOut().uncapture_output( x );
	}

	public void print_more_prompt() throws ParseException, ReparseException, HaltTurnException, GameOverException {
		this.getOut().print_more_prompt();
	}

	public void set_filter( final TObject filter ) {
		this.getOut().set_filter( filter );
	}

	// abort all output hiding/capturing
	public void cancel_outhiding() {
		this.getOut().cancel_outhiding();
	}

	// reset the column marker to start of line and clear the line count
	public void input_entered() {
		this.getOut().input_entered();
	}

	public String read_key() {
		return this.getIn().read_key();
	}

	public String read_line() {
		return this.getIn().read_line();
	}



}
