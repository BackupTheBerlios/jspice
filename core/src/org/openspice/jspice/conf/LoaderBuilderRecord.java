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
package org.openspice.jspice.conf;

public final class LoaderBuilderRecord {
	final String extension;
	final String className;
	final String comment;

	public LoaderBuilderRecord( String extension, String className, String comment ) {
		this.extension = extension;
		this.className = className;
		this.comment = comment;
	}

	public String getExtension() {
		return extension;
	}

	public String getClassName() {
		return className;
	}

	public String getComment() {
		return comment;
	}
}
