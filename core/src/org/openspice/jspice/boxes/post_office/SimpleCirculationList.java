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

public class SimpleCirculationList extends AbsLetterBox implements CirculationList {

	protected void  tidyUpOnClose() {
		this.subscribers = null;
	}

	public SimpleCirculationList( final PostOffice post_office ) {
		super( post_office );
	}

	public final synchronized void sendOne( final Letter letter ) {
		if ( this.isOpen() ) this.forwardOne( letter );
	}

	//	Collection< Letter >
	public final synchronized void sendMany( final Collection letters ) {
		if ( this.isOpen() ) this.forwardMany( letters );
	}

	//	Set< LetterBox >
	Set subscribers = new HashSet();

	public synchronized void addSubscriber( final LetterBox abs ) {
		if ( this.isOpen() ) this.subscribers.add( abs );
	}

	public synchronized void removeSubscriber( final LetterBox abs ) {
		this.subscribers.remove( abs );
	}

	final synchronized void forwardOne( final Letter x ) {
		if ( this.isClosed() ) return;
		for ( Iterator it = subscribers.iterator(); it.hasNext(); ) {
			final LetterBox lbox = (LetterBox)it.next();
			if ( lbox.isClosed() ) {
				it.remove();
			} else {
				lbox.sendOne( x );
			}
		}
	}

	//	Collection< Letter >
	final synchronized void forwardMany( final Collection x ) {
		if ( this.isClosed() ) return;
		for ( Iterator it = subscribers.iterator(); it.hasNext(); ) {
			final LetterBox lbox = (LetterBox)it.next();
			if ( lbox.isClosed() ) {
				it.remove();
			} else {
				lbox.sendMany( x );
			}
		}
	}

	public AutoReply newAutoReplySubscriber( final Robot robot ) {
		final AutoReply lbox = this.getPostOffice().newAutoReply( robot );
		this.addSubscriber( lbox );
		return lbox;
	}

	public CirculationList newCirculationListSubscriber() {
		final CirculationList lbox = this.getPostOffice().newCirculationList();
		this.addSubscriber( lbox );
		return lbox;
	}

	public POBox newPOBoxSubscriber() {
		final POBox lbox = this.getPostOffice().newPOBox();
		this.addSubscriber( lbox );
		return lbox;
	}

	public AutoReply newSwingAutoReplySubscriber( final Robot robot ) {
		final AutoReply lbox = this.getPostOffice().newSwingAutoReply( robot );
		this.addSubscriber( lbox );
		return lbox;
	}

}
