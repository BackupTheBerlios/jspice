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

import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Proc;

public final class ShowProcs {

	static final  class ShowlnProc extends PrintTools.SpiceShowStdOutProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( last ) {
				cuchar.ln();
			} else if ( !first ) {
				cuchar.outCharSequence( ", " );
			}
		}
	}
	public static final Proc showlnProc = new ShowlnProc();

	static final  class ShowlnToProc extends PrintTools.SpiceShowToProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( last ) {
				cuchar.ln();
			} else if ( !first ) {
				cuchar.out( ' ' );
			}
		}
	}
	public static final Proc showlnToProc = new ShowlnToProc();


	static final  class ShowProc extends PrintTools.SpiceShowStdOutProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( !first && !last ) cuchar.outCharSequence( ", " );
		}
	}
	public static final Proc showProc = new ShowProc();

	static final  class ShowToProc extends PrintTools.SpiceShowToProc {
		public void gap( final boolean first, final boolean last, final Consumer cuchar ) {
			if ( !first && !last ) cuchar.outCharSequence( ", " );
		}
	}
	public static final Proc showToProc = new ShowToProc();

}
