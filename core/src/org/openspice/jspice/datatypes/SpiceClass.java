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

import java.util.List;
import java.util.Map;

/**
 * Represents a Spice class.
 * Concerns:
 * 	-	Retains a list of other classes that it extends.
 * 	-	It is possible to determine which Spice classes it extends.
 *  -	Records the java.lang.Class which implements it.
 */
public class SpiceClass extends SpiceObject {

	private final Class concrete;

	public SpiceClass( Class concrete ) {
		this.concrete = concrete;
	}

	public Class getConcrete() {
		return concrete;
	}

	//	---oooOOOooo---

	public void addInstanceFields( final FieldAdder adder ) {
		adder.add( "concrete", this.concrete );
	}

	public void showTo( final Consumer cuchar ) {
		throw new RuntimeException( "tbd" );    // todo:
	}

	public void printTo( final Consumer cuchar ) {
		throw new RuntimeException( "tbd" );    // todo:
	}

	public List convertToList() {
		throw new RuntimeException( "tbd" );    // todo:
	}

	public Map convertToMap() {
		throw new RuntimeException( "tbd" );    // todo:
	}

	public SpiceObject convertFromList( final List list ) {
		throw new RuntimeException( "tbd" );    // todo:
	}

	public SpiceObject convertFromMap( final Map map ) {
		throw new RuntimeException( "tbd" );    // todo:
	}

	//	---oooOOOooo---

	public Object newInstance() {
		try {
			return this.concrete.newInstance();
		} catch ( InstantiationException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}
	}

}
