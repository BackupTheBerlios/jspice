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
package org.openspice.jspice.boxes.post_office.test;

import org.openspice.jspice.boxes.post_office.*;


public class Example {


	static public class Questioner extends Agent {
		int task;
		int sofar;
		LetterBox answerer;

		private void ask() {
			System.out.println( "task = " + this.task );
			System.out.println( "sofar = " + this.sofar );
			if ( this.task <= 1 ) {		//	defensive
				System.out.println( "Answer is " + this.sofar );
			} else {
				final Letter question = this.newLetterTo( this.answerer, "add" );
				question.addInt( sofar );
				question.addInt( task );
				question.send();
			}
		}

		public void start( final int n ) {
			this.task = n;
			this.sofar = 1;
			final LetterBox self = this.getLetterBox();
			final PostOffice office = self.getPostOffice();
			this.answerer = office.get( "answerer" );
			this.ask();
		}

		public void answer( final int n ) {
			this.sofar = n;
			this.task = this.task - 1;
			this.ask();
		}

	}

	static public class Answerer extends Agent {
		public void add( int x, int y ) {
			final Letter reply = this.getLetterBox().newReplyTo( this.getLetter(), "answer" );
			System.out.println( "Adding " + x + " to " + y );
			reply.addInt( x + y );
			reply.send();
		}
	}

	public static final void main( final String[] args ) {
		final PostOffice po = new PostOffice();
		final AutoReply questioner = po.newAutoReply( Questioner.class );
		final AutoReply answerer = po.newAutoReply( Answerer.class );
		po.put( "answerer", answerer );

		final Letter initial = questioner.newLetter( "start" );
		initial.addInt( 6 );
		questioner.sendOne( initial );
	}

}
