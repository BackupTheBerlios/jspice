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

import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.inspect.FieldAdder;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;

/**
 * You are expected to override hasNext and next.
 */
public abstract class Repeater extends SpiceObject.NonMap implements Enumeration, Iterator {

	public void addInstanceFields( FieldAdder adder ) {
		// Skip.
	}

	public void showTo( final Consumer cuchar ) {
		cuchar.outString( "-repeater-" );
	}

	public void printTo( final Consumer cuchar ) {
		while ( this.hasNext() ) {
			PrintTools.print( this.next() );
		}
	}

	public boolean hasMoreElements() {
		return this.hasNext();
	}

	public Object nextElement() {
		return this.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public static final class IteratorRepeater extends Repeater {

		final Iterator iterator;

		public IteratorRepeater( final Iterator iterator ) {
			this.iterator = iterator;
		}

		public Object next() {
			if ( this.hasNext() ) {
				return this.iterator.next();
			} else {
				return Termin.TERMIN;
			}
		}

		public boolean hasNext() {
			return this.iterator.hasNext();
		}

	}

	public static final class EnumerationRepeater extends Repeater {

		final Enumeration enumeration;

		public EnumerationRepeater( final Enumeration enumeration ) {
			this.enumeration = enumeration;
		}

		public Object next() {
			if ( this.enumeration.hasMoreElements() ) {
				return this.enumeration.nextElement();
			} else {
				return Termin.TERMIN;
			}
		}

		public boolean hasNext() {
			return this.enumeration.hasMoreElements();
		}

	}

	public static final class MatcherRepeater extends Repeater {

		/**
		 * 'F' = find
		 * 'M' = matches
		 * 'L' = lookingAt
		 * 'b' = binding waiting for pickup
		 * 'e' = exhausted
		 */
		char fsm;

		final Matcher matcher;
		Binding cubinding = null;

		public MatcherRepeater( final char fsm, final Matcher matcher ) {
			assert "FML".indexOf( fsm ) >= 0;
			this.fsm = fsm;
			this.matcher = matcher;
		}

		public boolean hasNext() {
			boolean f;
			switch ( this.fsm ) {
				case 'b':
					return true;
				case 'e':
					return false;
				case 'F':
					f = this.matcher.find();
					break;
				case 'M':
					f = this.matcher.matches();
					break;
				case 'L':
					f = this.matcher.lookingAt();
					break;
				default:
					throw Alert.unreachable();
			}
			if ( f ) {
				this.cubinding = new Binding( this.matcher );
				this.fsm = 'b';
			} else {
				this.fsm = 'e';
			}
			return f;
		}

		public Object next() {
			switch ( this.fsm ) {
				case 'b':
					final Binding b = this.cubinding;
					this.cubinding = null;
					this.fsm = 'f';
					return b;
				case 'e':
					return Termin.TERMIN;
				case 'F':
				case 'M':
				case 'L':
					this.hasNext();
					return this.next();
				default:
					throw Alert.unreachable();
			}
		}

	}

}
