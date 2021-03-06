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
package org.openspice.jspice.parse.miniparser;

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.alert.AlertException;

public final class ReservedMiniParser extends MiniParser {

	final String alt_message;

	public ReservedMiniParser( String message ) {
		this.alt_message = message;
	}

	public ReservedMiniParser() {
		this.alt_message = null;
	}

	public Expr parse( String interned, int prec, Expr lhs, Parser parser ) {
		Alert alert = new Alert( "Keyword reserved for future use" ).culprit( "keyword", interned );
		if ( this.alt_message != null ) {
			alert = alert.hint( "Use alternative, " + this.alt_message );
		}
		throw alert.mishap();
	}

}
