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

import org.openspice.jspice.expr.cases.LambdaExpr;
import org.openspice.jspice.main.Print;

public abstract class Var {
	Props props;							//	Forward declarations mean this cannot be final.
	public abstract boolean isType1();
	public abstract boolean isType3();
	
	public Var( final Props _props ) {
		this.props = _props;
	}
	
	public Props getProps() {
		return this.props;
	}
	
	public void setProps( final Props _props ) {
		this.props = _props;
	}

	static abstract class PermFlags extends Var {
		int flags;
		
		PermFlags( final Props _props ) {
			super( _props );
		}

		boolean getBit( final int bit ) {
			return ( this.flags & ( 1 << bit ) ) != 0;
		}
		
		void setBit( final int n ) {
			this.flags |= ( 1 << n );
		}
		
		void clearBit( final int n ) {
			this.flags &= ~( 1 << n );
		}
		
		PermFlags modBit( final boolean flag, final int n ) {
			if ( flag ) {
				this.setBit( n );
			} else {
				this.clearBit( n );
			}
			return this;
		}
		
	}
	
	public static final class Perm extends PermFlags {
		FacetSet 	facet_set;
		String 		id;
		Location 	location;

		public String getName() {
			return this.id;
		}

		public String toString() {
			return "permanent variable(" + this.id +")";
		}

		FacetSet getFacetSet() {
			return this.facet_set;
		}
		
		Perm modProps( final Props _props ) {
			this.setProps( _props );
			return this;
		}
		
		Perm modFacetSet( final FacetSet fs ) {
			this.facet_set = fs;
			return this;
		}
		
		public Location getLocation() {
			return this.location;
		}
		
		public Perm( final Props _props, final FacetSet _facet_set, final String _id, final Location _location ) {
			super( _props );
			this.facet_set = _facet_set;
			this.id = _id;
			this.location = _location;
		}

		boolean getIsNullFlag() { 
			return this.getBit( 0 ); 
		}
		
		Perm modIsNullFlag( final boolean flag ) {
			return (Perm)this.modBit( flag, 0 );
		}
		
		Perm setIsNullFlag() {
			return this.modIsNullFlag( true );
		}
	
		boolean getIsImportedFlag() {
			return this.getBit( 1 );
		}
		
		Perm modIsImportedFlag( final boolean flag ) {
			return (Perm)this.modBit( flag, 1 );
		}
		
		Perm setIsImportedFlag() {
			return this.modIsImportedFlag( true );
		}
	
		public boolean isType1() {
			return false;
		}
		
		public boolean isType3() {
			return false;
		}
		
		public boolean isGlobal() {
			return true;
		}
	}
	
	public static abstract class LocalOrAnon extends Var {
		LocalOrAnon( final Props _props ) {
			super( _props );
		}
	}
	
	public static final class Anon extends LocalOrAnon {
		public Anon( final Props _props ) {
			super( _props );
		}

		public String toString() {
			return "anonymous variable(_)";
		}

		public boolean isGlobal() {
			return false;
		}
	
		public boolean isType1() {
			return true;
		}
		
		public boolean isType3() {
			return false;
		}
		
	}
	
	static int lcount = 0;
	
	public static final class Local extends LocalOrAnon {
		final String title;
		final int count = lcount++;
		private int slot = -1;
		private boolean is_outer = false;	
		final LambdaExpr lambda;

		public Local( final Props _props, final String _title, final LambdaExpr _lambda ) {
			super( _props );
			this.title = _title;
			this.lambda = _lambda;
		}

		public String toString() {
			return "local variable(" + this.title +")";
		}

		public String getTitle() {
			return this.title;
		}
		
//		public String toString() {
//			return "<" + this.title + ":" + this.slot + ":#" + this.count + ":" + this.lambda + ">";
//		}
		
		public int getOffset() {
			if ( Print.wouldPrint( Print.PARSE ) ) {
				Print.println( "getting offset for " + this.toString() + " = " + this.lambda.getSlotCount() + " - " + this.slot + " = " + (this.lambda.getSlotCount() - this.slot ) );
			}
			return this.lambda.getSlotCount() - this.slot;
		}
		
		public void setSlot( final LambdaExpr.SlotCounter counter ) {
			if ( this.slot == -1 ) {
				this.slot = counter.nextSlot();
			} else {
				throw new RuntimeException( "set slot" );
			}
		}
		
		int getSlot() {
			return this.slot;
		}
		
		public boolean isType1() {
			return !this.is_outer;
		}
		
		public boolean isType3() {
			return this.is_outer;
		}
		
		public boolean isOuter() {
			return this.is_outer;
		}
		
		public boolean isGlobal() {
			return false;
		}
		
		public void setIsOuter() {
			this.is_outer = true;
		}
		
	}


}
