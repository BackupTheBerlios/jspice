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
package org.openspice.jspice.built_in.maps;

import org.openspice.jspice.lib.MapLib;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;

public final class IndexProc extends Binary1InvokeProc {

	public Object invoke( final Object key, final Object obj ) {
		return MapLib.getAt( obj, key );
	}

	public Object ucall( final Object tos, final VM vm, final int vargs, final int kargs ) {
		Arity.TWO.check( kargs );
		//	I strongly suspect I've got these arguments round the wrong way.
		final Object map_like = tos;
		final Object key = vm.pop();
		Arity.ONE.check( vargs );
		final Object val = vm.pop();
		MapLib.putAt( map_like, key, val );
		return vm.pop();
	}

	public final static IndexProc INDEX_PROC = new IndexProc();

}
