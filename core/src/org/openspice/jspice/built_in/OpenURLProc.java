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
package org.openspice.jspice.built_in;

import org.openspice.jspice.datatypes.proc.Unary0InvokeProc;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.lib.ConvertLib;
import org.openspice.browser_control.BrowserControl;

import java.io.IOException;

public class OpenURLProc extends Unary0InvokeProc {

	final BrowserControl browser_control;

	public OpenURLProc( BrowserControl browser_control ) {
		this.browser_control = browser_control;
	}

	public void invoke( Object x ) {
		try {
			this.browser_control.displayURL( ConvertLib.convertString( x ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final OpenURLProc OPEN_URL_PROC = new OpenURLProc( BrowserControl.build() );

}
