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
package org.openspice.jspice.built_in.inspect;

import org.openspice.jspice.datatypes.proc.Unary0InvokeProc;
import org.openspice.jspice.datatypes.ImmutableList;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.tools.Pair;
import org.openspice.jspice.tools.StringBufferConsumer;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.ReadTools;
import org.openspice.jspice.tools.ListTools;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class InspectProc extends Unary0InvokeProc {

	public static final int short_length = 64;

	public static final String asShortString( final Object x ) {
		final StringBufferConsumer charout = new StringBufferConsumer();
		PrintTools.showTo( charout, x );
		String s = charout.closeAsString();
		if ( s.length() > short_length ) {
			s = s.substring( 0, short_length );
			s = s.replace( '\n', '_' ).replace( '\t', '_' ).replace( '\r', '_' );
		}
		return s;
	}

	public static final void offerSelection( final List fields, final Object x, final List path ) {
		for(;;) {
			System.out.print( "Select field by number or 'b' for back 'q' for quit: " );
			System.out.flush();
			final String reply = ReadTools.readLine();
			if ( "b".equals( reply ) ) {
				if ( path.isEmpty() ) {
					System.out.println( "At top level" );
				} else {
					inspect( path.get( 0 ), ListTools.allbutlast( 1, path ) );
					return;
				}
			} else if ( "B".equals( reply ) ) {
				if ( path.isEmpty() ) {
					inspect( x, path );
				} else {
					inspect( path.get( 0 ), ImmutableList.EMPTY_LIST );
				}
				return;
			} else if ( "q".equals( reply ) ) {
				return;
			} else {
				try {
					final int k = Integer.parseInt( reply );
					if ( k == 0 ) {
						inspect( x.getClass(), ListTools.snoc( path, x ) );
						return;
					} else if ( 0 < k && k <= fields.size() ) {
						final Object y = ((Pair)fields.get( k - 1 )).getRight();		//	1-index downshifted to 0-index.
						inspect( y, ListTools.snoc( path, x ) );
						return;
					} else {
						System.out.println( "Selection out of range" );
					}
				} catch ( final NumberFormatException e ) {
					System.out.println( "Unrecognized option" );
				}
			}
		}
	}

	public static final List fromIterator( final Iterator it ) {
		final List fields = new ArrayList();
		while ( it.hasNext() ) {
			fields.add( new Pair( null, it.next() ) );
		}
		return fields;
	}

	public static final List getInstanceFields( final Object x ) {
		if ( x == null ) {
			return ImmutableList.EMPTY_LIST;
		} else if ( x instanceof List ) {
			return fromIterator( ((List)x).iterator() );
		} else if ( x instanceof Map ) {
			return fromIterator( ((Map)x).entrySet().iterator() );
		} else if ( x instanceof String ) {
			return fromIterator( ListTools.convertTo( x ).iterator() );
		} else if ( x instanceof SpiceObject ) {
			return ((SpiceObject)x).getInstanceFields();
		} else {
			final Class c = x.getClass();
			final Field[] all_fields = c.getDeclaredFields();
			final List fields = new ArrayList();
			for ( int j = 0; j < all_fields.length; j++ ) {
				final Field f = all_fields[ j ];
				final int mods = f.getModifiers();
				if ( ! Modifier.isStatic( mods ) ) {
					try {
						fields.add( new Pair( f.getName(), f.get( x ) ) );
					} catch ( IllegalAccessException e ) {
						fields.add( new Pair( f.getName(), new InaccessibleValue() ) );
					}
				}
			}
			return fields;
		}
	}

	public static final void inspect( final Object x, final List path ) {
		//	Short one-line summary.
		System.out.println( "[Summary]\t" + asShortString( x ) );
		//	Type.
		if ( x != null ) {
			System.out.println( "[0. Type]\t" + x.getClass().getName() );
		}
		//	Members.
		//		List< Pair< String, Object > >
		final List fields = getInstanceFields( x );
		//	Offer selection.
		{
			int i = 0;
			for ( Iterator it = fields.iterator(); it.hasNext(); i++ ) {
				final Pair pair = (Pair)it.next();						//	Pair< String, Object >
				final String name = (String)pair.getLeft();
				final Object value = pair.getRight();
				final String first = "" + ( i + 1 );
				final String second = name == null ? "" : ( ". " + name );
				System.out.println( "[" + first + second + "]\t" + asShortString( value ) );
			}
			offerSelection( fields, x, path );
		}
	}

	public void invoke( final Object x ) {
		inspect( x, ImmutableList.EMPTY_LIST );
	}

	public static final InspectProc INSPECT_PROC = new InspectProc();
}
