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

import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.tools.StdOutConsumer;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.FastProc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.lib.CastLib;

import java.util.LinkedList;

public final class PrintProcs {

	static final class PrintlnProc extends PrintTools.SpicePrintStdOutProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( last ) cuchar.ln();
		}
	}
	public static final Proc printlnProc = new PrintlnProc();

	static final class PrintlnToProc extends PrintTools.SpicePrintToProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( last ) cuchar.ln();
		}
	}
	public static final Proc printlnToProc = new PrintlnToProc();

	static  final class PrintProc extends PrintTools.SpicePrintStdOutProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
		}
	}
	public static final Proc printProc = new PrintProc();

	static  final class PrintToProc extends PrintTools.SpicePrintToProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
		}
	}
	public static final Proc printToProc = new PrintToProc();

	static class FormattedPrintProc extends FastProc {

		final boolean to_flag;
		final boolean ln_flag;

		public FormattedPrintProc( boolean to_flag, boolean ln_flag ) {
			this.to_flag = to_flag;
			this.ln_flag = ln_flag;
		}

		public Object fastCall( final Object tos, final VM vm, final int nargs ) {
			vm.push( tos );
			final LinkedList list = new LinkedList();
			for ( int i = 0; i < nargs; i++ ) {
				list.addFirst( vm.pop() );
			}
			final Consumer c = this.to_flag ? CastLib.toConsumer( vm.pop() ) : StdOutConsumer.OUT;
			final CharSequence control_string = CastLib.toCharSequence( list.removeFirst() );
			PrintTools.formatTo( StdOutConsumer.OUT, control_string, list.toArray() );
			if ( this.ln_flag ) c.ln();
			return vm.pop();
		}

		public Arity inArity() {
			return Arity.ONE_OR_MORE;
		}

		public Arity outArity() {
			return Arity.ZERO;
		}

	}

	public static final Proc FPRINT_PROC = new FormattedPrintProc( false, false );
	public static final Proc FPRINTLN_PROC = new FormattedPrintProc( false, true );
	public static final Proc FPRINT_TO_PROC = new FormattedPrintProc( true, false );
	public static final Proc FPRINTLN_TO_PROC = new FormattedPrintProc( true, true );


}
