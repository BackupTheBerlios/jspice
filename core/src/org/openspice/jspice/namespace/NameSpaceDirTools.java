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


public final class NameSpaceDirTools {

	static String isPackageDirectory( final String dirname ) {
		if ( !dirname.endsWith( Globals.NAME_SPACE_SUFFIX ) ) return null;
		return (
			dirname.substring( 
				0, 
				dirname.length() - Globals.NAME_SPACE_SUFFIX.length()
			).replace( '.', '_' )
		);
	}
	
	private static String packageDirKernel( final String cname ) {
		return cname.replace( '.', '_' );
	}
	
	static String packageDirInitFile( final String cname ) {
		return packageDirKernel( cname ) + Globals.SPICE_EXTN;
	}
	
	static String packageDirName( final String cname ) {
		return packageDirKernel( cname ) + Globals.NAME_SPACE_SUFFIX;
	}

}
