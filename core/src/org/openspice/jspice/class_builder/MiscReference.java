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

public class MiscReference {

	final byte ref_byte;
	final String className;
	final String methodName;
	final String descriptor;

	int offset;

	public MiscReference( final byte ref_byte, String name ) {
		this.ref_byte = ref_byte;
		final int n = name.lastIndexOf( '.' );
		if ( n < 0 ) throw new RuntimeException( "bad name " + name );
		this.descriptor = name.substring( n + 1 );
		final String class_and_method = name.substring( 0, n );
		final int m = class_and_method.lastIndexOf( '.' );
		if ( n < 0 ) throw new RuntimeException( "bad name " + name );
		this.className = class_and_method.substring( 0, m );
		this.methodName = class_and_method.substring( m + 1 );
	}

	public MiscReference( final byte ref_byte, String className, String methodName, String descriptor ) {
		this.ref_byte = ref_byte;
		this.className = className;
		this.methodName = methodName;
		this.descriptor = descriptor;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void bind( final int n ) {
		this.offset = n;
	}

	public PoolEntry poolEntry( final ConstantPool pool ) {
		try {
			final PoolEntry class_info_pe = pool.newClassInfoPoolEntry( this.className );

			final PoolEntry name_and_type_pe = pool.newPoolEntry();
			name_and_type_pe.writeByte( Constants.CONSTANT_NameAndType );
			name_and_type_pe.writeIndex( pool.newPoolEntry( PoolEntry.translate( this.methodName ) ) );
			name_and_type_pe.writeIndex( pool.newPoolEntry( PoolEntry.translate( this.descriptor ) ) );
			name_and_type_pe.close();

			final PoolEntry answer = pool.newPoolEntry();
			answer.writeByte( this.ref_byte );
			answer.writeIndex( class_info_pe );
			answer.writeIndex( name_and_type_pe );
			answer.close();

			return answer;
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}
}

