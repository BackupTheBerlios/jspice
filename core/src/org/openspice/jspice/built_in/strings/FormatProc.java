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
package org.openspice.jspice.built_in.strings;

import org.openspice.jspice.datatypes.proc.FastProc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.tools.StringBufferConsumer;

public class FormatProc extends FastProc {

	private static final void oops( final CharSequence s ) {
		throw new Alert( "malformed control string" ).culprit( "control_string", s ).mishap();
	}

	public static final void formatTo( final Consumer consumer, final CharSequence control_string, final Object[] args ) {
		int count = 0;
		for ( int i = 0; i < control_string.length(); i++ ) {
			final char ch = control_string.charAt( i );
			if ( ch == '%' ) {
				i += 1;
				if ( i < control_string.length() ) {
					char c = control_string.charAt( i );
					int index = 0;
					while ( Character.isDigit( c ) ) {
						index = index * 10 + ( c - '0' );
						i += 1;
						if ( i < control_string.length() ) {
							c = control_string.charAt( i );
						} else {
							oops( control_string );
						}
					}
					if ( c == '%' ) {
						consumer.out( c );
					} else if ( c == 's' ) {
						PrintTools.showTo( consumer, args[ index == 0 ? count++ : index - 1 ] );
					} else if ( c == 'p' ) {
						PrintTools.printTo( consumer, args[ index == 0 ? count++ : index - 1 ] );
					} else {
						oops( control_string );
					}
				} else {
					oops( control_string );
				}
			} else {
				consumer.out( ch );
			}
		}
	}

	{
		this.setDescription(
			"format",
			"%p( control_string, args... ) -> formatted_string",
			"returns a string created according to the formatted printing rules"
 		);
	}



	public Arity inArity() {
		return Arity.ONE_OR_MORE;
	}

	public Arity outArity() {
		return Arity.ONE;
	}


	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		vm.push( tos );
		final Object[] args = vm.popArray( nargs - 1 );
		final CharSequence control_string = CastLib.toCharSequence( vm.pop() );
		final StringBufferConsumer c = new StringBufferConsumer();
		formatTo( c, control_string, args );
		return c.closeAsString();
	}

	public static final FormatProc FORMAT_PROC = new FormatProc();
}
