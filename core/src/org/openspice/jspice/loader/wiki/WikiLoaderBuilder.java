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
package org.openspice.jspice.loader.wiki;

import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.loader.ValueLoader;
import org.openspice.jspice.loader.ValueLoaderBuilder;
import org.openspice.jspice.datatypes.elements.XmlElement;
import org.openspice.jspice.datatypes.elements.XmlElement;
import org.openspice.jspice.datatypes.elements.XmlElementHandler;
import org.openspice.vfs.VFile;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;

public class WikiLoaderBuilder extends ValueLoaderBuilder {


	public ValueLoader newValueLoader( final NameSpace current_ns ) {
		return new WikiLoader( this, current_ns );
	}

	public Object loadValueFromVFile( final VFile file ) {
		try {
			final XmlElementHandler h = new XmlElementHandler();
			new WikiParser( this.getJSpiceConf(), h ).parse( new BufferedReader( file.readContents() ) );
			return h.giveItUp();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		} catch ( SAXException e ) {
			throw new RuntimeException( e );
		}
	}
	
}
