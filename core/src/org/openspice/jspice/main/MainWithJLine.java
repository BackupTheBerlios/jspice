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

import jline.*;

import java.io.File;
import java.io.IOException;

import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.main.jline_stuff.PragmaCompletor;
import org.openspice.jspice.main.jline_stuff.SmartCompletor;

public class MainWithJLine extends Main {

	public void perform( final CmdLineOptions cmd ) {
		this.init( cmd.banner );
		try {
			// Setup the input stream.
			final ConsoleReader reader = new ConsoleReader();
			reader.setHistory( new History( new File( System.getProperty( "user.home" ), ".jline-" + FixedConf.getPropertyName( "history" ) ) ) );
			reader.addCompletor(
				new MultiCompletor(
					new Completor[] {
						new PragmaCompletor( this.jspice_conf ),
						new SmartCompletor( this.interpreter ) 
					}
				)
			);
			ConsoleReaderInputStream.setIn( reader, cmd.prompt != null ? cmd.prompt : this.jspice_conf.getPrompt() );

			this.interpreter.interpret( "" );
			this.shutdown();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	
}



