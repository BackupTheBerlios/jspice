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
package org.openspice.jspice.main.jline_stuff;

import org.openspice.jspice.main.Interpreter;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.parse.SpiceTokenParser;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SmartCompletor implements jline.Completor {

	final Interpreter interpreter;

	public SmartCompletor( final Interpreter intr ) {
		assert intr != null;
		this.interpreter = intr;
	}

	public static final Pattern word_boundary = Pattern.compile( "^(.*)\\b(\\w+)$" );

	//	We have to do something fairly clever to cope with
	//		66.toRadi
	//		         ^ cursor here
	//
	public int complete( final String buff, final int cursor, final List clist ) {
		if ( this.interpreter == null ) return -1;
		final Matcher matcher = word_boundary.matcher( buff );
		final boolean found = matcher.find();
		final String prefix = found ? matcher.group( 1 ) : "";
		final String suffix = found ? matcher.group( 2 ) : buff;

		final List list = new ArrayList();

		SpiceTokenParser.findCompletions( suffix, list );
		final int nbefore = list.size();

		final NameSpace ns = this.interpreter.getCurrentNameSpace();
		ns.findCompletions( suffix, list );
		final int nafter = list.size();

		if ( nbefore == 1 && nafter == 1 ) {
			list.set( 0, ( (String)list.get( 0 ) ) + " " );
		}

		clist.addAll( list );
		return list.isEmpty() ? -1 : prefix.length();
	}

}
