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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

final class ConstantPool {

	private final List list = new ArrayList();

	public PoolEntry newPoolEntry() {
		final PoolEntry e = new PoolEntry( (short)(this.list.size() + 1) );	//	add 1 because 0 is fake.
		this.list.add( e );
//		System.err.println( "opening " + e.getIndex() );
		return e;
	}

	private PoolEntry code = this.newPoolEntry( "Code" );
	public PoolEntry fetchCodeAttribute() {
		return this.code;
	}

	//	todo:	This is probably a decent enough place to share strings.
	public PoolEntry newPoolEntry( final String s ) {
		final PoolEntry pe = this.newPoolEntry();
		pe.writeUTF8( s );
		pe.close();
		return pe;
	}

	public PoolEntry newClassInfoPoolEntry( final String s ) {
		try {
			final PoolEntry pe = this.newPoolEntry();
			pe.writeByte( Constants.CONSTANT_Class );
			pe.writeIndex( this.newPoolEntry( PoolEntry.translate( s ) ) );
			pe.close();
			return pe;
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public short count() {
		return (short)(this.list.size() + 1);		//	add one because 0 is fake.
	}

	public Iterator iterator() {
		return this.list.iterator();
	}

}
