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
package org.openspice.jspice.datatypes.lists;

import org.openspice.jspice.datatypes.elements.XmlElement;

public final class XmlElementAsList extends PseudoList {
	final XmlElement xml_element;

	public Object getObject() {
		return this.xml_element;
	}

	public XmlElementAsList( final XmlElement _xml_element ) {
		this.xml_element = _xml_element;
	}

	public Object get( final int idx ) {
		return this.xml_element.get( idx );
	}

	public int size() {
		return this.xml_element.size();
	}

}
