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

import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.conf.JSpiceConf;

import java.lang.reflect.InvocationTargetException;

public class Main {

	public static final void main( final String[] args ) throws IllegalAccessException, InstantiationException {
		final JSpiceClassLoader jcl = new JSpiceClassLoader();
		final ClassBuilder b = jcl.newClassBuilder( "foo", "java.lang.Object" );
		b.newFieldSpec( "wabble", "B" );
		{
			final MethodSpec init = b.newInstanceMethodSpec( "<init>", "()V", 1, 1 );
			init.plantALoad( 0 );
			init.plantInvokeSpecial( "java.lang.Object.<init>.()V" );
			init.plantReturn();
		}

		final MethodSpec downcast = b.newStaticMethodSpec( "downcast", "(Ljava/lang/Object;)Lfoo;", 1, 1 );
		downcast.plantALoad( 0 );
		downcast.plantCheckCast( "foo" );
		downcast.plantAReturn();

		final Class c = b.newClass();

		final ClassBuilder pc = jcl.newClassBuilder( "GetWabbleProc", "org.openspice.jspice.datatypes.proc.Proc$Unary1InvokeProc" );
		{
			final MethodSpec init = pc.newInstanceMethodSpec( "<init>", "()V", 1, 1 );
			init.plantALoad( 0 );
			init.plantInvokeSpecial( "org.openspice.jspice.datatypes.proc.Proc$Unary1InvokeProc.<init>.()V" );
			init.plantReturn();
		}

		final MethodSpec p = pc.newInstanceMethodSpec( "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;", 2, 3 );
		p.plantNew( "java.lang.Byte" );
		p.plantDup();
		p.plantALoad( 1 );
		p.plantInvokeStatic( "foo.downcast.(Ljava/lang/Object;)Lfoo;" );
		p.plantGetField( "foo", "wabble", "B" );
		p.plantInvokeSpecial( "java.lang.Byte", "<init>", "(B)V" );
		p.plantAReturn();														//

		System.err.println( "modifiers: " + c.getModifiers() );
		System.err.println( "instance : " + c.newInstance() );
		try {
			System.err.println( c.getMethod( "downcast", new Class[] {Object.class} ).invoke( c.newInstance(), new Object[] { c.newInstance() } ) );
		} catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( IllegalArgumentException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( InvocationTargetException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( NoSuchMethodException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( SecurityException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}

		final Proc getWabbleProc = (Proc)pc.newClass().newInstance();
		final VM vm = new VM( new JSpiceConf() );
		System.err.println( getWabbleProc.call( c.newInstance(), vm, 1 ) );
	}

}
