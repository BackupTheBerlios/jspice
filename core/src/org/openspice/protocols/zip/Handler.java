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
package org.openspice.protocols.zip;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * To enable this protocol add this to the VM options.
 * -Djava.protocol.handler.pkgs=org.openspice.protocols
 * Remember to add to bin/jspice & bin/jspice-plain.
 */
public class Handler extends URLStreamHandler {

	static class DummyConnection extends URLConnection {

		public DummyConnection( final URL url ) {
			super( url );
		}

		public void connect() throws IOException {
			throw new RuntimeException( "tbd" ); 	//	todo: to be defined
		}

		public InputStream getInputStream() throws IOException {
			return super.getInputStream();    //To change body of overridden methods use File | Settings | File Templates.
		}

		public Object getContent() throws IOException {
			return super.getContent();    //To change body of overridden methods use File | Settings | File Templates.
		}

	}

	protected URLConnection openConnection( final URL url ) throws IOException {
		return new DummyConnection( url );
	}

}
