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
package org.openspice.jspice.namespace;

import org.openspice.tools.BooleanTools;
import org.openspice.jspice.lib.AbsentLib;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.datatypes.Termin;
import org.openspice.jspice.datatypes.proc.Unary1InvokeProc;
import org.openspice.jspice.built_in.*;
import org.openspice.jspice.built_in.strings.FormatProc;
import org.openspice.jspice.built_in.strings.IsStringProc;
import org.openspice.jspice.built_in.strings.NewStringProc;
import org.openspice.jspice.built_in.elements.ElementNameProc;
import org.openspice.jspice.built_in.elements.ElementAttributesProc;
import org.openspice.jspice.built_in.maps.NewMapProc;
import org.openspice.jspice.built_in.maps.IsEmptyProc;
import org.openspice.jspice.built_in.maps.IsMapProc;
import org.openspice.jspice.built_in.maps.IsMapFlavourProc;
import org.openspice.jspice.built_in.lists.*;
import org.openspice.jspice.built_in.inspect.InspectProc;
import org.openspice.jspice.built_in.arithmetic.*;
import org.openspice.graphics2d.ShowImage;
import org.openspice.vfs.VFolder;

import java.util.*;
import java.util.List;

public class SpiceStandardNameSpace extends NameSpace {

	public SpiceStandardNameSpace(
		final NameSpaceManager _manager,
		final Title title,
		final VFolder _directory,
		final FacetSet _default_facets,
		final List _ok_facet_list,
		final boolean _is_std_importer
	) {
		super( _manager, title, _directory, _default_facets, _ok_facet_list, _is_std_importer );
	}

	private void install( final String id, final Object value ) {
		this.install( id, value, Props.VAL );
	}

	private void install( final String id, final Object value, final Props props ) {
		final Var.Perm perm = (
			this.declarePerm(
				FacetSet.PUBLIC,
				id,
				props,
				false
			)
		);
		//System.out.println( "location = " + perm.getLocation() );
		perm.getLocation().setValue( value );
	}

