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

import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.conf.JSpiceConf;

import java.util.logging.Logger;

public abstract class AbsMain {

//	protected abstract void init( final boolean wantBanner );
//
//	protected abstract void perform( final boolean wantBanner, final String prompt );
//
//	private boolean initialized = false;
//
//	public final void call_init( final boolean wantBanner ) {
//		if ( this.initialized ) return;
//		this.init( wantBanner );
//	}
//
//	public final void call_perform( final boolean wantBanner, final String prompt ) {
//		this.call_init( wantBanner );
//		this.perform( wantBanner, prompt );
//	}

	static class CmdLineOptions {

		boolean banner = false;
		boolean help = false;
		boolean jline = false;
		String prompt = null;
		boolean splash = false;
		boolean version = false;

		void process( final String option ) {
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
			} else {
				System.err.println( "Unrecognized option: " + opt + " (try --help for help)" );
			}
		}

		void process( final String[] args ) {
			for ( int i = 0; i < args.length; i++ ) {
				this.process( args[ i ] );
			}
		}

	}

	public final void perform( final String[] args ) {
		final CmdLineOptions cmd = new CmdLineOptions();
		cmd.process( args );
		if ( cmd.version ) {
			StartVersion.printVersion( "JSpice Version %p.%p.%p" );
		} else if ( cmd.help ) {
			new Pragma( new JSpiceConf(), "help jspice" ).perform();
		} else {
			this.perform( cmd );
		}
	}

	public abstract void perform( final CmdLineOptions cmd );

}
