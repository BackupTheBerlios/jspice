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

import org.openspice.vfs.VFolderRef;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFileRef;
import org.openspice.vfs.AbsVFolderRef;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.vfs.codec.FolderNameCodec;
import org.openspice.vfs.codec.Codec;

import java.net.URI;


public class FtpVFolderRef extends AbsVFolderRef implements VFolderRef {

	protected Codec fileCodec() {
		return FileNameCodec.FILE_NAME_CODEC;
	}

	protected Codec folderCodec() {
		return FolderNameCodec.FOLDER_NAME_CODEC;
	}

	final URI uri;
	final FtpVVolume vvol;

	public FtpVFolderRef( URI uri, FtpVVolume vvol ) {
		this.uri = uri;
		this.vvol = vvol;
	}

	public VFolder getVFolder() {
		return FtpVFolder.make( this.uri,  this.vvol, true );
	}

	public VFileRef getVFileRef( String nam, String ext ) {
		return new FtpVFileRef( FtpTools.nextURI( this.uri, FileNameCodec.FILE_NAME_CODEC, nam,ext ), this.vvol );
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		return new FtpVFolderRef( FtpTools.nextURI( this.uri, FolderNameCodec.FOLDER_NAME_CODEC, nam,ext ), this.vvol );
	}

	public boolean exists() {
		return this.getVFolder() != null;
	}

}
