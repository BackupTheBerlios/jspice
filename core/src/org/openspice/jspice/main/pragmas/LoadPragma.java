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
package org.openspice.jspice.main.pragmas;

import org.openspice.vfs.file.FileVFile;
import org.openspice.vfs.file.FileVVolume;
import org.openspice.vfs.VFile;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Interpreter;

import java.util.List;
import java.util.Iterator;
import java.io.File;

public final class LoadPragma {

	public void load( final Interpreter interpreter, final List files ) {
		for ( Iterator it = files.iterator(); it.hasNext(); ) {
			final String fname = (String)it.next();
			final File f = new File( fname );
			if ( f.exists() ) {
				final VFile vfile = FileVVolume.getVFile( new File( fname ) );
				new Interpreter( interpreter.getCurrentNameSpace() ).loadVFile( vfile );
			} else {
				new Alert( "Cannot find file" ).culprit( "file", f ).warning();
			}
		}
	}

}
