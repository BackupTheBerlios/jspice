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
package org.openspice.jspice.built_in;

import org.openspice.jspice.datatypes.*;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Nullary1InvokeProc;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.class_builder.JSpiceClassLoader;
import org.openspice.jspice.class_builder.ClassBuilder;
import org.openspice.jspice.class_builder.FieldSpec;
import org.openspice.jspice.class_builder.MethodSpec;

import java.util.List;
import java.util.Iterator;

public final class SpiceClassProcs {

	public static final class SlotProc extends Binary1InvokeProc {

		final JSpiceConf jspice_conf;

		public SlotProc( JSpiceConf jspice_conf ) {
			this.jspice_conf = jspice_conf;
		}

		private int slot_proc_counter = 0;

		private Proc newSlotProc( final int n, final SpiceClass sclass ) {
			final JSpiceClassLoader jscl = this.jspice_conf.getClassLoader();
			final ClassBuilder b = (
				jscl.newClassBuilder(
					FixedConf.getDynPackagePrefix() + "slotproc" + slot_proc_counter++,
					org.openspice.jspice.datatypes.proc.SlotProc.class.getName()
				)
			);
			b.addDefaultConstructor( org.openspice.jspice.datatypes.proc.SlotProc.class.getName() );

			{
				final MethodSpec sp = b.newInstanceMethodSpec( "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;", 2, 1 );
				sp.plantALoad( 1 );
//				sp.plantTry();			//	todo:
				sp.plantCheckCast( sclass.getConcrete() );
				sp.plantGetField( sclass.getConcrete(), n );
				sp.plantAReturn();
//				sp.plantCatch();		//	todo: ClassCastException
//				STUFF
//				sp.plantEndTry();		//	todo:
			}

			{
				final MethodSpec sp = b.newInstanceMethodSpec( "invoke_updater", "(Ljava/lang/Object;Ljava/lang/Object;)V", 3, 2 );
				sp.plantALoad( 1 );		//	load object (key)
//				sp.plantTry();			//	todo:
				sp.plantCheckCast( sclass.getConcrete() );
				sp.plantALoad( 2 );		//	load value
				sp.plantPutField( sclass.getConcrete(), n );
				sp.plantReturn();
//				sp.plantCatch();		//	todo: ClassCastException
//				STUFF
//				sp.plantEndTry();		//	todo:
			}

			try {
				return (Proc)b.newClass().newInstance();
			} catch ( InstantiationException e ) {
				throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
			} catch ( IllegalAccessException e ) {
				throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
			}
		}

		public Object invoke( final Object n, final Object sclass ) {
			return this.newSlotProc( CastLib.toInteger( n ).intValue(), CastLib.toSpiceClass( sclass ) );
		}

	}

	public static final class NewClassProc extends Proc {

		final JSpiceConf jspice_conf;

		public NewClassProc( JSpiceConf jspice_conf ) {
			this.jspice_conf = jspice_conf;
		}

		private static int class_counter = 0;

		private SpiceClass newSpiceClass( final List list ) {
			final JSpiceClassLoader jscl = this.jspice_conf.getClassLoader();
			final String spice_object_cname = Vanilla.class.getName();
			final ClassBuilder b = (
				jscl.newClassBuilder(
					FixedConf.getDynPackagePrefix() + "class" + class_counter++,
					spice_object_cname
				)
			);
			b.addDefaultConstructor( spice_object_cname );
			int i = 0;
			for ( Iterator it = list.iterator(); it.hasNext(); ) {
				it.next();
				final FieldSpec fs = b.newFieldSpec( "field" + class_counter + "_" + i++, "Ljava/lang/Object;" );
			}
			return new SpiceClass( b.newClass() );
		}

		public Arity inArity() {
			return Arity.ONE;
		}

		public Arity outArity() {
			return Arity.ONE;
		}

		public Object call( final Object tos, final VM vm, int nargs ) {
			return this.newSpiceClass( CastLib.toList( tos ) );
		}

	}

	public static class NewProc extends Unary1InvokeProc {

		public Object invoke( final Object x ) {
			final SpiceClass c = CastLib.toSpiceClass( x );
			return(
				new Nullary1InvokeProc() {
					public Object invoke() {
						return c.newInstance();
					}
				}
			);
		}

		public static final Proc newProc = new NewProc();

	}

}