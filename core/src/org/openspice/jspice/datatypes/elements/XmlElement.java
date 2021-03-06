package org.openspice.jspice.datatypes.elements;

import org.openspice.jspice.tools.*;
import org.openspice.jspice.datatypes.*;
import org.openspice.jspice.datatypes.lists.PseudoList;
import org.openspice.jspice.datatypes.lists.XmlElementAsList;
import org.openspice.jspice.datatypes.maps.PseudoMap;
import org.openspice.jspice.datatypes.maps.XmlElementAsMap;
import org.openspice.jspice.built_in.inspect.FieldAdder;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

import org.xml.sax.*;
import org.openspice.tools.EmptyMap;

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

public final class XmlElement extends SpiceObject {
    Symbol name;
	Map attributes;
    Object[] children;
	
	public Symbol getTypeSymbol() {
		return this.name;
	}
	
	public Map getAttributes() {
		//	Should we copy?  Depends on whether or not this is a mutable element.
		return this.attributes;
	}
	
	public List getChildren() {
		return Arrays.asList( this.children );
	}

	public boolean equals( final Object x ) {
		if ( ! ( x instanceof XmlElement ) ) return false;
		final XmlElement that = (XmlElement)x;
		return this.name == that.name && Arrays.equals( this.children, that.children ) && this.attributes.equals( that.attributes ) ;
	}

	public Object get( final int idx ) {
		return this.children[ idx ];
	}
	
	public int size() {
		return this.children.length;
	}
	
	public boolean isEmpty() {
		return this.children.length == 0;
	}
	
	public void printTo( final Consumer cuchar ) {
		cuchar.out( '<' );
		PrintTools.printTo( cuchar, this.name );
		
		final Set set = this.attributes.entrySet();
		for ( Iterator it = set.iterator(); it.hasNext(); ) {
			final Map.Entry e = (Map.Entry)it.next();
			cuchar.out( ' ' );
			cuchar.outCharSequence( ((Symbol)e.getKey()).getInternedString() );
			cuchar.out( '=' );
			cuchar.out( '"' );
			PrintTools.printTo( cuchar, e.getValue() );
			cuchar.out( '"' );
		}
		
		if ( this.children.length == 0 ) {
			cuchar.outCharSequence( "/>" );
		} else {
			cuchar.out( '>' );
			
			{
				String gap = "";
				for ( int i = 0; i < this.children.length; i++ ) {
					final Object x = this.children[ i ];
					cuchar.outCharSequence( gap );
					gap = ", ";
					PrintTools.printTo( cuchar, x );
				}
			}
		
			cuchar.outCharSequence( "</" );
			cuchar.outCharSequence( this.name.getInternedString() );
			cuchar.out( '>' );
		}		
	}
	
	public void showTo( final Consumer cuchar ) {
		cuchar.out( '<' );
		cuchar.outCharSequence( this.name.getInternedString() );
		
		final Set set = this.attributes.entrySet();
		for ( Iterator it = set.iterator(); it.hasNext(); ) {
			final Map.Entry e = (Map.Entry)it.next();
			cuchar.out( ' ' );
			if (!(e.getKey() instanceof Symbol)) {
				System.err.println( this.attributes );
				System.err.println( "key = '" + e.getKey() + "' (" + e.getKey().getClass().getName() + ")" );
				System.err.println( "val = '" + e.getValue() + "' (" + e.getValue().getClass().getName() + ")" );
			}
			cuchar.outCharSequence( ((Symbol)e.getKey()).getInternedString() );
			cuchar.out( '=' );
			PrintTools.show( e.getValue() );
		}
		
		if ( this.children.length == 0 ) {
			cuchar.outCharSequence( "/>" );
		} else {
			cuchar.out( '>' );
			
			{
				String gap = "";
				for ( int i = 0; i < this.children.length; i++ ) {
					final Object x = this.children[ i ];
					cuchar.outCharSequence( gap );
					gap = ", ";
					PrintTools.showTo( cuchar, x );
				}
			}
		
			cuchar.outCharSequence( "</" );
			cuchar.outCharSequence( this.name.getInternedString() );
			cuchar.out( '>' );
		}
	}
	
	static final Map EMPTY_MAP = new EmptyMap();
	static final Object[] EMPTY_ARRAY = new Object[ 0 ];
	
	//	Default visibility - NOT PUBLIC!
	public XmlElement( final Symbol _name, final Map _map, final Object[] _children ) {
		this.name = _name;
		this.attributes = _map;
		this.children = _children;
	}
	
	public XmlElement( final Symbol _name, final Map _map, final List _list ) {
		this.name = _name;
		this.attributes = _map.isEmpty() ? EMPTY_MAP : new TreeMap( _map );
		if ( _list.isEmpty() ) {
			this.children = EMPTY_ARRAY;
		} else {
			this.children = (
				(
					_list instanceof ArrayList ?
					(ArrayList)_list :
					new ArrayList( _list )
				).toArray()
			);
		}
	}

	//	This constructor is private - used by Factory exclusively.
	XmlElement() {
	}

	public static XmlElement make( final Symbol _name, final Map _map, final List _list ) {
		return new XmlElement( _name, _map, _list );
	}

	public List convertToList() {
		return new XmlElementAsList( this );
	}
	
	public SpiceObject convertFromList( final List list ) {
		return new XmlElement( this.getTypeSymbol(), this.getAttributes(), list );
	}
	
	public Map convertToMap() {
		return new XmlElementAsMap( this );
	}
	
	public SpiceObject convertFromMap( final Map map ) {
		return new XmlElement( this.getTypeSymbol(), this.getAttributes(), ListTools.convertTo( map ) );
	}

	public boolean isListFlavour() {
		return true;
	}

	public boolean isMapFlavour() {
		return true;
	}

	//	----

	//	todo: how do we handle qualified names, local names etc etc etc.
	static Map attributesToMap( final Attributes attributes ) {
		final Map map = new HashMap();
		final int len = attributes.getLength();
		for ( int i = 0; i < len; i++ ) {
			final String localName = attributes.getLocalName( i );
			final String value = attributes.getValue( i );
			map.put( Symbol.make( localName ), value );
		}
		return map;
	}

	//	todo:	This is very free with the items it allocates, I suspect.  Review.
	static public XmlElement readXmlElement( final InputStream inputStream ) {
		final InputSource inputSrc = new InputSource( inputStream );
		final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		try {
			final SAXParser parser = parserFactory.newSAXParser();
			final XmlElementHandler h = new XmlElementHandler();
			parser.parse( inputSrc, h );
			return h.giveItUp();
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		} catch ( final ParserConfigurationException e ) {
			throw new RuntimeException( e );
		} catch ( final SAXException e ) {
			throw new RuntimeException( e );
		}
	}

	public void addInstanceFields( final FieldAdder adder ) {
    	adder.add( "name", this.name );
   		adder.add( "attributes", this.attributes );
    	adder.add( "children", this.children );
	}

}
