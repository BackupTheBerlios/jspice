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

import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.StdOutConsumer;

public class StartVersion {

	public static final void printVersion( final String control_string ) {
		PrintTools.formatTo(
			StdOutConsumer.OUT,
			control_string,
			new Object[] {
				FixedConf.getMajorVersion(),
				FixedConf.getMinorVersion(),
				FixedConf.getIncrementalVersion()
			}
		);
		System.out.println( "" );
	}

	public static final void main( final String[] args ) {
		final String control_string = args.length == 0 ? "%1p_%2p_%3p" : args[ 0 ];
		printVersion( control_string );
	}

}
