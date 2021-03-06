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

import org.openspice.vfs.VFile;
import org.openspice.vfs.VFileRef;
import org.openspice.vfs.PathAbsVFile;
import org.openspice.vfs.codec.Codec;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Print;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

public class FtpVFile extends PathAbsVFile implements VFile {

	protected Codec codec() {
		return FileNameCodec.FILE_NAME_CODEC;
	}

	protected String getPath() {
		return this.path;
	}

	protected String getName() {
		return FtpTools.getName( this.path );
	}

	protected String getParentPath() {
		return FtpTools.getParentPath( this.path );
	}

	static final FtpVFile make( final FtpVVolume fvol, final String path  ) {
		final boolean file_exists = FtpTools.fileExists( fvol, path );
		if ( file_exists ) {
			return new FtpVFile( fvol, path );
		} else {
			throw new Alert( "File does not exist" ).culprit( "path", path ).mishap();
		}
	}

	static final FtpVFile uncheckedMake( final FtpVVolume fvol, final String path ) {
		return new FtpVFile( fvol, path );
	}

	final FtpVVolume fvol;
	final String path;

	private FtpVFile( FtpVVolume fvol, String path ) {
		this.fvol = fvol;
		this.path = path;
	}

	public Reader readContents() {
		return new InputStreamReader( this.inputStreamContents() );
	}

	public Writer writeContents() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public InputStream inputStreamContents() {
		if ( Print.wouldPrint( Print.VFS ) ) {
			Print.println( "Trying to read contents of file " + this.path );
			Print.println( "path = " + this.path );
		}
		final FTPClient ftpc = this.fvol.getConnectedFTPClient();
		Print.println( Print.VFS,  "Connected .... " );
		try {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			if ( ftpc.retrieveFile( this.path, output ) ) {
				final String s  = new String( output.toByteArray() );
				if ( Print.wouldPrint( Print.VFS ) ) {
					Print.println( "Got file: " + s.length() );
				}
				if ( s.length() < 100 ) {
					if ( Print.wouldPrint( Print.VFS ) ) Print.println( "s = " + s );
				}
				return new ByteArrayInputStream( output.toByteArray() );
			} else {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "reply code = " + ftpc.getReplyCode() );
				throw new Alert( "Cannot retrieve file from FTP server" ).culprit( "file", this.path ).mishap();
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public OutputStream outputStreamContents() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public void delete() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public VFileRef getVFileRef() {
		return new FtpVFileRef( this.fvol, this.path );
	}

}
