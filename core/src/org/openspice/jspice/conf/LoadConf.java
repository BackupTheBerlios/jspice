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

package org.openspice.jspice.conf;

import org.openspice.jspice.main.Print;
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VFSTools;

import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

/**
 * Optional configuration file (loadInventory.conf).
 * Concerns:
 * 	-	lists the core file to be loaded and defines the default loadInventory order.
 *	-	it may use a regex to make maintenance easier
 *	-	If this is missing, the default is to look for and loadInventory the file loadInventory.spi or, failing that, the loadInventory SUB-folder loadInventory.
 *	-	The core file may state their dependencies via the loadInventory pragma (which takes precedence over the order supplied by the manifest.)
 *	-	Core file will only be loaded once.
 *	-	Core file are loaded with the parent package set as the current package.
 *	-	the manifest file may list further loadInventory-subfolders which are recursively loaded in the same manner.
 */

public final class LoadConf {

	final JSpiceConf jspice_conf;
	final LinkedList file_list = new LinkedList();

	public VFile nextFile() {
		if ( this.file_list.isEmpty() ) {
			return null;
		} else {
			final Object x = this.file_list.get( 0 );
			if ( x instanceof VFile ) {
				return (VFile)this.file_list.remove( 0 );
			} else {
				assert x instanceof LoadConf;
				final Object y = ((LoadConf)x).nextFile();
				if ( y == null ) {
					this.file_list.remove( 0 );
				} else {
					this.file_list.addFirst( y );
				}
				return this.nextFile();
			}
		}
	}

	private void add( final VFile f ) {
		this.file_list.add( f );
	}

	private void add( final LoadConf lc ) {
		this.file_list.add( lc );
	}

	private static final Pattern loadpatt = Pattern.compile( "^\\w*([^#\\w]+)" );

	private void init( final VFolder pkg_dir ) {
		Print.println( Print.CONFIG, "init START" );
		VFile f = pkg_dir.getVFile( FixedConf.load_file_nam,  FixedConf.CONF_EXT );
		VFolder d;
		if ( f != null ) {
			Print.println( Print.CONFIG, "found load.conf" );
			//	We have to parse the conf file and add the contents.
			try {
				final BufferedReader w = new BufferedReader( f.readContents() );
				for(;;) {
					final String line = w.readLine();
					if ( line != null ) break;
					final Matcher m = loadpatt.matcher( line );
					if ( m.find() ) {
						this.add( VFSTools.newVFile( pkg_dir, m.group( 1 ) ) );
					}
				}
			} catch ( final IOException ex ) {
				new Alert( "Cannot open the configuration file" ).culprit( "file", f ).mishap();
			}
		} else if ( ( d = pkg_dir.getVFolder( FixedConf.load_folder_nam, null ) ) != null ) {
			Print.println( Print.CONFIG, "found load folder" );
			this.add( this.newLoadConf( d ) );
		} else if ( ( f = pkg_dir.getVFile( FixedConf.load_spice_file_nam, FixedConf.SPICE_EXT ) ) != null ) {
			//	We only have to add this one file.
			Print.println( Print.CONFIG, "found load.spi name" );
			this.add( f );
		} else {
			Print.println( Print.CONFIG, "Cannot find any file that need loading" );
		}
		Print.println( Print.CONFIG, "init DONE" );
	}

	private LoadConf newLoadConf( final VFolder pkg_dir ) {
		return new LoadConf( pkg_dir, this.jspice_conf );
	}

	public LoadConf( final VFolder pkg_dir, final JSpiceConf _jconf ) {
		this.jspice_conf = _jconf;
		this.init( pkg_dir );
	}

}
