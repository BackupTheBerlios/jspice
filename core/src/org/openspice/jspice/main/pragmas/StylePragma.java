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

import org.openspice.jspice.tools.StyleWarning;
import org.openspice.jspice.alert.Alert;

import java.util.List;
import java.util.Iterator;

public class StylePragma {

	public void enable( final List args ) {
		if ( args.isEmpty() )  {
			//	Report on the status of the style warnings.
			for ( Iterator it = StyleWarning.optionsIterator(); it.hasNext(); ) {
				final String option = (String)it.next();
				System.out.println( ( StyleWarning.isTrue( option ) ? "+" : "-" ) + option  );
			}
		} else {
			for ( Iterator it = args.iterator(); it.hasNext(); ) {
				String arg = ((String)it.next());
				boolean flag = true;
				if ( arg.startsWith( "+" ) ) {
					flag = true;
					arg = arg.substring( 1 );
				} else if ( arg.startsWith( "-" ) ) {
					flag = false;
					arg = arg.substring( 1 );
				}
				if ( !StyleWarning.isValid( arg ) ) {
					new Alert( "Unrecognized style warning" ).culprit( "style warning", arg ).warning();
				} else {
					StyleWarning.put( arg, flag );
				}
			}
		}
	}

}
