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
import org.openspice.jspice.parse.Prec;

import java.util.*;

public abstract class MiniParser {

	public abstract Expr parse( final String sym, final int prec, final Expr lhs, final Parser parser );
	
	public int getPrec() {
		return Prec.one; 				//	Fairly arbitrary, although -infinity might be better?
	}

}

