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

package org.openspice.jspice.expr.cases;

import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.NameExprVisitor;
import org.openspice.jspice.expr.TmpName;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.tools.IteratorTools;

import java.util.Iterator;

public final class NamedExpr extends NameExpr {
	private final Object symbol;
	private String nickname = null;
	private Var var = null;

	public NamedExpr( final String _symbol ) {
		super();
		this.symbol = _symbol.intern();
	}

	public NamedExpr( final Integer _symbol ) {
		this.symbol = _symbol;
	}

	public NamedExpr( final TmpName _symbol ) {
		this.symbol = _symbol;
	}

	public boolean isTmp() {
		return !( this.symbol instanceof String );
	}

	public boolean isNamedExpr() {
		return true;
	}

	public Object getSymbol() {
		return this.symbol;
	}

	public String getTitle() {
		return this.symbol.toString();
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname( final String s ) {
		this.modNickname( s );
	}

	public NameExpr modNickname( final String s ) {
		this.nickname = s.intern();
		return this;
	}

	public Iterator getAllData() {
		return IteratorTools.make1( this.symbol );
	}

	public Object visit( final ExprVisitor v, final Object arg ) {
		return v.visitNamedExpr( this, arg );
	}

	public Object visit( final NameExprVisitor v, final Object arg ) {
		return v.visitNamedExpr( this, arg );
	}

	public Expr copy( final ExprIterator kids ) {
		return this;
	}

	public boolean isGlobal() {
		return this.var instanceof Var.Perm;
	}

	public boolean isType1() {
		return this.var.isType1();
	}

	public boolean isType3() {
		return this.var.isType3();
	}

	public int getOffset() {
		return ((Var.Local)this.var).getOffset();
	}

	public Var.Perm getPerm() {
		assert this.var != null;
		return (Var.Perm)this.var;
	}

	public void setVar( final Var p ) {
		this.var = p;
	}

	public Var getVar() {
		assert this.var != null;
		return this.var;
	}

	public boolean isConstant() {
		return this.var.getProps().isConstant();
	}
}
