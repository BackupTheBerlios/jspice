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
package org.openspice.jspice.datatypes;

import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.built_in.inspect.FieldAdder;

public final class Termin extends SpiceObject.NonMap {

	public String summary() {
		return "represents the end of a stream of values; a sentinel";
	}

	public void addInstanceFields( FieldAdder adder ) {
		//	Skip.
	}

	public void showTo( final Consumer cuchar ) {
		cuchar.outCharSequence( "termin" );
	}

	public void printTo( final Consumer cuchar ) {
		cuchar.outCharSequence( "termin" );
	}

	public static final Termin TERMIN = new Termin();
}
