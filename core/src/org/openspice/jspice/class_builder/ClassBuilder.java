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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ClassBuilder {

	final JSpiceClassLoader class_loader;
	final ConstantPool pool = new ConstantPool();

	//	---oooOOOooo---

	final String class_name;
	final String super_class_name;
	final List field_spec_list = new ArrayList();
	final List method_spec_list = new ArrayList();

	String getClassName() {
		return this.class_name;
	}

	String getSuperClassName() {
		return this.super_class_name;
	}

	List getFieldSpecs() {
		return this.field_spec_list;
	}

	List getMethodSpecs() {
		return this.method_spec_list;
	}

	public MethodSpec newInstanceMethodSpec( final String name, final String descriptor, final int maxlocals, final int maxstack ) {
		final MethodSpec ms = new MethodSpec( this.pool, Constants.ACC_PUBLIC, name, descriptor, maxlocals, maxstack );
		this.method_spec_list.add( ms );
		return ms;
	}

	public MethodSpec newStaticMethodSpec( final String name, final String descriptor, final int maxlocals, final int maxstack ) {
		final MethodSpec ms = new MethodSpec( this.pool, Constants.ACC_PUBLIC | Constants.ACC_STATIC, name, descriptor, maxlocals, maxstack );
		this.method_spec_list.add( ms );
		return ms;
	}

	public FieldSpec newFieldSpec( String name, String descriptor ) {
		final FieldSpec answer = new FieldSpec( this.pool, name, descriptor );
		this.field_spec_list.add( answer );
		return answer;
	}

	final PoolEntry class_info;
	final PoolEntry super_class_info;

	public ClassBuilder( final JSpiceClassLoader class_loader, final String cn, final String scn ) {
		this.class_loader = class_loader;
		this.class_name = cn;
		this.super_class_name = scn;
		this.class_info = this.pool.newClassInfoPoolEntry( cn );
		this.super_class_info = this.pool.newClassInfoPoolEntry( scn );
	}


	//	---ooOOOooo---



	private void writeConstantPool( final DataSink dios ) {
		try {
			dios.writeShort( this.pool.count() );			//	constant pool count (element 0 is fake, so add 1).
			for ( Iterator it = this.pool.iterator(); it.hasNext(); ) {
				final PoolEntry pe = (PoolEntry)it.next();
				dios.write( pe.toByteArray() );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	private void writeIndex( final DataSink dios, final PoolEntry pe ) {
		try {
			dios.writeShort( pe.getIndex() );
		} catch ( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}

	private void writeAccessFlags( final DataSink dios ) {
		try {
			dios.writeShort( Constants.ACC_FINAL | Constants.ACC_PUBLIC | Constants.ACC_SUPER );		//	ACC_PUBLIC ACC_FINAL ACC_SUPER
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	private void writeThisClass( final DataSink dios  ) {
		this.writeIndex( dios, this.class_info );
	}

	private void writeSuperClass( final DataSink dios ) {
		this.writeIndex( dios, this.super_class_info );
	}

	private void writeInterfaces( final DataSink dios ) {
		try {
			dios.writeShort( 0 );			//	todo:	just a quick hack
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	private void writeFields( final DataSink dios ) throws IOException {
		final List fslist = this.field_spec_list;
		dios.writeShort( fslist.size() );
		for ( final Iterator it = fslist.iterator(); it.hasNext(); ) {
			final FieldSpec fs = (FieldSpec)it.next();
			dios.writeShort( Constants.ACC_PUBLIC );
			this.writeIndex( dios, fs.getName() );
			this.writeIndex( dios, fs.getDescriptor() );
			dios.writeShort( 0 );										//	todo: attributes - quick hack
		}
	}

	private void writeMethods( final DataSink dios ) {
		try {
			dios.writeShort( this.method_spec_list.size() );
			for ( Iterator it = this.method_spec_list.iterator(); it.hasNext(); ) {
				final MethodSpec ms = (MethodSpec)it.next();
//				dios.writeShort( Constants.ACC_PUBLIC );
//				this.writeIndex( dios, ms.getName() );
//				this.writeIndex( dios, ms.getDescriptor() );
//
//				//	----
//				dios.writeShort( 1 );		//	Just the code attribute for the moment
//				this.writeIndex( dios, this.code_pe );
//				final IntMark mark = dios.newIntMark();
//				ms.write( dios );
//				mark.set();
				ms.write( dios );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	private void writeAttributes( final DataSink dios ) {
		try {
			dios.writeShort( 0 );				//	todo:	just a quick hack
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

	void write( final DataSink dios ) {
		try {
			dios.writeInt( 0xCAFEBABE );	//	magic
			dios.writeShort( 3 );			//	minor version number
			dios.writeShort( 45 );			//	major version number
			this.writeConstantPool( dios );
			this.writeAccessFlags( dios );
			this.writeThisClass( dios );
			this.writeSuperClass( dios );
			this.writeInterfaces( dios );
			this.writeFields( dios );
			this.writeMethods( dios );
			this.writeAttributes( dios );
		} catch ( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}

	public Class newClass() {
		final DataSink dsink = new DataSink();
		this.write( dsink );
		final byte[] bytes = dsink.toByteArray();
		
//		for ( int i = 0; i < bytes.length; i++ ) {
//			final int b = bytes[ i ];
//			final int ch1 = "0123456789abcdef".charAt( b & 0xF );
//			final int ch2 = "0123456789abcdef".charAt( ( b >> 8 ) & 0xF );
//			System.err.println( "byte[" + i + "] = " + b + " ("+ (char)ch1 + " " + (char)ch2 + ")" );
//		}

//		try {
//			final OutputStream s = new FileOutputStream( "/tmp/xxx" );
//			s.write( bytes );
//			s.close();
//		} catch ( IOException e ) {
//			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
//		}
		
		return class_loader.loadClassFromBytes( this.class_name, bytes );
	}

	public final void addDefaultConstructor( final String sclassname ) {
		final MethodSpec init = this.newInstanceMethodSpec( "<init>", "()V", 1, 1 );
		init.plantALoad( 0 );
		init.plantInvokeSpecial( sclassname, "<init>", "()V" );
		init.plantReturn();
	}

}
