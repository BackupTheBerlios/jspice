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
package org.openspice.jspice.main.manual;

import org.openspice.jspice.conf.FixedConf;
import org.openspice.jspice.conf.InventoryConf;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.main.Print;
import org.openspice.jspice.datatypes.ImmutableList;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class FileManual extends AbsFileManual {

	public FileManual( final JSpiceConf jspice_conf, final String manual_name, final String manual_folder_name, final String default_topic ) {
		super( jspice_conf, manual_name, manual_folder_name, default_topic );
	}

	public FileManual( final JSpiceConf jspice_conf, final String manual_name, final String default_topic ) {
		this( jspice_conf, manual_name, manual_name, default_topic );
	}

	//	----

	private void find( final SearchPhrase phrase, final File folder, final String nickname, final String pkg_name, final List accumulator ) {
		if ( folder.isDirectory() ) {
			//	This is just an efficiency hack to reduce the need to scan entire directories.
			final String exact = phrase.exactlyMatches( 2 );
			if ( exact != null ) {
				final String fname = FixedConf.topicNameToFileName( exact );
				final File file = new File( folder, fname );
				if ( file.exists() ) {
					final SearchPhrase term = new SearchPhrase();
					term.add( nickname );
					if ( pkg_name != null ) {
						term.add( pkg_name );
					}
					term.add( exact );
					accumulator.add( new BasicSearchResult( term, new FileManualPage( file ) ) );
				}
			} else {
				//	It would always be safe to arrive here.
				final File[] ifiles = folder.listFiles();
				for ( int i = 0; i < ifiles.length; i++  ) {
					final File ifile = ifiles[ i ];
					if ( true || Print.wouldPrint( Print.HELP ) ) {
						Print.println( "searching " + ifile + " (" + ifile.exists() + ")" );
					}
					final String topic = FixedConf.fileNameToTopicName( ifile.getName() );
					if ( ifile.exists() && topic != null ) {
						if ( phrase.match( 2, topic ) ) {
							final SearchPhrase term = new SearchPhrase();
							term.add( nickname );
							if ( pkg_name != null ) {
								term.add( pkg_name );
							}
							term.add( topic );
							accumulator.add( new BasicSearchResult( term, new FileManualPage( ifile ) ) );
						}
					}
				}
			}
		}
	}

	private void allPackageFiles( final SearchPhrase phrase, final InventoryConf iconf, final List accumulator ) {
		for ( Iterator it = iconf.getRootFolderList().iterator(); it.hasNext(); ) {
			final File root =  (File)it.next();
			final File[] folders = (
				root.listFiles(
					new FilenameFilter() {
						public boolean accept( File dir, String name ) {
							return name.endsWith( FixedConf.pkg_suffix );
						}
					}
				)
			);
			for ( int i = 0; i < folders.length; i++ ) {
				final File ifolder = folders[ i ];
				final String pkg_name = FixedConf.folderNameToPackageName( ifolder.getName() );
				if ( phrase.match( 1, pkg_name ) ) {
					final File fldr = new File( ifolder, this.getManualFolderName() );
					this.find( phrase, fldr, iconf.getNickname(), pkg_name, accumulator );
				}
			}
		}
	}



	//	List< SearchResult >
	private List findTopicFile( final SearchPhrase phrase ) {
		final List results = new ArrayList();
		for ( Iterator it = this.getJSpiceConf().getInventories().iterator(); it.hasNext(); ) {
			final InventoryConf iconf = (InventoryConf)it.next();
			final String nickname = iconf.getNickname();
			if ( phrase.match( 0, nickname ) ) {
				if ( phrase.match( 1, "." ) ) {		//	virtual package name.
					final File folder = new File( iconf.getPathFile(), this.getManualFolderName() );
					//	null inhibits the printing of the dot.
					this.find( phrase, folder, nickname, null, results );
				}
				//	Add all the matching files from the packages.
				this.allPackageFiles( phrase, iconf, results );
			}
		}
		return results;
	}

	//	----

	//	phrase 	::=	TOPIC
	//			::= INVENTORY_NICKNAME TOPIC
	//			::= INVENTORY_NICKNAME PACKAGE_NAME TOPIC
	public List search( final SearchPhrase phrase ) {
		final int n = phrase.size();
		if ( n == 0 ) {
			final SearchPhrase t = phrase.copy();
			t.add( this.getDefaultTopic() );
			if ( Print.wouldPrint( Print.HELP ) ) Print.println( "try with: " + t );
			return this.search( t );
		} else if ( n == 1 ) {
			final SearchPhrase t = new SearchPhrase();
			t.addMatchAll();
			t.addMatchAll();
			t.add( phrase.get( 0 ) );
			if ( Print.wouldPrint( Print.HELP ) ) Print.println( "try with: " + t );
			return this.search( t );
		} else if ( n == 2 ) {
			final SearchPhrase u = new SearchPhrase();
			u.add( phrase.get( 0 ) );
			u.add( "." );					//	virtual package for inventories
			u.add( phrase.get( 1 ) );
			if ( Print.wouldPrint( Print.HELP ) ) Print.println( "try with: " + u );
			return this.search( u );
		} else if ( n == 3 ) {
			return this.findTopicFile( phrase );
		} else {
			return ImmutableList.EMPTY_LIST;
		}
	}

//	public static final Manual HELP_MANUAL = new Manual( "help", "jspice" );
//	public static final Manual LICENCE_MANUAL = new Manual( "licence", "licences", "jspice" );
//	public static final Manual CONDITIONS_MANUAL = new Manual( "conditions", "licences", "jspice_conditions" );
//	public static final Manual WARRANTY_MANUAL = new Manual( "warranty", "licences", "jspice_warranty" );

}
