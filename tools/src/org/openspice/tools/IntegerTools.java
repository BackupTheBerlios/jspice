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

public abstract class IntegerTools {

	final static public Integer MINUS_ONE = new Integer( -1 );
	final static public Integer ZERO = new Integer( 0 );
	final static public Integer ONE = new Integer( 1 );

	//  Literal integers from (say) -32 to 31 are shared.
    final static int width = 32;
    final static Integer[] integers = new Integer[ 2 * width ];

    static {
        for ( int i = 0-width; i < width; i++ ) {
            integers[ i + width ] = new Integer( i );
        }
    }

    public static Integer make( final int n ) {
        return (
            -width <= n && n < width ? integers[ width + n ] :
            new Integer( n )
        );
    }

}