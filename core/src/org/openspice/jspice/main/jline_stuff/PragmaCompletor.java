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
package org.openspice.jspice.main.jline_stuff;

import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.main.Pragma;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class PragmaCompletor implements jline.Completor {

	final JSpiceConf jspice_conf;

	public PragmaCompletor( JSpiceConf jspice_conf ) {
		this.jspice_conf = jspice_conf;
	}


	public int complete( String buff, final int cursor, final List clist ) {
		if ( buff.length() == 0 ) {
			//	Skip
		} else if ( buff.charAt( 0 ) == '#' ) {
			buff = buff.substring( 1 );
		} else {
			return -1;
		}

		final List tmp = new ArrayList();
		final PrefixFilterAccumulator acc = new PrefixFilterAccumulator( buff, tmp );
		new Pragma( this.jspice_conf, "" ).findPragmaCompletions( acc );	//	null & "" are the appropriate dummy parameters.

		if ( tmp.size() == 0 ) return -1;

		for ( Iterator it = tmp.iterator(); it.hasNext(); ) {
			final String candidate = "#" + (String)it.next();
//			System.err.println( "adding candidate = '" + candidate + "'"  );
			clist.add( candidate );
		}

		return 0;
	}

}