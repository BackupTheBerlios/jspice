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

import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VFSTools;

import java.io.*;
import java.util.*;

/*

INVENTORY STRUCTURE

*	packages folders can be aggregated inside inventories which are folders that act as package libraries
	-	inventories are marked by containing a mandatory inventory.conf file
	-	package folders inside the inventory folder are included by default
		(unless turned off in the inventory.conf)
	-	the inventory.conf file can turn on/off inclusion by default
	-	to enable/disable a package, mark it as enabled/disabled in the inventory.conf
	-	additional inventories are listed in inventory.conf

*	packages are searched for starting from a root inventory; the algorithm
	-	searches the root inventory and all linked inventories recursively
	-	will typically cache the results of the search in deployment mode and will not in development mode
	-	searches exhaustively and duplicates are reported

*	the /usr/local/jspice/bin/jspice executable takes an inventory as its argument.
	-	an executable inventory should include at least one command-options file
	-	these file defines the entry points
	-	it may cache the list of package directories, subsequently overriding the inventory.conf


PACKAGE STRUCTURE

* 	package folders (or directories) are marked by suffix -pkg

*	the name of the folder is the package name with dots replaced by - (U002D = ASCII 45).

*	an optional configuration file loadInventory.conf that:
	-	lists the core file to be loaded and defines the default loadInventory order.
	-	it may use a regex to make maintenance easier
	-	If this is missing, the default is to look for and loadInventory the file loadInventory.spi or, failing that,
		the loadInventory SUB-folder loadInventory.
	-	The core file may state their dependencies via the loadInventory pragma (which takes precedence over the order
		supplied by the manifest.)
	-	Core file will only be loaded once.
	-	Core file are loaded with the parent package set as the current package.
	-	the manifest file may list further loadInventory-subfolders which are recursively loaded in the same manner.

*	the autoloadable folder auto, containing autoloadable file
	-	the variable name
	-	the facet
	-	content-type
	-	e.g. fred-public.xml

*	the library file in lib; these are subpackages - loading one of these will cause the parent package to
	loadInventory. The subpackages should also be marked with the suffix -pkg to facilitate renaming. Subpackages's
	full name is calculated by prefixed by the full name of their parent package.

*	documentation in a directory called docs


*	other folders and file are not relevant and will not interfere with loading

*/

/**
 * Given an inventory folder, parses an inventory.conf and records:
 * 	[1]	The path name of the inventory;
 *  [2] Linked inventories (which it adds recursively);
 * 	[3] Filter for identifying packages folders.
 * If the folder is not an inventory folder it throws.
 *
 * Format of an inventory file (inventory.conf) looks like this:
 * 	inventory: 			<pathname>   # end of line comment
 * 	namedInventory:		<nickname>
 * 	folder:				<pathname>
 */
public final class InventoryConf {

	private final JSpiceConf jspice_conf;
	private final VFolder inventory_path;
	private String nickname;

	private final List root_folder_list = new ArrayList();

	InventoryConf( final JSpiceConf _jspice_conf, final VFolder _inventory_path ) {
		assert _jspice_conf != null && _inventory_path != null;
		this.jspice_conf = _jspice_conf;
		this.inventory_path = _inventory_path;
		this.nickname = _inventory_path.getNam();
	}

	public String getNickname() {
		return this.nickname;
	}

	//	package level visibility is deliberate.
	void setNickname( final String nickname ) {
		this.nickname = nickname;
	}

	public List getRootFolderList() {
		return this.root_folder_list;
	}

	final Comparable getUniqueID() {
		return this.inventory_path.getUniqueID();
	}

	public final VFolder getPathFile() {
		return this.inventory_path;
	}

	final void loadInventory() {
		final VFile inventory_conf = this.inventory_path.getVFile( this.jspice_conf.getInventoryConfNam(), FixedConf.CONF_EXT );
//		final File inventory_conf = new File( this.inventory_path, this.jspice_conf.getInventoryConfFilename() );
		final BufferedReader b = new BufferedReader( inventory_conf.readContents() );
		for ( ; ; ) {
			try {
				String line = b.readLine();
				if ( line == null ) break;

				//	Dispose of end-of-line-comment.
				final int hash_posn = line.indexOf( '#' );
				if ( hash_posn >= 0 ) {
					line = line.substring( hash_posn );
				}

				//	Now test for blank line.
				line = line.trim();
				if ( line.length() == 0 ) continue;

				//	OK - it is obliged to be a content-line.
				final int colon_pos = line.indexOf( ':' );
				String name = null;
				String value = null;
				if ( colon_pos >= 0 ) {
					//	Only assign to the variables if the content makes sense.
					name = line.substring( 0, colon_pos ).trim();
					value = line.substring( colon_pos + 1 ).trim();
				}
				//	If the assignment failed, all these guards will fail, too.
				if ( "folder".equals( name ) ) {
					this.root_folder_list.add( VFSTools.newVFolder( this.inventory_path, value ) );
				} else if ( "inventory".equals( name ) ) {
					this.jspice_conf.installInventoryConf( VFSTools.newVFolder( this.inventory_path, value ) );
				} else if ( "namedInventory".equals( name ) ) {
					this.jspice_conf.installInventoryConf( this.jspice_conf.lookupInventoryNickname( value ) );
				} else if ( "nickname".equals( name ) ) {
					this.nickname = value;
				} else {
					new Alert( "Invalid line in inventory configuration file" ).culprit( "file", inventory_conf ).culprit( "line", line ).mishap();
				}
			} catch ( final IOException ex ) {
				new Alert( ex, "Problem encountered while read inventory configuration file" ).culprit( "file", inventory_conf ).mishap();
			}
		}

		if ( this.root_folder_list.isEmpty() ) {
			//	If no folders are added, we'll add one.
			this.root_folder_list.add( this.inventory_path );
		}
	}

	VFolder locatePackageFolder( final String pkg_name ) {
		VFolder answer = null;
		for ( Iterator it = this.root_folder_list.iterator(); it.hasNext(); ) {
			final VFolder vfolder = (VFolder)it.next();
			final VFolder f = vfolder.getVFolder( pkg_name, FixedConf.PKG_EXT );
			if ( f != null ) {
				if ( answer == null ) {
					answer = f;
				} else {
					new Alert( "Duplicate package folders" ).
					culprit( "pkg_name", pkg_name ).
					culprit( "folder", answer ).
					culprit( "folder", f ).
					mishap();
				}
			}
		}
		return answer;
	}

	Set allPackageFolders() {
		final Set result = new TreeSet();
		for ( Iterator it = this.root_folder_list.iterator(); it.hasNext(); ) {
			final VFolder vfolder = (VFolder)it.next();
			for ( Iterator jt = vfolder.listVFolders().iterator(); jt.hasNext(); ) {
				final VFolder vf = (VFolder)jt.next();
				if ( vf.hasExt( FixedConf.PKG_EXT ) ) {
					result.add( vf );
				}
			}
		}
		return result;
	}

}
