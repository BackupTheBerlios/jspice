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

import org.openspice.tools.ReflectionTools;

import java.io.IOException;
import java.lang.reflect.Field;

public class MethodSpec {

	final ConstantPool pool;
	final int modifiers;
	final PoolEntry name;
	final PoolEntry descriptor;
	final int maxlocals;
	final int maxstack;
	final DataSink code = new DataSink();


	public MethodSpec( final ConstantPool pool, final int modifiers, String name, final String descriptor, final int maxlocals, final int maxstack ) {
		this.pool = pool;
		this.modifiers = modifiers;
		this.name = pool.newPoolEntry( name );
		this.descriptor = pool.newPoolEntry( descriptor );
		this.maxlocals = maxlocals;
		this.maxstack = maxstack;
	}

	final PoolEntry getName() {
		return this.name;
	}

	public PoolEntry getDescriptor() {
		return descriptor;
	}

	void write( final DataSink ds ) {
		try {
			ds.writeShort( this.modifiers );
			ds.writeIndex( this.getName() );
			ds.writeIndex( this.getDescriptor() );
			ds.writeShort( 1 );							//	Just the single code attribute for the moment

			//	----
			ds.writeIndex( this.pool.fetchCodeAttribute() );
			final IntMark attmark = ds.newIntMark();

			ds.writeShort( this.maxstack );				//	max stack
			ds.writeShort( this.maxlocals );			//	max locals
			final IntMark mark = ds.newIntMark();
			ds.writeDataSink( this.code );
			mark.set();
			ds.writeShort( 0 );							//	exception table
			ds.writeShort( 0 );							//	attributes table

			attmark.set();
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	//	---oooOOOooo---
	//	Code planting methods.

	public void plantAConstNull() {
		try {
			this.code.writeByte( 0x1 );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantALoad( final int n ) {
		try {
			//	todo: should bump up the max number of locals.
			if ( 0 <= n && n < 4 ) {
				this.code.writeByte( 0x2a + n );
			} else {
				throw new RuntimeException( "out of range: " + n );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( RuntimeException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantAReturn() {
		try {
			this.code.writeByte( 0xb0 );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantAStore( final int n ) {
		try {
			if ( 0 <= n && n < 4 ) {
				this.code.writeByte( 0x4b + n );
			} else {
				this.code.writeByte( 0x3a );
				this.code.writeByte( n );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantCheckCast( final String s ) {
		try {
			final PoolEntry e = this.pool.newClassInfoPoolEntry( s );
			this.code.writeByte( 0xc0 );
			this.code.writeRef( e );
		} catch ( IOException e1 ) {
			throw new RuntimeException( e1 );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantCheckCast( final Class c ) {
		this.plantCheckCast( c.getName() );
	}

	public void plantDup() {
		try {
			this.code.writeByte( 0x59 );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantGetField( final String name ) {
		try {
			final FieldReference ref = new FieldReference( name );
			this.code.writeByte( 0xb4 );
			this.code.writeRef( ref.poolEntry( this.pool ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantGetField( final String cname, final String mname, final String desc ) {
		try {
			final FieldReference ref = new FieldReference( cname, mname, desc );
			this.code.writeByte( 0xb4 );
			this.code.writeRef( ref.poolEntry( this.pool ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantGetField( final Class clss, final int n ) {
		final String cname = clss.getName();
		final Field f = clss.getFields()[ n ];
		final String fn = f.getName();
		final String fd = ReflectionTools.descriptor( f.getType() );
		this.plantGetField( cname, fn, fd );
	}

	private void plantMiscInvoke( final int instr, final String name ) {
		try {
			final MethodReference mref = new MethodReference( name );
			this.code.writeByte( instr );
			this.code.writeRef( mref.poolEntry( this.pool ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	private void plantMiscInvoke( final int instr, final String cname, final String mname, final String desc ) {
		try {
			final MethodReference mref = new MethodReference( cname, mname, desc );
			this.code.writeByte( instr );
			this.code.writeRef( mref.poolEntry( this.pool ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	//	example - java.lang.Object.equals(Ljava.lang.Object;)Z
	public void plantInvokeSpecial( final String name ) {
		this.plantMiscInvoke( 0xb7, name );
	}

	public void plantInvokeSpecial( final String cname, final String mname, final String desc ) {
		this.plantMiscInvoke( 0xb7, cname, mname, desc );
	}

	public void plantInvokeStatic( final String name ) {
		this.plantMiscInvoke( 0xb8, name );
	}

	public void plantInvokeVirtual( final String name ) {
		this.plantMiscInvoke( 0xb6, name );
	}

	public void plantIStore( final int n ) {
		try {
			if ( 0 <= n && n < 4 ) {
				this.code.writeByte( 0x3b + n );
			} else {
				this.code.writeByte( 0x36 );
				this.code.writeByte( n );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantNew( final String cname ) {
		try {
			this.code.writeByte( 0xbb );
			this.code.writeRef( this.pool.newClassInfoPoolEntry( cname ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantReturn() {
		try {
			this.code.writeByte( 0xb1 );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	public void plantPutField( final String cname, final String mname, final String desc ) {
		try {
			final FieldReference ref = new FieldReference( cname, mname, desc );
			this.code.writeByte( 0xb5 );
			this.code.writeRef( ref.poolEntry( this.pool ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}


	public void plantPutField( final Class clss, final int n ) {
		final String cname = clss.getName();
		final Field f = clss.getFields()[ n ];
		final String fn = f.getName();
		final String fd = ReflectionTools.descriptor( f.getType() );
		this.plantPutField( cname, fn, fd );
	}



}
