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

abstract class Mode {

	boolean isInteractive() {
		return false;
	}

	static class App extends Mode {}
	static class CGI extends Mode {}
	static class Filter extends Mode {}
	static class Server extends Mode {}

	static class Dev extends Mode {
		boolean isInteractive() {
			return true;
		}
	}


	public static final Mode tryNewMode( final String mode_option ) {
		if ( mode_option.equals( "--app" ) ) return new App();
		if ( mode_option.equals( "--cgi" ) ) return new CGI();
		if ( mode_option.equals( "--filter" ) ) return new Filter();
		if ( mode_option.equals( "--server" ) ) return new Server();
		if ( mode_option.equals( "--dev" ) ) return new Dev();
		return null;
	}

}
