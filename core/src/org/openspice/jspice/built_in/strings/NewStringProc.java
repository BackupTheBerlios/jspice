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

import org.openspice.jspice.datatypes.proc.Vary1Proc;
import org.openspice.jspice.datatypes.Deferred;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.lib.ConvertLib;
import org.openspice.tools.StringBufferTools;

public class NewStringProc extends Vary1Proc {

	{
		setDescription(
			"newString",
			"%p( char|string ... ) -> string",
			"constructs a new mutable string from characters and strings"
		);
	}

	public Object call( final Object tos, final VM vm, final int nargs ) {
		final StringBuffer b = new StringBuffer();
		vm.push( tos );
		final int len = vm.length();
		for ( int i = len - nargs; i < len; i++ ) {
			final Object x = Deferred.deref( vm.get( i ) );
			if ( x instanceof Character ) {
				b.append( ((Character)x).charValue() );
			} else if ( x instanceof String ) {
				b.append( (String)x );
			} else if ( x instanceof StringBuffer ) {
				b.append( (StringBuffer)x );
			} else if ( x instanceof CharSequence ) {
				b.append( ((CharSequence)x) );
			} else {
				new Alert( "Character or String needed" ).culprit( "item", x ).mishap();
			}
		}
		vm.drop( nargs );
		return b;
	}


	public static final NewStringProc NEW_STRING_PROC = new NewStringProc();

}
