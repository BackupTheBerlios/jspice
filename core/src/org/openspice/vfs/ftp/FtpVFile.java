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
import org.openspice.vfs.codec.Codec;
import org.openspice.vfs.codec.FileNameCodec;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.class_builder.ByteSink;
import org.openspice.jspice.main.Print;
import org.openspice.tools.NullOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.net.URI;
import java.io.*;

public class FtpVFile extends AbsFtpVItem implements VFile {

	protected Codec codec() {
		return FileNameCodec.FILE_NAME_CODEC;
	}

	public static final FtpVFile make( final URI uri, final FtpVVolume vvol, final boolean null_allowed ) {
		final FTPClient ftpc = vvol.getConnectedFTPClient();
		try {
			final FTPFile[] files = ftpc.listFiles( uri.getPath() );
			if ( files.length != 1 ) {
				if ( null_allowed ) return null;
				throw new Alert( "Cannot determine this URI is a file" ).culprit(  "URI", uri ).mishap();
			} else {
				return new FtpVFile( uri, vvol );
			}
		} catch ( IOException e ) {
			if ( null_allowed ) return null;
			throw new RuntimeException( e );
		}
	}

	static final FtpVFile uncheckedMake( final URI uri, final FtpVVolume vvol ) {
		return new FtpVFile( uri, vvol );
	}

	private FtpVFile( final URI uri, final FtpVVolume vvol ) {
		super( uri, vvol );
	}

	protected char separator() {
		return FixedConf.VFILE_SEPARATOR;
	}

	public Reader readContents() {
		if ( Print.wouldPrint( Print.FTP ) ) {
			Print.println( "Trying to read contents of file " + this.uri );
			Print.println( "path = " + this.uri.getPath() );
		}
		final FTPClient ftpc = this.getConnectedFTPClient();
		Print.println( Print.FTP,  "Connected .... " );
		try {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			if ( ftpc.retrieveFile( this.uri.getPath(), output ) ) {
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
				new Alert( "Cannot retrieve file from FTP server" ).culprit( "file", this.uri ).mishap();
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
		return new FtpVFileRef( this.uri, this.vvol );
	}

}
