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

import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.lexis.PushableTokenizerImpl;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.CommaExpr;
import org.openspice.jspice.expr.cases.NameExpr;
import org.openspice.jspice.expr.cases.SkipExpr;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.main.Interpreter;

import java.io.*;

public class ParserImpl extends Parser {

	final Interpreter interpreter;
    final TokenParser token_parser;
	final PushableTokenizerImpl tokens;
	final int comma_prec;

	public ParserImpl( final Interpreter interpreter, final TokenParser _token_parser, final String origin, final Reader r, final String prompt ) {
		this.interpreter = interpreter;
		this.token_parser = _token_parser;
        this.tokens = new PushableTokenizerImpl( interpreter, origin, r, prompt );
		this.comma_prec = _token_parser.getPrec( "," );
    }

	private boolean holes_used = false;

	public void clearHolesUsed() {
		this.holes_used = false;
	}

	public boolean getHolesUsed() {
		return this.holes_used;
	}

	public void setHolesUsed() {
		this.holes_used = true;
	}

	char phase = 'P';

	private Alert alert( final String msg ) {
		return this.alert( msg, null );
	}

	private Alert alert( final String msg1, final String msg2 ) {
		return (
			new Alert( msg1, msg2, this.phase ).
			culprit( "file", this.tokens.getPrintName() ).
			culprit( "line no.", new Integer( this.tokens.getLineNumber() ) ).
			resetInsertionPoint()
		);
	}
	
	private TmpVarGenerator tvg = new TmpVarGenerator();
	
	public NameExpr newTmpNameExpr() {
		return this.tvg.newTmpVar();
	}
	
	public void saveTmp() {
		this.tvg.save();
	}
	
	public void restoreTmp() {
		this.tvg.restore();
	}
	
	public void clearTmp() {
		this.tvg.clear();
	}


	public boolean isAtEndOfInput() {
		return this.tokens.peekToken() == null;
	}
	
	public Token tryReadToken( final String sym ) {
		return this.tokens.tryReadToken( sym );
	}
	
	public boolean canReadToken( final String sym ) {
		return this.tokens.canReadToken( sym );
	}

	public void mustReadToken( final String sym ) {
		this.tokens.mustReadToken( sym );
	}
	
	public Token readToken() {
		return this.tokens.readToken();
	}

	public Token peekToken() {
		return this.tokens.peekToken();
	}
	
	public boolean canPeekToken( final String sym ) {
		return this.tokens.canPeekToken( sym );
	}
	
	private boolean isXmlChar( final char ch ) {
		return(
			Character.isLetter( ch ) ||
			Character.isDigit( ch ) ||
			ch == '-' ||
			ch == '_' ||
			ch == ':'
		);
	}
	
