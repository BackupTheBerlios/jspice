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

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class for holding parsed command-line options.
 */
class CmdLineOptions {

	boolean banner = false;
	boolean help = false;
	boolean jline = false;
	Mode mode = null;
	boolean personal = false;
	String prompt = null;
	boolean splash = false;
	boolean version = false;
	URL inventory = null;

	void process( final String option, final boolean is_last ) {
		final int n = option.indexOf( '=' );
		String opt;
		String arg;
		if ( n >= 0 ) {
			opt = option.substring( 0, n );
			arg = option.substring( n + 1 );
		} else {
			opt = option;
			arg = null;
		}
		if ( opt.equals( "--banner" ) ) {
			this.banner = arg == null || arg.equals( "on" );
		} else if ( opt.equals( "--prompt" ) ) {
			this.prompt = arg == null ? "" : arg;
		} else if ( opt.equals(  "--jline" ) ) {
			this.jline = arg == null || arg.equals( "on" );
		} else if ( opt.equals( "--splash" ) ) {
			this.splash = arg == null || arg.equals( "on" );
		} else if ( opt.equals( "--help" ) ) {
			this.help = arg == null || arg.equals( "on" );
		} else if ( opt.equals( "--version" ) ) {
			this.version = arg == null || arg.equals( "on" );
		} else if ( opt.equals( "--personal" ) ) {
			this.personal = arg == null || arg.equals( "on" );
		} else {
			//	Mode options
			final Mode m = Mode.tryNewMode( opt );
			if ( m != null ) {
				final boolean def = m.isInteractive();
				this.banner = def;
				this.jline = def;
				this.personal = def;
				this.mode = m;
			} else if ( opt.startsWith( "-" ) ) {
				new Alert( "Unrecognized option" ).culprit( "option", option ).mishap();
			} else if ( is_last ) {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Got inventory: " + option );
				try {
					this.inventory = new URL( option );
				} catch ( MalformedURLException e ) {
					throw new RuntimeException( e );
				}

			}
		}
	}

	void process( final String[] args ) {
		//	Set default to be --dev
		this.process( "--dev", false );			//	Oooh, clever.
		for ( int i = 0; i < args.length; i++ ) {
			this.process( args[ i ], i == args.length - 1 );
		}
	}

}
