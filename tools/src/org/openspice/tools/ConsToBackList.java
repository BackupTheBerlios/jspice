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
package org.openspice.tools;

import java.util.List;

public class ConsToBackList extends AbsConsList {

	final List allbutfoot;
	final Object foot;

	public ConsToBackList( Object head, List tail ) {
		this.foot = head;
		this.allbutfoot = tail;
	}

	public Object getFoot() {
		return foot;
	}

	public List getAllButFoot() {
		return allbutfoot;
	}


	//	----

	public Object get( final int index ) {
		final int sz = this.allbutfoot.size();
		//	Poor error handling todo:
		if ( index == sz ) {
			return this.foot;
		} else {
			return this.allbutfoot.get( index );
		}
	}

	public int size() {
		return 1 + this.allbutfoot.size();
	}

	public boolean isEmpty() {
		return false;
	}

	public boolean contains( Object o ) {
		return this.foot == null ? o == null : this.foot.equals( o ) ? true : this.allbutfoot.contains( o );
	}

}