	private void install_built_ins() {
		this.install( "abs", new AbsProc() );
		this.install( "absent", AbsentLib.ABSENT );
		this.install( "acos", new ACosProc() );
		this.install( "addFirst", AddFirstProc.ADD_FIRST_PROC );
		this.install( "addLast", AddLastProc.ADD_LAST_PROC );
		this.install( "allButFirst", AllButFirstProc.ALL_BUT_FIRST_PROC );
		this.install( "allButLast", AllButLastProc.ALL_BUT_LAST_PROC );
		this.install( "allMatches", RegexProcs.allMatches );
		this.install( "asin", new ASinProc() );
		this.install( "atan", new ATanProc() );
		this.install( "atan2", new ATan2Proc() );
		this.install( "attributeMap", ElementAttributesProc.ELEMENT_ATTRIBUTES_PROC );
		this.install( "ceil", new CeilProc() );
		this.install( "cons", ConsProc.CONS_PROC );
		this.install( "cos", new CosProc() );
		this.install( "elementAttributes", ElementAttributesProc.ELEMENT_ATTRIBUTES_PROC );
		this.install( "elementName", ElementNameProc.ELEMENT_NAME_PROC );
		this.install( "environment_variable", this.getJSpiceConf().getEnvMap() );
		this.install( "explode", ShortCuts.explodeProc );
		this.install( "false", BooleanTools.FALSE );
		this.install( "first", FirstProc.FIRST_PROC );
		this.install( "floor", new FloorProc() );
		this.install( "format", FormatProc.FORMAT_PROC );
		this.install( "fprint", PrintProcs.FPRINT_PROC );
		this.install( "fprintln", PrintProcs.FPRINTLN_PROC );
		this.install( "fprintTo", PrintProcs.FPRINT_TO_PROC );
		this.install( "fprintlnTo", PrintProcs.FPRINTLN_TO_PROC);
		this.install( "gensym", GensymProc.GENSYM_PROC );
		this.install( "hasMatch", RegexProcs.hasMatch );
		this.install( "head", FirstProc.FIRST_PROC );
		this.install( "inspect", InspectProc.INSPECT_PROC );
		this.install( "isEmpty", IsEmptyProc.IS_EMPTY_PROC );
		this.install( "isList", IsListProc.IS_LIST_PROC );
		this.install( "isListFlavour", IsListFlavourProc.IS_LIST_FLAVOUR_PROC );
		this.install( "isMap", IsMapProc.IS_MAP_PROC );
		this.install( "isMapFlavour", IsMapFlavourProc.IS_MAP_FLAVOUR_PROC );
		this.install( "isMatch", RegexProcs.isMatch );
		this.install( "isPrefixMatch", RegexProcs.isPrefixMatch );
		this.install( "isPartMatch", RegexProcs.isPartMatch );
		this.install( "isString", IsStringProc.IS_STRING_PROC );
		this.install( "justFirst", JustFirstProc.JUST_FIRST_PROC );
		this.install( "justLast", JustLastProc.JUST_LAST_PROC );		this.install( "last", LastProc.LAST_PROC );
		this.install( "length", ShortCuts.lengthProc );
		this.install( "loadValueFromVItem", new LoadValueFromFileProc( this.getSuperLoader() ) );
		this.install( "log", new LogProc() );
		this.install( "log2", new Log2Proc() );
		this.install( "log10", new Log10Proc() );
		this.install( "garbageCollect", GarbageCollectProc.GARBAGE_COLLECT_PROC );
		this.install( "none", NoneProc.noneProc );
		this.install( "not", NotProc.notProc );
		this.install( "lastResults", LastResultsProc.LAST_RESULTS_PROC );
		this.install( "low", new PowProc() );
		this.install( "openURL", OpenURLProc.OPEN_URL_PROC );
		this.install( "newList", NewListProc.NEW_LIST_PROC );
		this.install( "newMap", NewMapProc.NEW_MAP_PROC );
		this.install( "newString", NewStringProc.NEW_STRING_PROC );
		this.install( "newSymbol", NewSymbolProc.NEW_SYMBOL_PROC );
		this.install( "print", PrintProcs.printProc );
		this.install( "println", PrintProcs.printlnProc );
		this.install( "printTo", PrintProcs.printToProc );
		this.install( "printlnTo", PrintProcs.printlnToProc );
		this.install( "reverse", ReverseProc.REVERSE_PROC );
		this.install( "round", new RoundProc() );
		this.install( "show", ShowProcs.showProc );
		this.install( "showln", ShowProcs.showlnProc );
		this.install( "showTo", ShowProcs.showToProc );
		this.install( "showlnTo", ShowProcs.showlnToProc );
		this.install( "sin", new SinProc() );
		this.install( "snoc", SnocProc.SNOC_PROC );
		this.install( "split", RegexProcs.split );
		this.install( "sqrt", new SqrtProc() );
		this.install( "startServer", StartServerProc.START_SERVER_PROC );
		this.install( "symbolString", SymbolStringProc.SYMBOL_STRING_PROC );
		this.install( "tagName", ElementNameProc.ELEMENT_NAME_PROC );
		this.install( "tail", TailProc.TAIL_PROC );
		this.install( "tan", new TanProc() );
		this.install( "termin", Termin.TERMIN );
		this.install( "toDegrees", new ToDegreesProc() );
		this.install( "toRadians", new ToRadiansProc() );
		this.install( "true", BooleanTools.TRUE );

		this.install( "showImage", new Unary1InvokeProc() {
			public Object invoke( Object x ) {
				new ShowImage( CastLib.toImage( x ) ).main();
				return null;
			}
		} );
	}

	public NameSpace installBuiltIns() {
		this.install_built_ins();
		return this;
	}

}
