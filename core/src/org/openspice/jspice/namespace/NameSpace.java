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

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.conf.LoadConf;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.main.SuperLoader;
import org.openspice.jspice.main.Print;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VItem;

import java.util.*;

public class NameSpace {
	private NameSpaceManager	manager;
    private String 				name;
    private String 				default_nickname;
	private VFolder				vfolder;			// 	String or null.
    private FacetSet			default_facets;
    private List 				ok_facet_list;
    private boolean 			is_incremental;
    private boolean 			is_std_importer;
    private Map 				local_map;				//	Map< String, Var.Perm >
    private Map 				imports_by_nickname;	//	Map< String, Import >

	public void findCompletions( final String prefix, final List list ) {
		for ( Iterator it = local_map.keySet().iterator(); it.hasNext(); ) {
			final String key = (String)it.next();
			if ( key.startsWith( prefix ) ) {
				list.add( key );
			}
		}
		for ( Iterator it = imports_by_nickname.values().iterator(); it.hasNext(); ) {
			final Import imp = (Import)it.next();
			imp.getNameSpace().findCompletions( prefix, list );
		}
	}

	public Map getLocalBindings() {
		return this.local_map;
	}

	public String getName() {
		return this.name;
	}

	public NameSpaceManager getNameSpaceManager() {
		return this.manager;
	}

	public boolean isStdImporter() {
		return this.is_std_importer;
	}

	public boolean isIncremental() {
		return this.is_incremental;
	}

	public void setIsIncremental( final boolean b ) {
		this.is_incremental = b;
	}

	public FacetSet getDefaultFacets() {
		return this.default_facets;
	}

	public void addImport(
		final String nn,
		final boolean qflag, 
		final NameSpace ipkg,
		final FacetSet facets,
		final FacetSet into
	) {
		final Map bnn = this.imports_by_nickname;
		final NameSpace imp = (NameSpace)bnn.get( nn );
		if ( imp != null ) {
			new Alert(
				"Trying to add a second import with this nickname",
				"All imports to a package must have different nicknames"
			).
			culprit( "nickname", nn ).
			culprit( "package", this.name ).
			mishap( 'I' );
		} else {
			final Import impt = (
				new Import( nn, qflag, ipkg, facets, into )
			);
			bnn.put( nn, impt );
		}
	}

	
	private String nameFromNickname( final String nn ) {
		final Map bnn = this.imports_by_nickname;
		final Import imp = (Import)bnn.get( nn );
		if ( imp == null ) {
			new Alert( "No import with this nickname" ).culprit( "nickname", nn ).mishap( 'I' );
		}
		return imp.getNameSpace().getName();
	}

	void addStandardImport( final NameSpace imp_ns ) {
	    this.addImport(
			imp_ns.default_nickname,
			false,                  // unqualified
			imp_ns,
			FacetSet.PUBLIC,
			FacetSet.NONE
		);
    }

	NameSpace populate() {
		this.populateAutoloads( false );
		this.loadCoreFiles( this.getNameSpaceManager().getSuperLoader() );
		return this;
	}

	NameSpace(
		final NameSpaceManager nsmsg,
		final String cname,
		final String nname,
		final VFolder pkg_dir
	) {
		this.init( nsmsg, cname, nname, pkg_dir, FacetSet.PUBLIC, FacetSet.WELL_KNOWN, true );
	}

	NameSpace(
		final NameSpaceManager	_manager,
		final Title				title,
		final VFolder 			_directory,				// 	String or null.
		final FacetSet 			_default_facets,
		final List 				_ok_facet_list,
		final boolean 			_is_std_importer
	) {
		this.init( _manager, title, _directory,	_default_facets, _ok_facet_list, _is_std_importer );
	}

	private NameSpace init(
		final NameSpaceManager	_manager,
		final Title				title,
		final VFolder 			_directory,				// 	String or null.
		final FacetSet 			_default_facets,
		final List 				_ok_facet_list,
		final boolean 			_is_std_importer
	) {
	    final String name = title.compact();
		final String nickname = title.last();
		return(
			this.init(
				_manager,
				name,
				nickname,
				_directory,				// 	String or null.
				_default_facets,		
				_ok_facet_list,
				_is_std_importer
			)
		);
	}

	private NameSpace init(
		final NameSpaceManager	_manager,
	    final String 			_name,
		final String 			_default_nickname,
		final VFolder			_directory,				// 	String or null.
		final FacetSet 			_default_facets,
		final List 				_ok_facet_list,
		final boolean 			_is_std_importer
	) {
		assert _default_facets != null;
		this.manager = _manager;
		this.name = _name;
		this.default_nickname = _default_nickname;
		this.vfolder = _directory;
		this.default_facets = _default_facets;
		this.ok_facet_list = _ok_facet_list;
		this.is_incremental = true;
		this.is_std_importer = _is_std_importer;
    	this.local_map = new HashMap();
		this.imports_by_nickname = new HashMap();
		
		_manager.welcome( this );
		
		return this;
	}
	
