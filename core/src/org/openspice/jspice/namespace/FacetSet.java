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
package org.openspice.jspice.namespace;
import java.util.*;

/**
 * I'm not very happy with the implementation of FacetSets.  I dislike the
 * inclusive/exclusive trick.  It might have been more elegant to have
 * subclassed it into Inclusive/Exclusive.
 */
public class FacetSet {
	
	final boolean use_members;
	final TreeSet members;
	
	private FacetSet( final boolean _use_members  ) {
		this.use_members = _use_members;
		this.members = this.use_members ? new TreeSet() : null;
	}
	
	private FacetSet( final FacetSet proto ) {
		this.use_members = proto.use_members;
		this.members = this.use_members ? new TreeSet( proto.members ) : null;
	}
	
	private FacetSet( final String facet ) {
		this.use_members = true;
		this.members = new TreeSet();
		this.members.add( facet );
	}
	
	public String toString() {
		if ( this.use_members ) {
			final StringBuffer buff = new StringBuffer();
			buff.append( '[' );
			String gap = "";
			final Iterator it = this.members.iterator();
			while ( it.hasNext() ) {
				buff.append( gap );
				buff.append( it.next() );
				gap = ", ";
			}
			buff.append( ']' );
			return buff.toString();
		} else {
			return "[-all-]";
		}
	}

	public FacetSet addFacet( final String facet ) {
		if ( this.use_members ) {
			final FacetSet s = new FacetSet( this );
			s.members.add( facet );
			return s;
		} else {
			return this;
		}
	}

	/**
	 * Returns true if the intersection is non-empty.
	 * @param fs a facet set
	 * @return flag indicating the intersection of the sets is non-empty
	 */
	public boolean match( final FacetSet fs ) {
		if ( this.use_members ) {
			if ( fs.use_members ) {
				final Iterator it = this.members.iterator();
				while ( it.hasNext() ) {
					if ( fs.members.contains( it.next() ) ) return true;
				}
				return false;
			} else {
				return !this.members.isEmpty();
			}
		} else {
			return !fs.use_members || !fs.members.isEmpty();
		}
	}

	
	static public final FacetSet NONE = new FacetSet( true );
	static public final FacetSet ALL = new FacetSet( false );
	static public final FacetSet PUBLIC = new FacetSet( "public" );
	static public final FacetSet PRIVATE = new FacetSet( "private" );
	static public final FacetSet COMMON = new FacetSet( "common" );
	static public final FacetSet EXTERNAL = new FacetSet( "external" );

	static public Map WELL_KNOWN_MAP = new TreeMap();
	static {
		WELL_KNOWN_MAP.put( "private", PRIVATE );
		WELL_KNOWN_MAP.put( "public", PUBLIC );
		WELL_KNOWN_MAP.put( "common", COMMON );
		WELL_KNOWN_MAP.put( "external", EXTERNAL );
	}

	static public List WELL_KNOWN = new ArrayList( WELL_KNOWN_MAP.keySet() );

	static boolean isWellKnownFacet( final String name ) {
		return WELL_KNOWN_MAP.get( name ) != null;
	}

	static FacetSet make( final String name ) {
		final FacetSet s = (FacetSet)WELL_KNOWN_MAP.get( name );
		if ( s != null ) return s;
		return new FacetSet( name );
	}
}

