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
import org.openspice.jspice.class_builder.ByteSink;
import org.openspice.jspice.main.Print;
import org.openspice.tools.NullOutputStream;
import org.openspice.tools.SetOfBoolean;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.net.URI;
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


//	public static final FtpVFile make( final FtpVVolume vvol, final String path, final SetOfBoolean if_exists, final boolean create_if_needed ) {
//		final FTPClient ftpc = vvol.getConnectedFTPClient();
//		boolean exists = false;
//		try {
//			final FTPFile[] files = ftpc.listFiles( path );
//			if ( files.length == 1 ) {
//				exists = true;
//			} else if ( files.length > 1 ) {
//				throw new Alert( "Cannot determine this path is a file" ).culprit(  "path", path ).mishap();
//			}
//		} catch ( IOException e ) {
//			throw new RuntimeException( e );
//		}
//		if ( !if_exists.contains( exists ) ) {
//			throw new Alert( exists ? "File already exists" : "File does not exist" ).culprit( "file", path ).mishap();
//		} else if ( exists ) {
//			return new FtpVFile( vvol, path );
//		} else {
//			if ( create_if_needed ) {
//				throw new RuntimeException( "tbd" ); 	//	todo: to be defined
//			} else {
//				return null;
//			}
//		}
//	}

	static final FtpVFile make(  final FtpVVolume fvol, final String path  ) {
//		final FTPClient ftpc = fvol.getConnectedFTPClient();
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

	protected char separator() {
		return FixedConf.VFILE_SEPARATOR;
	}

	public Reader readContents() {
		if ( Print.wouldPrint( Print.FTP ) ) {
			Print.println( "Trying to read contents of file " + this.path );
			Print.println( "path = " + this.path );
		}
		final FTPClient ftpc = this.fvol.getConnectedFTPClient();
		Print.println( Print.FTP,  "Connected .... " );
		try {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			if ( ftpc.retrieveFile( this.path, output ) ) {
				final String s  = new String( output.toByteArray() );
				if ( Print.wouldPrint( Print.FTP ) ) {
					Print.println( "Got file: " + s.length() );
				}
				if ( s.length() < 100 ) {
					if ( Print.wouldPrint( Print.FTP ) ) Print.println( "s = " + s );
				}
				return new InputStreamReader( new ByteArrayInputStream( output.toByteArray() ) );
			} else {
				if ( Print.wouldPrint( Print.FTP ) ) Print.println( "reply code = " + ftpc.getReplyCode() );
				new Alert( "Cannot retrieve file from FTP server" ).culprit( "file", this.path ).mishap();
			}
			return new StringReader( "" );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}

	}

	public Writer writeContents() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public InputStream inputStreamContents() {
		throw new RuntimeException( "tbd" );	//	todo:
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
