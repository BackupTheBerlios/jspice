package org.openspice.jspice.expr;

import org.openspice.jspice.expr.cases.NameExpr;

import java.util.*;

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

public class TmpVarGenerator {

	static long edition_count = 0;
	
	final long edition;
	long version = 0;
	final LinkedList dump = new LinkedList();
	
	public TmpVarGenerator() {
		this.edition = edition_count++;
	}
	
	public NameExpr newTmpVar() {
		return NameExpr.make( new TmpName( this.edition, this.version++ ) );
	}
	
	public void save() {
		this.dump.addLast( new Long( this.version ) );
	}
	
	public void restore() {
		this.version = ((Long)this.dump.removeLast()).longValue();
	}
	
	public void clear() {
		this.version = 0;
		this.dump.clear();
	}
}
