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
import org.openspice.jspice.main.SuperLoader;

import java.io.File;


public class MapLoaderBuilder extends ObjectLoaderBuilder {

	static final class MapLoader extends ObjectLoader {

		public MapLoader( final NameSpace current_ns ) {
			super( current_ns );
		}

		public Object fileValue( final String name, final File file ) {
			final SuperLoader sloader = this.getSuperLoader();
			final boolean is_symtab = "symtab".equals( sloader.getExtension( file ) );
			final Accumulator acc = is_symtab ? (Accumulator)new Accumulator.SymMap() : (Accumulator)new Accumulator.StrMap();
			final File[] fs = file.listFiles();
			for ( int i = 0; i < fs.length; i++ ) {
				final File f = fs[ i ];
				if ( sloader.couldLoadFile( f ) ) {
					sloader.autoloadFileAsNamedValue( f, this.getCurrentNameSpace(), acc );
				}
			}
			return acc.getMap();
		}

	}

	public ObjectLoader newObjectLoader( NameSpace current_ns ) {
		return new MapLoader( current_ns );
	}



}
