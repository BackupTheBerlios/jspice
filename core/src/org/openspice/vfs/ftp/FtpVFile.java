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

import java.net.URI;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpVFile extends AbsFtpVThing implements VFile {

	public FtpVFile( final URI uri, final FtpVVolume vvol ) {
		super( uri, vvol );
	}

	protected char separator() {
		throw new RuntimeException( "tbd" );	//	todo:
	}

	public Reader readContents() {
		throw new RuntimeException( "tbd" );	//	todo:
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

}
