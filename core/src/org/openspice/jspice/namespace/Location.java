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

package org.openspice.jspice.namespace;

import org.openspice.jspice.lib.AbsentLib;
import org.openspice.vfs.VItem;

import java.io.File;


public final class Location {

    Object 		value;
    LifeCycle 	lifeCycle;

	public boolean referenceCheck() {
		return this.lifeCycle.lifeCycleCheck( false );
	}

	public void useCheck() {
		this.lifeCycle.lifeCycleCheck( true );
	}

	/**
	 * The location has fully matured.
	 */
	public void makeSet() {
		this.lifeCycle = LifeCycle.SET;
	}
 
	public void makeAutoloadReady( final NameSpace ns, final Var.Perm perm, final FacetSet fs, final VItem file ) {
		this.lifeCycle = new LifeCycle.AutoloadReady( ns, perm, fs, file );
	}

	public boolean isSet() {
		return this.value == LifeCycle.SET;
	}

	public Object getValue() {
		return this.value;
	}
	
	public void setValue( final Object x ) {
		this.value = x;
	}

	public Location() {
		this.value = AbsentLib.ABSENT;
		this.lifeCycle = LifeCycle.SET;
	}

	public Location( final Object value ) {
		this.value = value;
		this.lifeCycle = LifeCycle.SET;
	}

}
