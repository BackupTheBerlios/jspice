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
package org.openspice.jspice.loader.wiki;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.openspice.tools.StringBufferTools;

import java.util.Stack;

public class OutputEngine {

	final DefaultHandler defaultHandler;
	final StringBuffer buffer = new StringBuffer();

	public OutputEngine( DefaultHandler defaultHandler ) {
		this.defaultHandler = defaultHandler;
	}

	//	---oooOOOooo---


	public void flush() {
		if ( this.buffer.length() == 0 ) return;
		final char[] chars = StringBufferTools.toCharArray( this.buffer );
		this.buffer.setLength( 0 );
		this.sendText( chars );
	}

	//	---oooOOOooo---

	private static final Attributes NO_ATTRS =  new AttributesImpl();

	public final void sendText( final char[] chars ) {
		if ( chars.length == 0 ) return;
		try {
			if ( this.elementStack.isEmpty() ) {
				this.push( DIV );
			}
//			System.out.println( "sendText " + chars.length + " chars" );
			this.defaultHandler.characters( chars, 0, chars.length );
		} catch ( final SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	private final Stack elementStack = new Stack();

	public static final String DIV = "div";

	private void push( final String name ) {
		try {
//			System.out.println( "start " + name );
			this.defaultHandler.startElement( null, "wiki", name, NO_ATTRS );
			this.elementStack.push( name );
		} catch ( SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	public void startElement( final String name ) {
		this.flush();
		if ( this.elementStack.isEmpty() ) {
			this.push( DIV );
		}
		this.push( name );
	}

	private void pop() {
		try {
			final String name = (String)this.elementStack.pop();
//			System.out.println( "end element " + name );
			this.defaultHandler.endElement( null, "wiki", name );
		} catch ( SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	public void endElement() {
		this.flush();
		this.pop();
	}

	public void endDiv() {
		this.flush();
		while ( ! this.elementStack.isEmpty() ) {
			this.pop();
		}
	}

	public void startDocument() {
		try {
			this.defaultHandler.startDocument();
			this.defaultHandler.startElement( null, "wiki", "document", NO_ATTRS );
		} catch ( SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	public void endDocument() {
		this.endDiv();
		try {
			this.defaultHandler.endElement( null, "wiki", "document" );
			this.defaultHandler.endDocument();
		} catch ( SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	//	---oooOOOooo---


	public void accept( final char ch ) {
		this.buffer.append( ch );
	}

	public void accept( final char[] chs ) {
		this.buffer.append( chs );
	}

	public void accept( final StringBuffer b ) {
		this.buffer.append( b );
	}

	public void accept( final String s ) {
		this.buffer.append( s );
	}


}
