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
package org.openspice.jspice.built_in;

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.alert.Alert;

public final class PartApply extends Proc {
	final static public org.openspice.jspice.built_in.PartApply PART_APPLY = new org.openspice.jspice.built_in.PartApply();

	public Arity inArity() { return Arity.ONE_OR_MORE; }

	public Arity outArity() { return Arity.ONE; }

	public Object call( final Object tos, final VM vm, final int nargs ) {
		if ( nargs >= 2 ) {
			vm.push( tos );
			final Object[] frozvals = new Object[ nargs - 1 ];
			for ( int i = nargs - 2; i >= 0; i-- ) {
				frozvals[ i ] = vm.pop();
			}
			final Proc proc = CastLib.toProc( vm.pop() );
			//System.out.println( "subtract " +  frozvals.length + " from " + proc.inArity() );
			Arity ia = null;
			try {
				ia = proc.inArity().sub( frozvals.length );
			} catch ( final Arity.ArityException exn ) {
				new Alert(
					"Cannot partially apply this procedure to so many arguments"
				).culprit( "procedure", proc ).warning( 'E' );
				ia = Arity.ZERO_OR_MORE;
			}
			final Arity fia = ia;
			return(
				new Proc() {
					public Arity inArity() { return fia; }
					public Arity outArity() { return proc.outArity(); }
					public Object call( final Object tos, final VM vm, final int nargs ) {
						vm.push( tos );
						for ( int i = 0; i < frozvals.length; i++ ) {
							vm.push( frozvals[ i ] );
						}
						return proc.call( vm.pop(), vm, nargs + frozvals.length );
					}
				}
			);
		} else if ( nargs == 1 ) {
			return tos;
		} else {
			Arity.ONE_OR_MORE.check( nargs );
			return null;	//	sop
		}
	}
}
