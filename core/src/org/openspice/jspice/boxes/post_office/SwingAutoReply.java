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

public abstract class SwingAutoReply extends AbsAutoReply {

	protected SwingAutoReply( final PostOffice postOffice ) {
		super( postOffice );
	}

	public abstract void invoke( final Letter letter );

	public final boolean inThisThread() {
		return true;
	}

	public final void replyTo( final Letter letter ) {
		SwingUtilities.invokeLater( new SwingAutoReplyThunk( this, letter ) );
	}

	public final boolean usePostOffice() {
		return false;
	}

	static final class SwingAutoReplyThunk implements Runnable {
		final SwingAutoReply swingAutoReply;
		final Letter letter;

		public SwingAutoReplyThunk( SwingAutoReply swingAutoReply, Letter letter ) {
			this.swingAutoReply = swingAutoReply;
			this.letter = letter;
		}

		public void run() {
			this.swingAutoReply.invoke( this.letter );
		}
	}

}
