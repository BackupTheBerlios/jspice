package org.openspice.jspice.datatypes;
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

public abstract class PseudoList extends AbstractList {
	public abstract Object getObject();
	
	public boolean compatibleWith( final Object example ) {
		return example.getClass().isInstance( this.getObject() );
	}
	
	public static final class StringAsList extends PseudoList {
		final String string;
		
		public StringAsList( final String _string ) {
			this.string = _string;
		}
		
		public Object get( final int idx ) {
			return new Character( this.string.charAt( idx ) );
		}
		
		public Object getObject() {
			return this.string;
		}

		public int size() { 
			return this.string.length(); 
		}
	}
	
	public static final class MapAsList extends PseudoList {
		final Map map;
		
		public Object getObject() {
			return this.map;
		}

		public MapAsList( final Map _map ) {
			this.map = _map;
		}
		
		public Object get( final int idx ) {
			return this.map.get( new Integer( idx ) );
		}
		
		public int size() { 
			return this.map.size(); 
		}
	}
	
	public static final class XmlElementAsList extends PseudoList {
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
}
