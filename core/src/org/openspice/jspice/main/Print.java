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

public class Print {
	
	public static final int INFO = 1 << 0;
	public static final int LOAD = 1 << 1;
	public static final int CONFIG = 1 << 2;
	public static final int AUTOLOAD = 1 << 3;
	public static final int IMPORT = 1 << 4;
	public static final int HELP = 1 << 5;
	public static final int PARSE = 1 << 6;
	public static final int VFS = 1 << 7;

	public static final int DEBUGGING = (
		Print.INFO | Print.LOAD | Print.CONFIG | Print.AUTOLOAD
	);

	public static int current_mode = 0; 	//Print.VFS | Print.AUTOLOAD;

	public static final void printMode( final int mode ) {
		System.out.print( "JSPICE(" );
		System.out.print( ( ( mode & INFO ) != 0 ) ? "." : "" );
		System.out.print( ( ( mode & LOAD ) != 0 ) ? "l" : "" );
		System.out.print( ( ( mode & CONFIG ) != 0 ) ? "c" : "" );
		System.out.print( ( ( mode & AUTOLOAD ) != 0 ) ? "a" : "" );
		System.out.print( ( ( mode & IMPORT ) != 0 ) ? "i" : "" );
		System.out.print( ( ( mode & HELP ) != 0 ) ? "h" : "" );
		System.out.print( ( ( mode & PARSE ) != 0 ) ? "p" : "" );
		System.out.print( ( ( mode & VFS ) != 0 ) ? "v" : "" );
		System.out.print( "): " );
	}

	public static final void println( final int mode, final Object s ) {
		if ( ( current_mode & mode ) != 0 ) {
			printMode( mode );
			Print.println( s );
		}
	}

	public static final void println( final Object s ) {
		System.out.println( s == null ? "null" : s.toString() );
	}

	public static final boolean wouldPrint( final int mode ) {
		return ( current_mode & mode ) != 0;
	}

}
