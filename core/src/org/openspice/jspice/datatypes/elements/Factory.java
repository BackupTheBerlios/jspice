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
package org.openspice.jspice.datatypes.elements;

import org.openspice.jspice.datatypes.Symbol;

import java.util.TreeMap;
import java.util.ArrayList;

final class Factory {
	private Symbol name;
	private TreeMap attributes;
	private ArrayList children;

	Factory( final Symbol _name ) {
		this.name = _name;
		this.attributes = new TreeMap();
		this.children  = new ArrayList();
	}

	Factory addChild( final Object x ) {
		this.children.add( x );
		return this;
	}

	Factory addAttribute( final String key, final Object value ) {
		this.attributes.put( key, value );
		return this;
	}

	XmlElement make() {
		final XmlElement answer = new XmlElement();
		answer.name = Symbol.make( this.name.getInternedString() );
		if ( !this.attributes.isEmpty() ) {
			answer.attributes = this.attributes;
		} else {
			answer.attributes = XmlElement.EMPTY_MAP;
		}
		if ( !this.children.isEmpty() ) {
			answer.children = this.children.toArray();
		} else {
			answer.children = XmlElement.EMPTY_ARRAY;
		}
		return answer;
	}
}
