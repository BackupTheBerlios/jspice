package org.openspice.jspice.datatypes;

import org.openspice.jspice.alert.Alert;

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


public final class Arity {

	public static final class ArityException extends Exception {
	}

	final private boolean more;
	final private int count;
	
	public Arity( final int _count, final boolean _more ) throws ArityException {
		if ( _count >= 0 ) {
			this.more = _more;
			this.count = _count;
		} else {
			throw new ArityException();
		}
	}
	
	public Arity( final int n ) throws ArityException {
		this( n, false );
	}
	
	public Arity( final Arity arity, final boolean _more ) {
		this.count = arity.count;
		this.more = _more;
	}
	
	static private final Arity make( final int _count, final boolean _more ) {
		try {
			return new Arity( _count, _more );
		} catch ( final ArityException exn ) {
			//	Never happens.
			assert false;
			return null;	//	sop
		}
	}
	
	public static final Arity ZERO = make( 0, false );
	public static final Arity ONE = make( 1, false );
	public static final Arity TWO = make( 2, false );
	public static final Arity THREE = make( 3, false );
	
	public static final Arity ZERO_OR_MORE = make( 0, true );
	public static final Arity ONE_OR_MORE = make( 1, true );
	
	public Arity add( final Arity that ) {
		try {
			return new Arity( this.count + that.count, this.more || that.more );
		} catch ( final ArityException exn ) {
			//	Never happens.
			assert false;
			return null;	//	sop
		}
	}
	
	public Arity sub( final Arity that ) throws ArityException {
		if ( that.more ) {		
			return ZERO_OR_MORE;
		} else {
			final int a = this.count;
			final int b = that.count;
			return new Arity( a - b, this.more );
		}
	}

	public Arity sub( final int b )  throws ArityException {
		return new Arity( this.count - b, this.more );
	}

	public boolean hasFixedCount( final int n ) {
		return !this.more && this.count == 1;
	}
	
	public boolean isVariadic() {
		return this.more;
	}
	
	public boolean isFixed() {
		return !this.more;
	}
	
	public int getCount() {
		return this.count; 
	}
	
	public boolean equals( final Object obj ) {
		if ( ! ( obj instanceof Arity ) ) return false;
		final Arity x = (Arity)obj;
		return this.more == x.more && this.count == x.getCount();
	}
	
	public boolean satisfiedBy( final int n ) {
		return n == this.count || this.more && n > this.count;
	}
	
	public boolean tooFew( final int n ) {
		return n < this.count;
	}
	
	public void check( final int nargs ) {
		if ( nargs < this.count ) {
			new Alert( "Too few arguments" ).mishap( 'E' );
		} else if ( nargs > this.count && !this.more ) {
			new Alert( "Too many arguments" ).mishap( 'E' );
		}
	}
	
	public void failCheck( final int nargs ) {
		this.check( nargs );
		Alert.unreachable();
	}
	
	public static void failCheck( final int count, final int nargs ) {
		try {
			new Arity( count ).failCheck( nargs );
		} catch ( final ArityException exn ) {
			Alert.unreachable( exn );
		}
	}
	
	public String toString() {
		return "<" + this.count + "|" + ( this.more ? "+>" : ".>" );
	}

	public String toNiceString() {
		return this.count + ( this.more ? "+" : "" );
	}
}
