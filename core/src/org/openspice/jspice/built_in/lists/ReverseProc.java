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
package org.openspice.jspice.built_in.lists;

import org.openspice.jspice.lib.MapLib;
import org.openspice.jspice.lib.ListLib;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.tools.ListTools;

public class ReverseProc extends Unary1InvokeProc {

	{
		this.setDescription(
			"reverse",
			"%p( S:seq ) -> T:seq",
			"returns a reversed copy of a sequence (list, string, XML element, etc)"
 		);
	}

	public final static ReverseProc REVERSE_PROC = new ReverseProc();

	public Object invoke( final Object obj ) {
		return ListLib.reverse( obj );
	}

}
