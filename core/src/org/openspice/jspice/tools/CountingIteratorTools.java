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
package org.openspice.jspice.tools;

import org.openspice.jspice.arithmetic.Add;
import org.openspice.jspice.arithmetic.GreaterThanOrEqual;
import org.openspice.jspice.arithmetic.LessThanOrEqual;
import org.openspice.tools.ImmutableIterator;

import java.util.*;

public class CountingIteratorTools {
	
	static abstract class NumberCountingIterator extends ImmutableIterator {
		protected Number sofar;
		final Number by_number;
		
		NumberCountingIterator( final Number _from_num, final Number _by_number ) {
			this.sofar = _from_num;
			this.by_number = _by_number;
		}
		
		public Object next() {
			final Number ans = this.sofar;
			this.sofar = (Number)Add.ADD.apply( this.sofar, this.by_number );
			return ans;		
		}
	}
	
	public static Iterator fromBy( final Number from_num, final Number by_num ) {
		return(
			new NumberCountingIterator( from_num, by_num ) {
				public boolean hasNext() { return true; }
			}
		);
	}
	
	public static Iterator fromByTo( final Number from_num, final Number by_num, final Number to_num ) {
		if ( GreaterThanOrEqual.gtez( by_num ) ) {
			//	Counting up.
			return(
				new NumberCountingIterator( from_num, by_num ) {
					public boolean hasNext() { 
						return LessThanOrEqual.lte( this.sofar, to_num );
					}
				}
			);			
		} else {
			//	Counting down.
			return(
				new NumberCountingIterator( from_num, by_num ) {
					public boolean hasNext() { 
						return GreaterThanOrEqual.gte( this.sofar, to_num );
					}
				}
			);
		}
	}

}