	private boolean isXmlName( final String s ) {
		for ( int i = 0; i < s.length(); i++ ) {
			if ( !isXmlChar( s.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}
	
	private boolean shouldGlue( final String a, final String b ) {
		return a.length() >= 1 && Character.isLetter( a.charAt( 0 ) ) && isXmlName( a ) && isXmlName( b );
	}
	
	public Token peekXmlName() {
		Token tok = this.tokens.readToken();
		while ( tok instanceof NameToken ) {
			final Token next = this.tokens.peekToken();
			if ( next.hadWhiteSpaceStart() ) break;
			final String a = tok.getWord();
			final String b = next.getWord();
			if ( !this.shouldGlue( a, b ) ) break;
			//System.out.println( "glueing " + a + " to " + b );
			tok = new NameToken( tok.hadWhiteSpaceStart(), a + b );
			this.tokens.dropToken();
		} 
		this.tokens.pushToken( tok );
		return tok;
	}
	
	public void dropToken() {
		this.tokens.dropToken();
	}
	
	public void clear() {
		this.tokens.clear();
	}

    public NameExpr readNameExpr() {
		final Token token = this.tokens.readToken();
		final NameExpr expr = this.token_parser.makePlainNameExpr( token );
		if ( expr != null ) return expr;
        this.alert( "Identifier needed" ).culprit( "found", token ).mishap();
		return null;	//	sop
    }

	public NameToken readNameToken( final boolean nullAllowed, final boolean whiteSpaceAllowed ) {
		final Token t = this.tokens.readToken();
		if ( t instanceof NameToken ) {
			if ( !whiteSpaceAllowed && t.hadWhiteSpaceStart() ) {
				this.alert( "unexpected white space" ).culprit( "token", t ).mishap();
			}
			return (NameToken)t;
		} else if ( !nullAllowed ) {
			this.alert( "Name needed" ).culprit( "token", t ).mishap();
		}
		return null;
	}

	public NameToken readPackageName() {
		final StringBuffer buff = new StringBuffer();
		final NameToken t = this.readNameToken( false, true );
//		System.out.println( "token t = " + t );
		buff.append( t.getWord() );
		while ( this.canReadToken( "." ) ) {
			buff.append( '.' );
			buff.append( this.readNameToken( false, false ).getWord() );
		}
		return new NameToken( t.hadWhiteSpaceStart(), buff.toString() );
	}

	public Expr readAtomicExpr( final boolean quote_symbol ) {
		if ( this.peekToken().hasName( "(" ) ) {
			this.tokens.dropToken();
			return this.readExprTo( ")" );
		} else {
			final Token token = this.tokens.readToken();
			return this.token_parser.makeAtomicExpr( token, quote_symbol );
		}
	}
	
	public Expr readPrimary() {
		final Expr ans = this.readOptPrimary();
		if ( ans == null ) {
			this.alert( "Unexpected end of input" ).mishap();
		}
		return ans;
	}

    private Expr readOptPrimary() {
        final Token token = this.tokens.readToken();
		if ( token == null ) return null;
		final Expr expr = token.parse( this, Prec.zero, null, this.token_parser );
		if ( expr != null ) {
			return expr;
		}
		this.tokens.pushToken( token );
		return null;
   }
	
   public Expr readOptExprPrec( final int prec ) {
        Expr sofar = readOptPrimary();
        if ( sofar == null ) return null;

		for (;;) {
			final Token token = this.tokens.readToken();
			if ( token == null ) return sofar;
			//System.out.println( "phase 2, token = " + token );
			final Expr e = token.parse( this, prec, sofar, this.token_parser );
			if ( e == null ) {
				this.tokens.pushToken( token );
				return sofar;
			}
			sofar = e;
		}
    }

    public Expr readExprPrec( final int prec ) {
        final Expr e = readOptExprPrec( prec );
        if ( e == null ) {
			final Token t = this.peekToken();
            this.alert(
                "Missing expression",
                "An expression cannot follow this token"
            ).culprit( "token", t ).mishap();
        }
        return e;
    }

    public Expr readOptExpr() {
        return readOptExprPrec( Prec.zero );
    }

    public Expr readExpr() {
        return readExprPrec( Prec.zero );
    }

	public Expr readDefineHead() {
		final char phase_sv = this.phase;
		this.phase = 'H';
		try {
			return this.readExpr();
		} finally {
			this.phase = phase_sv;
		}
	}

    public Expr readExprTo( final String sym ) {
        final Expr e = this.readExpr();
        this.tokens.mustReadToken( sym );
        return e;
    }

    public Expr readOptExprTo( final String sym ) {
        final Expr e = this.readOptExpr();
        this.tokens.mustReadToken( sym );
        return e;
    }

    //    Read expressions upto but NOT including commas.
    public Expr readExprUpToComma() {
        return this.readExprPrec( this.comma_prec );
    }

    public Expr readStmnts() {
        Expr x = SkipExpr.make();
        do {
            final Expr y = this.readOptExprPrec( Prec.zero );
            if ( y == null ) {
                return x;
            } else {
                x = CommaExpr.make( x, y );
            }
		} while ( this.tokens.tryReadToken( ";" ) != null );
		return x;
    }
	

    public Expr readStmntsTo( final String sym ) {
        final Expr e = readStmnts();
        this.tokens.mustReadToken( sym );
        return e;
    }

	public JSpiceConf getJSpiceConf() {
		return this.interpreter.getJSpiceConf();
	}

}

