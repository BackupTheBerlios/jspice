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

import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VItem;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VFolder;

import java.io.File;
import java.io.IOException;

public abstract class ValueLoaderBuilder extends LoaderBuilder {

	public final Loader newLoader( final NameSpace current_ns ) {
		return this.newValueLoader( current_ns );
	}

	public abstract ValueLoader newValueLoader( final NameSpace current_ns );

	public Object loadValueFromVItem( final VItem file ) throws IOException {
		if ( file instanceof VFile ) {
			return this.loadValueFromVFile( (VFile)file );
		} else {
			return this.loadValueFromVFolder( (VFolder)file );
		}
	}

	public Object loadValueFromVFile( final VFile file ) throws IOException {
		throw Alert.unreachable();
	}

	public Object loadValueFromVFolder( final VFolder file ) throws IOException {
		throw Alert.unreachable();
	}

}
