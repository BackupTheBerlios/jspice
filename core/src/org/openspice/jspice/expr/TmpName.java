package org.openspice.jspice.expr;

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

public class TmpName implements Comparable {
	final long edition;
	final long version;

	TmpName( final long _edition, final long _version ) {
		this.edition = _edition;
		this.version = _version;
	}

	public String toString() {
		return "Tmp(" + this.edition + "," + this.version + ")";
	}

	public boolean equals( final Object obj ) {
		if ( obj instanceof TmpName ) {
			final TmpName that = (TmpName)obj;
			return this.edition == that.edition && this.version == that.version;
		} else {
			return false;
		}
	}

	public int compareTo( final Object obj ) {
		final TmpName that = (TmpName)obj;
		final long e = this.edition - that.edition;
		if ( e != 0 ) return e < 0 ? -1 : 1;
		final long v = this.version - that.version;
		return v < 0 ? -1 : v > 0 ? 1 : 0;
	}
}
