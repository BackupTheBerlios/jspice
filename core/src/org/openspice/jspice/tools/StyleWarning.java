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

import org.openspice.jspice.alert.Alert;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class StyleWarning {

	public static final String DOT_RHS_PARENTHESES = "DotRHS";
	public static final String SINGLE_CHAR_LITERAL = "OneChar";

	public static final Map options = new HashMap();
	static {
		options.put( DOT_RHS_PARENTHESES, Boolean.FALSE );
		options.put( SINGLE_CHAR_LITERAL, Boolean.FALSE );
	}

	public static final boolean isTrue( final String key ) {
		final Object x = options.get( key );
		if ( ! ( x instanceof Boolean ) ) return false;
		return ((Boolean)x).booleanValue();
	}

	public static final boolean isValid( final String key ) {
		return options.get( key ) != null;
	}

	public static final void put( final String key, final boolean value ) {
		options.put( key, Boolean.valueOf( value ) );
	}

	public static Iterator optionsIterator() {
		return new TreeSet( options.keySet() ).iterator();
	}

	//	---oooOOOooo---

	public static final void dot_rhs_parentheses() {
		if ( isTrue( DOT_RHS_PARENTHESES )  ) {
			new Alert( "Using dot operator with unbracketed right hand side" ).warning();
		}
	}

	public static final void one_char_literal( final String s ) {
		if ( isTrue( SINGLE_CHAR_LITERAL ) ) {
			new Alert( "Using multi-character literal" ).culprit( "literal", s ).warning();
		}
	}

}
