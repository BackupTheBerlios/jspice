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

import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.tokens.QuotedToken;
import org.openspice.jspice.tokens.NumberToken;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Pragma;
import org.openspice.jspice.main.Interpreter;
import org.openspice.jspice.namespace.NameSpace;

import java.io.*;

class TokenizerImpl extends ParseEscape implements Tokenizer {
    private final Source source;
    private final StringBuffer buff = new StringBuffer();
	private final Interpreter interpreter;

    TokenizerImpl( final Interpreter interpreter, final String _printName, final Reader _reader, final String _prompt ) {
		super( interpreter.getJSpiceConf() );
		this.interpreter = interpreter;
        this.source = new Source( _printName, _reader, _prompt );
    }

	public NameSpace getCurrentNameSpace() {
		return this.interpreter.getCurrentNameSpace();
	}

	public void clear() {
		this.buff.setLength( 0 );
	}
	
	public char readChar( final char default_char ) {
		return this.source.readChar( default_char );
	}

	public char readCharNoEOF() {
		return this.source.readCharNoEOF();
	}

	private int readInt() {
		return this.source.readInt();
	}
	
	private void pushInt( final int ich ) {
		this.source.pushInt( ich );
	}

    public String getPrintName() {
        return this.source.getPrintName();
    }

    public int getLineNumber() {
        //	  Adjust for 0-indexing.
        return this.source.getLineNumber() + 1;
    }

	public final Alert alert( final String msg1, final String msg2 ) {
		final Alert a = new Alert( msg1, msg2, 'T' );
        a.culprit( "file", this.getPrintName() );
        a.culprit( "line no.", new Integer( this.getLineNumber() ) );
		return a;
	}

    private void addChar( final int ich ) {
        this.buff.append( (char)ich );
    }

	private char okChar( final int ich, final char default_char ) {
		this.addChar( ich );
		return this.readChar( default_char );
	}

	private char peekChar( final char default_char ) {
		final int ch = this.source.readInt();
		this.source.pushInt( ch );
		return ch == -1 ? default_char : (char)ch;
	}

	private char okCharNoEOF( final int ich ) {
		this.addChar( ich );
		return this.readCharNoEOF();
	}

    private static boolean isSign( final char ch ) {
        return "<>!$%^&*-+=|:?/~".indexOf( ch ) >= 0;
    }

    //	  Support function for cantStick
    private char lastCharInBuff() {
        return this.buff.charAt( this.buff.length() - 1 );
    }

    //	  Required to support the tokenization bodge for REXML i.e.
    //	  to unstick sequences such as "></" into ">" and "</"
    private boolean cantStick( final char ch ) {
		return(
			ch == '<' && this.lastCharInBuff() == '>' ||
			ch == '>' && this.lastCharInBuff() == '<'
		);
    }

	private void readRepetitions( final int orig ) {
		int ich = orig;
		do {
			ich = this.okChar( ich, ' ' );
		} while ( orig == ich );
		this.source.pushInt( ich );
	}

	private void parseString( final char end_char ) {
		char ch = this.readCharNoEOF();   //okChar( endChar );
		while ( ch != end_char ) {
			if ( ch == '\r' || ch == '\n' ) {
				new Alert( "Unterminated string",
					"A newline or carriage return was encountered before the closing quotes" ).
					culprit( "partial string", getErrorString() ).
					mishap( 'T' );
			} else if ( ch == '\\' ) {
				ch = this.okCharNoEOF( this.parseEscape() );
			} else {
				ch = this.okCharNoEOF( ch );
			}
		}
	}

	private String makeString() {
		return this.buff.toString();
	}

	private Token makeNameToken( final boolean hadWhite ) {
		return new NameToken( hadWhite, this.makeString() );
	}

	private Token readStringToken( final boolean hadWhite, final char ch ) {
		this.parseString( ch );
		final String s = this.makeString();
		return new QuotedToken( hadWhite, s, ch );
	}

	private Token makeIntToken( final boolean hadWhite ) {
		try {
			return new NumberToken( hadWhite, this.makeString() );
		} catch ( NumberFormatException ex ) {
			this.alert( "Cannot internalize this number" ).culprit( "number", this.makeString() ).mishap();
		}
		return null;
	}

	public Token readToken() {
		return this.readToken( false );
	}

	/**
	 * Nested comments.  You enter here having read # and the start_char.
	 * You return after having read the corresponding stop-char.
	 */
	private void readNestedCommentTo( final char start_char, final char stop_char ) {
		char ch = this.readCharNoEOF();
		char nch;
		int level = 1;
		for (;;) {
			if ( ch == '#' ) {
				nch = this.readCharNoEOF();
				if ( nch == stop_char ) {
					level -= 1;
					if ( level == 0 ) return;
					ch = this.readCharNoEOF();
				} else if ( nch == start_char ) {
					level += 1;
					ch = this.readCharNoEOF();
				} else {
					ch = nch;
				}
			} else {
				ch = this.readCharNoEOF();
			}
		}
	}

