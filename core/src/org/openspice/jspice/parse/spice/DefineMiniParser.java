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
package org.openspice.jspice.parse.spice;

import org.openspice.jspice.parse.miniparser.Prefix;
import org.openspice.jspice.parse.Parser;
import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.namespace.Props;
import org.openspice.jspice.built_in.SpiceClassProcs;
import org.openspice.jspice.conf.JSpiceConf;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


public final class DefineMiniParser extends Prefix {

	//	define class NAME[;] (slot NAME [:TYPE] [= EXPR];) enddefine
	static final class DefineClass {
		final Parser parser;
		final String closing_keyword;

		public DefineClass( final Parser parser, final String closing_keyword ) {
			this.parser = parser;
			this.closing_keyword = closing_keyword;
		}

		private final JSpiceConf getJSpiceConf() {
			return this.parser.getJSpiceConf();
		}

		private static List slotNames( final List slot_name_exprs ) {
			final List list = new ArrayList();
			for ( Iterator it = slot_name_exprs.iterator(); it.hasNext(); ) {
				final NameExpr ne = (NameExpr)it.next();
				list.add( ne.getTitle() );
			}
			return list;
		}

		private Expr result( final NameExpr cname, final List slot_name_exprs, final List init_exprs ) {

			final Expr new_class = ApplyExpr.make( new SpiceClassProcs.NewClassProc( this.getJSpiceConf() ), ConstantExpr.make( slotNames( slot_name_exprs ) ) );
			final InitExpr class_intro = InitExpr.make( null, Props.VAL, cname, new_class );

			Expr sofar = class_intro;

			int n = 0;
			for ( Iterator it = slot_name_exprs.iterator(); it.hasNext(); ) {
				final NameExpr sname = (NameExpr)it.next();
				final Expr get_slot = ApplyExpr.make1( new SpiceClassProcs.SlotProc( this.getJSpiceConf() ), ConstantExpr.make( n++ ), cname );
				final InitExpr slot_intro = InitExpr.make( null, Props.VAL, sname, get_slot );

				sofar = CommaExpr.make( sofar, slot_intro );
			}

			return sofar;
		}

		public Expr parse() {
			final NameExpr cname = parser.readNameExpr();
			parser.tryReadToken( ";" );							//	dispose of optional semi.
			final List slot_names = new ArrayList();
			final List init_exprs = new ArrayList();
			while ( parser.tryReadToken( "slot") != null ) {
				final NameExpr sname = parser.readNameExpr();
				Expr init_expr = null;
				if ( parser.tryReadToken( "=" ) != null ) {
					init_expr = parser.readExprTo( ";" );
				}
				slot_names.add( sname );
				init_exprs.add( init_expr );
				parser.mustReadToken( ";" );
			}
			parser.mustReadToken( this.closing_keyword );
			return result( cname, slot_names, init_exprs );
		}

	}

	//	define function APPLY => STMNTS enddefine
	//	forward val F
	//	val F = fun APPLY => STMNTS endfun
	public Expr prefix( final String interned, final Parser parser ) {
		if ( parser.tryReadToken( "class" ) != null ) {
			return new DefineClass( parser, "enddefine" ).parse();
		} else {
			final Decurrier decurrier = Decurrier.parse( parser, "enddefine" );
			final ApplyExpr app = decurrier.getApp();
			final Expr body = decurrier.getBody();
			final NameExpr name = (NameExpr)app.getFun();
			final InitExpr fwd = InitExpr.make( null, Props.FWD_VAL, name, ConstantExpr.ABSENT_EXPR );
			final InitExpr intro = InitExpr.make( null, Props.VAL, name, LambdaExpr.make( app, body ) );
			return CommaExpr.make( fwd, intro );
		}
	}
	
}
