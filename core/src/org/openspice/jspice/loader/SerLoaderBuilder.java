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
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VFile;

import java.io.*;

public class SerLoaderBuilder extends ValueLoaderBuilder {

	static final class SerLoader extends ValueLoader {

		private SerLoader( final ValueLoaderBuilder vlb, final NameSpace ns ) {
			super( vlb, ns );
		}

	}

	public ValueLoader newValueLoader( final NameSpace current_ns ) {
		return new SerLoader( this, current_ns );
	}

	public Object loadValueFromVItem( final VFile file ) throws IOException {
		try {
			return new ObjectInputStream( file.inputStreamContents() ).readObject();
		} catch ( final ClassNotFoundException exn ) {
			throw new Alert( exn, "Cannot resolve class while deserializing Java object file" ).culprit( "file", file ).mishap();
		}
	}
	
}
