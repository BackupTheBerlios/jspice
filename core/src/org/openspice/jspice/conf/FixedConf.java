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

import org.openspice.jspice.main.Print;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

public abstract class FixedConf {

	//	---- Versioning ----

	final static private String major_version = "0";
	final static private String minor_version = "4";
	final static private String incremental_version = "11";

	public static final String STD_LIB = "org.openspice.stdlib";

	static final String licence_nam = "LICENSE";  			//	U.S. spelling for licence, not a spelling mistake.
	static final String CONF_EXT = "conf";
	static final String SPICE_EXT = "spi";
	static final String TXT_EXT = "txt";
	public static final String PKG_EXT = "pkg";
	public static final String AUTO_EXT = "auto";
	public static final String SYMTAB_EXT = "symtab";
	static final String JSPICE_CONF_NAM = "jspice";
	static final String INVENTORY_NAM = "inventory";
	static final String std_inventory_name = INVENTORY_NAM;

	public static final char VFILE_SEPARATOR = '.';
	public static final char VFOLDER_SEPARATOR = '-';
	public static final char VFOLDER_TERMINATOR = '/';
	public static final char VITEM_ESCAPE = '%';

	static final String load_file_nam = "load";
	static final String load_folder_nam = load_file_nam;
	static final String load_conf_file_name = load_folder_nam + VFILE_SEPARATOR + FixedConf.CONF_EXT;
	static final String load_spice_file_nam = load_folder_nam;
	static final String load_spice_file_name = load_spice_file_nam + VFILE_SEPARATOR + FixedConf.SPICE_EXT;


	public static String getIncrementalVersion() {
		return incremental_version;
	}

	public static String getMajorVersion() {
		return major_version;
	}

	public static String getMinorVersion() {
		return minor_version;
	}

	public static String version() {
		return major_version + '.' + minor_version + '.' + incremental_version;
	}

	public static String banner() {
		return(
			"Welcome to JSpice (version " + version() + "), Copyright (C) 2003, Stephen F. K. Leach\n" +
			"JSpice comes with ABSOLUTELY NO WARRANTY; for details type: `#warranty'\n" +
			"This is free software, and you are welcome to redistribute it under certain\n" +
			"conditions; type `#conditions' for details."
		);
	}

	public static void printBanner() {
		System.out.println( banner() );
		if ( Print.wouldPrint( Print.INFO ) ) {
			System.out.println( "Current working directory is: " + System.getProperty( "user.dir" ) );
			System.out.println( "The home directory is: " + System.getProperty( "user.home" ) );
			System.out.println( "The user name is: " + System.getProperty( "user.name" ) );

			//	And these are important because they provide a strategy for self-homing.
			System.out.println( "The class path is: " + System.getProperty( "java.class.path" ) );
			System.out.println( "The system home is: " + System.getProperty( "org.openspice.jspice.home" ) );
		}
	}

	//	----

	public static final String BASE_PROMPT = ">>>";

	//	--- Internal names ---

	private static final String package_root = "org.openspice.jspice";
//	private static final String env_pname = package_root + ".env";
	private static final String dynld_pname = package_root + ".dynld";

	public static final String getPackageRoot() {
		return package_root;
	}

	public static final String getDynPackagePrefix() {
		return dynld_pname;
	}

	public static final String getPropertyName( final String suffix ) {
		return package_root + '.' + suffix;
	}

	//	---oooOOOooo---

	//	todo: This is rubbish really.  We ought to handle many extensions.
	public static final String helpFileExtension = ( '.' + "txt" );

	public static final String topicNameToFileName( final String topic ) {
		try {
			return URLEncoder.encode( topic, "UTF-8" ) + FixedConf.helpFileExtension;
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final String fileNameToTopicName( final String fname ) {
		if ( fname.endsWith( FixedConf.helpFileExtension ) ) {
			final String f = fname.substring( 0, fname.length() -  FixedConf.helpFileExtension.length() );
			try {
				return URLDecoder.decode( f, "UTF-8" );
			} catch ( UnsupportedEncodingException e ) {
				throw new RuntimeException( e );
			}
		} else {
			return null;
		}
	}

	//	---oooOOOooo---

	public static final char pkg_separator = '-';
	static final char pkg_replace_dot = '-';
	public static final String pkg_suffix = ( pkg_separator + PKG_EXT );

	public static final String packageNameToFolderName( final String pname ) {
		return pname.replace( '.', pkg_replace_dot ) + pkg_suffix;
	}

	public static final String folderNameToPackageName( final String fname ) {
		return fname.substring( 0, fname.length() - pkg_suffix.length() ).replace( pkg_replace_dot, '.' );
	}

}