	private String readToEndOfLine( int ch ) {
		final StringBuffer b = new StringBuffer();
		while ( ch != '\n' && ch != -1 ) {
			b.append( (char)ch );
			ch = this.readCharNoEOF();
		}
		return b.toString();
	}

	private void readPragma( final String string ) {
		new Pragma( this.interpreter, string ).perform();
	}

	private void readComment() {
		int ch;
		//	  Dispose of a sequence of comments.
		do {
			ch = this.readCharNoEOF();
			switch ( ch ) {
				case ' ':
				case '\t':
				case '#':
				case '\r':
				case '\n':
					while ( ch != '\n' ) {
						ch = this.readCharNoEOF();
					}
					break;
				case '(':
					this.readNestedCommentTo( '(', ')' );
					break;
				case '[':
					this.readNestedCommentTo( '[', ']' );
					break;
				case '{':
					this.readNestedCommentTo( '{', '}' );
					break;
				case '<':
					this.readNestedCommentTo( '<', '>' );
					break;
				default:
					this.readPragma( this.readToEndOfLine( ch ) );
					break;
			}

			//	Eat any white space before checking whether to continue.
			do {
				ch = this.readInt();
			} while ( ch != -1 && Character.isWhitespace( (char)ch ) );
		} while ( ch == '#' );
		this.pushInt( ch );
	}

	private Token readToken( boolean hadWhiteAtStart ) {
		this.buff.setLength( 0 );
		int ch = this.readInt();
		while ( Character.isWhitespace( (char)ch ) ) {
			hadWhiteAtStart = true;
			ch = this.readInt();
		}
		if ( ch == -1 ) {
			return null;						//	 end of file.
		} else if ( ch == '#' ) {
			hadWhiteAtStart = true;
			this.readComment();
			//	Recurse.
			return this.readToken( hadWhiteAtStart );
		} else if ( Character.isLetter( (char)ch ) ) {
			ch = this.okChar( ch, ' ' );
			while ( Character.isLetterOrDigit( (char)ch ) || ch == '_' ) {
				ch = this.okChar( ch, ' ' );
			}
			this.source.pushInt( ch );
			return this.makeNameToken( hadWhiteAtStart );
		} else if ( Character.isDigit( (char)ch ) || ch == '-' && Character.isDigit( this.peekChar( ' ' ) ) ) {
			ch = this.okChar( ch, ' ' );
			while ( Character.isDigit( (char)ch ) ) {
				ch = this.okChar( ch, ' ' );
			}
			if ( ch == '.' && Character.isDigit( this.peekChar( ' ' ) ) ) {
				ch = this.okChar( ch, ' ' );
				while ( Character.isDigit( (char)ch ) ) {
					ch = this.okChar( ch, ' ' );
				}
				this.pushInt( ch );
			} else {
				this.pushInt( ch );
			}
			if ( this.buff.length() == 1 && this.buff.charAt( 0 ) == '-' ) {
				return this.makeNameToken( hadWhiteAtStart );
			} else {
				return this.makeIntToken( hadWhiteAtStart );
			}
		} else if ( ch == '"' || ch == '\'' || ch == '`' || ch == '/' && this.source.tryRead( "/" ) ) {
			return this.readStringToken( hadWhiteAtStart, (char)ch );
		} else if ( ch == '.' || ch == '@' || ch == '?' ) {
			this.readRepetitions( ch );
			return this.makeNameToken( hadWhiteAtStart );
		} else if ( isSign( (char)ch ) ) {
			ch = this.okChar( ch, ' ' );
			while ( isSign( (char)ch ) && !( this.cantStick( (char)ch ) ) ) {
				ch = this.okChar( ch, ' ' );
			}
			this.source.pushInt( ch );
			return this.makeNameToken( hadWhiteAtStart );
		} else {
			this.addChar( ch );
			return this.makeNameToken( hadWhiteAtStart );
		}
	}

    public String getString() {
        return this.buff.toString();
    }

    public String getErrorString() {
        return this.buff.toString();
    }

    public String getTagName() {
        int size = this.buff.length();
        for ( int i = 0; i < size; i++ ) {
            char ch = this.buff.charAt( i );
            if ( ! (
                Character.isLetterOrDigit( ch ) ||
                "_:-".indexOf( ch ) >= 0
            ) ) {
                this.alert( "Invalid tag name" ).culprit( "name", this.makeString() ).mishap();
            }
        }
        return this.makeString();
    }

}




