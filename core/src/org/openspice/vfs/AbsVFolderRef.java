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
package org.openspice.vfs;

import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.codec.Codec;

import java.util.StringTokenizer;

public abstract class AbsVFolderRef implements VFolderRef {

	protected abstract Codec fileCodec();
	protected abstract Codec folderCodec();

	public VFolderRef getVFolderRefFromPath( final String path ) {
		VFolderRef vfr = this;
		final StringTokenizer tokens = new StringTokenizer( path, "" + FixedConf.VFOLDER_SEPARATOR );
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			if ( token.length() > 0 ) {
				final String[] namext = folderCodec().decode( token );
				vfr = vfr.getVFolderRef( namext[0], namext[1] );
			} else {
				//	GO BACK TO ROOT OF VOLUME!
				throw new RuntimeException( "tbd" ); 	//	todo: to be defined
			}
		}
		return vfr;
	}

	public VFileRef getVFileRefFromPath( final String path ) {
		VFolderRef vfr = this;
		final StringTokenizer tokens = new StringTokenizer( path, "" + FixedConf.VFOLDER_TERMINATOR );
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			if ( token.length() > 0 ) {
				if ( tokens.hasMoreTokens() ) {
					final String[] namext = folderCodec().decode( token );
					vfr = vfr.getVFolderRef( namext[0], namext[1] );
				} else {
					final String[] namext = fileCodec().decode( token );
					return vfr.getVFileRef( namext[0], namext[1] );
				}
			} else {
				//	GO BACK TO ROOT
				throw new RuntimeException( "tbd" ); 	//	todo: to be defined
			}
		}
		throw new Alert( "Cannot construct file from path" ).culprit( "origin", this ).culprit( "path", path ).mishap();
	}

	public VFolder getVFolderFromPath( final String path ) {
		return this.getVFolderRefFromPath( path ).getVFolder();
	}

	public VFile getVFileFromPath( final String path ) {
		return this.getVFileRefFromPath( path ).getVFile();
	}

	public VItem getVItem() {
		return this.getVFolder();
	}

	public boolean isVFileRef() {
		return false;
	}

	public boolean isVFolderRef() {
		return true;
	}

	public VItem getVItemFromPath( String path ) {
		final VFileRef vfileref = this.getVFileRefFromPath( path );
		if ( vfileref.exists() ) {
			return vfileref.getVFile();
		} else {
			return this.getVFolderFromPath( path );
		}
	}

}
