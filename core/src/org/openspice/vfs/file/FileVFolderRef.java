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
import org.openspice.vfs.tools.VFolderView;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.vfs.codec.FolderNameCodec;
import org.openspice.vfs.codec.Codec;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.main.Print;
import org.openspice.tools.SetOfBoolean;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

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

//	private static File find( File file, final LinkedList track_back ) {
//		for(;;) {
//			if ( file.exists() ) {
//				return file;
//			} else {
//				final File parent = file.getParentFile();
//				if ( parent == null ) return null;
//				final String name = file.getName();
//				track_back.addFirst( name );
//				file = parent;
//			}
//		}
//	}
//
//	public static final VFolderRef makeArchiveVFolderRef( final File found, final LinkedList track_back ) {
//		VFolderRef ref = VFolderView.make( found ).getRootVFolderRef();
//		for ( Iterator it = track_back.iterator(); ref != null && it.hasNext(); ) {
//			final String name = (String)it.next();
//			final String[] namext = FolderNameCodec.FOLDER_NAME_CODEC.decode( name );
//			ref = ref.getVFolderRef( namext[0], namext[1] );
//		}
//		return ref;
//	}

	public VFolder getVFolder( final SetOfBoolean if_exists, final boolean create_if_needed ) {
		if ( this.file.isDirectory() ) {
			return FileVFolder.make( this.file );
		} else if ( FixedConf.TRACK_BACK_ENABLED ) {
			return VFolderView.makeVFolderRefWithTrackBack( this.file ).getVFolder( if_exists, create_if_needed );
		} else {
			return VFolderView.make( this.file ).getRootVFolderRef().getVFolder( if_exists, create_if_needed );
		}
	}
//		final LinkedList track_back = new LinkedList();
//		final File found = this.find( this.file, track_back );
//		if ( found == null ) {
//			if ( !if_exists.contains( false ) ) {
//				new Alert( "Could not find VFolder needed" ).culprit(  "file", this.file ).mishap();
//			}
//			if ( create_if_needed ) {
//				if ( !this.file.exists() ) {
//					if ( !this.file.mkdir() ) {
//						new Alert( "Could not create new directory" ).culprit( "path", this.file ).mishap();
//					}
//				}
//			}
//			return null;
//		} else if ( track_back.isEmpty() ) {
//			if ( found.isDirectory() ) {
//				if ( !if_exists.contains( true ) ) {
//					new Alert( "Directory already exists" ).culprit(  "file", this.file ).mishap();
//				}
//				return FileVFolder.make( this.file );
//			} else {
//				//	Ooooh!  Possible archive.
//				return VFolderView.make( this.file ).getRootVFolderRef().getVFolder( if_exists, create_if_needed  );
//			}
//		} else {
//			//	Ooooh! Possible archive.
//			return makeArchiveVFolderRef( found, track_back ).getVFolder( if_exists, create_if_needed  );
//		}
//	}

	public VFileRef getVFileRef( final String nam, final String ext ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
		return new FileVFileRef( new File( this.file, name ) );
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		if ( VFolderView.isArchiveExt( ext ) ) {
			if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Constructing archive folderref: ext = " + ext );
			final String name = FileNameCodec.FILE_NAME_CODEC.encode( nam, ext );
			final File file = new File( this.file, name );
			return VFolderView.make( ext, file ).getRootVFolderRef();
		} else {
			if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Constructing normal folderref; ext = " + ext );
			final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( nam, ext );
			return new FileVFolderRef( new File( this.file, name ) );
		}
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
