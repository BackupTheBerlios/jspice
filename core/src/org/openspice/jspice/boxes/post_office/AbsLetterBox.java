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


public abstract class AbsLetterBox implements LetterBox {

	final PostOffice postOffice;

	protected AbsLetterBox( final PostOffice postOffice ) {
		this.postOffice = postOffice;
	}

	public final PostOffice getPostOffice() {
		return postOffice;
	}

	protected abstract void tidyUpOnClose();

	private boolean is_open = true;

	public final boolean isOpen() {
		return this.is_open;
	}

	public final boolean isClosed() {
		return !this.is_open;
	}

	public final void close() {
		this.is_open = false;
		this.tidyUpOnClose();
	}

	public final void subscribeTo( final CirculationList circ ) {
		circ.addSubscriber( this );
	}

	public final void unsubscribeFrom( final CirculationList circ ) {
		circ.removeSubscriber( this );
	}

	public final Letter newLetter( final String subject ) {
		return new Letter( this, null, subject );
	}

	public final Letter newLetterTo( LetterBox dst, final String subject ) {
		return new Letter( this, dst, subject );
	}

	public final Letter newReplyTo( Letter letter, String subject ) {
		return new Letter( this, letter.getFrom(), subject );
	}

}
