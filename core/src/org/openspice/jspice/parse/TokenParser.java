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
package org.openspice.jspice.parse;

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.NameExpr;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.tokens.QuotedToken;
import org.openspice.jspice.tokens.NumberToken;

public abstract class TokenParser {

	public abstract int getPrec( final String s );
	public abstract Expr parseNumber( final NumberToken numberToken, final int prec, final Expr lhs, final org.openspice.jspice.parse.Parser parser );
	public abstract Expr parseQuoted( final QuotedToken quotedToken, final int prec, final Expr lhs, final org.openspice.jspice.parse.Parser parser );
	public abstract Expr parseName( final NameToken nameToken, final int prec, final Expr lhs, final Parser parser );
	public abstract NameExpr plainName( final NameToken nameToken );

	public abstract Expr atomicName( final NameToken nameToken );
	public abstract Expr atomicQuoted( final QuotedToken quotedToken );
	public abstract Expr atomicNumber( final NumberToken numberToken );

	public final NameExpr makePlainNameExpr( final Token token ) {
		if ( token instanceof NameToken ) {
			return this.plainName( (NameToken)token );
		} else {
			return null;
		}
	}

	public final Expr makeAtomicExpr( final Token token, final boolean quote_symbol ) {
		if ( token instanceof NameToken ) {
			return (
				quote_symbol ? this.atomicName( (NameToken)token ) :
				NameExpr.make( ( (NameToken)token ).getInterned() )
			);
		} else if ( token instanceof NumberToken ) {
			return this.atomicNumber( (NumberToken)token );
		} else if ( token instanceof QuotedToken ) {
			return this.atomicQuoted( (QuotedToken)token );
		} else {
			throw Alert.unreachable();
		}
	}

	public final Expr makeAtomicNameExpr( final Token token ) {
		if ( token instanceof NameToken ) {
			return this.atomicName( (NameToken)token );
		} else {
			throw new Alert( "Name token required" ).culprit( "token", token ).mishap( 'P' );
		}
	}
}
