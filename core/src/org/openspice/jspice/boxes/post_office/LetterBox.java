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

import java.util.Collection;

public interface LetterBox {

	/**
	 * puts a letter thru the letter box.
	 * @param x a letter
	 */
	void sendOne( final Letter x );

	/**
	 * Puts lots of letters thru the letter box.  There is no guarantee
	 * over order of delivery.
	 * @param x a collection of letters
	 */
	void sendMany( final Collection x );

	/**
	 * Adds this letter-box onto a circulation list.  This creates a
	 * hard link from the circulation list to this.  There is no reverse
	 * link.
	 *
	 * @param circ a circulation list
	 */
	void subscribeTo( final CirculationList circ );

	/**
	 * Removes this letter box from a circulation list.  It doesn't
	 * matter if the letter box is not on the list - the operation is
	 * ignored in that case.
	 *
	 * @param circ a circulation list
	 */
	void unsubscribeFrom( final CirculationList circ );

	/**
	 * Returns a flag indicating whether or not the letter box is open
	 * for receiving mail.  Note that in a multi-threaded environment you
	 * cannot rely on a letter box remaining open after this query unless
	 * you synchronize on the letter box object.
	 * @return the letter box is open
	 */
	boolean isOpen();

	/**
	 * The negation of isOpen - a convenience method.
	 * @return the letter box is closed.
	 */
	boolean isClosed();


	/**
	 * This method causes the letter box to close and silently refuse
	 * any further actions (that last bit is probably a mistake).  Once
	 * a letter box is closed it cannot be reopened - and it will
	 * promptly free all pointers for garbage collection.  A closed
	 * letter box will be silently removed from any circulation lists
	 * it appears on.
	 */
	void close();

	Letter newLetter( final String subject );

	Letter newLetterTo( final LetterBox dst, final String subject );

	Letter newReplyTo( final Letter letter, final String subject );

	PostOffice getPostOffice();

}
