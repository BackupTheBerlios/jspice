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

import org.openspice.jspice.lexis.ParseEscape;
import org.openspice.jspice.conf.JSpiceConf;

final class ProcessInlineText extends ParseEscape {

	int position;
	final String text;
	final StringBuffer buffer = new StringBuffer();

	public ProcessInlineText( final JSpiceConf jconf, final int position, final String _text ) {
		super( jconf );
		this.position = position;
		this.text = _text;
	}

	public ProcessInlineText( final JSpiceConf jconf, final String txt ) {
		this( jconf, 0, txt );
	}

	public int getPosition() {
		return position;
	}

	public char readChar( final char default_char ) {
		try {
			return text.charAt( this.position++ );
		} catch ( final IndexOutOfBoundsException _ ) {
			return default_char;
		}
	}

	public char readCharNoEOF() {
		return text.charAt( this.position++ );
	}

	public boolean canReadChar() {
		return this.position < this.text.length();
	}

}
