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

import javax.swing.*;
import java.util.*;

/**
 * This class is suitable for use as a pick-up point by client threads.  It can also
 * be used for one client to sendOne directly to another.
 */
public class LetterBox extends AbsCirculationList implements LetterBoxIntf {

	private int maxsize = Integer.MAX_VALUE;		//	Block when the queue gets this big.  0 = don't block.
	private long timeout = 0;						//	0 = don't timeout.
	private boolean throw_on_timeout = false;
	private Letter timeout_default = null;
	private boolean is_interruptible = false;

	private PostOffice post_office;
	private AutoReply auto_reply = null;
	private boolean is_closed = false;
	private boolean is_retaining_locally = true;

	private LinkedList queue = new LinkedList();	//	Should really be a priority queue - but that's too much like hard work.
//	private boolean available = false;				//	queue is available.

	/**
	 * Constructs a LetterBox.
	 * @param post_office may be null
	 */
	public LetterBox( final PostOffice post_office ) {
		this.post_office = post_office;
	}

	//	---- RECEIVING

	private boolean isAvailable() {
		return !this.queue.isEmpty();
	}

	private boolean isUnavailable() {
		return this.queue.isEmpty();
	}

	private Letter doReceive() {
		final Letter result = (Letter)queue.remove( 0 );
		// notify Producer that value has been retrieved
		this.notifyAll();
		return result;
	}

	public synchronized Letter receive() {
		while ( this.isUnavailable() ) {
			try {
				// wait for Producer to putOne value
				this.wait( this.timeout );
				if ( this.isUnavailable() ) {
					if ( this.throw_on_timeout ) throw new LetterBoxTimeOutException();
					return this.timeout_default;
				}
			} catch ( InterruptedException e ) {
				if ( this.is_interruptible ) throw new LetterBoxInterruptedException( e );
			}
		}
		return this.doReceive();
	}


	//	---- SENDING

	private boolean hasNoRoom() {
		return queue.size() >= this.maxsize;
	}

	private void doNotifyAll() {
		if ( this.isAvailable() ) {
			this.notifyAll();
			final AutoReply auto_reply = this.getAutoReply();
			final PostOffice post_office = this.getPostOffice();
			if ( auto_reply != null ) {
				if ( auto_reply.inThisThread() ) {
					auto_reply.autoReply( this );
				} else if ( post_office != null && auto_reply.usePostOffice() ) {
						post_office.addAutoReplyEvent( auto_reply, this );
				} else {
					//	We have to synthesize our own Thread.
					final Runnable r = (
						new Runnable() {
							public void run() {

							}
						}
					);
					new Thread( r ).start();
				}
			}
		}
	}

	private void waitForRoom() {
		while ( this.hasNoRoom() ) {
			try {
				// wait for Consumer to get value
				this.wait();
				if ( this.hasNoRoom() ) throw new LetterBoxTimeOutException();
			} catch ( InterruptedException e ) {
				if ( this.is_interruptible ) throw new LetterBoxInterruptedException( e );
			}
		}
	}

	private void doPut( final Letter value ) {
		queue.add( value );
		this.doNotifyAll();
	}

	private void putOne( final Letter value ) {
		if ( this.is_retaining_locally ) {
			if ( this.is_closed ) throw new LetterBoxClosedException();
			this.waitForRoom();
			this.doPut( value );
		}
	}

	private void doPutMany( final Collection value ) {
		queue.addAll( value );
		this.doNotifyAll();
	}

	private void putMany( final Collection value ) {
		if ( this.is_retaining_locally ) {
			if ( this.is_closed ) throw new LetterBoxClosedException();
			this.waitForRoom();
			this.doPutMany( value );
		}
	}

	//	---oooOOOooo---


	public void close() {
		this.is_closed = true;
	}

	public boolean isOpen() {
		return ! this.is_closed;
	}

	public boolean isRetainingLocally() {
		return this.is_retaining_locally;
	}

	public void setRetainingLocally( boolean retaining_locally ) {
		this.is_retaining_locally = retaining_locally;
	}

	//	---oooOOOooo---

	public PostOffice getPostOffice() {
		return post_office;
	}

	public void setPostOffice( PostOffice post_office ) {
		this.post_office = post_office;
	}

	public AutoReply getAutoReply() {
		return auto_reply;
	}

	public void setAutoReply( AutoReply auto_reply ) {
		this.auto_reply = auto_reply;
	}

	//	---oooOOOooo---
	//
	//	Implements missing methods of CirculationList
	//

	public synchronized void sendOne( final Letter letter ) {
		this.putOne( letter );
		this.forwardOne( letter );
	}

	//	Collection< Letter >
	public synchronized void sendMany( final Collection letters ) {
		this.putMany( letters );
		this.forwardMany( letters );
	}

	//	---oooOOOooo---
	//
	//	Swing plumbing.
	//

	public void invokeLaterOnLetterArrival( final Runnable runnable ) {
		this.setAutoReply(
			new AutoReply() {

				public boolean inThisThread() {
					return true;
				}

				public void autoReply( final LetterBox _letter_box ) {
					SwingUtilities.invokeLater( runnable );
				}

			}
		);
	}

}
