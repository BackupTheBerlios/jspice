package org.openspice.jspice.expr;

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

public abstract class ExprTransform {
	public abstract Expr transform( Expr _ );
	
	public ExprTransform compose( final ExprTransform u ) {
		return(
			new ExprTransform() {
				final ExprTransform t = this;
				public Expr transform( Expr x ) {
					return u.transform( t.transform( x ) );
				}
			}
		);
	}
	
	public Expr map( final Expr e ) {
		return e.copy( e.getAllKids().map( this ) );
	}
	
	public ExprTransform bottomUp() {
		final ExprTransform that = this;
		return( 
			new ExprTransform() {
				public Expr transform( final Expr e ) {
					return that.transform( this.map( e ) );
				}
			}
		);
	}
	
	static class BottomUp extends ExprTransform {
		final ExprTransform transform;
		
		BottomUp( final ExprTransform _transform ) {
			this.transform = _transform;
		}
		
		public Expr transform( final Expr e ) {
			return this.transform.transform( this.map( e ) );
		}
	}
}

