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

import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VFolderRef;
import org.openspice.vfs.VFileRef;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.vfs.codec.FolderNameCodec;
import org.openspice.vfs.codec.Codec;
import org.openspice.jspice.alert.Alert;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.IOException;

public class FtpVFolder extends AbsFtpVItem implements VFolder {

	protected Codec codec() {
		return FolderNameCodec.FOLDER_NAME_CODEC;
	}

	public static final FtpVFolder make( final URI uri, final FtpVVolume vvol, final boolean null_allowed ) {
		final FTPClient ftpc = vvol.getConnectedFTPClient();
		try {
			if ( ftpc.changeWorkingDirectory( uri.getPath() ) ) {
				return new FtpVFolder( uri, vvol );
			} else {
				if ( null_allowed )return null;
				throw new Alert( "Could not verify URI is directory" ).culprit( "URI", uri ).mishap();
			}
		} catch ( IOException e ) {
			if ( null_allowed ) return null;
			throw new RuntimeException( e );
		}
	}

	private FtpVFolder( final URI uri, final FtpVVolume vvol ) {
		super( uri, vvol );
	}

	public VFolder newVFolder( String nam, String ext ) {
		final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( this.uri.toString(), nam, ext );
		try {
			return new FtpVFolder( new URI( name ), this.vvol );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFile newVFile( final String nam, final String ext, Reader contents ) {
		final String name = FileNameCodec.FILE_NAME_CODEC.encode( this.uri.toString(), nam, ext );
		throw new RuntimeException( "tbd" ); 	//	todo: to be defined
	}

	public void delete() {
		throw new RuntimeException( "tbd" ); 	//	todo: to be defined
	}

	private List list( final boolean want_folders, final boolean want_files ) {
		try {
			final FTPClient ftpc = this.getConnectedFTPClient();
			final FTPFile[] files = ftpc.listFiles();
			final List answer = new ArrayList();
			for ( int i = 0; i < files.length; i++ ) {
				final FTPFile file = files[ i ];
				if ( want_folders && file.isDirectory() ) {
					final String[] namext = FolderNameCodec.FOLDER_NAME_CODEC.decode( file.getName() );
					final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( this.uri.toString(), namext[0], namext[1] );
					answer.add( new FtpVFolder( new URI( name ), this.vvol ) );
				} else if ( want_files && file.isFile() ) {
					final String[] namext = FileNameCodec.FILE_NAME_CODEC.decode( file.getName() );
					final String name = FileNameCodec.FILE_NAME_CODEC.encode( this.uri.toString(), namext[0], namext[1] );
					answer.add( FtpVFile.uncheckedMake( new URI( this.uri.toString() + file.getName() ), this.vvol ) );
				}
			}
			return answer;
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public List listVFolders() {
		return this.list( true, false );
	}

	public List listVItems() {
		return this.list( true, true );
	}

	public List listVFiles() {
		return this.list( false, true );
	}

	public VFolder getVFolder( final String nam, final String ext ) {
		try {
			final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( this.uri.toString(), nam, ext );
			return FtpVFolder.make( new URI( name ), this.vvol, true );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFile getVFile( final String nam, final String ext ) {
		try {
			final String name = FileNameCodec.FILE_NAME_CODEC.encode( this.uri.toString(), nam, ext );
			return FtpVFile.make( new URI( name ), this.vvol, true );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFolderRef getVFolderRef( String nam, String ext ) {
		try {
			final String name = FolderNameCodec.FOLDER_NAME_CODEC.encode( this.uri.toString(), nam, ext );
			return new FtpVFolderRef( new URI( name ), this.vvol );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFileRef getVFileRef( String nam, String ext ) {
		try {
			final String name = FileNameCodec.FILE_NAME_CODEC.encode( this.uri.toString(), nam, ext );
			return new FtpVFileRef( new URI( name ), this.vvol );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFolderRef getVFolderRef() {
		return new FtpVFolderRef( this.uri, this.vvol );
	}

}
