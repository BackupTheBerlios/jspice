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

import org.openspice.jspice.datatypes.proc.Trinary1InvokeProc;
import org.openspice.jspice.datatypes.Symbol;
import org.openspice.jspice.datatypes.elements.XmlElement;
import org.openspice.jspice.lib.CastLib;

import java.util.Map;
import java.util.List;

public class NewElementProc extends Trinary1InvokeProc {

	{
		setDescription(
			"newElement",
			"%p( name : symbol, attrs : map, kids : list ) -> element",
			"constructs a new XML element"
		);
	}

	public Object invoke( final Object name, final Object attrs, final Object kids ) {
		final Symbol sym_name = CastLib.toSymbol( name );
		final Map attr_map = CastLib.toMap( attrs );
		final List list_kids = CastLib.toList( kids );
		return XmlElement.make( sym_name, attr_map, list_kids );
	}

	public static final NewElementProc NEW_ELEMENT_PROC = new NewElementProc();

}
