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

import org.openspice.jspice.alert.Alert;

import java.util.*;
import java.io.*;


public final class PrintTools {
	
	public static final boolean debugging = false;

	public static void spaces( final int nspaces ) {
		for ( int i = 0; i < nspaces; i++ ) {
			System.out.print( ' ' );
		}
	}
	
	public static void print( final Object obj ) {
		System.out.print( obj );
	}

	public static void print( final char ch ) {
		System.out.print( ch );
	}

	public static void println( final Object obj ) {
		System.out.println( obj );
	}

	public static final void printFile( final File file ) {
		try {
			final BufferedReader r = new BufferedReader( new FileReader( file ) );
			for(;;) {
				final String s = r.readLine();
				if ( s == null ) break;
				System.out.println( s );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}

	}

	private static final void oops( final String s ) {
		throw new Alert( "malformed control string" ).culprit( "control_string", s ).mishap();
	}

	public static final void formatTo( final Consumer consumer, final String control_string, final Object[] args ) {
		int count = 0;
		for ( int i = 0; i < control_string.length(); i++ ) {
			final char ch = control_string.charAt( i );
			if ( ch == '%' ) {
				i += 1;
				if ( i < control_string.length() ) {
					char c = control_string.charAt( i );
					int index = 0;
					while ( Character.isDigit( c ) ) {
						index = index * 10 + ( c - '0' );
						i += 1;
						if ( i < control_string.length() ) {
							c = control_string.charAt( i );
						} else {
							oops( control_string );
						}
					}
					if ( c == '%' ) {
						consumer.out( c );
					} else if ( c == 's' ) {
						PrintTools.showTo( consumer, args[ index == 0 ? count++ : index - 1 ] );
					} else if ( c == 'p' ) {
						PrintTools.printTo( consumer, args[ index == 0 ? count++ : index - 1 ] );
					} else {
						oops( control_string );
					}
				} else {
					oops( control_string );
				}
			} else {
				consumer.out( ch );
			}
		}
	}

	public static void printTo( final Consumer cuchar, final Object obj ) {
		if ( obj == null ) {
			//	nothing
		} else if ( obj instanceof String ) {
			cuchar.outString( (String)obj );
		} else if ( obj instanceof List ) {
			for ( Iterator it = ((List)obj).iterator(); it.hasNext(); ) {
			 printTo( cuchar, it.next() );
			}
		} else if ( obj instanceof Character ) {
			cuchar.outObject( obj );
		} else if ( obj instanceof Map.Entry ) {
			printTo( cuchar, ((Map.Entry)obj).getValue() );
		} else if ( obj instanceof org.openspice.jspice.datatypes.SpiceObject ) {
			((org.openspice.jspice.datatypes.SpiceObject)obj).printTo( cuchar );
		} else {
			cuchar.outObject( obj );
		}
	}



	public static String printToString( final Object obj ) {
		final StringBufferConsumer sbc = new StringBufferConsumer();
		printTo( sbc, obj );
		return (String)sbc.close();
	}

	public static void showTo( final Consumer cuchar, final Object obj ) {
		if ( obj instanceof Showable ) {
			((Showable)obj).showTo( cuchar );
		} else if ( obj == null ) {
			cuchar.outString( "absent" );
		} else if ( obj instanceof String ) {
			cuchar.out( '\"' );
			cuchar.outObject( obj );
			cuchar.out( '\"' );			
		} else if ( obj instanceof List ) {
			cuchar.out( '{' );
			String gap = "";
			for ( Iterator it = ((List)obj).iterator(); it.hasNext(); ) {
				cuchar.outString( gap );
				showTo( cuchar, it.next() );
				gap = ", ";
			}
			cuchar.out( '}' );
		} else if ( obj instanceof Character ) {
			cuchar.out( '\'' );
			cuchar.outObject( obj );
			cuchar.out( '\'' );
		} else if ( obj instanceof org.openspice.jspice.datatypes.SpiceObject ) {
			((org.openspice.jspice.datatypes.SpiceObject)obj).showTo( cuchar );
		} else if ( obj instanceof Map.Entry ) {
			cuchar.out( '(' );
			showTo( cuchar, ((Map.Entry)obj).getKey() );
			cuchar.outString( " ==> " );
			showTo( cuchar, ((Map.Entry)obj).getValue() );
			cuchar.out( ')' );
		} else {
			cuchar.outObject( obj );
		}
	}
	
	public static void show( final Object obj ) {
		showTo( StdOutConsumer.OUT, obj );
	}
	
	public static String showToString( final Object obj ) {
		final StringBufferConsumer sbc = new StringBufferConsumer();
		showTo( sbc, obj );
		return (String)sbc.close();
	}

	
	public static void showln( final Object obj ) {
		show( obj );
		System.out.println();
	}
	
	public static abstract class SpiceRenderProc extends org.openspice.jspice.datatypes.proc.Proc {
		public org.openspice.jspice.datatypes.Arity inArity() { return org.openspice.jspice.datatypes.Arity.ZERO_OR_MORE; }
		public org.openspice.jspice.datatypes.Arity outArity() { return org.openspice.jspice.datatypes.Arity.ZERO; }

		public abstract void renderTo( final Consumer cuchar, final Object obj );
		public abstract void gap( final boolean first, final boolean last, final Consumer cuchar );

		void applyHelper( final Consumer cuchar, final org.openspice.jspice.vm_and_compiler.VM vm, final int nargs, final int orig_nargs ) {
			if ( nargs <= 1 ) this.gap( true, orig_nargs == 0, cuchar );
			//this.renderTo( cuchar, vm_and_compiler.pop() );
			if ( nargs >= 1 ) {
				final Object tos = vm.pop();
				if ( nargs >= 2 ) {
					this.applyHelper( cuchar, vm, nargs - 1, orig_nargs );
				}
				//System.out.println( "nargs = " + nargs + "/" + orig_nargs );
				this.renderTo( cuchar, tos );
				this.gap( false, nargs == orig_nargs, cuchar );
			} 
		}

		Object getFirstArg( final org.openspice.jspice.vm_and_compiler.VM vm, int nargs ) {
			assert nargs >= 1;
			return vm.get( nargs - 1 );
		}
	}
	
	static abstract class SpiceRenderStdOutProc extends SpiceRenderProc {
		public Object call( Object tos, final org.openspice.jspice.vm_and_compiler.VM vm, int nargs ) {
			vm.push( tos );		//	Normalize data stack.
			this.applyHelper( StdOutConsumer.OUT, vm, nargs, nargs );
			return vm.pop();	//	Expand data stack.
		}
	}

	public static abstract class SpicePrintStdOutProc extends SpiceRenderStdOutProc {
		public void renderTo( final Consumer cuchar, final Object obj ) {
			PrintTools.printTo( cuchar, obj );
		}
	}

	public static abstract class SpiceShowStdOutProc extends SpiceRenderStdOutProc {
		public void renderTo( final Consumer cuchar, final Object obj ) {
			PrintTools.showTo( cuchar, obj );
		}
	}

	
	static abstract class SpiceRenderToProc extends SpiceRenderProc {
		public Object call( Object tos, final org.openspice.jspice.vm_and_compiler.VM vm, int nargs ) {
			vm.push( tos );		//	Normalize data stack.
			final Consumer c = (Consumer)this.getFirstArg( vm, nargs );
			this.applyHelper( c, vm, nargs - 1, nargs - 1 );
			vm.drop();
			return vm.pop();	//	Expand data stack.
		}
	}

	public static abstract class SpicePrintToProc extends SpiceRenderToProc {
		public void renderTo( final Consumer cuchar, final Object obj ) {
			PrintTools.printTo( cuchar, obj );
		}
	}

	public static abstract class SpiceShowToProc extends SpiceRenderToProc {
		public void renderTo( final Consumer cuchar, final Object obj ) {
			PrintTools.showTo( cuchar, obj );
		}
	}

}
