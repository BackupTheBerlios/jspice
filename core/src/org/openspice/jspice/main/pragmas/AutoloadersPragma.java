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
package org.openspice.jspice.main.pragmas;

import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.conf.LoaderBuilderRecord;

import java.util.Iterator;

public class AutoloadersPragma {

	final JSpiceConf jspice_conf;

	public AutoloadersPragma( JSpiceConf jspice_conf ) {
		this.jspice_conf = jspice_conf;
	}

	public void list() {
		for ( Iterator it = this.jspice_conf.getAutoloaders().iterator(); it.hasNext(); ) {
			final LoaderBuilderRecord r = (LoaderBuilderRecord)it.next();
			final String extn = r.getExtension();
			System.out.print( extn );
			final int len = 4 - extn.length();
			for ( int i = 0; i < len; i++ ) {
				System.out.print( " " );
			}
 			System.out.println( "\t: " + r.getComment() );

		}
	}

}
