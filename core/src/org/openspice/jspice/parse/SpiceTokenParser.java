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

import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.tokens.QuotedToken;
import org.openspice.jspice.tokens.NumberToken;
import org.openspice.jspice.parse.miniparser.Table;
import org.openspice.jspice.parse.miniparser.MiniParser;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.arithmetic.GreaterThanOrEqual;
import org.openspice.jspice.arithmetic.Negate;
import org.openspice.jspice.datatypes.Symbol;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.built_in.ShortCuts;
import org.openspice.jspice.built_in.symbols.NewSymbolProc;
import org.openspice.jspice.built_in.strings.FormatProc;
import org.openspice.jspice.built_in.lists.NewImmutableListProc;
import org.openspice.jspice.built_in.lists.InvListProc;
import org.openspice.jspice.main.Interpreter;
import org.openspice.jspice.lib.CastLib;

import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.StringReader;

public final class SpiceTokenParser extends TokenParser {

	final static Table mini_parser_table = new SpiceMiniParserTable();

	final Interpreter interpreter;

	public SpiceTokenParser( Interpreter interpreter ) {
		this.interpreter = interpreter;
	}

	public static final void findCompletions( final String prefix, final List candidates ) {
		mini_parser_table.findCompletions( prefix, candidates );
	}

	public int getPrec( final String s ) {
		return mini_parser_table.lookup( s ).getPrec();
	}

	public Expr atomicNumber( final NumberToken numberToken ) {
		return ConstantExpr.make( numberToken.getNumber() );
	}

	public Expr parseNumber( final NumberToken numberToken, final int prec, final Expr lhs, final Parser parser ) {
		if ( lhs == null ) {
			return this.atomicNumber( numberToken );
		} else {
			final Number num = numberToken.getNumber();
			if ( GreaterThanOrEqual.gtez( num ) ) return null;
			//	EXPR -1 --> EXPR - 1
			return (
				ApplyExpr.make1(
					ShortCuts.lookupProc( "-" ),
					lhs,
					//	Dodgey - implicitly crossing from java.lang.Number to jspice.lang.Number
					ConstantExpr.make( Negate.doNegate( num ) )
				)
			);
		}
	}

	private String insertPercentPee( final String w, final Map interpolation_map ) {
		final StringBuffer b = new StringBuffer();
		for ( int i = 0; i < w.length(); i++ ) {
			final CharSequence cs = (CharSequence)interpolation_map.get( new Integer( i ) );
			if ( cs != null ) {
				b.append( "%p" );
			}
			if ( i >= 0 ) {
				final char ch = w.charAt( i );
				if ( ch == '%' ) {
					b.append( "%%" );
				} else {
					b.append( ch );
				}
			}
		}
		if ( interpolation_map.get( new Integer( w.length() ) ) != null ) {
			b.append( "%p" );
		}
		return b.toString();
	}

	private Expr parseCharSequence( final CharSequence cs ) {
		final String interp = cs.toString();
		final SpiceParser p = new SpiceParser( this.interpreter, "interpolated string", new StringReader( interp ), null );
		final Expr e = p.readStmnts();
		if ( p.peekToken() != null ) {
			new Alert( "Invalid interpolation expression" ).culprit( "interpolated", interp ).mishap();
		}
		return e;
	}

	public Expr atomicQuoted( final QuotedToken quotedToken ) {
		final String w = quotedToken.getWord();
		final String flav = quotedToken.kind();
		final Map opt_interpolation_map = quotedToken.getOptInterpolationMap();
		if ( opt_interpolation_map != null ) {
			final Map interpolation_map = opt_interpolation_map;
			final String wpp = this.insertPercentPee( w, interpolation_map );
			Expr sofar = SkipExpr.make();
			for ( Iterator it = interpolation_map.values().iterator(); it.hasNext(); ) {
				final CharSequence cs = (CharSequence)it.next();
				final Expr e = this.parseCharSequence( cs );
				sofar = (
					CommaExpr.make(
						sofar,
						ApplyExpr.make( NewImmutableListProc.NEW_IMMUTABLE_LIST_PROC, e )
					)
				);
			}
			final Expr string_e = (
				ApplyExpr.make(
					FormatProc.FORMAT_PROC,
					CommaExpr.make( ConstantExpr.make( wpp ), sofar )
				)
			);
			if ( flav == "string" ) {
				return string_e;
			} else if ( flav == "symbol" ) {
				return ApplyExpr.make( NewSymbolProc.NEW_SYMBOL_PROC, string_e );
			} else if ( flav == "chars" ) {
				return ApplyExpr.make( InvListProc.INV_LIST_PROC, string_e );
			} else if ( flav == "regexp" ) {
				//	Not at all sure about this.
				return (
					ApplyExpr.make(
						new Unary1InvokeProc() {
							public Object invoke( Object x ) {
								return Pattern.compile( CastLib.toString( x ) );
							}
						},
						string_e
					)
				);
			}
		} else {
			if ( flav == "string" ) {
				return ConstantExpr.make( w );
			} else if ( flav == "symbol" ) {
				return ConstantExpr.make( Symbol.make( w ) );
			} else if ( flav == "chars" ) {
				Expr sofar = SkipExpr.make();
				for ( int i = 0; i < w.length(); i++ ) {
					final char c = w.charAt( i );
					sofar = (
						CommaExpr.make(
							sofar,
							ConstantExpr.make( new Character( c ) )
						)
					);
				}
				return sofar;
			} else if ( flav == "regexp" ) {
				return ConstantExpr.make( Pattern.compile( w ) );
			}
		}
		throw Alert.unreachable( flav );
	}

	public Expr parseQuoted( final QuotedToken quotedToken, final int prec, final Expr lhs, final Parser parser ) {
		return lhs == null ? this.atomicQuoted( quotedToken ) : null;
	}

	public Expr parseName( final NameToken nameToken, final int prec, final Expr lhs, final Parser parser ) {
		final String intn = nameToken.getInterned();
		assert intn != null;
		final MiniParser mini_parser = mini_parser_table.lookup( intn );
		assert mini_parser != null;

		final int q = mini_parser.getPrec();
		if ( q <= prec ) return null;
		return mini_parser.parse( intn, q, lhs, parser );
	}

	public Expr atomicName( final NameToken nameToken ) {
		return ConstantExpr.make( Symbol.make( nameToken.getInterned() ) );
	}

	public NameExpr plainName( final NameToken nameToken ) {
		final String intn = nameToken.getInterned();
		final MiniParser mini_parser = mini_parser_table.get( intn );
		if ( mini_parser == null ) {
			return NameExpr.make( intn );
		} else {
			return null;
		}
	}
}
