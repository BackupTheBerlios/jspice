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
import org.openspice.jspice.datatypes.proc.Nullary0FastProc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VItem;
import org.openspice.vfs.VFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import tads2.jetty.GameTerminal;
import tads2.jetty.Jetty;

public class TADS2LoaderBuilder extends ValueLoaderBuilder {

	static final class TADS2Loader extends ValueLoader {

		private TADS2Loader( final ValueLoaderBuilder vlb, final NameSpace ns ) {
			super( vlb, ns );
		}

	}

	public ValueLoader newValueLoader( final NameSpace current_ns ) {
		return new TADS2Loader( this, current_ns );
	}

	public Object loadValueFromVFile( final VFile file ) {
		return(
//				new Nullary0FastProc() {
//					public Object fastCall( final Object tos, final VM vm, 	final int nargs ) {
//						new JettyWindow().run( file );
//						return tos;
//					}
//				}
			new Nullary0FastProc() {
				public Object fastCall( final Object tos, final VM vm, 	final int nargs ) {
					final GameTerminal term = new GameTerminal();
					final Jetty j = new Jetty( term, file.inputStreamContents() );
					if ( j.load() ) {
						j.run();
					}
					return tos;
				}
			}
		);

	}
}
