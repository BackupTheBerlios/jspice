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
package org.openspice.browser_control;

import java.io.IOException;

/*
This code based on Steven Spencer's "Java Tip 66: Control browsers from your Java application"
from <http://www.javaworld.com/javaworld/javatips/jw-javatip66.html>.

Summary:
With a little platform-specific Java code, you can easily use your system's default browser to
display any URL. Instead of purchasing a Java HTML widget for a help system, just use this class
to control Netscape or Internet Explorer from Windows or Unix.

Readers Ryan Stevens and Mark Weakly sent in modifications for Mac OS X and 9 respectively.  Their
suggestions have been heavily modified and incorporated into the main code:

From Ryan Stevens:
import com.apple.mrj.MRJFileUtils;
import java.io.*;

class Open {
String tools = "http://www.yourpage.com/";

public static void main(String[] args) { new Open(); }

Open() {
    try {
        MRJFileUtils.openURL(tools);
    } catch (IOException ex) {}
    }
}

from Mark Weakly:
You can launch the command line stuff on Mac similar to Unix (MacOS 8 and 9), except you must place the
command-line tokens into a java.lang.String array. The array gets passed to the process exec() method.
For example:
   String[] commandLine = { "netscape", "http://www.javaworld.com/" };
   Process process = Runtime.getRuntime().exec(commandLine);
*/

/*
List of typical os.name values:
	os.name=            "Windows 95"
	os.name=            "Windows CE"
	os.name=            "Windows NT"

	os.name=            "Mac OS"
	os.name=            "MacOS"

	os.name=            "Linux"
	os.name=            "Solaris"
	os.name=            "OS/2"

	os.name=            "MPE/iX"
	os.name=            "HP-UX"
	os.name=            "AIX"
	os.name=            "Solaris"
	os.name=            "FreeBSD"
	os.name=            "Irix"
	os.name=            "Digital Unix"

	os.name=            "NetWare 4.11"
*/

/**
 * A simple, static class to display a URL in the system browser.
 * <p/>
 * <p/>
 * <p/>
 * Under Unix, the system browser is hard-coded to be 'netscape'.
 * Netscape must be in your PATH for this to work.  This has been
 * tested with the following platforms: AIX, HP-UX and Solaris.
 * <p/>
 * <p/>
 * <p/>
 * Under Windows, this will bring up the default browser under windows,
 * usually either Netscape or Microsoft IE.  The default browser is
 * determined by the OS.  This has been tested under Windows 95/98/NT.
 * <p/>
 * <p/>
 * <p/>
 * Examples:
 * <p/>
 * <p/>
 * BrowserControl.displayURL("http://www.javaworld.com")
 * <p/>
 * BrowserControl.displayURL("file://c:\\docs\\index.html")
 * <p/>
 * BrowserContorl.displayURL("file:///user/joe/index.html");
 * <p/>
 * <p/>
 * Note - you must include the tools type -- either "http://" or
 * "file://".
 */
public abstract class BrowserControl {

	/**
	 * Display a file in the system browser.  If you want to display a
	 * file, you must include the absolute path name.
	 *     
	 *
	 * @param url the file's tools (the tools must start with either "http://" or * "file://").    
	 */
	public abstract void displayURL( String url ) throws IOException;


	public static final BrowserControl build( String os_name, String browser_name ) {
		if ( os_name == null ) os_name = "unix";
		os_name = os_name.toLowerCase();
		if ( browser_name == null ) browser_name = "netscape";	//	will be ignored on more advanced platforms.
		if ( os_name.startsWith( "windows" ) ) {
			return new WindowsBrowserControl();
		} else if ( os_name.equals( "mac os x" ) ) {
			return new MacOSXBrowserControl();
		} else if ( os_name.startsWith( "mac" ) ) {
			return new MacOSClassicBrowserControl();
		} else {
			final GenericUnixBrowserControl g = new GenericUnixBrowserControl();
			g.unix_path = browser_name;
			return g;
		}
	}

	public static final BrowserControl build() {
		return build( System.getProperty(  "os.name" ), null );
	}

	static class MacOSXBrowserControl extends BrowserControl {

		public void displayURL( final String url ) throws IOException {
			Runtime.getRuntime().exec( new String[] { "/usr/bin/open", url } );
		}
	}

	static class MacOSClassicBrowserControl extends BrowserControl {

		//	Not sure about making this work yet.
		final String browser = "netscape";

		public void displayURL( final String url ) throws IOException {
			Runtime.getRuntime().exec( new String[] { this.browser, url } );
		}

	}

	static class GenericUnixBrowserControl extends BrowserControl {

		// The default browser under unix.
		String unix_path = "netscape";
		// The flag to display a tools.
		final String unix_flag = "-remote";
		//	Part of the argument needed.
		final String unix_arg = "openURL";

		public void displayURL( final String url ) throws IOException {
			// Under Unix, Netscape has to be running for the "-remote"
			// command to work.  So, we try sending the command and
			// check for an exit value.  If the exit command is 0,
			// it worked, otherwise we need to start the browser.
			// 		'netscape -remote openURL(http://www.javaworld.com)'
			final Process p = Runtime.getRuntime().exec( new String[] { unix_path, unix_flag, unix_arg + "(" + url + ")" } );
			try {
				// wait for exit code -- if it's 0, command worked,
				// otherwise we need to start the browser up.
				int exitCode = p.waitFor();
				if ( exitCode != 0 ) {
					// Command failed, start up the browser
					// 		'netscape http://www.javaworld.com'
					Runtime.getRuntime().exec( new String[] { unix_path, url } );
				}
			} catch ( InterruptedException x ) {
				//	I am unsure about this - looks wrong to me (Steve Leach, July 04).
				throw new RuntimeException( x );
			}
		}
	}

	static class WindowsBrowserControl extends BrowserControl {

		// Used to identify the windows platform.
		private static final String WIN_ID = "Windows";
		// The default system browser under windows.
		private static final String WIN_PATH = "rundll32";
		// The flag to display a tools.
		private static final String WIN_FLAG = "tools.dll,FileProtocolHandler";

		public void displayURL( final String url ) throws IOException {
			// cmd = 'rundll32 tools.dll,FileProtocolHandler http://...'
			final String cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
			Process p = Runtime.getRuntime().exec( cmd );
		}

	}




}

