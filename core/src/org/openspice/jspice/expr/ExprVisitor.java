package org.openspice.jspice.expr;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.expr.cases.*;

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

public abstract class ExprVisitor { // extends ExprTransform {
	abstract public Object visitExpr( final Expr expr, final Object arg );


    public abstract static class DefaultReturn extends ExprVisitor {
        public Object visitExpr( final Expr e, final Object arg ) { return e; }
    }
	
	public abstract static class DefaultUnimplemented extends ExprVisitor {
		public Object visitExpr( final Expr e, final Object arg ) {
			org.openspice.jspice.alert.Alert.unimplemented();
			return (Expr)null;	//	sop.
		}
	}
	
	public abstract static class DefaultUnreachable extends ExprVisitor {
		public Object visitExpr( final Expr e, final Object arg ) {
			org.openspice.jspice.alert.Alert.unreachable();
			return (Expr)null;	//	sop.
		}
	}
	
	ExprTransform transformer( final Object arg ) {
		final ExprVisitor that = this;
		return(
			new ExprTransform() {
				public Expr transform( final Expr e ) {
					return (Expr)e.visit( that, arg );
				}
			}
		);
	}
	
	/*Expr transform( final Expr e ) {
		return (Expr)e.visit( this, null );
	}*/
	
	/*Expr transform( final Expr e, final Object arg ) {
		return (Expr)e.visit( this, arg );
	}*/
	
	public Object visit( final Expr e ) {
		return e.visit( this, null );
	}

	public Object visit( final Expr e, final Object arg ) {
		return e.visit( this, arg );
	}

	public Object visitTotalAssignExpr( final TotalAssignExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitApplyExpr( final ApplyExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	/*public Object visitApplyUpdaterExpr( final Expr.ApplyUpdaterExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}*/

	public Object visitBlockExpr( final BlockExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitCheckCountExpr( final CheckCountExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitCheckOneExpr( final CheckOneExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitCheckBooleanExpr( final CheckBooleanExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitCommaExpr( final CommaExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitCompoundExpr( final CompoundExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitConstantExpr( final ConstantExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
	
	/*public Object visitExplodeExpr( final Expr.ExplodeExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}*/
	
	public Object visitHelloExpr( final HelloExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitHoleExpr( final HoleExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitIf3Expr( final If3Expr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitImportExpr( final ImportExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitInitExpr( final InitExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
	
	/*public Object visitIntroExpr( final Expr.IntroExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}*/
	
	public Object visitLambdaExpr( final LambdaExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitAnonExpr( final AnonExpr expr, final Object arg ) {
		return this.visitNameExpr( expr, arg );
	}

	public Object visitNamedExpr( final NamedExpr expr, final Object arg ) {
		return this.visitNameExpr( expr, arg );
	}
	
	public Object visitNameExpr( final NameExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );		
	}

	public Object visitRelOpChainExpr( final RelOpChainExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
	
	public Object visitRepeatExpr( final RepeatExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
	
	public Object visitShortCircuitExpr( final ShortCircuitExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitSkipExpr( final SkipExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}

	public Object visitXmlAttrExpr( final XmlAttrExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
	
	public Object visitXmlElementExpr( final XmlElementExpr expr, final Object arg ) {
		return this.visitExpr( expr, arg );
	}
}
