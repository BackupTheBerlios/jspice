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
import org.openspice.jspice.tools.StringBufferConsumer;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.inspect.FieldAdder;

import java.util.*;

public abstract class SpiceObject {
	public abstract void showTo( final Consumer cuchar );
	public abstract void printTo( final Consumer cuchar );
	public abstract List convertToList();
	public abstract Map convertToMap();
	public abstract SpiceObject convertFromList( List list );
	public abstract SpiceObject convertFromMap( Map map );
	public abstract void addInstanceFields( FieldAdder adder );

	public String showToString() {
		final StringBufferConsumer sbc = new StringBufferConsumer();
		this.showTo( sbc );
		return (String)sbc.close();
	}
	
	public String printToString() {
		final StringBufferConsumer sbc = new StringBufferConsumer();
		this.printTo( sbc );
		return (String)sbc.close();
	}

	public final List getInstanceFields() {
		final List fields = new ArrayList();		//	List< Pair< String, Object > >
		this.addInstanceFields( new FieldAdder( fields ) );
		return fields;
	}

	public abstract static class NonMap extends SpiceObject {

		public List convertToList() {
			throw new Alert( "Cannot convert Termin into a List" ).mishap();
		}

		public Map convertToMap() {
			throw new Alert( "Cannot convert Termin into a Map" ).mishap();
		}

		public SpiceObject convertFromList( final List list ) {
			throw Alert.unreachable();
		}

		public SpiceObject convertFromMap( final Map map ) {
			throw Alert.unreachable();
		}

	}
}

