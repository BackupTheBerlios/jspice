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
import org.xml.sax.SAXException;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.lexis.ParseEscapeException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * two newlines in a row --> a division.
 * \&entity;  \&#code;
 * = Top level header [=]
 * == 2nd level header [==]
 * ---- ruler
 * [WikiWord|Text]
 * {code}
 * _italic_, *bold*, _*bold italic*_
 * # comment
 */
final class WikiParser {

	final JSpiceConf jspice_conf;
	final OutputEngine output_engine;

	public WikiParser( final JSpiceConf jconf, final DefaultHandler defaultHandler ) {
		this.jspice_conf = jconf;
		this.output_engine = new OutputEngine( defaultHandler );
	}

//	private static final Attributes NO_ATTRS =  new AttributesImpl();
//
//	public final void sendText( final char[] chars ) {
//		if ( chars.length == 0 ) return;
//		try {
//			this.defaultHandler.characters( chars, 0, chars.length );
//		} catch ( SAXException e ) {
//			throw new RuntimeException( e );
//		}
//	}
//
//	private final Stack elementStack = new Stack();
//
//	public void startElement( String name ) {
//		try {
//			this.defaultHandler.startElement( null, "wiki", name, NO_ATTRS );
//			this.elementStack.push( name );
//		} catch ( SAXException e ) {
//			throw new RuntimeException( e );
//		}
//	}
//
//	public void endElement() {
//		try {
//			this.defaultHandler.endElement( null, "wiki", (String)this.elementStack.pop() );
//		} catch ( SAXException e ) {
//			throw new RuntimeException( e );
//		}
//	}

	private final void processHeading( String s ) throws SAXException {
		int n = 0;
		while ( n < s.length() && s.charAt( n ) == '=' ) {
			n += 1;
		}
		s = s.substring( n ).trim();
		if ( s.length() == 0 ) {
			this.processTextLine( s );
		} else {
			int m = 0;
			while ( m < s.length() && s.charAt( s.length() - 1 - m ) == '=' ) {
				m += 1;
			}
			s = s.substring( 0, s.length() - m ).trim();
			this.output_engine.startElement( "heading" );
			this.processInlineText( s );
			this.output_engine.endElement();
		}
		this.output_engine.accept( '\n' );
	}

	private final void processComment( final String s ) {
		if ( s.length() > 1 ) {
			final char ch = s.charAt( 1 );
			switch ( ch ) {
				case ' ':
				case '#':
				case '!':
					return;
				default:
					throw new Alert( "Undefined commented sequence" ).culprit( "comment", s ).mishap();
			}
		}
	}

	//	todo: add the explicit anchor text.
	//	todo: add explicit URL schemes.
	private final void processAnchor( final ProcessInlineText pinline ) {
		//	Read to the matching close bracket.
		final StringBuffer b = new StringBuffer();
		int level = 1;
		for(;;) {
			final char ch = pinline.readCharNoEOF();
			if ( ch == '[' ) {
				level += 1;
			} else if ( ch == ']' ) {
				level -= 1;
			}
			if ( level == 0 ) break;
			b.append( ch );
		}
		this.output_engine.startElement( "wikiword" );
		this.output_engine.accept( b );
		this.output_engine.endElement();
	}

	private final void processStyleModifier( final ProcessInlineText pinline, final char closer, final String name ) throws SAXException {
		final StringBuffer b = new StringBuffer();
		for(;;) {
			final char ch = pinline.readCharNoEOF();
			if ( ch == closer ) break;
			b.append( ch );
		}
		this.output_engine.startElement( name );
		this.processInlineText( b.toString() );
		this.output_engine.endElement();
	}

	public final void processInlineText( final String s ) throws SAXException {
		this.processInlineText( new ProcessInlineText( this.jspice_conf, s ) );
	}

	public final void processInlineText( final ProcessInlineText pinline ) throws SAXException {
		while ( pinline.canReadChar() ) {
			final char ch = pinline.readCharNoEOF();
			if ( ch == '\\' ) {
				try {
					this.output_engine.accept( pinline.parseEscape() );
				} catch ( ParseEscapeException e ) {
					throw new Alert( "Trying to use forbidden escape sequence in Wiki '\\('" ).mishap();
				}
			} else if ( ch == '_' || ch == '*' ) {
				this.processStyleModifier( pinline, ch, ch == '_' ? "i" : "b" );
			} else if ( ch == '[' ) {
				this.processAnchor( pinline );
			} else {
				this.output_engine.accept( ch );
			}
		}
	}

	private final void processTextLine( final String s ) throws SAXException {
		this.processInlineText( s );
		this.output_engine.accept( '\n' );
	}

	private final void processRuler( final String s ) throws SAXException {
		this.output_engine.startElement( "hr" );
		this.output_engine.endElement();
		this.output_engine.accept( '\n' );
	}

	public void parse( final BufferedReader reader ) throws IOException, SAXException {
		this.output_engine.startDocument();
		for (;;) {
			final String s = reader.readLine();
			if ( s == null ) break;
			if ( s.length() == 0 ) {
				this.output_engine.endDiv();
			} else {
				final char ch = s.charAt( 0 );
				if ( ch == '=' ) {
					this.processHeading( s );
				} else if ( ch == '-' && s.startsWith( "----" ) ) {
					this.processRuler( s );
				} else if ( ch == '#' ) {
					this.processComment( s );
				} else {
					this.processTextLine( s );
				}
			}
		}
		this.output_engine.endDocument();
	}

}
