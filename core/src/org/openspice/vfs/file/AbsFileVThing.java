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

import org.openspice.jspice.conf.FixedConf;

import java.io.File;
import java.io.IOException;

abstract class AbsFileVThing {

	protected File file;

	public AbsFileVThing( final File file ) {
		this.file = file;
	}

	/**
	 *	Only use this if you _know_ what you are doing.  (You probably don't)
	 * @return the underlying file implementation - yuck!
	 */
	public File getFile() {
		return file;
	}

	public String toString() {
		return this.file.toString();
	}

	public static final char vfile_separator = FixedConf.VFILE_SEPARATOR;
	public static final char vfolder_separator = FixedConf.VFOLDER_SEPARATOR;

	protected abstract char separator();

	final static String makeVFileName( final String nam, final String ext ) {
		return FileNameTools.makeFileName( nam, vfile_separator, ext );
	}

	static final String getVFileNam( final String name ) {
		return FileNameTools.extractNam( name, vfile_separator );
	}

	static final String getVFileExt( final String name ) {
		return FileNameTools.extractExt( name, vfile_separator );
	}

	final static String makeVFolderName( final String nam, final String ext ) {
		return FileNameTools.makeFileName( nam, vfolder_separator, ext );
	}

	static final String getVFolderNam( final String name ) {
		return FileNameTools.extractNam( name, vfolder_separator );
	}

	static final String getVFolderExt( final String name ) {
		return FileNameTools.extractExt( name, vfolder_separator );
	}

	public final String makeName( final String nam, final String ext ) {
		return FileNameTools.makeFileName( nam, this.separator(), ext );
	}

	public final String getNam() {
		return FileNameTools.extractNam( this.file.getName(), this.separator() );
	}

	public final void setNam( final String nam ) {
		final String name = this.makeName( nam, this.getExt() );
		final File new_name = new File( this.file.getParentFile(), name );
		this.file.renameTo( new_name );
		this.file = new_name;
	}

	public final String getExt() {
		return FileNameTools.extractExt( this.file.getName(), this.separator() );
	}

	public final boolean hasExt( final String ext1 ) {
		final String ext2 = FileNameTools.extractExt( this.file.getName(), this.separator() );
		return ext1 == null ? ext2 == null : ext1.equals( ext2 );
	}

	public final void setExt( final String ext ) {
		final String name = this.makeName( this.getNam(), ext );
		final File new_name = new File( this.file.getParentFile(), name );
		this.file.renameTo( new_name );
		this.file = new_name;
	}

	public final void delete() {
		this.file.delete();
	}

	public String getFullName() {
		return this.file.getName();
	}

	public Comparable getUniqueID() {
		try {
			return this.file.getCanonicalPath();
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

}
