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


abstract class PropsFlags {
	protected int flags = 0;
	
	boolean getBit( final int bit ) {
		return ( this.flags & ( 1 << bit ) ) != 0;
	}
	
	void setBit( final int n ) {
		this.flags |= ( 1 << n );
	}
	
	void clearBit( final int n ) {
		this.flags &= ~( 1 << n );
	}
		
	PropsFlags modBit( final int n, final boolean flag ) {
		if ( flag ) {
			this.setBit( n );
		} else {
			this.clearBit( n );
		}
		return this;
	}
}
	

public class Props extends PropsFlags {
	
	public boolean isConstant() {
		return this.getBit( 0 );
	}
	
	public Props modConstant( final boolean b ) {
		return (Props)this.modBit( 0, b );
	}

	public boolean isForward() {
		return this.getBit( 1 );
	}
	
	public boolean isForwardVersion( final Props that ) {
		//System.out.println( "Comparing " + this.flags + " with " + that.flags ); 
		return this.isForward() && ( ( this.flags ^ that.flags ) == ( 1 << 1 ) );
	}
	
	public Props modForward( final boolean b ) {
		return (Props)this.modBit( 1, b );
	}
	
	public static final Props FWD_VAL = new Props().modConstant( true ).modForward( true );
	public static final Props VAL = new Props().modConstant( true );
	public static final Props VAR = new Props();
}
