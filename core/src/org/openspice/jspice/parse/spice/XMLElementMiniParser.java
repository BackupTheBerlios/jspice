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

import org.openspice.jspice.parse.miniparser.Bothfix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.alert.Alert;

import java.util.List;
import java.util.ArrayList;

public final class XMLElementMiniParser extends Bothfix {

	public Expr postfix( final String sym, final int prec, final Expr lhs, final Parser parser ) {
		return RelOpMiniParser.PROTOTYPE.postfix( sym, prec, lhs, parser );
	}

	private Expr readXmlElement( Parser parser ) {
		final Token opening_tok = parser.peekXmlName();
		final Expr el_name = parser.readAtomicExpr( true );

		final List el_attrs = new ArrayList();
		do {
			if ( parser.tryReadToken( ">" ) != null ) {
				break;
			} else if ( parser.tryReadToken( "/>" ) != null ) {
				return XmlElementExpr.make( el_name, CompoundExpr.make( el_attrs ), SkipExpr.make() );
			} else {
				parser.peekXmlName();
				final Expr name = parser.readAtomicExpr( true );
				parser.mustReadToken( "=" );
				final Expr value = parser.readAtomicExpr( false );
				el_attrs.add( XmlAttrExpr.make( name, value ) );
			}
		} while ( true );

		final Expr el_body = parser.readStmnts();
		if ( parser.tryReadToken( "</" ) != null ) {
			parser.peekXmlName();
			final Token closing_tok = parser.readToken();
			if ( !opening_tok.matches( closing_tok ) ) {
				new Alert(
					"Mismatched closing tag",
					"Opening and closing tag names must match"
				).
				culprit( "opening", opening_tok ).culprit( "closing", closing_tok ).
				hint( "Use the universal closing tag </> to match any opening tag" ).
				mishap( 'P' );
			}
			parser.mustReadToken( ">" );
		} else {
			parser.mustReadToken( "</>" );
		}

		return XmlElementExpr.make( el_name, CompoundExpr.make( el_attrs ), el_body );
	}

	public Expr prefix( String sym, Parser parser ) {
		//	Read a sequence of juxtaposed XmlElements and XmlComments.
		final Expr e = this.readXmlElement( parser );
		final Token tok = parser.peekToken();
		if ( !tok.hasName( "<" ) && !tok.hasName( "<!--" ) ) {
			return e;
		} else {
			return CommaExpr.make( e, parser.readExpr() );
		}
	}
}
