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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.openspice.tools.IntStack;
import org.openspice.jspice.datatypes.Symbol;

import java.util.LinkedList;

public class XmlElementHandler extends DefaultHandler {
	final LinkedList open_stack = new LinkedList();
	final LinkedList kid_stack = new LinkedList();
	final IntStack count_stack = new IntStack();

	static final Object[] dummy = new Object[ 0 ];

	public void startElement( final String nameSpace, final String localName, final String fullName, final Attributes attributes ) throws SAXException {
		this.count_stack.push( this.kid_stack.size() );
		this.open_stack.addLast( new XmlElement( Symbol.make( fullName ), XmlElement.attributesToMap( attributes ), dummy ) );
	}

	public void endElement( final String nameSpace, final String localName, final String fullName ) throws SAXException {
		final int n_kids = this.kid_stack.size() - this.count_stack.pop();
		final Object[] kids = new Object[ n_kids ];
		for ( int i = n_kids - 1; i >= 0; i -= 1 ) {
			kids[ i ] = this.kid_stack.removeLast();
		}
		final XmlElement e = (XmlElement)this.open_stack.removeLast();
		e.children = kids;
		this.kid_stack.addLast( e );
	}

	public void characters( final char[] chars, final int offset, final int count ) throws SAXException {
		this.kid_stack.addLast( new String( chars, offset, count ) );
	}

	public void processingInstruction( final String s, final String s1 ) throws SAXException {
		throw new RuntimeException( "tbd" );    // todo:
	}

	/**
	 * Return the XmlElement that has been built.
	 * @return the XmlElement that has been built.
	 */
	public XmlElement giveItUp() {
		final XmlElement e = (XmlElement)this.kid_stack.removeLast();
		assert this.kid_stack.isEmpty();
		assert this.open_stack.isEmpty();
		assert this.count_stack.isEmpty();
		return e;
	}

}
