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
package org.openspice.jspice.datatypes;

import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.lib.AbsentLib;
import org.openspice.jspice.vm_and_compiler.VM;

public class ThunkDeferred extends Deferred {

	Proc thunk;
	VM vm;

	public ThunkDeferred( final Proc _thunk, final VM _vm ) {
		this.thunk = _thunk;
		this.vm = _vm;
	}

	public Object calculate() {
		//	Push a superfluous absent.
		final Object answer = this.thunk.call( AbsentLib.ABSENT, this.vm, 0 );
		//	Discard it.
		this.vm.pop();
		return answer;
	}

	//	Free resources for garbage collection.
	public void freeResources() {
		this.thunk = null;
		this.vm = null;
	}


}
