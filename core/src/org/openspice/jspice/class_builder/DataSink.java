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

import java.io.*;

public class DataSink extends DataOutputStream {

	boolean done = false;

	public DataSink( boolean d ) {
		super( new ByteSink() );
		this.done = d;
	}

	public DataSink() {
		this( true );
	}

	public final IntMark newIntMark() {
		return new IntMark( this );
	}

	public final ShortMark newShortMark() {
		return new ShortMark( this );
	}

	public final ByteSink getIOS() {
		return (ByteSink)out;
	}

	static final class Handle extends ByteSink.Handle {

		public Handle( final DataSink idos ) {
			super( idos.getIOS() );
		}

		public void copyBytes( final DataSink d ) {
			this.copyBytes( d.getIOS() );
		}

	}

	public DataSink.Handle newHandle() {
		return new Handle( this );
	}

	public byte[] toByteArray() {
		if ( this.done ) {
			return this.getIOS().toByteArray();
		} else {
			throw new RuntimeException( "unfinished datasink" );
		}
	}

	public void close() {
		try {
			super.close();
			this.done = true;
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public final void writeDataSink( final DataSink s ) {
		try {
			this.write( s.toByteArray() );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public final void writeRef( final PoolEntry pe ) {
		try {
			final int n = pe.getIndex();
			this.writeByte( ( n >> 8 ) & 0xFF );
			this.writeByte( n & 0xFF );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public final void writeIndex( final PoolEntry pe ) {
		try {
			this.writeShort( pe.getIndex() );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}


}
