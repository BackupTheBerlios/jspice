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
import java.util.Iterator;

public abstract class AbsAutoReply extends AbsLetterBox implements AutoReply {

	protected AbsAutoReply( final PostOffice postOffice ) {
		super( postOffice );
	}

	/**
	 * Returns true if it is both cheap enough and, more importantly,
	 * safe enough to run the reply in the thread of the poster.  In
	 * general the answer is _no_ it is not safe because of the risk of
	 * infinite loops.  However, if you think you are superman you may
	 * decide to override this.
	 *
	 * @return flag indicating it is safe to reply in the originating thread
	 */
	public boolean inThisThread() {
		return false;
	}

	private final boolean isSingleThreaded() {
		return this.inThisThread() || this.usePostOffice();
	}

	private void autoReply( final Letter letter ) {
		if ( this.isOpen() ) {
			if ( this.inThisThread() ) {
				this.replyTo( letter );
			} else {
				final Runnable runnable = new AutoReplyThunk( this, letter );
				if ( this.usePostOffice() ) {
					this.getPostOffice().addRunnable( runnable );
				} else {
					new Thread( runnable ).start();
				}
			}
		}
	}

	public final synchronized void sendOne( final Letter letter ) {
		this.autoReply( letter );
	}

	public final synchronized void sendMany( final Collection letters ) {
		if ( this.isOpen() ) {
			if ( this.isSingleThreaded() ) {
				for ( Iterator it = letters.iterator(); it.hasNext(); ) {
					final Letter letter = (Letter)it.next();
					this.autoReply( letter );
				}
			} else {
				for ( Iterator it = letters.iterator(); it.hasNext(); ) {
					final Letter letter = (Letter)it.next();
					new Thread( new AutoReplyThunk( this, letter ) ).start();
				}
			}
		}
	}

	static class AutoReplyThunk implements Runnable {
		final AutoReply auto_reply;
		final Letter letter;

		public AutoReplyThunk( AutoReply auto_reply, Letter letter ) {
			this.auto_reply = auto_reply;
			this.letter = letter;
		}

		public void run() {
			this.auto_reply.replyTo( this.letter );
		}
	}

}
