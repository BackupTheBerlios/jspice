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
package org.openspice.vfs.tools;

import org.openspice.vfs.VVolume;
import org.openspice.vfs.VFolderRef;
import org.openspice.vfs.zip.ZipVVolume;
import org.openspice.vfs.ftp.FtpVVolume;
import org.openspice.vfs.file.FileVVolume;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Print;
import org.openspice.jspice.conf.FixedConf;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class UrlVFolderRef {

	public static final VFolderRef make( final String url ) {
		try {
			return make( new URL( url ) );
		} catch ( MalformedURLException e ) {
			throw new RuntimeException( e );
		}
	}

	private static final VFolderRef makeFromPath( final URL url ) {
		final String protocol = url.getProtocol();
		final String path = url.getPath();
		if ( "file".equals( protocol ) ) {
			final File file = new File( path );
//			final boolean is_folder = file.exists() ? file.isDirectory() : ( query != null || file.getPath().matches( ".*/" ) );
			if ( file.exists() || file.getPath().matches( ".*/" ) ) {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "File-based volume: path = " + path + " (exists = " + new File( path ).exists()  + ")");
				return new FileVVolume( file ).getRootVFolderRef();
			} else if ( FixedConf.TRACK_BACK_ENABLED ) {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Archive-based volume - trackback: path = " + path + " (exists = " + new File( path ).exists()  + ")");
				return VFolderView.makeVFolderRefWithTrackBack( file );
			} else {
				if ( Print.wouldPrint( Print.VFS ) ) Print.println( "Archive-based volume - query: path = " + path + "; query = " + url.getQuery() );
				return VFolderView.make( file ).getRootVFolderRef();
			}
		} else if ( "ftp".equals( protocol ) ) {
			return new FtpVVolume( url ).getRootVFolderRef();
		} else {
			throw new Alert( "No handler assigned to the protocol of this URL" ).culprit( "protocol", protocol ).culprit( "URL", url ).mishap();
		}
	}

	private static final VFolderRef extendWithOneQuery( final VFolderRef vfr, final String query ) {
		return vfr.getVFolderRefFromPath( query );	//	todo: This is a shortcut.
	}

	private static final VFolderRef extendWithMultipleQuery( final VFolderRef vfr, final String query ) {
		final int n = query.indexOf( FixedConf.QUERY_CHAR );
		if ( n == -1 ) {
			return extendWithOneQuery( vfr, query );
		} else {
			return extendWithMultipleQuery( extendWithOneQuery( vfr, query.substring( 0, n ) ), query.substring( n + 1 ) );
		}
	}

	public static final VFolderRef make( final URL url ) {
		final String query = url.getQuery();
		final VFolderRef vfr = makeFromPath( url );
		return query == null ? vfr : extendWithMultipleQuery( vfr, query );
	}

}
