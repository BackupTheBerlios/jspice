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
package org.openspice.jspice.lib;

import org.openspice.jspice.datatypes.Deferred;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.jspice.datatypes.Symbol;
import org.openspice.jspice.datatypes.Termin;
import org.openspice.jspice.datatypes.elements.XmlElement;

import java.util.Map;
import java.util.List;

public class IsLib {

	public static final boolean isAbsent( final Object x ) {
		return ( x == null ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() == null );
	}

	public static final boolean isBoolean( final Object x ) {
		return ( x instanceof Boolean ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Boolean );
	}

	public static final boolean isCharacter( final Object x ) {
		return ( x instanceof Character ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Character );
	}


	public static final boolean isCharSequence( final Object x ) {
		return ( x instanceof CharSequence ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof CharSequence );
	}

	public static final boolean isList( final Object x ) {
		return ( x instanceof List ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof List );
	}

	public static final boolean isListFlavour( Object x ) {
		return ( x instanceof Map ) || ( x instanceof List ) || ( x instanceof String ) || ( x instanceof SpiceObject && ((SpiceObject)x).isListFlavour() );
	}

	public static final boolean isMap( final Object x ) {
		return ( x instanceof Map ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Map );
	}

	public static final boolean isMapEntry( final Object x ) {
		return ( x instanceof Map.Entry ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Map.Entry );
	}

	public static final boolean isMapFlavour( Object x ) {
		return ( x instanceof Map ) || ( x instanceof List ) || ( x instanceof String ) || ( x instanceof SpiceObject && ((SpiceObject)x).isMapFlavour() );
	}

	public static final boolean isNumber( Object x ) {
		return ( x instanceof Number ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Number );
	}

	public static final boolean isString( final Object x ) {
		return ( x instanceof String ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof String );
	}

	public static final boolean isSymbol( final Object x ) {
		return ( x instanceof Symbol ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Symbol );
	}

	public static final boolean isTermin( final Object x ) {
		return ( x instanceof Termin ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof Termin );
	}

	public static final boolean isXmlElement( final Object x ) {
		return ( x instanceof XmlElement ) || ( x instanceof Deferred ) && ( ((Deferred)x).force() instanceof XmlElement );
	}

}
