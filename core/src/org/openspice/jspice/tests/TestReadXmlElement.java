/**
 *	JSpice, n Open Spice interpreter and library.
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
package org.openspice.jspice.tests;

import org.openspice.jspice.datatypes.XmlElement;
import org.openspice.jspice.tools.PrintTools;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.*;
import java.io.*;

import java.io.FileNotFoundException;

public final class TestReadXmlElement {

	public static final void main( final String[] args ) {
		try {
			final XmlElement x = XmlElement.readXmlElement( new java.io.FileInputStream( "poem.xml" ) );
			PrintTools.showln( x );
		} catch ( FileNotFoundException e ) {
			throw new RuntimeException( e );
		}
	}

	static public void printThrow( final Document doc, final OutputStream out ) throws TransformerFactoryConfigurationError, TransformerException {
		final TransformerFactory tf = TransformerFactory.newInstance();
		final Transformer t = tf.newTransformer();
		t.transform( new DOMSource( doc ), new StreamResult( out ) );
	}

	static public void print( final Document doc, final OutputStream out ) {
		try {
			printThrow( doc, out );
		} catch ( final TransformerFactoryConfigurationError ex ) {
			throw new RuntimeException( ex );
		} catch ( final TransformerException ex ) {
			throw new RuntimeException( ex );
		}
	}

}
