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

import org.openspice.jspice.tools.ListTools;
import org.openspice.jspice.alert.Alert;

import java.util.*;

public class ListLib {
	
	public final static Object getAt( final Object obj, final Object key ) {
		try {
			final int idx = ( (Integer)key ).intValue() - 1;
			if ( obj instanceof List ) {
				return ((List)obj).get( idx );
			} else if ( obj instanceof String ) {
				return new Character( ((String)obj).charAt( idx ) );
			} else if ( obj instanceof Map ) {
				return ((Map)obj).get( key );
			} else {
				return ListTools.convertTo( obj ).get( idx );
			}
		} catch ( final ClassCastException exn ) {
			new Alert(
				"Index not an integer"
			).culprit( "index", key ).culprit( "map", obj ).mishap( 'E' );
			return null;
		}
	}
	
	public final static Object putAt( final Object obj, final Object key, final Object val ) {
		try {
			final int idx = ( (Integer)key ).intValue() - 1;
			if ( obj instanceof List ) {
				return ((List)obj).set( idx, val );
			} else if ( obj instanceof Map ) {
				return ((Map)obj).put( key, val );
			} else {
				new Alert(
					"Cannot convert object to an assignable list"
				).culprit( "object", obj ).mishap( 'E' );
				return null;		//	sop
			}
		} catch ( final ClassCastException exn ) {
			new Alert(
				"Index not an integer"
			).culprit( "index", key ).culprit( "map", obj ).mishap( 'E' );
			return null;			//	sop
		}
	}
	
	public final static Object length( final Object obj ) {
		try {
			if ( obj instanceof List ) {
				return new Integer( ((List)obj).size() );
			} else if ( obj instanceof String ) {
				return new Integer(((String)obj).length() );
			} else {
				return new Integer( ListTools.convertTo( obj ).size() );
			}
		} catch ( final ClassCastException exn ) {
			new Alert(
				"Object cannot be converted to a list"
			).culprit( "object", obj ).mishap( 'E' );
			return null;
		}
	}

	public static Object append( final Object x, final Object y ) {
		if ( x instanceof List && y instanceof List ) {
			final ArrayList list = new ArrayList( (List)x );
			list.addAll( (List)y );
			return list;
		} else if ( x instanceof String && y instanceof String ) {
			return (String)x + (String)y;
		} else {
			new Alert(
				"Mismatched arguments for append"
			).culprit( "first", x ).culprit( "second", y ).mishap( 'E' );
			return null;	//	sop.
		}
	}

}
