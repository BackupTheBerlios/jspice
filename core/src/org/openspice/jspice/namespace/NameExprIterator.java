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

package org.openspice.jspice.namespace;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.NameExpr;

import java.util.*;

public abstract class NameExprIterator {
	abstract public boolean hasNext();
	abstract public NameExpr  next();

	static NameExprIterator make0() {
		return(
			new NameExprIterator() {
				public boolean hasNext() {
					return false;
				}
				
				public NameExpr  next() {
					throw new IllegalStateException();
				}
			}
		);
	}

	static NameExprIterator make1( final NameExpr name_expr ) {
		return(
			new NameExprIterator() {
				private boolean has_next = true;
				
				public boolean hasNext() {
					return this.has_next;
				}
				
				public NameExpr  next() {
					this.has_next = false;
					return name_expr;
				}
			}
		);
	}
	
	static NameExprIterator make2( final NameExpr x, final NameExpr y ) {
		return(
			new NameExprIterator() {
				private int count = 0;
				
				public NameExpr next() {
					this.count += 1;
					if ( count == 1 ) {
						return x;
					} else {
						return y;
					} 
				}
				
				public boolean hasNext() {
					return this.count < 2;
				}
			}
		);
	}
	
	static NameExprIterator make( final Iterator iterator ) {
		return(
			new NameExprIterator() {
				public boolean hasNext() { 
					return iterator.hasNext(); 
				}
				public NameExpr  next() {
					return (NameExpr)iterator.next();
				}
			}
		);
	}

}
