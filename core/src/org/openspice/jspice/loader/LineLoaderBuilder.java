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
import org.openspice.jspice.conf.JSpiceConf;

import java.io.*;

public class LineLoaderBuilder extends ValueLoaderBuilder {

//	public LineLoaderBuilder( final JSpiceConf jconf ) {
//		super( jconf );
//	}

	static final class LineLoader extends ValueLoader {

		private LineLoader( final ValueLoaderBuilder vlb, final NameSpace ns ) {
			super( vlb, ns );
		}

	}

	public ValueLoader newValueLoader( final NameSpace current_ns ) {
		return new LineLoader( this, current_ns );
	}

	public Object loadValueFromFile( final String name, final File file ) throws IOException {
		final BufferedReader rdr = new BufferedReader( new FileReader( file ) );
		final String line = rdr.readLine();
		if ( line == null ) {
			new Alert( "Empty line file" ).culprit( "file", file ).mishap();
		}
		return line;
	}

}