	private Var.Perm lookupResource( final FacetSet facets, final String id, final boolean repopulate ) {
		final Var.Perm perm = (Var.Perm)this.local_map.get( id );
		if ( perm != null ) {
			if ( Print.wouldPrint( Print.IMPORT ) ) {
				Print.println( Print.IMPORT, "found " + perm + " with facets = " + ( perm != null ? perm.getFacetSet().toString() : "-" ) );
				Print.println( Print.IMPORT, "facets = " + facets );
				Print.println( Print.IMPORT, "match? " + perm.getFacetSet().match( facets ) );
			}
			return perm.getFacetSet().match( facets ) ? perm : null;
		} else if ( repopulate ) {
			this.populateAutoloads( true );
			return this.lookupResource( facets, id, false );
		} else {
			return null;
		}
	}

	SuperLoader getSuperLoader() {
		return this.getNameSpaceManager().getSuperLoader();
	}

	JSpiceConf getJSpiceConf() {
		return this.getSuperLoader().getJSpiceConf();
	}

	private void addAutoloadable( final VItem file, final String facet, final String name ) {
		Print.println( Print.AUTOLOAD, "add autoloadable name = " + name + "; facet = " + facet );
		final FacetSet fs = FacetSet.make( facet );
		final Var.Perm perm = this.declarePerm( fs, name, Props.VAL );
		perm.getLocation().makeAutoloadReady( this, perm, fs, file );
	}

	private void populateAutoloads( final boolean reload_flag ) {
		Print.println( Print.AUTOLOAD,  "populate autoloads" );
		for ( Iterator it = this.vfolder.listVFolders().iterator(); it.hasNext(); ) {
			final VFolder d = (VFolder)it.next();
			if ( d.hasExt( FixedConf.AUTO_EXT ) ) {
				final String facet = d.getNam();
				//	-d- is an autoloadable folder with facet -facet-
				for ( Iterator jt = d.listVItems().iterator(); jt.hasNext(); ) {
					final VItem f = (VItem)jt.next();
					if ( this.getSuperLoader().couldLoadVItem( f ) ) {
						Print.println( Print.AUTOLOAD, "autoloadable file: " + f.getFullName() );
						this.addAutoloadable( f, facet, f.getNam() );
					}
				}
			}
		}
	}

	public final void loadCoreFiles( final SuperLoader sloader ) {
		final LoadConf load_conf = new LoadConf( this.vfolder, this.manager.getSuperLoader().getJSpiceConf() );
		Print.println( Print.LOAD, "loading files ... ");
		for(;;) {
			final VFile f = load_conf.nextFile();
			if ( f == null ) break;
			Print.println( Print.LOAD, "load file " + f );
			sloader.loadFile( f, this );
		}
		Print.println( Print.LOAD, "... done" );
	}

	Var.Perm tryLookupPerm( final FacetSet okfacets, final String id, final boolean repopulate ) {

		if ( Print.wouldPrint( Print.IMPORT ) ) Print.println( "lookup " + id + " with facets " + okfacets );
		
		Var.Perm perm = this.lookupResource( okfacets, id, repopulate );
		if ( perm != null ) if ( Print.wouldPrint( Print.IMPORT ) ) Print.println( "found in namespace = " + this.getName() );
		if ( perm != null ) return perm;

		Import eff_import = null;

		for ( Iterator imp_it = this.imports_by_nickname.values().iterator(); imp_it.hasNext(); ) {
			final Import imp = (Import)imp_it.next();
			final FacetSet into = imp.getIntoFacetSet();
			if ( into.match( okfacets ) ) {
				final Var.Perm d = imp.getNameSpace().tryLookupPerm( imp.getFacetSet(), id, repopulate );
				if ( d != null && okfacets.match( d.getFacetSet() ) ) {
					if ( perm != null ) {
						//	Ambiguous!
						new Alert(
							"Ambiguous import",
							"There are two imports for this identifier"
						).
						culprit( "identifier", id ).
						culprit( "import1", eff_import ).
						culprit( "import2", imp ).
						mishap( 'I' );
					} else {
						eff_import = imp;
						perm = d;
					}
				}
			}
		}
		
		if ( perm != null & eff_import != null ) {

			final NameSpace ins = eff_import.getNameSpace();
			final FacetSet into = eff_import.getIntoFacetSet();
			
			Var.Perm nperm = (
				new Var.Perm(
					perm.getProps(),
					//;;; importedCacheFacetSet ||
					into,                  //	the relevant facets
					id,
					perm.getLocation()
				).setIsImportedFlag()
			);

			Print.println( Print.IMPORT, "---" );
			Print.println( Print.IMPORT, "Importing identifier " + id );
			Print.println( Print.IMPORT, "Permanent " + nperm );
			Print.println( Print.IMPORT, "Into " + into );
			Print.println( Print.IMPORT, "From package " + ins.getName() );
			Print.println( Print.IMPORT, "To package " + this.getName() );
			
			this.local_map.put( id, nperm );
			return nperm;
		} else {
			return null;
		}
	}
	
