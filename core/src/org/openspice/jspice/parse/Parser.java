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
import org.openspice.jspice.tokens.Token;
import org.openspice.jspice.tokens.NameToken;
import org.openspice.jspice.conf.JSpiceConf;

public abstract class Parser {

	abstract public NameExpr readNameExpr();
	abstract public Expr readPrimary();
	abstract public Expr readAtomicExpr( final boolean quote_word );
	abstract public Expr readExprPrec( final int prec );
	abstract public Expr readOptExprPrec( final int prec );
	abstract public Expr readExpr();
	abstract public Expr readOptExpr();
	abstract public Expr readExprUpToComma();
	abstract public Expr readExprTo( final String sym );
	abstract public Expr readOptExprTo( final String sym );
	abstract public Expr readStmnts();
	abstract public Expr readStmntsTo( final String sym );

	//	Token-oriented reading.
	abstract public Token tryReadToken( final String sym );
	abstract public boolean canReadToken( final String sym );
	abstract public void mustReadToken( final String sym );
	abstract public Token peekToken();
	abstract public boolean canPeekToken( final String sym );
	abstract public void dropToken();
	abstract public Token readToken();
	abstract public Token peekXmlName();
//	abstract public List readNameTokenList();
	abstract public NameToken readNameToken( final boolean nullAllowed, final boolean whiteSpaceAllowed );
	abstract public NameToken readPackageName();


	//abstract public int newTmp();
	abstract public NameExpr newTmpNameExpr();
	abstract public void saveTmp();
	abstract public void restoreTmp();
	abstract public void clearTmp();

	abstract public void setHolesUsed();
	abstract public void clearHolesUsed();
	abstract public boolean getHolesUsed();

	//	JSpiceConf
	abstract public JSpiceConf getJSpiceConf();

}
