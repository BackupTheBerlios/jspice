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

import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.conf.FixedConf;

import java.util.Observable;

public class Main extends AbsMain {

	JSpiceConf jspice_conf;
	SuperLoader super_loader;
	Interpreter interpreter;

	protected void shutdown() {
		Hooks.SHUTDOWN.ping();
	}

	protected void init( final boolean wantBanner ) {

//		System.setProperty( "java.net.preferIPv4Stack", "true" );

		this.jspice_conf = new JSpiceConf();
		Print.current_mode = this.jspice_conf.isDebugging() ? Print.INFO | Print.LOAD | Print.CONFIG | Print.AUTOLOAD : 0;
		if ( wantBanner ) FixedConf.printBanner();
		this.super_loader = new SuperLoader( this.jspice_conf );
		this.interpreter = new Interpreter( this.super_loader.getNameSpace( "spice.interactive_mode" ) );
	}

	public void perform( final CmdLineOptions cmd ) {
		this.init( cmd.banner );
		this.interpreter.interpret( cmd.prompt != null ? cmd.prompt : this.jspice_conf.getPrompt() );
		this.shutdown();
	}

}

