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

import org.openspice.jspice.parse.spice.*;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.cases.NameExpr;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.parse.miniparser.*;

import java.util.*;

public final class SpiceMiniParserTable extends Standard {

	private void install( final String keys, final MiniParser mini_parser ) {
		for ( final StringTokenizer stok = new StringTokenizer( keys ); stok.hasMoreTokens(); ) {
			this.put( stok.nextToken(), mini_parser );
		}
	}

	private void install( final String[] keys_array, final MiniParser mini_parser ) {
		for ( int i = 0; i < keys_array.length; i++ ) {
			install( keys_array[ i ], mini_parser );
		}
	}

	public SpiceMiniParserTable() {

		super(
			//	The default miniparser for names.
			new Prefix() {
				public Expr prefix( final String interned, final Parser parser ) {
					if ( parser.tryReadToken( "::" ) != null ) {
						final Token tok = parser.readToken();
						if ( tok instanceof NameToken ) {
							return NameExpr.make( ((NameToken)tok).getInterned() ).modNickname( interned );
						} else {
							new Alert( "Unexpected constant after ::" ).culprit( "constant", tok ).mishap( 'P' );
							return null; 	//	sop
						}
					} else {
						return NameExpr.make( interned );
					}
				}
			}
		);

		install( "?", new HoleMiniParser() );
		install( "{", new ListMiniParser() );
		install( "<!--", new XMLCommentMiniParser() );
		install( "block", new BlockMiniParser() );
		install( "define", new DefineMiniParser() );
		install( "for", new ForMiniParser() );
		install( "fun", new FunMiniParser() );
		install( "if", new ConditionalMiniParser( "endif" ) );
		install( "import", new ImportMiniParser() );
		install( "new", new NewMiniParser() );
		install( "package", new PackageMiniParser() );
		install( "unless", new ConditionalMiniParser( "endunless" ) );
		install( "switch", new SwitchMiniParser() );
		install( "val", new InitMiniParser( true ) );
		install( "var", new InitMiniParser( false ) );
		install( "$", new DollarMiniParser() );

		install( ":=", new LeftTotalAssignMiniParser().modPrec( Prec.assign ) );
		install( "||", new ShortCircuitMiniParser( false, false ).modPrec( Prec.or_absent ) );
		install( "&&", new ShortCircuitMiniParser( true, false ).modPrec( Prec.and_absent ) );
		install( "or", new ShortCircuitMiniParser( false, true ).modPrec( Prec.or ) );
		install( "and", new ShortCircuitMiniParser( true, true ).modPrec( Prec.and ) );

		install( ",", new CommaMiniParser().modPrec( Prec.comma ) );
		install( "...", new ExplodeMiniParser().modPrec( Prec.explode ) );
		install( "++", new AppendMiniParser().modPrec( Prec.append ) );

		install( "<= = /= == /== > >=", RelOpMiniParser.PROTOTYPE );
		install( "<", new XMLElementMiniParser().modPrec( Prec.lt ) );
		install( "@", new DotMiniParser().modPrec( Prec.at ) );

		install( "+ - +: -:", new ArithMiniParser().modPrec( Prec.arith1 ) );
		install( "* / mod div", new ArithMiniParser().modPrec( Prec.arith2 ) );
		install( "**", new ArithMiniParser().modPrec( Prec.arith3 ) );

		install( "==>", new MapletMiniParser().modPrec( Prec.maplet ) );
		install( ".", new DotMiniParser().modPrec( Prec.dot ) );
		install( "[", new IndexMiniParser().modPrec( Prec.bracket ) );
		install( "(", new ParenMiniParser().modPrec( Prec.paren ) );

		install( "<<", new QuasiQuoteMiniParser() );

		install(
			"by !! downto endclass endmethod into facet as default where",
			new ReservedMiniParser()
		);

		install( "!=", new ReservedMiniParser( "/= or /==" ) );
		install( "!==", new ReservedMiniParser( "/= or /==" ) );
		install( "!", new ReservedMiniParser( "not" ) );

		install(
			new String[] {
				"=| |=",
				"class slot method",
				"do endfor from by to in while until suchthat finally",
				"=> as",
				".. /> </ </> ) ] % } : case endswitch",
				"then else elseif elseunless endif endunless",
				"enddefine endfun endfor",
				"; ::",
				"endblock",
				">>"
			},
			new NonfixMiniParser()
		);
	}

}