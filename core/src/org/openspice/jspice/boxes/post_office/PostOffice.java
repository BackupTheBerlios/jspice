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

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

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
public class PostOffice implements PostOfficeIntf {

	static final class Worker extends Thread {

		final PostOffice po;

		public Worker( PostOffice po ) {
			this.po = po;
		}

		public void run() {
			for (;;) {
				final Runnable r = this.po.next();
				if ( r == null ) return;
				r.run();
			}
		}

	}

	/**
	 * This variable is THREAD SHARED so all access MUST be synchronized for it to be safe.
	 * When it is null, this means either that there is no active worker or that the worker
	 * thread is in the process of tear-down and useless.
	 */
	private Thread worker = null;


	//	THREAD SHARED all access MUST be synchronized.
	//	List< Runnable >
	private final LinkedList runnable_list = new LinkedList();
	
	synchronized Runnable next() {
		if ( this.runnable_list.isEmpty() ) {
			this.worker = null;
			return null;
		}
		return (Runnable)this.runnable_list.removeFirst();
	}

	synchronized public void addRunnable( final Runnable runnable ) {
		this.runnable_list.add( runnable );
		if ( worker == null ) {
			( this.worker = new PostOffice.Worker( this ) ).start();
		}
	}

	public POBox newPOBox() {
		return new POBox( this );
	}

	public CirculationList newCirculationList() {
		return new SimpleCirculationList( this );
	}

	public AutoReply newAutoReply( final Class agentClass ) {
		return(
			new AbsAutoReply( this ) {

				protected void tidyUpOnClose() {
					//	Skip.
				}

				public void replyTo( final Letter letter ) {
					try {
						final Agent agent = (Agent)agentClass.newInstance();
						agent.setLetter( letter );
						agent.setLetterBox( this );
						agent.perform();
					} catch ( InstantiationException e ) {
						throw new RuntimeException( e );
					} catch ( IllegalAccessException e ) {
						throw new RuntimeException( e );
					}
				}

				public boolean usePostOffice() {
					return true;
				}

			}
		);
	}

	public AutoReply newSwingAutoReply( final Class agentClass ) {
		return(
			new SwingAutoReply( this ) {

				protected void tidyUpOnClose() {
					//	Skip.
				}

				public void invoke( Letter letter ) {
					try {
						final Agent agent = (Agent)agentClass.newInstance();
						agent.setLetter( letter );
						agent.setLetterBox( this );
						agent.perform();
					} catch ( InstantiationException e ) {
						throw new RuntimeException( e );
					} catch ( IllegalAccessException e ) {
						throw new RuntimeException( e );
					}
				}

			}
		);
	}

	public AutoReply newAutoReply( final Robot outer_robot ) {
		return(
			new AbsAutoReply( this ) {
				Robot robot = outer_robot;
				protected void tidyUpOnClose() {
					this.robot = null;
				}
				public void replyTo( final Letter letter ) {
					this.robot.handle( letter, this );
				}
				public boolean usePostOffice() {
					return true;
				}
			}
		);
	}

	public AutoReply newSwingAutoReply( final Robot outer_robot ) {
		return(
			new SwingAutoReply( this ) {
				Robot robot = outer_robot;
				protected void tidyUpOnClose() {
					this.robot = null;
				}
				public void invoke( final Letter letter ) {
					this.robot.handle( letter, this );
				}
			}
		);
	}

	//	Map< String, LetterBox >
	private Map letterBoxMap = new HashMap();

	public void put( String name, LetterBox lbox ) {
		this.letterBoxMap.put( name, lbox );
	}

	public LetterBox get( String name ) {
		return (LetterBox)this.letterBoxMap.get( name );
	}

}
