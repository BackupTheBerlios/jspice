/**
 *	JSpice, n Open Spice interpreter and library.
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
package org.openspice.jspice.tests;

import org.openspice.jspice.main.Symbol;

public class TestArrayHash {

	public static final void more() {
		Symbol aaa1 = Symbol.fetchSymbol( "aaa" );
		Symbol aaa2 = Symbol.fetchSymbol( "aaa" );
		Symbol bbb = Symbol.fetchSymbol( "bbb" );

		System.out.println( "aaa1 == aaa2? " + ( aaa1 == aaa2 ) );
		System.out.println( "aaa1 == bbb? " + ( aaa1 == bbb ) );
	}

	public static final void main( String[] args ) {
		System.out.println( "hash " + new char[0].hashCode() );
		System.out.println( "hash " + new char[0].hashCode() );
		System.out.println( "equals " + new char[0].equals( new char[ 0 ] ) );

		System.out.println( "1 < \"ferd\"? " + new Integer( 1 ).compareTo( "ferd" ) );

		more();
		Symbol.report();
		System.gc();
		Symbol.report();
	}
}