	private Var.Perm tryFetchUnqualifiedPerm( final FacetSet okfacets, final String id ) {
		Var.Perm perm = this.tryLookupPerm( okfacets, id, false );
		if ( perm != null ) return perm;
		if ( this.manager.repopulateOption() ) {
			return this.tryLookupPerm( okfacets, id, true );
		}
		return null;
	}
	
	private Var.Perm fetchQualifiedPerm( final String nickname, final String id ) {
		final Import imp = (Import)this.imports_by_nickname.get( nickname );
		if ( imp != null ) {
			final Var.Perm d = (
				imp.getNameSpace().tryFetchUnqualifiedPerm(
					imp.getFacetSet(),
					id
				)
			);
			if ( d != null ) return d;
			new Alert( "Cannot import identifier" ).
			culprit(  "nickname", nickname ).
			culprit(  "identifier", id ).
			mishap( 'I' );
		} 
		new Alert( "No import associated with this nickname" ).
		culprit( "nickname", nickname ).
		mishap( 'I' );
		return null;	//	sop
	}

	private Location fetchQualifiedRef( final String nickname, final String id ) {
		return this.fetchQualifiedPerm( nickname, id ).getLocation();
	}

	
	private Var.Perm fetchUnqualifiedPerm( final String id ) {
		final Var.Perm answer = this.tryFetchUnqualifiedPerm( FacetSet.ALL, id );
		if ( answer != null ) return answer;
		new Alert(
			"Undeclared identfier",
			"Neither declared locally nor importable"
		).
		culprit( "identifier", id ).
		culprit( "package", this.getName() ).
		mishap( 'I' );
		return null;	//	sop
	}

	/**
	 * Fetch a qualified or unqualified permanent identifier.
	 * @param nickname may be null
	 * @param name name of identifier
	 * @return return permanent
	 */
	public Var.Perm fetchPerm( final String nickname, final String name ) {
		if ( nickname == null ) {
			return this.fetchUnqualifiedPerm( name );
		} else {
			return this.fetchQualifiedPerm( nickname, name );
		}
	}
	
	private Location fetchUnqualifiedRef( final String id ) {
		return this.fetchUnqualifiedPerm( id ).getLocation();
	}

	private void cancelPerm( final String id ) {
		final Var.Perm perm = (Var.Perm)this.local_map.get( id );
		if ( perm != null ) {
			if ( Print.wouldPrint( Print.IMPORT ) ) Print.println( "# CANCELLING " + id );
			this.local_map.remove( id );
		}
	}
	
	/**
	 * 	Returns the appropriate Perm.
	 */
	public Var.Perm declarePerm( final FacetSet facet_set, final String id, final Props props ) {
		return this.declarePerm( facet_set, id, props, true );
	}

	/**
	 * Declares a permanent identifier in the current namespace.
	 * @param facet_set the facet set the identifier shares in
	 * @param id the print string of the identifier
	 * @param props the set of properties of the identifier
	 * @return the permanent
	 */
	Var.Perm declarePerm( final FacetSet facet_set, final String id, final Props props, final boolean prev_allowed ) {
		Var.Perm d = (Var.Perm)this.local_map.get( id );

		if ( d != null ) {
			if ( d.getProps().isForwardVersion( props ) ) {
				if ( d.getIsImportedFlag() ) {
					new Alert(
						"Trying to redeclare implicitly declared import",
						"Identifier has been imported by unqualified reference"
					).
					culprit( "identifier", id ).
					culprit( "package", this.getName() ).
					culprit( "suggest", "Change to a qualified reference" ).
					mishap( 'I' );
				}
				d.modFacetSet( facet_set ).modProps( props );
				return d;
			} else {
				if ( Print.wouldPrint( Print.IMPORT ) ) Print.println( "previous declaration noticed (" + prev_allowed + ")" );
				if ( !prev_allowed ) {
					new Alert( "Identifier already declared" ).
					culprit( "name", id ).
					mishap( 'I' );
				}
				if ( this.is_incremental ) {
					if ( this.manager.cancelIdentifiersOption() ) {
						this.cancelPerm( id );
						return this.declarePerm( facet_set, id, props );
					} else {
						d.modFacetSet( facet_set ).modProps( props );
						return d;
					}
				} else {
					if ( d.getIsImportedFlag() ) {
						new Alert(
							"Trying to redeclare implicitly declared import",
							"Identifier has been imported by unqualified reference"
						).
						culprit( "identifier", id ).
						culprit( "package", this.getName() ).
						culprit( "suggest", "Change to a qualified reference" ).
						mishap( 'I' );
					} else {
						new Alert(
							"Duplicate declaration of local identifier found",
							"Identifier is already declared"
						).
						culprit( "identifier", id ).
						culprit( "package", this.getName() ).
						mishap( 'I' );
					}
					return null;	//	sop.
				}
			}
		} else {
			final Location locn = new Location();
			final Var.Perm nperm = new Var.Perm( props, facet_set, id, locn ).setIsNullFlag();
			this.local_map.put( id, nperm );
			if ( Print.wouldPrint( Print.IMPORT ) ) Print.println( "nperm.getLocation() = " + nperm.getLocation() );
			return nperm;
		}
	}




}
