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
import org.openspice.tools.ReaderWriterTools;
import org.openspice.vfs.VFile;
import java.lang.ref.SoftReference;

import BFI.BFI;

public class BFILoaderBuilder extends ValueLoaderBuilder {

	static final class StringRef {

		final VFile file;
		SoftReference ref = null;

		public StringRef( final VFile _file ) {
			this.file = _file;
		}

		public String fetch() {
			final String s = ReaderWriterTools.readerToString( this.file.readContents() );
			ref = new SoftReference( s );
			return s;
		}

		public String get() {
			if ( ref == null ) {
				return this.fetch();
			} else {
				final String s = (String)this.ref.get();
				if ( s == null ) {
					return this.fetch();
				} else {
					return s;
				}
			}
		}

	}

	static final class BFILoader extends ValueLoader {

		private BFILoader( final ValueLoaderBuilder vlb, final NameSpace ns ) {
			super( vlb, ns );
		}

	}

	public ValueLoader newValueLoader( final NameSpace current_ns ) {
		return new BFILoader( this, current_ns );
	}

	public Object loadValueFromVFile( final VFile file ) {
		final BFI bfi = new BFI();
		final StringRef ref = new StringRef( file );
		return(
			new Nullary0FastProc() {
				public Object fastCall( Object tos, VM vm, int nargs ) {
					bfi.execute( ref.get() );
					return tos;
				}
			}
		);
	}

}

