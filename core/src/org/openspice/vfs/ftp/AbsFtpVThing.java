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

import org.openspice.jspice.conf.FixedConf;
import org.openspice.vfs.file.FileNameTools;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTP;

import java.net.URI;
import java.net.InetAddress;
import java.util.List;
import java.io.IOException;

public abstract class AbsFtpVThing {

	final URI uri;
	final FtpVVolume vvol;

	protected AbsFtpVThing( final URI uri, final FtpVVolume vvol ) {
		this.uri = uri;
		this.vvol = vvol;
	}

	public FTPClient getFTPClient() {
		return this.vvol.ftp_client;
	}

	private void reconnect( final FTPClient ftpc ) throws IOException {
		final String user_info = this.uri.getUserInfo();
		final int n = user_info.indexOf( ':' );
		final String user = n < 0 ? user_info.substring( 0, n ) : user_info;
		final String pw = n < 0 ? user_info.substring( n + 1 ) : "";
		ftpc.connect( this.uri.getHost() );
		ftpc.login( user, pw );
		ftpc.setFileType( FTP.BINARY_FILE_TYPE );
	}

	public FTPClient getConnectedFTPClient() {
		final FTPClient ftpc = this.getFTPClient();
		try {
			if ( !ftpc.isConnected() ) {
				this.reconnect( ftpc );
			}
			ftpc.noop();
		} catch ( final FTPConnectionClosedException e ) {
			try {
				ftpc.disconnect();
				this.reconnect( ftpc );
			} catch ( IOException e1 ) {
				throw new RuntimeException( e1 );
			}
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
		return ftpc;
	}

	public static final char vfile_separator = FixedConf.VFILE_SEPARATOR;
	public static final char vfolder_separator = FixedConf.VFOLDER_SEPARATOR;
	public static final char vfolder_terminator = FixedConf.VFOLDER_TERMINATOR;

	protected abstract char separator();

	public final String getPath() {
		return uri.getPath();
	}

	public final String getName() {
		final String path = this.getPath();
		final int n = path.lastIndexOf( '/' );
		if ( n >= 0 ) {
			return path.substring( n + 1 );
		} else {
			return "";
		}
	}

	public final String getNam() {
		return FileNameTools.extractNam( this.getName(), this.separator() );
	}

	public final void setNam( final String nam ) {
		throw new RuntimeException( "tbd" ); 	//	todo: to be defined
	}


	public final String getExt() {
		return FileNameTools.extractExt( this.getName(), this.separator() );
	}

	public final boolean hasExt( final String ext1 ) {
		final String ext2 = FileNameTools.extractExt( this.getName(), this.separator() );
		return ext1 == null ? ext2 == null : ext1.equals( ext2 );
	}

	public final void setExt( final String ext ) {
		throw new RuntimeException( "tbd" ); 	//	todo: to be defined
	}

	public String getFullName() {
		return this.getName();
	}



	public Comparable getUniqueID() {
		return this.uri;
	}

}
