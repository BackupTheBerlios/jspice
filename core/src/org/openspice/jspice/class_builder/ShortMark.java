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
package org.openspice.jspice.class_builder;

import java.io.IOException;

public class ShortMark {

	final int origin;
	final DataSink.Handle handle;
	final DataSink sink;

	public ShortMark( final DataSink d ) {
		try {
			this.sink = d;
			this.handle = d.newHandle();
//			System.err.println( "before = " + d.size() );
			d.writeShort( 0 );
//			System.err.println( "after = " + d.size() );
			this.origin = d.size();
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void set() {
		try {
			final int diff = this.sink.size() - origin;
			if ( 0 <= diff && diff < 65536 ) {
				final DataSink s = new DataSink( true );
				s.writeShort( diff );
				System.err.println( "mark writing diff = " + diff );
				this.handle.copyBytes( s );
			} else {
				throw new RuntimeException( "out of range" );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

}
