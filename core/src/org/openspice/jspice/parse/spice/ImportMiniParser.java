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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.ImportExpr;

public class ImportMiniParser extends Prefix {

	static class ReadPackageNameAndAlias {

		final NameToken nickname;
		final NameToken pkg_name;

		ReadPackageNameAndAlias( final Parser parser ) {
			final NameToken name1 = parser.readPackageName();
			if ( parser.tryReadToken( "=" ) != null ) {
				//	name1 got the nickname.  So now we need to read the package name
				final NameToken name2 = parser.readPackageName();
				//	And we must verify that name1 is a feasible nickname.
				if ( name1.getWord().indexOf( '.' ) != -1 ) {
					new Alert( "invalid nickname" ).culprit( "token", name1 ).mishap( 'P' );
				}
				this.nickname = name1;
				this.pkg_name = name2;
			} else {
				this.pkg_name = name1;
				//	There is no nickname - so we have to pick it out.
				final String s = name1.getWord();
				final int n = s.lastIndexOf( '.' );
				if ( n == -1 ) {
					//	Package name and nickname are the same when there are no dots.
					this.nickname = name1;
				} else {
					this.nickname = new NameToken( false, s.substring( n + 1 ) );
				}
			}
		}
	}

	/**
	 * Parses
	 * 		import [qualified] [ NICKNAME = ] PKGNAME [ facet FACETS ] [ into FACET ]
	 * 		......^  this is where the input rests.
	 * @param interned
	 * @param parser
	 * @return
	 */
	public Expr prefix( final String interned, final Parser parser ) {
		final boolean qualified = parser.canReadToken( "qualified" );
		final ReadPackageNameAndAlias r = new ReadPackageNameAndAlias( parser );
		final NameToken nickname = r.nickname;
		final NameToken pkg_name = r.pkg_name;
//		System.out.println( "read nickname = " + nickname );
//		System.out.println( "read pkg name = " + pkg_name );
		return ImportExpr.make( pkg_name.getWord(), nickname.getWord(), qualified );
	}

}
