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
import org.openspice.jspice.alert.Alert;

import java.util.*;

public final class ListTools {

	public final static List convertTo( final Object obj ) {
		if ( obj instanceof List ) {
			return (List)obj;
        } else if ( obj instanceof CharSequence ) {
			return new org.openspice.jspice.datatypes.lists.CharSequenceAsList( (CharSequence)obj );
		} else if ( obj instanceof org.openspice.jspice.datatypes.SpiceObject ) {
			return ((org.openspice.jspice.datatypes.SpiceObject)obj).convertToList();
		} else if ( obj instanceof Map ) {
			return new ArrayList( ( (Map)obj ).values() );
		} else {
			new Alert(
				"List conversion failed",
				"An unsuitable object was used in a list context"
			).
			culprit( "object", obj ).
			mishap( 'E' );
			return null;	//	sop.
        }
    }
	
	public final static Object convertFrom( final List list, final Object example ) {
		if ( example instanceof List ) {
			return list;
		} else if ( list instanceof PseudoList && ((PseudoList)list).compatibleWith( example ) ) {
			return ((PseudoList)list).getObject();
		} else if ( example instanceof CharSequence ) {
            final StringBuffer buffer = new StringBuffer();
            for ( Iterator it = list.iterator(); it.hasNext(); ) {
                buffer.append( ( (Character)it.next()).charValue() );
            }
            return buffer.toString();
		} else if ( example instanceof SpiceObject ) {
			return ((SpiceObject)example).convertFromList( list );
		} else {
			new Alert(
				"Conversion from List failed",
				"The conversion to lists is not always reversible"
			).
			culprit( "list", list ).
			culprit( "target-type", example.getClass().getName() ).
			mishap( 'E' );
			return null;	//	sop.
		}
	}

	public final static Object get( final Object obj, final int idx ) {
		if ( obj instanceof List ) {
			return ((List)obj).get( idx );
		} else if ( obj instanceof CharSequence ) {
			return new Character( ((CharSequence)obj).charAt( idx ) );
		} else if ( obj instanceof Map ) {
			return ((Map)obj).get( new Integer( idx ) );
		} else {
			return convertTo( obj ).get( idx );
		}
	}

	public final static Iterator iterator( final Object obj ) {
		if ( obj instanceof List ) {
			return ((List)obj).iterator();
		} else if ( obj instanceof Map ) {
			return ((Map)obj).values().iterator();
		} else {
			return convertTo( obj ).iterator();
		}
	}

	public static final List allbutfirst( final int n, final List list ) {
		return new ArrayList( list.subList( n, list.size() ) );
	}

	public static final List justfirst( final int n, final List list ) {
		return new ArrayList( list.subList( 0, n ) );
	}

	public static final List allbutlast( final int n, final List list ) {
		return new ArrayList( list.subList( 0, list.size() - n ) );
	}

	public static final List justlast( final int n, final List list ) {
		final int len = list.size();
		return new ArrayList( list.subList( len - n, len ) );
	}

	public static final List cons( final Object x, final List list ) {
		final ArrayList L = new ArrayList( list.size() + 1 );
		L.add( x );
		L.addAll( list );
		return L;
	}

	public static final List snoc( final List list, final Object x ) {
		final ArrayList L = new ArrayList( list );
		L.add( x );
		return L;
	}

	public static final List reverse( final List list ) {
		final Stack stack = new Stack();									//	todo: inefficient.
		for ( Iterator it = list.iterator(); it.hasNext(); ) {
			stack.push( it.next() );
		}
		final List answer = new ArrayList();
		while ( !stack.isEmpty() ) {
			answer.add( stack.pop() );
		}
		return answer;
	}

}
