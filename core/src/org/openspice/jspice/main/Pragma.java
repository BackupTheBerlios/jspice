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
package org.openspice.jspice.main;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.main.manual.Manual;
import org.openspice.jspice.main.manual.ManualPragma;
import org.openspice.jspice.main.manual.SearchPhrase;
import org.openspice.jspice.main.jline_stuff.PrefixFilterAccumulator;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

public class Pragma {

	final JSpiceConf jspice_conf;
	final String input_string;
	final List command_list;

	public Pragma( JSpiceConf jspice_conf, final String input_string ) {
		this.jspice_conf = jspice_conf;
		this.input_string = input_string;
		this.command_list = new ArrayList();
		{
			final StringTokenizer tok = new StringTokenizer( input_string );
			while ( tok.hasMoreTokens() ) {
				this.command_list.add( tok.nextToken() );
			}
		}
	}

	public JSpiceConf getJSpiceConf() {
		return jspice_conf;
	}

	private String command() {
		return this.command_list.isEmpty() ? null : (String)this.command_list.get( 0 );
	}

	private String arg( final int n ) {
		return n < this.command_list.size() ? (String)this.command_list.get( n ) : null;
	}

	private int nargs() {
		return this.command_list.size();
	}

	//	#debug [on|off]
	private void debugPragma() {
		final String t2 = this.arg( 1 );
		boolean is_debugging = this.getJSpiceConf().isDebugging();
		if ( t2 != null ) {
			if ( "on".equals( t2 ) ) {
				is_debugging = true;
			} else if ( "off".equals( t2 ) ) {
				is_debugging = false;
			} else {
				new Alert( "Unrecognized argument for debug pragma" ).culprit( "arg", t2 ).mishap();
			}
			this.getJSpiceConf().setIsDebugging( is_debugging );
		}
		Print.println( "debugging is " + ( is_debugging ? "on" : "off" ) );
	}

	//	#hXXXXX WHITESPACE [TOPIC]
	private void manualPragma( final Manual manual ) {
		final SearchPhrase t = new SearchPhrase();
		for ( int i = 1; i < this.nargs(); i++ ) {
			t.add( this.arg( i ) );
		}
		new ManualPragma().help( manual, t );
	}

	public void perform() {
		final String c = this.command().intern();
		if ( c == "debug" ) {
			this.debugPragma();
		} else if ( c == "quit" || c == "exit" ) {
			System.exit( 0 );
		} else if ( c == "conditions" || c == "warranty" ) {
			final Manual manual = this.getJSpiceConf().getManualByName( "licence" );
			final SearchPhrase t = new SearchPhrase();
			t.add( "system" );
			t.add( "." );					//	virtual package for inventory.
			t.add( "jspice_" + c );
			new ManualPragma().help( manual, t );
		} else {
			final Manual manual = this.getJSpiceConf().getManualByName( c );
			if ( manual != null ) {
				this.manualPragma( manual );
			} else {
				throw new Alert( "Invalid pragma after #" ).culprit( "line", this.input_string ).mishap();
			}
		}
	}

	public void findPragmaCompletions( final PrefixFilterAccumulator acc ) {
		acc.add( "quit" );
		acc.add( "exit" );
		acc.add( "debug" );
		acc.add( "conditions" );
		acc.add( "warranty" );
		this.jspice_conf.findManualCompletions( acc );
	}
}
