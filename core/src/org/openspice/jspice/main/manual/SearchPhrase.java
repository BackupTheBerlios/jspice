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
package org.openspice.jspice.main.manual;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class SearchPhrase {

	//	List< String >
	final LinkedList items;

	public SearchPhrase() {
		this( new LinkedList() );
	}

	private SearchPhrase( final LinkedList list ) {
		this.items = list;
	}

	public SearchPhrase add( final String s ) {
		this.items.add( s );
		return this;
	}

	public SearchPhrase addMatchAll() {
		return this.add( "//.*/" );
	}

	public SearchPhrase copy() {
		return new SearchPhrase( new LinkedList( items ) );
	}

	public SearchPhrase copyAdd( final String s ) {
		final SearchPhrase that = this.copy();
		that.add( s );
		return that;
	}

	public int size() {
		return this.items.size();
	}

	public String get( final int n ) {
		return (String)this.items.get( n );
	}

	private static final Pattern regex_pattern = Pattern.compile( "//(.*)/" );

	public boolean match( final int n, final String x ) {
		assert x != null;
		final String pattern = this.get( n );
		final Matcher matcher = regex_pattern.matcher( pattern );
		if ( matcher.find() ) {
			return x.matches( matcher.group( 1 ) );
		} else {
			return x.equals( pattern );
		}
	}

	public String exactlyMatches( final int n ) {
		final String p = this.get( n );
		final Matcher matcher = regex_pattern.matcher( p );
		return matcher.find() ? null : p;
	}

	public String toString() {
		final StringBuffer b = new StringBuffer();
		String gap = "";
		for ( Iterator it = this.items.iterator(); it.hasNext(); ) {
			b.append( gap );
			gap = " ";
			b.append( it.next() );
		}
		return b.toString();
	}

}
