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


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;


public final class JettyWindow {

//    { "statusFont", "name", "font to use for status window" },
//    { "statusFontSize", "number", "point size of status window font" },
//    { "statusForegroundColor", "color", "foreground color for status window" },
//    { "statusBackgroundColor", "color", "background color for status window" },
//    { "mainFont", "name", "font to use for main window" },
//    { "mainFontSize", "number", "point size of main window font" },
//    { "mainForegroundColor", "color", "foreground color for main window" },
//    { "mainBackgroundColor", "color", "background color for main window" },
//    { "inputFont", "name", "font to use for input line" },
//    { "inputFontSize", "number", "point size of input line font" },
//    { "inputForegroundColor", "color", "foreground color for input line" },
//    { "cursorColor", "color", "color of cursor on input line" },

	static public void run( final File game_file ) {
		final Map map = new TreeMap();
//		map.putOne( "statusFont", );
//		map.putOne( "statusFontSize", );
//		map.putOne( "statusForegroundColor", );
//		map.putOne( "statusBackgroundColor", );
//		map.putOne( "mainFont", );
//		map.putOne( "mainFontSize", );
//		map.putOne( "mainForegroundColor", );
//		map.putOne( "mainBackgroundColor", );
//		map.putOne( "inputFont", );
//		map.putOne( "inputFontSize", );
//		map.putOne( "inputForegroundColor", );
//		map.putOne( "cursorColor", );

		run( game_file, map );
	}

	static public void run( final File game_file, final Map map ) {
		final Frame frame = new Frame( "MyApplet" );
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent event ) {
					frame.dispose();
				}
			}
		);
		final JettyStandAlone stand_alone = new JettyStandAlone( game_file, map );
		stand_alone.setSize( 256, 128 );
		frame.add( stand_alone );
		frame.pack();
		stand_alone.init();
		frame.setSize( 256, 256 + 20 ); // add 20, seems enough for the Frame title,
		frame.show();
	}

}