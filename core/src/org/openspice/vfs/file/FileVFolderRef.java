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
package org.openspice.vfs.file;

import org.openspice.vfs.*;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.vfs.codec.FolderNameCodec;
import org.openspice.vfs.codec.Codec;

import java.io.File;

public class FileVFolderRef extends AbsVFolderRef implements VFolderRef {

	protected Codec fileCodec() {
		return FileNameCodec.FILE_NAME_CODEC;
	}

	protected Codec folderCodec() {
		return FolderNameCodec.FOLDER_NAME_CODEC;
	}

	private final File file;

	FileVFolderRef( File file ) {
		this.file = file;
	}

	public VFolder getVFolder() {
		return new FileVFolder( this.file );
	}

	public VItem getVItem() {
		return this.getVFolder();
	}

	public VFileRef getVFileRef( final String nam, final String ext ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		return new FileVFileRef( new File( this.file, name ) );
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
		return new FileVFolderRef( new File( this.file, name ) );
	}

	public boolean isVFileRef() {
		return false;
	}

	public boolean isVFolderRef() {
		return true;
	}

	public boolean exists() {
		return this.file.exists();
	}
	
}
