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

import org.openspice.vfs.codec.Codec;
import org.openspice.vfs.VItem;
import org.apache.commons.net.ftp.FTPClient;

import java.net.URI;

public abstract class AbsFtpVItem implements VItem {

	protected abstract Codec codec();

	final URI uri;
	final FtpVVolume vvol;

	protected AbsFtpVItem( final URI uri, final FtpVVolume vvol ) {
		this.uri = uri;
		this.vvol = vvol;
	}

	final FTPClient getConnectedFTPClient() {
		return this.vvol.getConnectedFTPClient();
	}

	private final String getPath() {
		return uri.getPath();
	}

	private final String getName() {
		final String path = this.getPath();
		final int n = path.lastIndexOf( '/' );
		if ( n >= 0 ) {
			return path.substring( n + 1 );
		} else {
			return "";
		}
	}

	public final String getNam() {
		return this.codec().decodeNam( this.getName() );
	}

	public final String getExt() {
		return this.codec().decodeExt( this.getName() );
	}

	public final boolean hasExt( final String ext1 ) {
		final String ext2 = this.getExt();
		return ext1 == null ? ext2 == null : ext1.equals( ext2 );
	}

	public void setNamExt( final String nam, final String ext ) {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public String getFullName() {
		return this.getName();
	}

	public void delete() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public Comparable getUniqueID() {
		return this.uri;
	}

}
