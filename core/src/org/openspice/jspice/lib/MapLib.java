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
import org.openspice.jspice.*;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.PseudoMap;
import org.openspice.jspice.datatypes.SpiceObject;

import java.util.*;

public final class MapLib {

	public final static Map convertTo( final Object obj ) {
		if ( obj instanceof Map ) {
			return (Map)obj;
        } else if ( obj instanceof String ) {
			return new PseudoMap.StringAsMap( (String)obj );
		} else if ( obj instanceof SpiceObject ) {
			return ((SpiceObject)obj).convertToMap();
		} else if ( obj instanceof List ) {
			return new PseudoMap.ListAsMap( (List)obj );
		} else {
			new Alert(
				"Map conversion failed",
				"An unsuitable object was used in a map context"
			).culprit( "object", obj ).mishap( 'E' );
			return null;	//	sop.
        }
    }
	
	public final static Object convertFrom( final Map map, final Object example ) {
		if ( example instanceof Map ) {
			return map;
		} else if ( map instanceof PseudoMap && ((PseudoMap)map).compatibleWith( example ) ) {
			return ((PseudoMap)map).getObject();
		} else if ( example instanceof String ) {
			final char[] chars = new char[ map.size() ];
            //final StringBuffer buffer = new StringBuffer();
            for ( Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
				final Map.Entry maplet = (Map.Entry)it.next();
				try {
					chars[ ((Integer)maplet.getKey()).intValue() ] = ((Character)maplet.getValue()).charValue();
				} catch ( final Exception exn ) {
					new Alert(
						"Cannot convert Maplet to a valid Character in a valid position",
						"Trying to convert a Map to a String"
					).culprit( "map", map ).mishap( 'E' );
				}
                //buffer.append( ( (Character)it.next()).charValue() );
            }
            return new String( chars );
		} else if ( example instanceof SpiceObject ) {
			return ((SpiceObject)example).convertFromMap( map );
		} else if ( example instanceof List ) {
			final Object[] objects = new Object[ map.size() ];
            for ( Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
				final Map.Entry maplet = (Map.Entry)it.next();
				try {
					objects[ ((Integer)maplet.getKey()).intValue() ] = maplet.getValue();
				} catch ( final Exception exn ) {
					new Alert(
						"Cannot putOne Maplet value in a valid position",
						"Trying to convert a Map to a List"
					).culprit( "map", map ).mishap( 'E' );
				}
                //buffer.append( ( (Character)it.next()).charValue() );
            }
            return Arrays.asList( objects );
		} else {
			new Alert(
				"Conversion from Map failed",
				"The conversion to maps is not always reversible"
			).
			culprit( "map", map ).
			culprit( "target-type", example.getClass().getName() ).
			mishap( 'E' );
			return null;	//	sop.
		}
	}

	public final static Object getAt( final Object obj, final Object key ) {
		if ( obj instanceof Map ) {
			return ((Map)obj).get( key );
		} else if ( obj instanceof String ) {
			try {
				return new Character( ((String)obj).charAt( ((Integer)key).intValue() - 1 ) );
			} catch ( final ClassCastException exn ) {
				//	Arguable.
				return AbsentLib.ABSENT;
			}
		} else if ( obj instanceof List ) {
			return ListLib.getAt( obj, key );
		} else {
			return convertTo( obj ).get( key );
		}
	}
	
	public final static Object putAt( final Object obj, final Object key, final Object val ) {
		if ( obj instanceof Map ) {
			return ((Map)obj).put( key, val );
		} else if ( obj instanceof List ) {
			return ListLib.putAt( obj, key, val );
		} else {
			new Alert(
				"cannot convert object to assignable map"
			).culprit( "object", obj ).mishap( 'E' );
			return null;	//	sop
		}
	}
	
	public final static Set entrySet( final Object obj ) {
		return convertTo( obj ).entrySet();
	}
	
}
