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

/**
 * The functions of the post office are to provide:-
 * 		1.	logical names for mail-lists and mail-boxes
 * 		2.	a binding point for external processes
 * 		3.	a central point for connecting Runnables/Threads
 *
 * QUESTION: 	Can a Post Office be the integrating concept of a Box?
 * ANSWER:		No - it is just a fairly general kind of Box.
 *
 */
public interface PostOfficeIntf {

	void addRunnable( final Runnable runnable );

	public POBox newPOBox();

	public CirculationList newCirculationList() ;

	public AutoReply newAutoReply( final Robot robot );

	public AutoReply newSwingAutoReply( final Robot robot );

	public void put( final String name, final LetterBox lbox );

	public LetterBox get( final String name );

}
