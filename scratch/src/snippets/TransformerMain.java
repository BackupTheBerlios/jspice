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
package snippets;

import org.openspice.jspice.datatypes.elements.XmlElement;

import javax.xml.transform.*;

/**
 * Scratch code for figuring out how to drive the java.xml.transform stuff.
 */
public class TransformerMain {

	Source xmlSource;
	Result outputTarget;
	XmlElement start_here;

	public final void main() {



		final TransformerFactory f = TransformerFactory.newInstance();
		try {
			final Transformer t = f.newTransformer();
			t.transform( xmlSource, outputTarget );

		} catch ( TransformerException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final void main( final String[] args ) {
		new TransformerMain().main();
	}

}
