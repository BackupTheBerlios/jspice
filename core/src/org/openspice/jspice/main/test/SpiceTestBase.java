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
package org.openspice.jspice.main.test;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import org.openspice.jspice.main.StringInterpreter;

public class SpiceTestBase extends TestCase {

	public static final List one( final Object x ) {
		final List list = new ArrayList();
		list.add( x );
		return list;
	}

	public static final List two( final Object x, final Object y ) {
		final List list = new ArrayList();
		list.add( x );
		list.add( y );
		return list;
	}

	public static final List three( final Object x, final Object y , final Object z ) {
		final List list = new ArrayList();
		list.add( x );
		list.add( y );
		list.add( z );
		return list;
	}

	public static final List interpret( final String s ) {
		return new StringInterpreter().interpret( s );
	}

}
