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
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.IOException;

public class FtpVFolder extends AbsFtpVThing implements VFolder {

	public FtpVFolder( final URI uri, final FtpVVolume vvol ) {
		super( uri, vvol );
	}

	protected char separator() {
		return AbsFtpVThing.vfolder_separator;
	}

	public VFolder newVFolder( String name, String ext ) {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public VFile newVFile( String nam, String ext, Reader contents ) {
		throw new RuntimeException( "tbd" );	//	todo:
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
					answer.add( new FtpVFolder( new URI( this.uri.toString() + file.getName() + AbsFtpVThing.vfolder_terminator ), this.vvol ) );
				} else if ( want_files && file.isFile() ) {
					answer.add( new FtpVFile( new URI( this.uri.toString() + file.getName() ), this.vvol ) );
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
			return new FtpVFolder( new URI( this.uri.toString() + nam + AbsFtpVThing.vfolder_separator + ext + AbsFtpVThing.vfolder_terminator ), this.vvol );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public VFile getVFile( final String nam, final String ext ) {
		try {
			return new FtpVFile( new URI( this.uri.toString() + /* AbsFtpVThing.vfolder_terminator + */ nam + AbsFtpVThing.vfile_separator + ext ), this.vvol );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

}
