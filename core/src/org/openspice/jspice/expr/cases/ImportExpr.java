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

import org.openspice.jspice.datatypes.Arity;
import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.expr.ExprVisitor;
import org.openspice.jspice.expr.markers.ZeroResults;
import org.openspice.jspice.expr.iterators.ExprIterator;

public class ImportExpr extends NullaryExpr implements ZeroResults {

	final String pkg_name;
	final String nickname;
	final boolean qualified;

	private ImportExpr( final String _pkg_name, final String _nickname, final boolean _qualified ) {
		this.qualified = _qualified;
		this.pkg_name = _pkg_name;
		this.nickname = _nickname;
	}

	public static Expr make( final String _pkg_name, final String _nickname, final boolean _qualified ) {
		return new ImportExpr( _pkg_name, _nickname, _qualified );
	}

	public Arity arity() {
		return this.defaultArity();
	}

	//	todo: is this right?
	public Expr copy( final ExprIterator kids ) {
		return this;
	}

	public Object visit( final ExprVisitor visitor, final Object arg ) {
		return visitor.visitImportExpr( this, arg );
	}

	//	---

	public String getPkgName() {
		return this.pkg_name;
	}

	public String getNickname() {
		return this.nickname;
	}

	public boolean isQualified() {
		return this.qualified;
	}

}
