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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.MiniParser;
import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.ApplyExpr;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.built_in.ShortCuts;
import org.openspice.jspice.built_in.lists.NewImmutableListProc;
import org.openspice.jspice.built_in.maps.NewMapProc;
import org.openspice.jspice.built_in.maps.NewImmutableMapProc;

public final class ListMiniParser extends Prefix {

	public Expr prefix( final String interned, final Parser parser ) {
		if ( parser.tryReadToken( "%" ) != null ) {
			final Expr contents = parser.readStmntsTo( "%" );
			parser.mustReadToken( "}" );
			return ApplyExpr.make( NewImmutableMapProc.NEW_IMMUTABLE_MAP_PROC, contents );
		} else {
			final Expr contents = parser.readStmntsTo( "}" );
			return ApplyExpr.make( NewImmutableListProc.NEW_IMMUTABLE_LIST_PROC, contents );
		}
	}

}
