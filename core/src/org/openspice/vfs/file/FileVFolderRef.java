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
import org.openspice.jspice.alert.Alert;
import org.openspice.tools.SetOfBoolean;

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

	public VFolder getVFolder( final SetOfBoolean if_exists, final boolean create_if_needed ) {
		if ( !if_exists.isFull() ) {
			final boolean is_dir = this.file.isDirectory();
			if ( ! if_exists.contains( is_dir ) ) {
				new Alert( is_dir ? "Directory already exists" : "Directory needed" ).culprit(  "file", this.file ).mishap();
			}
		}
		if ( create_if_needed ) {
			if ( !this.file.exists() ) {
				if ( !this.file.mkdir() ) {
					new Alert( "Could not create new directory" ).culprit( "path", this.file ).mishap();
				}
			}
		}
		if ( this.file.isDirectory() ) {
			return FileVFolder.make( this.file );
		} else {
			return null;
		}
	}

	public VFileRef getVFileRef( final String nam, final String ext ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		return new FileVFileRef( new File( this.file, name ) );
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
		return new FileVFolderRef( new File( this.file, name ) );
	}

	public boolean exists() {
		return this.file.exists();
	}

	//	public VFolder newVFolder( final String nam, final String ext ) {
//		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
//		final File d = new File( this.file, name );
//		if ( d.mkdir() ) {
//			return new FileVFolder( d );
//		} else {
//			throw new RuntimeException( "Filed to create directory: " + d );
//		}
//	}
//
//	public VFile newVFile( final String nam, final String ext, final Reader reader ) {
//		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
//		final File f = new File( this.file, name );
//		if ( f.exists() ) {
//			throw new RuntimeException( "file already exists: " + f );
//		} else {
//			try {
//				ReaderWriterTools.readerToWriter( reader, new FileWriter( f ) );
//			} catch ( IOException e ) {
//				throw new RuntimeException( e );
//			}
//		}
//		return FileVFile.make( f );
//	}

}
