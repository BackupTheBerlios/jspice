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
package org.openspice.vfs.zip;

import org.openspice.vfs.AbsVFileRef;
import org.openspice.vfs.VFile;
import org.openspice.tools.SetOfBoolean;

import java.util.zip.ZipEntry;

public class ZipVFileRef extends AbsVFileRef {

	final String path;
	final ZipVVolume zvol;

	public ZipVFileRef( ZipVVolume zvol, String path ) {
		this.path = path;
		this.zvol = zvol;
	}

	public final VFile getVFile( final SetOfBoolean if_exists, final boolean create_if_needed ) {
//		return ZipVFile.make( this.zvol, this.path );
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public final boolean exists() {
		final ZipEntry e = this.zvol.zip_file.getEntry( this.path );
		return e != null && !e.isDirectory();
	}

}
