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

import org.openspice.vfs.VVolume;
import org.openspice.vfs.VFolder;
import org.openspice.jspice.alert.Alert;
import org.apache.commons.net.ftp.FTPClient;

import java.net.URI;
import java.net.URISyntaxException;

public class FtpVVolume implements VVolume {

	final URI root_uri;
	final FTPClient ftp_client = new FTPClient();

	public static final URI toURI( final String s ) {
		try {
			return new URI( s );
		} catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
	}

	public FtpVVolume( final String uri ) {
		this( toURI( uri ) );
	}

	public FtpVVolume( final URI uri ) {
		if ( "ftp".equals( uri.getScheme() ) ) {
			this.root_uri = uri;
		} else {
			this.root_uri = null;
			throw new Alert( "Not an FTP URI" ).culprit(  "uri", uri ).mishap();
		}
	}

	public VFolder getRootVFolder() {
		final FTPClient ftpc = new FTPClient();
		throw new RuntimeException( "tbd" ); 	//	todo: to be defined
	}

}
