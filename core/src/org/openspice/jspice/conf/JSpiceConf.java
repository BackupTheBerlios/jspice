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

package org.openspice.jspice.conf;

import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.class_builder.JSpiceClassLoader;
import org.openspice.jspice.main.Print;
import org.openspice.jspice.main.jline_stuff.PrefixFilterAccumulator;
import org.openspice.jspice.main.manual.FileManual;
import org.openspice.jspice.main.manual.Manual;
import org.openspice.jspice.main.pragmas.StylePragma;
import org.openspice.tools.ImmutableSetOfBoolean;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VFolder;
import org.openspice.vfs.VVolume;
import org.openspice.vfs.file.FileVVolume;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public final class JSpiceConf {

	//	---- File and Folder Naming Conventions ----
	//	I suspect that almost all of the following should be relegated to
	//	parameters in the jspice.conf file.

	private static final String auto_folder_suffix = "-auto";
	private static final Pattern auto_folder_pattern = Pattern.compile( "^([a-zA-Z]\\w*)" + auto_folder_suffix + "$" );


//	private static final String inventory_conf_file_name = FixedConf.INVENTORY_NAM + "." + FixedConf.CONF_EXT;
	private static final File personal_inventory = new File( ".jspice", FixedConf.INVENTORY_NAM );

	//	---oooOOOooo---

	String prompt_base = FixedConf.BASE_PROMPT + " ";

	public String getPrompt() {
		return this.prompt_base;
	}

	//	---oooOOOooo---


	public final String getLoadConfFileName() {
		return FixedConf.load_conf_file_name;
	}

	public final String getLoadFolderName() {
		return FixedConf.load_folder_nam;
	}

	public static final String getLoadSpiceFileName() {
		return FixedConf.load_spice_file_name;
	}

	public static final Pattern getAutoFolderPattern() {
		return auto_folder_pattern;
	}

	private static final char dir_extn_char = '-';
	private static final char file_extn_char = '.';

	public char getExtensionChar( final File f ) {
		 return f.isDirectory() ? dir_extn_char : file_extn_char;
	}

	//	---- Inventories ----

	private Set inventories = (
		new TreeSet(
			new Comparator() {
				public int compare( final Object a, final Object b ) {
					return ((InventoryConf)a).getUniqueID().compareTo( ((InventoryConf)b).getUniqueID() );
				}
			}
		)
	);

	public Set getInventories() {
		return inventories;
	}

	public void installInventoryConf( final VFolder inventory_path ) {
		if ( !inventory_path.getVFileRef( FixedConf.std_inventory_name, FixedConf.CONF_EXT ).exists() ) {
			new Alert( "Missing inventory configuration file" ).culprit( "path", inventory_path ).warning();
		}
		final InventoryConf invc = new InventoryConf( this, inventory_path );
		if ( !this.inventories.contains( invc ) ) {
			//	Get nicknames that are in use.
			final Set nicknames = new HashSet();
			for ( Iterator it = this.inventories.iterator(); it.hasNext(); ) {
				nicknames.add( ((InventoryConf)it.next()).getNickname() );
			}
			//	Add immediately, to inhibit duplicate loading attempts.
			this.inventories.add( invc );
			//	Complete 2nd (& more expensive) phase of life cycle.
			Print.println( Print.CONFIG, "loading inventory: " + inventory_path );
			invc.loadInventory();
			//	Now enforce a unique nickname.
			String nn = invc.getNickname();
			while ( nicknames.contains( nn )  ) {
				nn += "*";
			}
			invc.setNickname( nn );
		}
	}


	final String getInventoryConfNam() {
		return FixedConf.INVENTORY_NAM;
	}

	final VFolder lookupInventoryNickname( final String nickname ) {
		final VVolume root = new FileVVolume( new File( "/" )  );
		if ( "standard".equals( nickname ) ) {
			return root.getVFolderFromPath( "usr/local/jspice/inventory" );
		} else if ( "local".equals( nickname ) ) {
			return root.getVFolderFromPath( "etc/jspice/inventory" );
		} else if ( "personal".equals( nickname ) ) {
			return root.getVFolderRefFromPath( System.getProperty( "user.home" ) ).getVFolderRefFromPath( personal_inventory.getPath() ).getVFolder( ImmutableSetOfBoolean.EITHER, false );
		} else {
			throw new Alert( "Invalid inventory nickname" ).culprit( "nickname", nickname ).mishap();
		}
	}

	//
	//	Given a package name, locate the package folder.  The answer must be unqiue
	//	even after searching every inventory.
	//
	final public VFolder locatePackage( final String pkg_name ) {
		VFolder answer = null;
		for ( Iterator it = this.inventories.iterator(); it.hasNext(); ) {
			final InventoryConf iconf = (InventoryConf)it.next();
			final VFolder f = iconf.locatePackageFolder( pkg_name );
			if ( answer == null ) {
				answer = f;
			} else if ( f != null ) {
				new Alert( "Duplicate package folders" ).
				culprit( "pkg_name", pkg_name ).
				culprit( "folder", answer ).
				culprit( "folder", f ).
				mishap();
			}
		}
		return answer;
	}

	//	---- getLoaderClassName ----

	//	Map< String, LoaderBuilderRecord >
	private final Map extnMap = new TreeMap();

	public String getLoaderBuilderClassName( final String extn, final boolean null_allowed ) {
		final LoaderBuilderRecord r = extn != null ? (LoaderBuilderRecord)this.extnMap.get( extn ) : null;
		if ( r !=  null ) return r.getClassName();
		if ( null_allowed )	return null;
		throw new Alert( "No loader associated with this extension" ).culprit( "extension", extn ).mishap();
	}

	public Collection getAutoloaders() {
		return this.extnMap.values();
	}


	//	---- Global Modes ----

	boolean is_debugging = "on".equals( System.getProperty( FixedConf.getPropertyName( "debug" ) ) );
	public boolean isDebugging() {
		return is_debugging;
	}

	public void setIsDebugging( final boolean _is_debugging ) {
		this.is_debugging = _is_debugging;
	}

	//	---- Self Homing ----


	private final VFolder jspice_home;
	private VFile jspice_conf_vfile;

	private static File home() {
		final String jhomef = System.getProperty( FixedConf.getPropertyName( "home" ) );
		if ( jhomef == null ) return null;
		final File jhome = new File( jhomef );
		if ( jhome.canRead() ) return jhome;
		return null;
	}

	public VFolder getUserHome() {
		return new FileVVolume( new File( System.getProperty( "user.home" ) ) ).getRootVFolder();
	}

	public VFolder getHome() {
		return this.jspice_home;
	}




	//	---- Find help file ----

	//	Maps commands/manual names into Manuals
	Map manuals = new HashMap();
	{
		this.manuals.put( "help", new FileManual( this, "help", "jspice" ) );
		this.manuals.put( "licence", new FileManual( this, "licence", "licences", "jspice" ) );
	}

	public Manual getManualByName( final String mname ) {
		return (Manual)manuals.get( mname );
	}

	public void findManualCompletions( final PrefixFilterAccumulator acc ) {
		acc.addAll( this.manuals.keySet() );
	}

	// 	---- Licence File ----

	public VFile getLicenceFile() {
		return this.jspice_home.getVFile( FixedConf.licence_nam, FixedConf.TXT_EXT );
	}

	//	---- Environment Variables ----
	//	The environment is passed in via -D parameters.  Java is pants.

	private Map env_map = null;
	private static final Pattern wboundary = Pattern.compile( "[\\n\\r]+" );

	public Map getEnvMap() {
		if ( this.env_map != null ) return this.env_map;
		this.env_map = new TreeMap();
		final String env = System.getProperty( FixedConf.getPropertyName( "env" ) );
		if ( env == null ) return this.env_map;

		final String[] bindings = wboundary.split( env );
		for ( int i = 0; i < bindings.length; i++ ) {
			final String b = bindings[ i ];
			final int n = b.indexOf( '=' );
			if ( n >= 0 ) {
				final String key = b.substring( 0, n );
				final String val = b.substring( n+1 );
				this.env_map.put( key, val );
			}
		}

		return this.env_map;
	}

	//	---- Entity Tables ----

	final Map entityToStringMap = new HashMap();	//	Map< String, Character >
	final Map charToEntityMap = new HashMap();		//	Map< Character, String >

	public Character decode( final String s ) {
		return (Character)this.entityToStringMap.get( s );
	}

	public String encode( final char ch ) {
		return (String)this.charToEntityMap.get( new Character( ch ) );
	}

	public List listEntities( final String regex ) {
		final List answer = new ArrayList();
		for ( Iterator it = this.charToEntityMap.values().iterator(); it.hasNext(); ) {
			final String ent = (String)it.next();
			if ( ent.matches( regex ) ) {
				answer.add( ent );
			}
		}
		return answer;
	}

	//	---- Constructor ----



	/**
	 * This is placeholding stuff at present.  Obviously enough the configuration
	 * file should have a unified format.  However, for the moment the basic rules are:-
	 * 	1.	'#' is an end of line comment
	 * 	2.	data entry lines are
	 *          AddLoaderBuilder EXTN CLASSNAME
	 *          Entity NAME HEX_CODE
	 *          Prompt STRING
	 * 	3.	blank lines are discarded
	 */
	private final void parseJSpiceConf( final VFile conf_file ) { //, final Map env_map, final Map entity_to_code_map, final Map code_to_entity_map ) {
		final ConfTokenizer conft = new ConfTokenizer( conf_file.readContents() );
		for ( final List list = new ArrayList(); conft.next( list ) != null; list.clear() ) {
			final int list_size = list.size();	//	Guaranteed to be at least length 1.
			final String command = (String)list.get( 0 );
			if ( "AddLoaderBuilder".equals( command ) && 3 <= list_size && list_size <= 4 ) {
				final String extn = (String)list.get( 1 );
				final String cname = (String)list.get( 2 );
				final String comment = (String)list.get( 3 );
				this.extnMap.put( extn, new LoaderBuilderRecord( extn, cname, comment ) );
			} else if ( "Entity".equals( command ) && list_size == 3 ) {
				final String name = (String)list.get( 1 );
				final String hex_code_str = (String)list.get( 2 );
				if ( hex_code_str.length() > 0 && hex_code_str.charAt( 0 ) == 'U' ) {
					final char code = (char)Integer.parseInt( hex_code_str.substring( 1 ), 16 );
					final Character ch = new Character( code );
					this.entityToStringMap.put( name, ch );
					this.charToEntityMap.put( ch, name );
				} else {
					new Alert( "Unrecognised entity code" ).culprit( "name", name ).culprit( "code", hex_code_str ).warning();
				}
			} else if ( "Prompt".equals( command ) && list_size == 2 ) {
				this.prompt_base = (String)list.get( 1 );
			} else if ( "StyleWarning".equals( command ) ) {
				list.remove( 0 );
				new StylePragma().enable( list );
			} else {
				final Object directive = list.remove( 0 );
				new Alert( "Unrecognized directive JSpice conf file" ).culprit( "directive", directive ).culprit_list( list ).mishap();
			}
		}
	}

	public static JSpiceClassLoader jspice_class_loader = new JSpiceClassLoader();

	public JSpiceClassLoader getClassLoader() {
		return jspice_class_loader;
	}


	public JSpiceConf() {
//		this.jspice_home = UrlVFolderRef.make( "ftp://heather:lucy@127.0.0.1" + home() + "/" ).getVFolder( ImmutableSetOfBoolean.ONLY_TRUE,  false );
		final File h = home();
		if ( h == null ) {
			new Alert( "Cannot determine JSpice home directory" ).mishap();
		}
		this.jspice_home = new FileVVolume( home() ).getRootVFolderRef().getVFolder( ImmutableSetOfBoolean.ONLY_TRUE, false );
		if ( this.jspice_home == null ) {
			new Alert( "Cannot locate JSpice home directory" ).warning();
		}
		this.jspice_conf_vfile = this.jspice_home.getVFile( FixedConf.JSPICE_CONF_NAM, FixedConf.CONF_EXT );

		//	Parse the JConf file.
		this.parseJSpiceConf( this.jspice_conf_vfile );

		Print.println( Print.CONFIG, "Installing std inventory ..." );
		this.installInventoryConf( this.jspice_home.getVFolder( FixedConf.std_inventory_name, null ) );
		Print.println( Print.CONFIG, "... installed" );

		//	todo: move the strings constants to FixedConf
		this.parseJSpiceConf( this.getUserHome().getVFolderRef().getVFileFromPath( ".jspice/jspice.conf" ) );
//		try {
//			this.parseJSpiceConf( new ZipVVolume( new ZipFile( new File( "/tmp/foo.zip" ) ) ).getVFileFromPath( ".jspice/jspice.conf" ) );
//		} catch ( IOException e ) {
//			throw new RuntimeException( e );
//		}
	}

}
