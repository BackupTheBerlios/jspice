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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

abstract class AbsCirculationList extends CirculationList {

	//	Set< CirculationList >
	final Set subscribers = new HashSet();

	public synchronized void addSubscriber( final CirculationList abs ) {
		this.subscribers.add( abs );
	}

	public synchronized void removeSubscriber( final CirculationList abs ) {
		this.subscribers.remove( abs );
	}

	final synchronized void forwardOne( final Letter x ) {
		for ( Iterator it = subscribers.iterator(); it.hasNext(); ) {
			final CirculationList abs = (CirculationList)it.next();
			abs.sendOne( x );
		}
	}

	//	Collection< Letter >
	final synchronized void forwardMany( final Collection x ) {
		for ( Iterator it = subscribers.iterator(); it.hasNext(); ) {
			final CirculationList abs = (CirculationList)it.next();
			abs.sendMany( x );
		}
	}

}
