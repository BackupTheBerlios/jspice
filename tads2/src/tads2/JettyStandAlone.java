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
package tads2;

//import java.applet.Applet;
import tads2.jetty.GameWindow;
import tads2.jetty.Jetty;

import java.awt.*;
//import java.net.URL;
//import java.net.MalformedURLException;
import java.io.*;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.ZipInputStream;
import java.util.Map;

public class JettyStandAlone extends Panel implements Runnable {

	private ScrollPane _scrollbar;
	private GameWindow _display;
	private Jetty _jetty;
	private Thread _thread;

	private final File game_file;
	private final Map properties;

	public JettyStandAlone( final File file, final Map properties ) {
		this.game_file = file;
		this.properties = properties;
	}

	private final String getParameter( final String key ) {
		return (String)this.properties.get( key );
	}

	public void init() {

		String[] fonts = new String[ 3 ];
		String[] fg_colors = new String[ 3 ];
		String[] bg_colors = new String[ 3 ];

		fonts[ 0 ] = getParameter( "statusFont" );
		fg_colors[ 0 ] = getParameter( "statusForegroundColor" );
		bg_colors[ 0 ] = getParameter( "statusBackgroundColor" );
		fonts[ 1 ] = getParameter( "mainFont" );
		fg_colors[ 1 ] = getParameter( "mainForegroundColor" );
		bg_colors[ 1 ] = getParameter( "mainBackgroundColor" );
		fonts[ 2 ] = getParameter( "inputFont" );
		fg_colors[ 2 ] = getParameter( "inputForegroundColor" );
		bg_colors[ 2 ] = getParameter( "cursorColor" );

		int[] font_sizes = {0, 0, 0};
		for ( int i = 0; i < 3; i++ ) {
			String n = getParameter( _params[ i * 4 + 2 ][ 0 ] );
			try {
				if ( n != null )
					font_sizes[ i ] = Integer.parseInt( n );
			} catch ( NumberFormatException nfe ) {
				System.err.println( "Error reading parameter: font size not a number," +
						" is '" + n + "'" );
				font_sizes[ i ] = 0;
			}
		}

		setLayout( new BorderLayout() );
		_scrollbar = new ScrollPane();
		add( "Center", _scrollbar );
		_display = new GameWindow( fonts, font_sizes, fg_colors, bg_colors );
		_scrollbar.add( _display );

		// now fix the display's size (why the extra 4? no idea.)
		int sw = _scrollbar.getVScrollbarWidth() + 4;
		_display.init_size( getSize().width, getSize().width - sw, getSize().height, _scrollbar );
		add( "North", _display.get_status_line() );

		// the scrollable area is the size of the visible display window
		_scrollbar.setSize( getSize().width,
				getSize().height - _display.get_status_line().getSize().height );

		try {
			_jetty = new Jetty( _display, new FileInputStream( this.game_file ) );
		} catch ( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}

	}

	public void start() {
		if ( _display != null && _jetty != null ) {
			_thread = new Thread( this );
			_thread.setPriority( Thread.MIN_PRIORITY );
			_display.reset();
			_scrollbar.setScrollPosition( 0, _display.getSize().height );
			validate(); // and fix up the layout
			_thread.start();
			_display.requestFocus();
		}
	}

	public void run() {
		if ( _jetty.load() ) {
			_display.loaded( true );
			_jetty.run();
		}
	}

//	public void stop() {
//		if ( _jetty.load() ) {
//			_thread.stop();
//		}
//	}

	public String getAppletInfo() {
		return (
			"Jetty: a TADS 2 interpreter written in Java\n" +
			"by Dan Shiovitz (dbs@cs.wisc.edu)\nType $$credits for more info" +
			"modified by Stephen Leach (steve@watchfield.com), June 2004"
		);
	}

	public String[][] getParameterInfo() {
		return _params;
	}

	private final String[][] _params = {
//		{"file", "filename", "name of gamefile (with \".gam\", \".gam.zip\", or \".gam.gz\" suffix)"},
		{"statusFont", "name", "font to use for status window"},
		{"statusFontSize", "number", "point size of status window font"},
		{"statusForegroundColor", "color", "foreground color for status window"},
		{"statusBackgroundColor", "color", "background color for status window"},
		{"mainFont", "name", "font to use for main window"},
		{"mainFontSize", "number", "point size of main window font"},
		{"mainForegroundColor", "color", "foreground color for main window"},
		{"mainBackgroundColor", "color", "background color for main window"},
		{"inputFont", "name", "font to use for input line"},
		{"inputFontSize", "number", "point size of input line font"},
		{"inputForegroundColor", "color", "foreground color for input line"},
		{"cursorColor", "color", "color of cursor on input line"},
	};

}


