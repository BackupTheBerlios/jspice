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
package org.openspice.jspice.vm_and_compiler;

import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.datatypes.ImmutableList;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.namespace.NameSpaceManager;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.lib.AbsentLib;

import java.util.*;

public final class VM {
	Stack stack = new Stack();
	Stack callstack = new Stack();
	Stack intstack = new Stack();

	/**
	 * Virtual machines registers.  Can be used for anything in principle.
	 * However, n_args is used in function calls and v_args in assignments &
	 * initializations.
	 */
	int n_args;			//	How many arguments are waiting on the stack.
	int v_args;			//	How many values are waiting on the stack.


	private final JSpiceConf jspice_conf;

	public VM( JSpiceConf _jconf ) {
		this.jspice_conf = _jconf;
		this.stack = new Stack();
		this.callstack = new Stack();
		this.intstack = new Stack();
	}

	public JSpiceConf getJSpiceConf() {
		return this.jspice_conf;
	}

	public int intpop() {
		return ((Integer)this.intstack.pop()).intValue();
	}
	
	public void intpush( final int n ) {
		this.intstack.push( new Integer( n ) );
	}

	//	Very inefficient!  Note that in the future we should be planning on
	//	using arraycopy to make this go faster! 
	public Object callpush( Object tos, final boolean[] args_named, final int nslots ) {
		assert nslots >= 0 && args_named != null;
		final int sz = this.callstack.size() + nslots;
		this.callstack.setSize( sz );
		int n = 1;
		for ( int i = args_named.length - 1; i >= 0; i-- ) {
			if ( args_named[ i ] ) {
				//System.out.println( "set[" + i + "]: " + tos );
				this.callstack.set( sz - n++, tos );
				tos = this.stack.pop();
			} else {
				//System.out.println( "drop " + tos );
				tos = this.stack.pop();
			}
		}
		return tos;
	}
	
	public void callpush( final int nslots ) {
		assert nslots >= 0;
		final int sz = this.callstack.size() + nslots;
		this.callstack.setSize( sz );
	}

	
	//	Very inefficient!
	public void calldrop( final int n ) {
		assert n >= 0;
		this.callstack.setSize( this.callstack.size() - n );
	}
	
	//	Very inefficient!
	public Object load( final int offset ) {
		//System.out.println( "load offset = " + offset );
		return this.callstack.get( this.callstack.size() - offset );
	}
	
	//	Very inefficient!
	public void store( final int offset, final Object obj ) {
		this.callstack.set( this.callstack.size() - offset, obj );
	}
	
	public Object pop() {
		return this.stack.pop();
	}

	public Object[] popArray( final int n ) {
		final Object[] array = new Object[ n ];
		for ( int i = n - 1; i >= 0; i-- ) {
			array[ i ] = this.stack.pop();
		}
		return array;
	}
	

	public Object get( final int n ) {
		return this.stack.get( n );
	}
	
	public void drop() {
		this.stack.pop();
	}
	
	public void drop( final int n ) {
		assert( n >= 0 );
		this.stack.setSize( this.stack.size() - n );
	}
	
	public void conslist( final int n ) {
		assert( n >= 0 );
		final Object[] data = new Object[ n ];
		for ( int i = n - 1; i >= 0; i-- ) {
			data[ i ] = this.stack.pop();
		}
		this.stack.push( new ImmutableList( data ) );
	}
	
	public void push( Object x ) {
		this.stack.push( x );
	}
	
	public void save( final Object[] buffer ) {
		for ( int i = 0; i < buffer.length; i++ ) {
			buffer[ i ] = this.stack.pop();
		}
	}
	
	public void restore( final Object[] buffer ) {
		for ( int i = buffer.length - 1; i >= 0; i-- ) {
			this.stack.push( buffer[ i ] );
		}
	}
	
	public void clearAll() {
		this.stack.clear();
	}
	
	//	Might be the wrong place for this.
	public void showAll() {
		final Enumeration enum = this.stack.elements();
		final int count = this.stack.size();
		for ( int i = 1; i < count; i++ ) {
			final Object obj = this.stack.get( i );
			PrintTools.showln( obj );
		}
	}

	private List saved_results = ImmutableList.EMPTY_LIST;
	
	public void saveAllResults() {
		final List list = new ArrayList();
		final int count = this.stack.size();
		for ( int i = 1; i < count; i++ ) {
			list.add( this.stack.get( i ) );
		}
		this.saved_results = list;
	}

	public List getAllResults() {
		return this.saved_results;
	}

	/**
	 * Note that we will end up with an extra null at the
	 * start of the stack.
	 */
	public void run( final Pebble p ) {
		this.push( p.run( null, this ) );
	}
	
	public int length() { 
		return this.stack.size();
	}

	/**
	 * Ditch the extra null.
	 */
	public void copyTo( final List list ) {
		final Iterator it = this.stack.iterator();
		if ( it.hasNext() ) it.next();
		while ( it.hasNext() ) {
			list.add( it.next() );
		}
	}
}
