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

package org.openspice.jspice.lexis;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.conf.JSpiceConf;

import java.util.Stack;
import java.io.Reader;

public class PushableTokenizerImpl implements PushableTokenizer {

	private final TokenizerImpl tokenizer;
	private final Stack stack;

	PushableTokenizerImpl( final TokenizerImpl _tokenizer ) {
		this.tokenizer = _tokenizer;
		this.stack = new Stack();
	}

    public PushableTokenizerImpl( final JSpiceConf jconf, final String _printName, final Reader _reader, final String _prompt ) {
        this( new TokenizerImpl( jconf, _printName, _reader, _prompt ) );
    }

	public String getPrintName() {
		return this.tokenizer.getPrintName();
	}

	public Token readToken() {
		//System.out.println( "> readToken // stack length = " + this.stack.size() );
		final Token answer = this.stack.empty() ? this.tokenizer.readToken() : (Token)this.stack.pop();
		//System.out.println( "< readToken " + answer + " // stack length = " + this.stack.size() );
		return answer;
	}

	public NameToken readNameToken() {
		final Token t = this.readToken();
		if ( t.isNameToken() ) {
			return (NameToken)t;
		} else {
			new Alert( "A name is required here" ).culprit( "token", t ).mishap( 'T' );
			return null;
		}
	}

	public int getLineNumber() {
		return this.tokenizer.getLineNumber();
	}

	public void pushToken( final Token t ) {
		//System.out.println( "Token being pushed " + t );
		this.stack.push( t );
	}

	public Token tryReadToken( final String sym ) {
		final Token t = this.readToken();
		if ( t != null && t.hasName( sym ) ) {
			return t;
		} else {
			//System.out.println( "Push by tryReadToken " + t );
			this.pushToken( t );
			return null;
		}
	}

	public boolean canReadToken( final String sym ) {
		return this.tryReadToken( sym ) != null;
	}



	public Token mustReadToken( final String sym ) {
		final Token t = this.tryReadToken( sym );
		if ( t == null ) {
			this.tokenizer.alert( "Unexpected symbol" ).
			culprit( "found", this.peekToken() ).
			culprit( "wanted", sym ).
			mishap();
		}
		return t;
	}

	public Token peekToken() {
		if ( this.stack.empty() ) {
			final Token t = this.tokenizer.readToken();
			//System.out.println( "Push by peekToken " + t );
			this.pushToken( t );
			return t;
		} else {
			return (Token)this.stack.peek();
		}
	}

	public boolean canPeekToken( final String sym ) {
		final Token t = this.peekToken();
		return t != null && t.hasName( sym );
	}

	public void dropToken() {
		this.readToken();
	}

	public void clear() {
		this.tokenizer.clear();
		this.stack.clear();
	}
}
