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

import org.openspice.jspice.datatypes.proc.FastProc;
import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.Deferred;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.built_in.lists.NewListProc;
import org.openspice.jspice.tools.MapTools;

import java.util.*;

public class NewMapProc extends FastProc {

	{
		this.setDescription(
			"newMap",
			"%p( A1, ..., An ) -> map",
			"returns a mutable map whose members are the maps or maplets A1 to An"
 		);
	}

	private void addValue( final Map map, final Object x ) {
		final Object arg = Deferred.deref( x );
		if ( arg instanceof Map.Entry ) {
			final Map.Entry me = (Map.Entry)arg;
			map.put( me.getKey(), me.getValue() );
		} else if ( arg instanceof Map ) {
			map.putAll( (Map)arg );
		} else {
			map.putAll( MapTools.convertTo( arg ) );
		}
	}

	public Object fastCall( final Object tos, final VM vm, final int nargs ) {
		final Map answer = new HashMap();
		if ( nargs == 0 ) {
			vm.push( tos );
			return answer;
		} else if ( nargs == 1 ) {
			final Object arg = Deferred.deref( tos );
			this.addValue( answer, tos );
			return answer;
		} else {
			//	We must add in the correct order!
			final Object[] allbutlastarg = vm.popArray( nargs - 1 );
			//	This is inefficient - must find a good idiom for this.  todo:
			for ( int i = 0; i < allbutlastarg.length; i++ ) {
				this.addValue( answer, allbutlastarg[ i ] );
			}
			//	Then do the last argument.
			this.addValue( answer, tos );
			return answer;
		}
	}

	public Arity inArity() {
		return Arity.ZERO_OR_MORE;
	}

	public Arity outArity() {
		return Arity.ONE;
	}

	public static final NewMapProc NEW_MAP_PROC = new NewMapProc();

}
