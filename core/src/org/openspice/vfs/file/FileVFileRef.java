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
package org.openspice.vfs.file;

import org.openspice.vfs.*;
import org.openspice.vfs.tools.VFolderView;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.alert.Alert;
import org.openspice.tools.SetOfBoolean;

import java.io.File;
import java.io.IOException;

public class FileVFileRef extends AbsVFileRef implements VFileRef {

	private final File file;

	FileVFileRef( File file ) {
		this.file = file;
	}

	public VFile getVFile( final SetOfBoolean if_exists, final boolean create_if_needed ) {
		if ( this.file.isFile() ) {
			return FileVFile.make( this.file );
		} else if ( FixedConf.TRACK_BACK_ENABLED ) {
			return VFolderView.makeVFileRefWithTrackBack( this.file ).getVFile( if_exists, create_if_needed );
		} else {
			return null;
		}
//
//		if ( !if_exists.isFull() ) {
//			final boolean is_file = this.file.isFile();
//			if ( ! if_exists.contains( is_file ) ) {
//				new Alert( is_file ? "Ordinary file already exists" : "Ordinary file needed" ).culprit( "file", this.file ).mishap();
//			}
//		}
//		if ( create_if_needed ) {
//			if ( ! this.file.exists() ) {
//				try {
//					if( ! this.file.createNewFile() ) {
//						new Alert( "Could not create new ordinary file" ).culprit( "file", this.file ).mishap();
//					}
//				} catch ( IOException e ) {
//					throw new RuntimeException( e );
//				}
//			}
//		}
//		if ( this.file.isFile() ) {
//			return FileVFile.uncheckedMake( this.file );
//		} else {
//			return null;
//		}
	}

	public boolean exists() {
		return this.file.exists();
	}

}
