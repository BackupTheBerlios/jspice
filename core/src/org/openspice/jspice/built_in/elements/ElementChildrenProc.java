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
package org.openspice.jspice.built_in.elements;

import org.openspice.jspice.datatypes.proc.Unary1FastProc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.lib.CastLib;

public class ElementChildrenProc extends Unary1FastProc {

	{
		setDescription(
			"elementChildren",
			"%p( element ) -> list",
			"returns the children of an element as a list"
		);
	}

	public Object fastCall( Object tos, VM vm, int nargs ) {
		return CastLib.toXmlElement( tos ).getChildren();
	}

}
