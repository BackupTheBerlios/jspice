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
package org.openspice.jspice.tools;

import org.openspice.jspice.datatypes.lists.PseudoList;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.jspice.datatypes.maps.PseudoMap;
import org.openspice.jspice.datatypes.maps.*;
import org.openspice.jspice.alert.Alert;

import java.util.*;

public final class MapTools {

	public final static Map convertTo( final Object obj ) {
		if ( obj instanceof Map ) {
			return (Map)obj;
		} else if ( obj instanceof List ) {
			return new ListAsMap( (List)obj );
        } else if ( obj instanceof CharSequence ) {
			return new CharSequenceAsMap( (String)obj );
		} else if ( obj instanceof SpiceObject ) {
			return ((SpiceObject)obj).convertToMap();
		} else {
			new Alert(
				"Map conversion failed",
				"An unsuitable object was used in a map context"
			).
			culprit( "object", obj ).
			mishap( 'E' );
			return null;	//	sop.
        }
    }
	
	public final static Object convertFrom( final Map map, final Object example ) {
		if ( example instanceof Map ) {
			return map;
		} else if ( map instanceof PseudoMap && ((PseudoMap)map).compatibleWith( example ) ) {
			return ((PseudoList)map).getObject();
		} else if ( example instanceof CharSequence ) {
			return ListTools.convertFrom( ListTools.convertTo( map ), example );
		} else if ( example instanceof SpiceObject ) {
			return ((SpiceObject)example).convertFromMap( map );
		} else {
			throw new Alert(
				"Conversion from Map failed",
				"The conversion to maps is not always reversible"
			).
			culprit( "map", map ).
			culprit( "target-type", example.getClass().getName() ).
			mishap( 'E' );
		}
	}

}
