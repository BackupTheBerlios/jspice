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
package org.openspice.vfs.url;

import org.openspice.vfs.VVolume;
import org.openspice.vfs.zip.ZipVVolume;
import org.openspice.vfs.ftp.FtpVVolume;
import org.openspice.vfs.file.FileVVolume;
import org.openspice.jspice.alert.Alert;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class UrlVVolume {

	public static final VVolume make( final String url ) {
		try {
			return make( new URL( url ) );
		} catch ( MalformedURLException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final VVolume make( final URL	url ) {
		final String protocol = url.getProtocol();
		final String path = url.getPath();
		if ( "file".equals( protocol ) ) {
			return new FileVVolume( new File( path ) );
		} else if ( "ftp".equals( protocol ) ) {
			return new FtpVVolume( url );
		} else if ( "zip".equals( protocol ) ) {
			try {
				return new ZipVVolume( new ZipFile( path ) );
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		} else {
			throw new Alert( "No handler assigned to the protocol of this URL" ).culprit( "protocol", protocol ).culprit( "URL", url ).mishap();
		}
	}

}
