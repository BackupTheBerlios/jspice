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
package org.openspice.jspice.datatypes.repeaters;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.datatypes.repeaters.Repeater;
import org.openspice.jspice.datatypes.Binding;
import org.openspice.jspice.datatypes.Termin;

import java.util.regex.Matcher;

public final class MatcherRepeater extends Repeater {

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
