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
package org.openspice.jspice.loader;

import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VItem;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;

import java.io.File;
import java.io.IOException;

public abstract class ObjectLoader extends Loader {

	public ObjectLoader( final NameSpace current_ns ) {
		super( current_ns );
	}

	public final void autoloadVItem( VItem file, Var.Perm perm, FacetSet facets ) {
		this.bind( perm, this.autoloadFileForValue( perm.getName(), file ) );
	}

	public final Object autoloadFileForValue( String name, VItem file ) {
		return this.fileValue( name, file );
	}

	public Object fileValue( final String name, final VItem file ) {
		return file instanceof VFile ? this.fileValueFromVFile( name, (VFile)file ) :  this.fileValueFromVFolder( name, (VFolder)file );
	}

	public Object fileValueFromVFile( final String name, final VFile file ) {
		throw Alert.unreachable();
	}

	public Object fileValueFromVFolder( final String name, final VFolder file ) {
		throw Alert.unreachable();
	}

}
