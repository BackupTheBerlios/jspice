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
import org.openspice.vfs.codec.FolderNameCodec;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.vfs.codec.Codec;
import org.openspice.tools.ReaderWriterTools;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class FileVFolder extends AbsFileVItem implements VFolder {

	protected Codec codec() {
		return FolderNameCodec.FOLDER_NAME_CODEC;
	}

	FileVFolder( final File file ) {
		super( file );
		if ( !this.file.isDirectory() ) {
			throw new RuntimeException( "directory needed: " + file );
		}
	}

	public List listVFolders() {
		final File[] files = (
			this.file.listFiles(
				new FileFilter() {
					public boolean accept( File pathname ) {
						return pathname.isDirectory();
					}
				}
			)
		);
		final List list = new ArrayList();
		for ( int i = 0; i < files.length; i++ ) {
			list.add( new FileVFolder( files[ i ] ) );
		}
		return list;
	}

	public List listVFiles() {
		final File[] files = (
			this.file.listFiles(
				new FileFilter() {
					public boolean accept( File pathname ) {
						return pathname.isFile();
					}
				}
			)
		);
		final List list = new ArrayList();
		for ( int i = 0; i < files.length; i++ ) {
			list.add( new FileVFile( files[ i ] ) );
		}
		return list;
	}

	public List listVItems() {
		final File[] files = (
			this.file.listFiles(
				new FileFilter() {
					public boolean accept( File pathname ) {
						return pathname.isFile() || pathname.isDirectory();
					}
				}
			)
		);
		final List list = new ArrayList();
		for ( int i = 0; i < files.length; i++ ) {
			final File file = files[ i ];
			list.add( file.isFile() ? (VItem)new FileVFile( file ) : (VItem)new FileVFolder( file ) );
		}
		return list;
	}


	public VFolder newVFolder( final String nam, final String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
		final File d = new File( this.file, name );
		if ( d.mkdir() ) {
			return new FileVFolder( d );
		} else {
			throw new RuntimeException( "Filed to create directory: " + d );
		}
	}

	public VFile newVFile( final String nam, final String ext, final Reader reader ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		final File f = new File( this.file, name );
		if ( f.exists() ) {
			throw new RuntimeException( "file already exists: " + f );
		} else {
			try {
				ReaderWriterTools.readerToWriter( reader, new FileWriter( f ) );
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}
		return new FileVFile( f );
	}

	public VFolder getVFolder( final String nam, String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
		final File file = new File( this.file, name );
		if ( !file.isDirectory() ) return null;
		return new FileVFolder( file );
	}

	public VFile getVFile( String nam, String ext ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		final File file = new File( this.file, name );
		if ( !file.exists() ) return null;
		return new FileVFile( file );
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
		final File file = new File( this.file, name );
		return new FileVFolderRef( file );
	}

	public VFileRef getVFileRef( String nam, String ext ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		final File file = new File( this.file, name );
		return new FileVFileRef( file );
	}

	public VFolderRef getVFolderRef() {
		return new FileVFolderRef( this.file );
	}

}
