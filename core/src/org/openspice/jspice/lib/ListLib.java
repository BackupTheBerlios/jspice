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

	private static final Object get( final Object obj, final int n ) {
		try {
			if ( obj instanceof List ) {
				return ((List)obj).get( n );
			} else if ( obj instanceof CharSequence ) {
				return new Character( ((CharSequence)obj).charAt( n ) );
			} else if ( obj instanceof Map ) {
				return ((Map)obj).get( new Integer( n ) );
			} else {
				return ListTools.convertTo( obj ).get( n );
			}
		} catch ( java.lang.IndexOutOfBoundsException e ) {
			throw new Alert( "Index out of bounds" ).culprit(  "item", obj ).culprit( "index", new Integer( n ) ).mishap();
		}
	}

	private static final int size( final Object obj ) {
		try {
			if ( obj instanceof List ) {
				return ((List)obj).size();
			} else if ( obj instanceof CharSequence ) {
				return ((CharSequence)obj).length();
			} else {
				return ListTools.convertTo( obj ).size();
			}
		} catch ( final ClassCastException exn ) {
			throw new Alert(
				"Object cannot be converted to a list"
			).culprit( "object", obj ).mishap( 'E' );
		}
	}

	public static final Object first( final Object obj ) {
		return get( obj, 0 );
	}


	public static final Object last( final Object obj ) {
		return get( obj, size( obj ) - 1 );
	}

	public static final Object allbutfirst( final Object n, final Object obj ) {
		final int k = CastLib.to_int( n );
		if ( obj instanceof List ) {
			try {
				return ListTools.allbutfirst( k, (List)obj );
			} catch ( java.lang.IllegalArgumentException e ) {
				throw new Alert( e, "Invalid arguments" ).culprit( "number to remove", n ).culprit( "list", obj ).mishap();
			}
		} else if ( obj instanceof String ) {
			try {
				return ((String)obj).substring( k );
			} catch ( java.lang.StringIndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid arguments" ).culprit( "number to remove", n ).culprit( "string", obj ).mishap();
			}
		} else {
			try {
				return ListTools.convertFrom( ListTools.allbutfirst( k, ListTools.convertTo( obj ) ), obj );
			} catch ( java.lang.IllegalArgumentException e ) {
				throw new Alert( e, "Invalid arguments" ).culprit( "number to remove", n ).culprit( "list", obj ).mishap();
			}
		}
	}

	public static final Object justfirst( final Object n, final Object seq ) {
		final int k = CastLib.to_int( n );
		if ( seq instanceof List ) {
			try {
				return ListTools.justfirst( k, ((List)seq) );
			} catch ( java.lang.IndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to extract", n ).culprit( "list", seq ).mishap();
			}
		} else if ( seq instanceof String ) {
			try {
				return ((String)seq).substring( 0, k );
			} catch ( java.lang.StringIndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid arguments" ).culprit( "number to extract", n ).culprit( "string", seq ).mishap();
			}
		} else {
			try {
				return ListTools.convertFrom( ListTools.justfirst( k, ListTools.convertTo( seq ) ), seq );
			} catch ( java.lang.IndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to extract", n ).culprit( "list", seq ).mishap();
			}
		}
	}

	public static final Object allbutlast( final Object n, final Object seq ) {
		final int k = CastLib.to_int( n );
		if ( seq instanceof List ) {
			try {
				return ListTools.allbutlast( k, (List)seq );
			} catch ( java.lang.IllegalArgumentException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to remove", n ).culprit( "list", seq ).mishap();
			}
		} else if ( seq instanceof String ) {
			final String s = (String)seq;
			try {
				return ((String)seq).substring( 0, s.length() - k );
			} catch ( java.lang.StringIndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to remove", n ).culprit( "string", seq ).mishap();
			}
		} else {
			try {
				return ListTools.convertFrom( ListTools.allbutlast( k, ListTools.convertTo( seq ) ), seq );
			} catch ( java.lang.IllegalArgumentException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to remove", n ).culprit( "list", seq ).mishap();
			}
		}
	}

	public static final Object justlast( final Object n, final Object seq ) {
		final int k = CastLib.to_int( n );
		if ( seq instanceof List ) {
			try {
				return ListTools.justlast( k, (List)seq );
			} catch ( java.lang.IndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to extract", n ).culprit( "list", seq ).mishap();
			}
		} else if ( seq instanceof String ) {
			final String s = (String)seq;
			try {
				return s.substring( s.length() - k );
			} catch ( java.lang.StringIndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to extract", n ).culprit( "string", seq ).mishap();
			}
		} else {
			try {
				return ListTools.convertFrom( ListTools.justlast( k, ListTools.convertTo( seq ) ), seq );
			} catch ( java.lang.IndexOutOfBoundsException e ) {
				throw new Alert( e, "Invalid argument" ).culprit( "number to extract", n ).culprit( "list", seq ).mishap();
			}
		}
	}

	public static final Object cons( final Object x, final Object seq ) {
		if ( seq instanceof List ) {
			return ListTools.cons( x, (List)seq );
		} else if ( seq instanceof CharSequence ) {
			return CastLib.toCharacter( x ) + seq.toString();
		} else {
			return ListTools.convertFrom( ListTools.cons( x, ListTools.convertTo( seq ) ), seq );
		}

	}

	public static final Object snoc( final Object seq, final Object x ) {
		if ( seq instanceof List ) {
			return ListTools.snoc( (List)seq, x );
		} else if ( seq instanceof CharSequence ) {
			return seq.toString() + CastLib.toCharacter( x );
		} else {
			return ListTools.convertFrom( ListTools.snoc( ListTools.convertTo( seq ), x ), seq );
		}

	}

	public final static Object getAt( final Object obj, final Object key ) {
		try {
			final int idx = CastLib.to_int( key ) - 1;
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
	
	public final static void putAt( final Object obj, final Object key, final Object val ) {
		try {
			final int idx = CastLib.to_int( key ) - 1;
			if ( obj instanceof List ) {
				( (List)obj ).set( idx, val );
			} else if ( obj instanceof Map ) {
				( (Map)obj ).put( key, val );
			} else if ( obj instanceof StringBuffer ) {
				((StringBuffer)obj).setCharAt( idx, CastLib.toCharacter( val ).charValue() );
			} else {
				new Alert( "Cannot convert object to an assignable list" ).culprit( "object", obj ).mishap( 'E' );
			}
		} catch ( final ClassCastException exn ) {
			throw new Alert( "Index not an integer" ).culprit( "index", key ).culprit( "map", obj ).mishap( 'E' );
		} catch ( final UnsupportedOperationException e ) {
			throw new Alert( "Trying to update the index of an immutable object" ).culprit( "object", obj ).culprit( "index/key", key ).culprit( "value", val ).mishap( 'E' );
		}
	}
	
	public final static Object length( final Object obj ) {
		return new Integer( size( obj ) );
	}

	public static Object append( final Object x, final Object y ) {
		if ( x instanceof List && y instanceof List ) {
			final ArrayList list = new ArrayList( (List)x );
			list.addAll( (List)y );
			return list;
		} else if ( x instanceof CharSequence && y instanceof CharSequence ) {
			return x.toString() + y.toString();
		} else {
			new Alert(
				"Mismatched arguments for append"
			).culprit( "first", x ).culprit( "second", y ).mishap( 'E' );
			return null;	//	sop.
		}
	}

	public static final Object reverse( final Object x ) {
		if ( x instanceof List ) {
			return ListTools.reverse( (List)x );
		} else if ( x instanceof CharSequence ) {
			final CharSequence s = (CharSequence)x;
			final int len = s.length();
			final int len1 = len - 1;
			final char[] chars = new char[ len ];
			for ( int i = 0; i < len; i++ ) {
				final char ch = s.charAt( i );
				chars[ len1 - i ] = ch;
			}
			return new String( chars );
		} else {
			return ListTools.convertFrom( ListTools.reverse( ListTools.convertTo( x ) ), x );
		}
	}

}
