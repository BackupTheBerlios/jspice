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

final class PoolEntry extends DataSink {

//	public static final byte CONSTANT_Utf8_info = 1;
//	public static final byte CONSTANT_Class = 7;
//	public static final byte CONSTANT_Methodref = 10;
//	public static final byte CONSTANT_NameAndType = 12;

//	public void writeIndex( final PoolEntry pe ) {
//		try {
//			this.writeShort( pe.getIndex() );
//		} catch ( final IOException ex ) {
//			throw new RuntimeException( ex );
//		}
//	}

	public static final String translate( final String s ) {
		return s.replace( '.', '/' );
	}

	final short idx;

	PoolEntry( final short i ) {
		super( false );
		this.idx = i;
	}

	public short getIndex() {
		return this.idx;
	}

	public final void writeUTF8( final String s ) {
		try {
			final byte[] bytes = s.getBytes( "UTF-8" );
			this.writeByte( Constants.CONSTANT_Utf8_info );
			this.writeShort( bytes.length );
			this.write( bytes );
		} catch ( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}

//	public void close() {
//		super.close();
//		System.err.println( "closing " + this.idx );
//	}
}

