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
package org.openspice.jspice.main;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * Code based on article in http://www.randelshofer.ch/oop/javasplash/javasplash.html
 * Title: How to do a fast Splash screen in Java
 */
public class StartSplash {

	public static void main( final String[] args ) {
		final CmdLineOptions cmd = new CmdLineOptions();
		cmd.process( args );
		if ( cmd.splash ) {
			splash_main( cmd );
		} else {
			no_splash_main( cmd );
		}
	}

	private static void no_splash_main( final CmdLineOptions cmd ) {
		try {
			final Class start_class = Class.forName( "org.openspice.jspice.main.StartWithJLine" );
			final Method perform = start_class.getMethod( "main", new Class[] { CmdLineOptions.class } );
			perform.invoke( null, new Object[] { cmd } );
		} catch ( ClassNotFoundException e ) {
			throw new RuntimeException( e );
		} catch ( NoSuchMethodException e ) {
			throw new RuntimeException( e );
		} catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		} catch ( InvocationTargetException e ) {
			throw new RuntimeException( e );
		}
	}

	private static void splash_main( final CmdLineOptions cmd ) {
		//  Read the image data and display the splash screen

		final URL imageURL = StartSplash.class.getResource( "splashdaw.gif" );
		if ( imageURL != null ) {
			final Frame splashFrame = SplashWindow.splash( Toolkit.getDefaultToolkit().createImage( imageURL ) );
			Hooks.READY.addObserver(
				new Observer() {
					public void update( final Observable source, final Object arg ) {
						if ( splashFrame != null ) {
							splashFrame.dispose();
						}
						source.deleteObserver( this );
					}
				}
			);
		}

		no_splash_main( cmd );

	}

}
