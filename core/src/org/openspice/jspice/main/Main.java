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
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.tools.ImmutableSetOfBoolean;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFolderRef;
import org.openspice.vfs.tools.UrlVFolderRef;

public class Main extends AbsMain {

	JSpiceConf jspice_conf;
	SuperLoader super_loader;
	Interpreter interpreter;

	protected void shutdown() {
		Hooks.SHUTDOWN.ping();
	}

	protected void init( final CmdLineOptions cmd ) {
		this.jspice_conf = new JSpiceConf();
		if ( cmd.personal ) {
			//todo: substitute standard strings.
			final VFolderRef personal_inv = this.jspice_conf.getUserHome().getVFolderRef().getVFolderRefFromPath( ".jspice/inventory/" );
			if ( personal_inv.exists() ) {
				this.jspice_conf.installInventoryConf( personal_inv.getVFolder( ImmutableSetOfBoolean.ONLY_TRUE, false ) );
			}
		}
		if ( cmd.inventory != null ) {
			final VFolder vfolder = UrlVFolderRef.make( cmd.inventory ).getVFolder( ImmutableSetOfBoolean.EITHER, false );
			if ( vfolder != null ) {
				this.jspice_conf.installInventoryConf( vfolder );
			} else {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Installing inventory: path = " + cmd.inventory.getPath() + "; query = " + cmd.inventory.getQuery() );
				new Alert( "Cannot find inventory specified on command-line" ).culprit( "inventory path", cmd.inventory ).warning();
			}
		}
		Print.current_mode |= this.jspice_conf.isDebugging() ? Print.DEBUGGING : 0;
		if ( cmd.banner ) FixedConf.printBanner();
		this.super_loader = new SuperLoader( this.jspice_conf );
		this.interpreter = new Interpreter( this.super_loader.getNameSpace( "spice.interactive_mode" ) );
	}

	protected void startUp( final CmdLineOptions cmd ) {
		this.init( cmd );
		this.interpreter.interpret( cmd.prompt != null ? cmd.prompt : this.jspice_conf.getPrompt() );
		this.shutdown();
	}

}

