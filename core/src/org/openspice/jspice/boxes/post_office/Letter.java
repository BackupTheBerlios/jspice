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
package org.openspice.jspice.boxes.post_office;

import java.util.List;
import java.util.ArrayList;

/**
 * A marker interface at present.
 */
public class Letter {

	LetterBox from;
	LetterBox to;
	String subject;

	List classes = new ArrayList();
	List objects = new ArrayList();

	public Letter( final LetterBox from, final LetterBox to, final String subject ) {
		this.from = from;
		this.to = to;
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	public Class[] signature() {
		final Class[] cs = new Class[ this.classes.size() ];
		this.classes.toArray( cs );
		return cs;
	}

	public Object[] arguments() {
		return this.objects.toArray();
	}

	public LetterBox getFrom() {
		return this.from;
	}

	public LetterBox getTo() {
		return this.to;
	}

	public void addArg( Class key, Object val ) {
		this.classes.add( key );
		this.objects.add( val );
	}

	public void addObject( Object val ) {
		this.addArg( Object.class, val );
	}

	public void addInt( int n ) {
		this.addArg( int.class, new Integer( n ) );
	}

	public void send() {
		this.getTo().sendOne( this );
	}

}
