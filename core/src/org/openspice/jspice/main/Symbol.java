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
package org.openspice.jspice.main;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.Serializable;

/**
 * This class need not be final - I have done this as general good practice.
 * As a rough rule: it is good practice for your own internal classes to be
 * defined as final and bad practice for external classes to be defined as
 * final.  This rule emerges from a pair of design errors in the language
 * ((*sigh*),(*sigh*)) that you must circumvent.
 *
 * Symbols implement CharSequence, since Java 1.4 introduces this feeble but
 * welcome interface to the disaster area of string-like classes.  But it
 * cannot extend String or StringBuffer because they are final.  (*sigh*)
 * The embarrassing CharSequence is their only shared interface.
 *
 * Anyone for assembly programming?
 */
public final class Symbol implements CharSequence, Comparable, Serializable {

	/**
	 * Because Java incorrectly defines the equality for arrays (*sigh*) we
	 * have to take over.  I notice that there are some improvements in Java 1.5
	 * in this general area.  Breath holding is not advocated.
	 *
	 * Note that I do not implement the usual alphabetic ordering.  Nothing
	 * says you have to - and this one is far more efficient.  Don't like it?
	 * Unlucky, 'cos the Java team made a good call on this one.
	 */
	static class CharArrayComparator implements Comparator {

		/**
		 * We need the static method to avoid a pointless allocation cost
		 * when Symbol comparison.  But because Java does not have first
		 * class functions, we also need to implement it as an instance
		 * method.  More stupid plumbing games caused by more stupid
		 * design mistakes.  (*sigh*)
		 */
		public static final int compareTo( final Object x, final Object y ) {
			final char[] xs = (char[])x;
			final char[] ys = (char[])y;
			if ( xs.length < ys.length ) return -1;
			if ( xs.length > ys.length ) return 1;
			for ( int i = 0; i < xs.length; i++ ) {
				final char xc = xs[ i ];
				final char yc = ys[ i ];
				if ( xc < yc ) return -1;
				if ( xc > yc ) return 1;
			}
			return 0;
		}

		//	And ignoring case ....
		public static final int compareToIgnoreCase( final Object x, final Object y ) {
			final char[] xs = (char[])x;
			final char[] ys = (char[])y;
			if ( xs.length < ys.length ) return -1;
			if ( xs.length > ys.length ) return 1;
			for ( int i = 0; i < xs.length; i++ ) {
				final char xc = Character.toLowerCase( xs[ i ] );
				final char yc = Character.toLowerCase( ys[ i ] );
				if ( xc < yc ) return -1;
				if ( xc > yc ) return 1;
			}
			return 0;
		}

		public int compare( final Object x, final Object y ) {
			return compareTo( x, y );
		}

	}

	//	This is the only slot of Symbols - the array of characters.  We
	//	cannot use a String because of the notorious poor implementation of
	//	Strings.  (*sigh*)
	private final char[] contents;

	//	The constructor must be hidden.
	private Symbol( final char[] chars ) {
		this.contents = chars;
	}

	/**
	 * This is where we will keep the symbols.  Because WeakHashMaps are
	 * "tmpboth" and not "tmpval" (or "tmparg") we have to manually implement
	 * a tmpval mapping.
	 * 		TreeMap< char[], WeakReference( Symbol ) >
	 * Unfortunately this
	 */
    private static TreeMap table = new TreeMap( new CharArrayComparator() );

	/**
	 * Report some internal stats on the table.
	 */
	public static final void report() {
		System.out.println( "Size of table: " + table.size() );
		int gone_count = 0;
		for ( Iterator it = table.values().iterator(); it.hasNext(); ) {
			final WeakReference ref = (WeakReference)it.next();
			if ( ref.get() == null ) gone_count += 1;
		}
		System.out.println( "  #gone = " + gone_count );
	}


	/**
	 * When Symbols are garbage collected, we take the chance to clean up
	 * the table.
	 */
	protected void finalize() throws Throwable {
		final WeakReference ref = (WeakReference)this.table.get( this.contents );
		//	Check that the reference is _still_ to itself.  It does not have to be.
		if ( ref.get() == null ) {
			//	It is - so knock it out.
			table.remove( this.contents );
		}
	}

	private static Symbol fetch( final char[] immutable_chars ) {
		final WeakReference ref = (WeakReference)table.get( immutable_chars );
		if ( ref == null ){
			final Symbol sym = new Symbol( immutable_chars );
			table.put( immutable_chars, new WeakReference( sym ) );
//			System.gc();		//	Need to do at least one test with this in.
			return sym;
		} else {
			Symbol sym = (Symbol)ref.get();
			if ( sym == null ) {
				sym = new Symbol( immutable_chars );
				table.put( immutable_chars, sym );
			}
			return sym;
		}
	}

	/**
	 * This is the main allocator for Symbols.  Given a String (which could be
	 * widened to include other types) it returns a Symbol.  It returns a new
	 * Symbol if required - otherwise the old matching Symbol.
	 */
	public static Symbol fetchSymbol( final String name ) {
		return fetch( name.toCharArray() );
    }

	/**
	 * But this subsidary allocator is useful, to.
	 */
	public static Symbol fetchSymbol( final CharSequence seq ) {
		final char[] chars = new char[ seq.length() ];
		for ( int i = 0; i < chars.length; i++ ) {
			chars[ i ] = seq.charAt( i );
		}
		return fetch( chars );
	}

