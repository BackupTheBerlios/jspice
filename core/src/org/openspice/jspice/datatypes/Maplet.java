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

package org.openspice.jspice.datatypes;
import java.util.*;

public final class Maplet implements Map.Entry {
	private final Object key;
	private final Object val;
	
	public Maplet( final Object _key, final Object _val ) {
		this.key = _key;
		this.val = _val;
	}

	public Object getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.val;
	}

	public Object setValue( final Object arg0 ) {
		throw new UnsupportedOperationException();
	}
}

