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

import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.namespace.NameSpaceDirTools;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.SuperLoader;
import org.openspice.vfs.VFolder;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;

public class NameSpaceManager {

	private final SuperLoader super_loader;

	public SuperLoader getSuperLoader() {
		return this.super_loader;
	}

	private final Hashtable space_table = new Hashtable();
		
	private final List standard_import_list = new LinkedList();

	boolean repopulateOption() {
		return false;
	}

	boolean cancelIdentifiersOption() {
		return false;
	}

	public NameSpace get( final String cname ) {
		return (NameSpace)this.space_table.get( cname );
	}
	
	void put( final String cname, final NameSpace ns ) {
		this.space_table.put( cname, ns );
	}
	
	void addStandardImportList( final NameSpace ns ) {
		for ( Iterator it = standard_import_list.iterator(); it.hasNext(); ) {
			ns.addStandardImport( (NameSpace)it.next() );
		}
	}
	
	void addNewStandardNameSpace( final NameSpace std ) {
		if ( ! ( this.standard_import_list.contains( std ) ) ) {
			this.standard_import_list.add( std );
			
			final Iterator it = this.space_table.entrySet().iterator();
			while ( it.hasNext() ) {
				final Map.Entry me = (Map.Entry)it.next();
				final String cname = (String)me.getKey();
				final NameSpace ns = (NameSpace)me.getValue();
				if ( ns.isStdImporter() ) {
					ns.addStandardImport( std );
				}
			}
			//;;; Package coherency can be violated by this operation
			//;;; so we need a post-hoc coherency check here.
			//postHocPackageCoherencyCheck()
			
		}
	}

	void welcome( final NameSpace newbie ) {
		this.space_table.put( newbie.getName(), newbie );
		if ( newbie.isStdImporter() ) {
			this.addStandardImportList( newbie );
		}
	}

	NameSpace makeNameSpace(
		final Title				title,
		final VFolder 				_directory,				// 	String or null.
		final FacetSet 			_default_facet,
		final List 				_ok_facet_list,
		final boolean 			_is_std_importer
	) {
		return (
			new NameSpace(
				this,
				title,
				_directory,				// 	String or null.
				_default_facet,			//  String or null.
				_ok_facet_list,
				_is_std_importer
			)
		);
	}

	NameSpace declareNameSpace(
		final Title 	namelist,
		final FacetSet 	_default_facet,
		final List	 	_ok_facet_names,
		final boolean 	_std_importer
	) {
		final String cname = namelist.compact();
		NameSpace ns = (NameSpace)this.space_table.get( cname );
		if ( ns == null ) {
			ns = this.makeNameSpace( namelist, null, _default_facet, _ok_facet_names, _std_importer );
		}
		return ns;
	}
	
	NameSpace autoload( final String cname ) {
		//	;;; dlocal package_loaded_list = [];    ;;; DISABLED

		/*
		;;; Given a package directory, load the core file, and establish
		;;; the autoloadable stubs.
		*/

		final Title title = new Title( cname );
		final String nname = title.last();
		final String dname = NameSpaceDirTools.packageDirName( cname );
		final String fname = NameSpaceDirTools.packageDirInitFile( cname );

		/*lvars dir;
		for dir in spice_package_search_list do
			lvars d = dir dir_>< dname;
			if sysisdirectory( d ) then
				load_package( cname, nname, d, fname );
				return( package_space( cname ) )
			endif
		endfor;
		*/
		return null;
	}

	private static final Pattern nnpatt = Pattern.compile( "^(.*)\\.[^\\.]*$" );
	public NameSpace load_package( final String cname, final VFolder dir ) {
		//	The default nick name of a package is its last component.
		final Matcher m = nnpatt.matcher( cname );
		final String nname = m.find() ? m.group( 1 ) : cname;
		return this.load_package( cname, nname, dir );
	}

	private NameSpace load_package( final String cname, final String nname, final VFolder pkg_dir ) {
		return new NameSpace( this, cname, nname, pkg_dir ).populate();
	}

	NameSpace fetchNameSpaceShort( final String cname ) {
		NameSpace nmsp = (NameSpace)space_table.get( cname );
		if ( nmsp != null ) return nmsp;
		nmsp = this.autoload( cname );
		if ( nmsp != null ) return nmsp;
		new Alert( "No such package" ).culprit( "name", cname ).mishap( 'I' );
		return null;	//	sop
	}


	public NameSpaceManager( final SuperLoader sloader ) {
		this.super_loader = sloader;

		//	The package space starts off with two standard packages:
		//	    1.  spice.standard
		//	    2.  spice.interaction

		final NameSpace standardNameSpace = (
			((SpiceStandardNameSpace)(
				new SpiceStandardNameSpace(
					this,
					new Title( "spice.standard" ),
					null,
					FacetSet.PUBLIC,
					FacetSet.WELL_KNOWN,
					false
				)
			)).installBuiltIns()
		);

		standardNameSpace.setIsIncremental( false );
		this.addNewStandardNameSpace( standardNameSpace );

		final NameSpace interactiveModeNameSpace = (
			this.declareNameSpace(
				new Title( "spice.interactive_mode" ),
				FacetSet.PUBLIC,
				FacetSet.WELL_KNOWN,
				true
			)
		);
		interactiveModeNameSpace.setIsIncremental( true );


	}
}