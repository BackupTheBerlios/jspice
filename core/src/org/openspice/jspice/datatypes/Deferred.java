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
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.ListTools;
import org.openspice.jspice.lib.MapLib;
import org.openspice.jspice.lib.IsLib;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.inspect.FieldAdder;

import java.util.List;
import java.util.Map;
import java.lang.ref.SoftReference;

public abstract class Deferred extends SpiceObject {

	public abstract Object calculate();
	public abstract void freeResources();

	private Object ref = null;

	static final class HardReference {
		final Object x;

		public HardReference( Object x ) {
			this.x = x;
		}

		public Object get() {
			return x;
		}
	}

	/**
	 * Must include null.
	 */
	public static final boolean isSimple( final Object x ) {
		return x == null || x instanceof Number || x instanceof String;
	}

	public final Object force() {
		final Object x = this.calculate();
		if ( isSimple( x ) ) {
			this.ref = new HardReference( x );
			this.freeResources();
		} else {
			this.ref = new SoftReference( x );
		}
		return x;
	}

	public final Object get() {
		if ( this.ref == null ) {
			return this.force();
		} else if ( this.ref instanceof SoftReference ) {
			final Object x = ((SoftReference)this.ref).get();
			if ( x == null ) {
				return this.force();
			} else {
				return x;
			}
		} else if ( this.ref instanceof HardReference ) {
			return ((HardReference)this.ref).get();
		} else {
			throw Alert.unreachable();
		}
	}

	public static final Object deref( final Object x ) {
		if ( x instanceof Deferred ) {
			return ((Deferred)x).get();	
		} else {
			return x;
		}
	}

	public void showTo( final Consumer cuchar ) {
		PrintTools.showTo( cuchar, this.get() );
	}

	public void printTo( final Consumer cuchar ) {
		PrintTools.printTo( cuchar, this.get() );
	}

	public List convertToList() {
		return ListTools.convertTo( this.get() );
	}

	public Map convertToMap() {
		return MapLib.convertTo( this.get() );
	}

	public SpiceObject convertFromList( final List list ) {
		throw Alert.unreachable();
	}

	public SpiceObject convertFromMap( final Map map ) {
		throw Alert.unreachable();
	}

	public boolean isEmpty() {
		return MapLib.isEmpty( this.get() );
	}

	public boolean isMapFlavour() {
		return IsLib.isMapFlavour( this.get() );
	}

	public boolean isListFlavour() {
		return IsLib.isMapFlavour( this.get() );
	}

	public void addInstanceFields( FieldAdder adder ) {
		adder.add( "cache", this.ref );
	}
}