	/**
	 * Not clear what this should return because of the disaster area
	 * that Java has made of printing.  So we do the obvious thing which
	 * is inevitably wrong in some circumstances.  (*sigh*)
	 */
	public String toString() {
		return this.asString();
	}

	public int length() {
		return this.contents.length;
	}

	public char charAt( final int i ) {
		return this.contents[ i ];
	}

	public String asString() {
		return new String( this.contents );
	}

	//	Lazy.
	public CharSequence subSequence( final int i, final int i1 ) {
		//	Do _not_ make the mistake of calling "toString" here.  It is a
		//	coincidence that the strings are the same.
		return this.asString().subSequence( i, i1 );
	}

	public int compareTo( final Symbol that ) {
		return CharArrayComparator.compareTo( this.contents, that.contents );
	}

	public int compareTo( final Object obj ) {
		/**
		 * As far as I can work out, this downcast is legitimized by the phrase:
		 * 	throws ClassCastException - if the specified object's type prevents it
		 * 	from being compared to this Object.
		 */
		return this.compareTo( (Symbol)obj );
	}

	public int compareToIgnoreCase( final Symbol that ) {
		return CharArrayComparator.compareToIgnoreCase( this.contents, that.contents );
	}

	public boolean equalsIgnoreCase( final Symbol that ) {
		return this.compareToIgnoreCase( that ) == 0;
	}

	public Symbol concat( final Symbol that ) {
		final StringBuffer b = new StringBuffer();
		b.append( this.contents );
		b.append( that.contents );
		return fetchSymbol( b );
	}

	//	Lazy.
	public boolean endsWith( final String s ) {
		return this.asString().endsWith( s );
	}

	//	Lazy
	public byte[] getBytes() {
		return this.asString().getBytes();
	}

	//	Lazy
	public byte[] getBytes( final String charSetName ) throws java.io.UnsupportedEncodingException {
		return this.asString().getBytes( charSetName );
	}

	//	Lazy
	public void getChars( int srcBegin, int srcEnd, char[] dst, int dstBegin ) {
		this.asString().getChars( srcBegin, srcEnd, dst, dstBegin );
	}

	public int indexOf( final String s ) {
		return this.asString().indexOf( s, 0 );
	}

	public int indexOf( final char ch ) {
		return this.indexOf( ch, 0 );
	}

	//	Lazy.
	public int indexOf( final String s, final int fromIndex ) {
		return this.asString().indexOf( s, fromIndex );
	}

	public int indexOf( final char ch, final int fromIndex ) {
		for ( int i = fromIndex; i < this.contents.length; i++ ) {
			final char x = this.contents[ i ];
			if ( x == ch ) return i;
		}
		return -1;
	}

	public String substring( int beginIndex ) {
		return this.substring( beginIndex, this.length() );
	}

	//	Lazy.
	public String substring( int beginIndex, int endIndex ) {
		return this.asString().substring( beginIndex, endIndex );
	}

	public Symbol subsymbol( int beginIndex ) {
		return this.subsymbol( beginIndex, this.length() );
	}

	//	Slightly Lazy.
	public Symbol subsymbol( int beginIndex, int endIndex ) {
		return fetchSymbol( this.substring( beginIndex, endIndex ) );
	}

	public char[] toCharArray() {
		//	Must copy.
		final char[] answer = new char[ this.contents.length ];
		System.arraycopy( this.contents, 0, answer, 0, this.contents.length );
		return answer;
	}

	public Symbol toLowerCase() {
		boolean changed = false;
		final char[] chars = this.toCharArray();
		for ( int i = 0; i < chars.length; i++ ) {
			final char ch = chars[ i ];
			final char lch = Character.toLowerCase( ch );
			if ( ch != lch ) {
				chars[ i ] = lch;
				changed = true;
			}
		}
		return changed ? fetch( chars ) : this;
	}

	public Symbol toUpperCase() {
		boolean changed = false;
		final char[] chars = this.toCharArray();
		for ( int i = 0; i < chars.length; i++ ) {
			final char ch = chars[ i ];
			final char lch = Character.toUpperCase( ch );
			if ( ch != lch ) {
				chars[ i ] = lch;
				changed = true;
			}
		}
		return changed ? fetch( chars ) : this;
	}

	//	Lazy
	public Symbol toLowerCase( final java.util.Locale locale ) {
		return fetchSymbol( this.asString().toLowerCase( locale ) );
	}

	//	Lazy
	public Symbol toUpperCase( final java.util.Locale locale ) {
		return fetchSymbol( this.asString().toUpperCase( locale ) );
	}

	//	Lazy
	public boolean startsWith( final String prefix ) {
		return this.asString().startsWith( prefix );
	}

	//	Lazy
	public boolean startsWith( final String prefix, final int toffset ) {
		return this.asString().startsWith( prefix, toffset );
	}

	//	Lazy
	public Symbol trim() {
		return fetchSymbol( this.asString().trim() );
	}

	//	And many other methods too boring to implement right now ...
	//		lastIndexOf (4 variants)
	//		matches
	//		regionMatches (2 variants)
	//		replace
	//		replaceAll
	//		replaceFirst
	//		split (2 variants)

}
