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
package org.openspice.jspice.datatypes.repeaters;

import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.inspect.FieldAdder;
import org.openspice.jspice.datatypes.SpiceObject;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;

/**
 * You are expected to override hasNext and next.
 */
public abstract class Repeater extends SpiceObject.NonMap implements Enumeration, Iterator {

	public void addInstanceFields( FieldAdder adder ) {
		// Skip.
	}

	public void showTo( final Consumer cuchar ) {
		cuchar.outString( "-repeater-" );
	}

	public void printTo( final Consumer cuchar ) {
		while ( this.hasNext() ) {
			PrintTools.print( this.next() );
		}
	}

	public boolean hasMoreElements() {
		return this.hasNext();
	}

	public Object nextElement() {
		return this.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
