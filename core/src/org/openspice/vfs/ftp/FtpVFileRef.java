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
package org.openspice.vfs.ftp;

import org.openspice.vfs.VFileRef;
import org.openspice.vfs.VFile;
import org.openspice.vfs.AbsVFileRef;
import org.openspice.vfs.VItem;

import java.net.URI;

public class FtpVFileRef extends AbsVFileRef implements VFileRef {

	final URI uri;
	final FtpVVolume vvol;

	public FtpVFileRef( URI uri, FtpVVolume vvol ) {
		this.uri = uri;
		this.vvol = vvol;
	}

	public VFile getVFile() {
		return FtpVFile.make( this.uri, this.vvol, true );
	}

	public VItem getVItem() {
		return this.getVFile();
	}

	public boolean isVFileRef() {
		return true;
	}

	public boolean isVFolderRef() {
		return false;
	}

	public boolean exists() {
		return this.getVFile() != null;
	}

}
