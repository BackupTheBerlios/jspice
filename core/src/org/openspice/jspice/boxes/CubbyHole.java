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
package org.openspice.jspice.boxes;

class CubbyHole {

	private Object contents;
	private boolean available = false;

	public synchronized Object fetch( final long timeout ) throws InterruptedException {
		while ( available == false ) {
			this.wait( timeout );
		}
		available = false;
		// notify Producer that value has been retrieved
		notifyAll();
		return contents;
	}

	public synchronized Object get( final long timeout ) {
		while ( available == false ) {
			try {
				// wait for Producer to putOne value
				this.wait( timeout );
			} catch ( InterruptedException e ) {
			}
		}
		available = false;
		// notify Producer that value has been retrieved
		notifyAll();
		return contents;
	}

	public synchronized Object get() {
		return this.get( 0 );
	}

	public synchronized void put( final Object value ) {
		while ( available == true ) {
			try {
				// wait for Consumer to get value
				wait();
			} catch ( InterruptedException e ) {
			}
		}
		contents = value;
		available = true;
		// notify Consumer that value has been set
		notifyAll();
	}
	
}
