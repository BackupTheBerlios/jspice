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

import org.openspice.jspice.datatypes.*;
import org.openspice.jspice.datatypes.proc.Proc;
import org.openspice.jspice.datatypes.proc.Binary1InvokeProc;
import org.openspice.jspice.lib.AbsentLib;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
isMatch( regexp, str ) -> Boolean  # poor name but you don't use it much
hasMatch( regexp, str ) -> Absent | Binding
split( regexp, str ) -> { String }
allMatches( regexp, str ) -> { Binding }
bindingMatchString( Binding ) -> String
bindingMatched( Binding ) -> String
bindingMatchedLimits( Binding ) -> ( lo : Int, hi : Int )
bindingMatchVar( Binding, Int ) -> String
bindingMatchVarLimits( Binding, Int ) -> ( lo : Int, hi : Int )


And I assumed the following apply-actions:

         regexp( str ) ----> isMatch( regexp, str )

         binding()     ----> bindingMatched( binding )
         binding[ n ]  ----> bindingMatchVar( binding, n )

*/

public final class RegexProcs {

	public static final Proc split = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				return new ImmutableList( ((Pattern)regexp).split( (CharSequence)str ) );
			}
		}
	);

	public static final Proc isMatch = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				final Matcher x = ((Pattern)regexp).matcher( (CharSequence)str );
				return x.matches() ? Boolean.TRUE : Boolean.FALSE;
			}
		}
	);

	public static final Proc isPrefixMatch = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				final Matcher x = ((Pattern)regexp).matcher( (CharSequence)str );
				return x.lookingAt() ? Boolean.TRUE : Boolean.FALSE;
			}
		}
	);

	public static final Proc isPartMatch = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				final Matcher x = ((Pattern)regexp).matcher( (CharSequence)str );
				return x.find() ? Boolean.TRUE : Boolean.FALSE;
			}
		}
	);

	public static final Proc hasMatch = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				final Matcher x = ((Pattern)regexp).matcher( (CharSequence)str );
				return x.matches() ? new Binding( x ) : AbsentLib.ABSENT;
			}
		}
	);

	public static final Proc allMatches = (
		new Binary1InvokeProc() {
			public Object invoke( final Object regexp, final Object str ) {
				final Matcher x = ((Pattern)regexp).matcher( (CharSequence)str );
				if ( x.find() ) {
//					System.err.println( "find" );
					return new BindingList( x );
				} else {
					return EmptyImmutableList.exemplar;
				}
			}
		}
	);



}
