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
package org.openspice.tools;

public final class BooleanTools {

	public static final Boolean TRUE = Boolean.TRUE;
	public static final Boolean FALSE = Boolean.FALSE;

	public static Boolean or( final Object a, final Object b ) {
		return Boolean.valueOf( ( (Boolean)a ).booleanValue() || ( (Boolean)b ).booleanValue() );
	}

	public static Boolean and( final Object a, final Object b ) {
		return Boolean.valueOf( ( (Boolean)a ).booleanValue() && ( (Boolean)b ).booleanValue() );
	}

	public static Boolean not( final Boolean a ) {
		return Boolean.valueOf( !a.booleanValue() );
	}

	public static Boolean not( final Object a ) {
		return Boolean.valueOf( ( !( (Boolean)a ).booleanValue() ) );
	}

}

