package org.openspice.jspice.datatypes;
import org.openspice.jspice.lib.AbsentLib;
import org.openspice.jspice.datatypes.Maplet;

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

public abstract class PseudoMap extends AbstractMap {
	public abstract Object getObject();
	
	public boolean compatibleWith( final Object example ) {
		return example.getClass().isInstance( this.getObject() );
	}
	
	public static final class StringAsMap extends PseudoMap {
		private final String string;
		
		public StringAsMap( final String _string ) {
			this.string = _string;
		}
		
		public Object getObject() {
			return this.string;
		}

		public Set entrySet() {
			return new StringAsMapSet( this.string );
		}
		
		public Object get( final Object key ) {
			try {
				return new Character( this.string.charAt( ((Integer)key).intValue() ) );
			} catch ( final ClassCastException exn ) {
				return AbsentLib.ABSENT;
			}
		}
		
		public boolean isEmpty() {
			return this.string.length() == 0;
		}
		
		public int size() { 
			return this.string.length(); 
		}

		static final class StringAsMapSet extends AbstractSet {
			final String string;
			
			StringAsMapSet( final String _string ) {
				this.string = _string;
			}
			
			public int size() {
				return this.string.length();
			}
			
			public Iterator iterator() {
				return(
					new Iterator() {
						private int n = 0;
						
						public boolean hasNext() {
							return this.n < string.length();
						}
						
						public Object next() {
							final Maplet ans = (
								new Maplet(
									new Integer( this.n + 1 ),					//	 Spice is 1-indexed.
									new Character( string.charAt( this.n ) )
								)
							);
							this.n += 1;
							return ans;
						}
						
						public void remove() {
							throw new UnsupportedOperationException();
						}
					}
				);
			}
		}
	}
	
	public static final class ListAsMap extends PseudoMap {
		private final List list;
		
		public ListAsMap( final List _list ) {
			this.list = _list;
		}
		
		public Object getObject() {
			return this.list;
		}

		public Set entrySet() {
			return new ListAsMapSet( this.list );
		}
		
		public Object get( final Object key ) {
			try {
				return this.list.get( ((Integer)key).intValue() );
			} catch ( final ClassCastException exn ) {
				return AbsentLib.ABSENT;
			}
		}
		
		public boolean isEmpty() {
			return this.list.isEmpty();
		}
		
		public int size() { 
			return this.list.size(); 
		}

		static final class ListAsMapSet extends AbstractSet {
			final List list;
			
			ListAsMapSet( final List _list ) {
				this.list = _list;
			}
			
			public int size() {
				return this.list.size();
			}
			
			public Iterator iterator() {
				final Iterator it = this.list.iterator();
				
				return(
					new Iterator() {
						int n = 0;
						
						public boolean hasNext() {
							return it.hasNext();
						}
						
						public Object next() {
							return (
								new Maplet(
									new Integer( ++this.n ),	//	Spice is 1-indexed.
									it.next()
								)
							);
						}
						
						public void remove() {
							throw new UnsupportedOperationException();
						}
					}
				);
			}
		}
	}
		
	public static final class XmlElementAsMap extends PseudoMap {
		private final XmlElement xml_element;
		
		public XmlElementAsMap( final XmlElement _xml_element ) {
			this.xml_element = _xml_element;
		}
		
		public Object getObject() {
			return this.xml_element;
		}

		public Set entrySet() {
			return new XmlElementAsMapSet( this.xml_element );
		}
		
		public Object get( final Object key ) {
			try {
				return this.xml_element.get( ((Integer)key).intValue() );
			} catch ( final ClassCastException exn ) {
				return AbsentLib.ABSENT;
			}
		}
		
		public boolean isEmpty() {
			return this.xml_element.isEmpty();
		}
		
		public int size() { 
			return this.xml_element.size(); 
		}

		static final class XmlElementAsMapSet extends AbstractSet {
			final XmlElement xml_element;
			
			XmlElementAsMapSet( final XmlElement _xml_element ) {
				this.xml_element = _xml_element;
			}
			
			public int size() {
				return this.xml_element.size();
			}
			
			public Iterator iterator() {
				final Iterator it = xml_element.getChildren().iterator();
				
				return(
					new Iterator() {
						int n = 0;
						
						public boolean hasNext() {
							return it.hasNext();
						}
						
						public Object next() {
							return (
								new Maplet(
									new Integer( ++this.n ),	//	Spice is 1-indexed.
									it.next()
								)
							);
						}
						
						public void remove() {
							throw new UnsupportedOperationException();
						}
					}
				);
			}
		}
	}
}
