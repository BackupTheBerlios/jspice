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

import org.mortbay.http.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.http.handler.*;
import org.mortbay.util.MultiException;
import org.openspice.jspice.boxes.CommandLineBoxServlet;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.vfs.files.FileVFolder;

import java.io.File;
import java.util.Observer;
import java.util.Observable;

/**
 * Scratch code for messing around with Jetty.
 */
public class ServerMain implements Observer {

	HttpServer server;

	public void update( final Observable obs, final Object arg ) {
		try {
			server.stop();
		} catch ( InterruptedException e ) {
		}
	}

	public final HttpServer startUp( final JSpiceConf jconf ) {

		//	Horror show!  We have to set the Jetty logging via the system property!
//		System.setProperty( "LOG_CLASSES", "org.mortbay.util.NullLogSink" );

		// 	Create the server.
		this.server = new HttpServer();

		// 	Create a port listener.
		final SocketListener listener = new SocketListener();
		listener.setPort( 8181 );
		server.addListener( listener );

		// 	Create a context - this aggregates handlers e.g. ServletHandlers.
		final HttpContext context = new HttpContext();
		context.setContextPath( "/mystuff/*" );
		server.addContext( context );

		// 	Create a servlet container.
		final ServletHandler servlets = new ServletHandler();
		context.addHandler( servlets );
		context.setAttribute( "JSpiceConf", jconf );

		// 	Map a servlet onto the container.
		servlets.addServlet( "Dump", "/Dump/*", "org.mortbay.servlet.Dump" );
		final ServletHolder holder = servlets.addServlet( "Hello", "/hello/*", CommandLineBoxServlet.class.getName() );



		// Serve static content from the context.
		context.setResourceBase( new File( ((FileVFolder)jconf.getHome()).getFile(), "tut" ).getPath() + "/" );
		context.addHandler( new ResourceHandler() );

		// Start the http server.
		try {
			server.start();
		} catch ( MultiException e ) {
			throw new RuntimeException( e );
		}

		return server;
	}

	public void start( final JSpiceConf jconf) {
		this.startUp( jconf );
		Main.SHUTDOWN.addObserver( this );		//	Horrible temporary hack.
	}

}

