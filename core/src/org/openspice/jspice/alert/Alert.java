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

package org.openspice.jspice.alert;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

class AlertBase {

	private int index = 0;								//	index into list of culprits.
	private LinkedList culprits = new LinkedList();

	protected void add( final Culprit culprit ) {
		this.culprits.add( this.index++, culprit );
	}

	protected Iterator culprit_iterator() {
		return this.culprits.iterator();
	}

	public void resetIndex() {
		this.index = 0;
	}

}

public final class Alert extends AlertBase {

	//	---- Different style for raising error messages ----
	//	new Alert( snafu, ok ).culprit( MSG, OBJ )....mishap();

	final Throwable cause;
	final String complaint;
	final String explanation;

//	int index = 0;									//	index into list of culprits.
//	LinkedList culprits = new LinkedList(); 		//new CulpritGroup();

	public Alert( final Throwable t, final String _complaint, final String _explanation, final char phase ) {
		this.cause = t;
		this.complaint = _complaint;
		this.explanation = _explanation;
		this.setPhase( phase );
	}

	public Alert( final Throwable t, final String _complaint ) {
		this( t, _complaint, null, '-' );
	}

	public Alert( final Throwable t, final String _complaint, final String _explanation ) {
		this( t, _complaint, _explanation, '-' );
	}

	public Alert( final String _complaint, final String _explanation, final char phase ) {
		this( null, _complaint, _explanation, phase );
	}

	public Alert( final String _snafu_message, final String _ok_message ) {
		this( _snafu_message, _ok_message, '-' );
	}

	public Alert( final String _snafu_message, final char phase ) {
		this( _snafu_message, null, phase );
	}

	public Alert( final String _snafu_message ) {
		this( _snafu_message, '-' );
	}

	String getComplaint() {
		return this.complaint;
	}

	Throwable getCause() {
		return this.cause;
	}

	private void setPhase( final char phase ) {
		final String p = (
			phase == 'T' ? "Tokenization" :
			phase == 'P' ? "Parsing" :
			phase == 'H' ? "Parsing head of define" :
			phase == 'R' ? "Resolving names" :
			phase == 'I' ? "Importing" :
			phase == 'G' ? "Generating code" :
			phase == 'E' ? "Evaluation" :
			null
		);
		if ( p != null ) {
			this.add( new Culprit( "Phase", p, false, true ) );
		}
	}

	public Alert culprit( final String desc, final Object arg ) {
		this.add( new Culprit( desc, arg ) );
		return this;
	}

	public Alert culprit( final String desc, final int arg ) {
		return this.culprit( desc, new Integer( arg ) );
	}

	private int arg_count = 1;
	public Alert culprit_list( final List list ) {
		for ( Iterator it = list.iterator(); it.hasNext(); ) {
			this.culprit( "arg(" + this.arg_count++ + ")", it.next() );
		}
		return this;
	}

	public Alert hint( final String hint_text ) {
		this.add( Culprit.hint( hint_text ) );
		return this;
	}

	public Alert typedCulprit( final String desc, final Object arg ) {
		this.add( Culprit.typedCulprit( desc, arg ) );
		return this;
	}

	public AlertException mishap() {
		return this.report( Abort.abort );
	}


	public AlertException mishap( final char phase ) {
		this.setPhase( phase );
		return this.report( Abort.abort );
	}

	public void warning() {
		this.report( Warning.warning );
	}

	public void internal_warning() {
		this.report( Warning.internal );
	}

	public void warning( final char phase ) {
		this.setPhase( phase );
		this.report( Warning.warning );
	}

	private AlertException report( final Severity severity ) {
		Output.println( "\n" );
		Output.print( severity.getDescription() );
		Output.print( " : " );

		Output.println( this.complaint );
		if ( this.explanation != null ) {
			Output.print( "BECAUSE : " );
			Output.println( this.explanation );
		}
		this.output();
		Output.flushAll();

		return severity.throwUp( new AlertException( this ) );
	}

	private void output() {
		for ( Iterator it = this.culprit_iterator(); it.hasNext(); ) {
			final Culprit c = (Culprit)it.next();
			c.output();
		}
		Output.println( "" );
		Output.flushAll();
	}

	//	---- This section just deals with the statics ----

	public static AlertException unreachable() {
		throw unreachable( (Throwable)null );
	}

	public static AlertException unreachable( final Throwable t ) {
		throw unreachable( "Internal error", t );
	}

	public static AlertException unreachable( final String msg ) {
		throw unreachable( msg, null );
	}

	public static AlertException unreachable( final String msg, final Throwable t ) {
		throw new Alert( t, msg, "Some supposedly unreachable code has been executed", '-' ).mishap();
	}

	public static AlertException unimplemented( final String msg ) {
		final Alert alert = new Alert( "Internal error", "An unimplemented feature is required" );
		alert.culprit( "message", msg );
		throw alert.mishap();
	}

	public static AlertException unimplemented() {
		return Alert.unimplemented( "unimplemented" );
	}

	public Alert resetInsertionPoint() {
		this.resetIndex();
		return this;
	}

}
