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
package org.openspice.jspice.main.test;

import junit.framework.TestSuite;
import org.openspice.tools.IntegerTools;
import org.openspice.jspice.datatypes.elements.XmlElement;
import org.openspice.jspice.datatypes.Symbol;

import java.util.TreeMap;

public class TestXMLTags extends SpiceTestBase {

	public TestXMLTags() {
	}

	public static final TestSuite suite() {
		return new TestSuite( TestXMLTags.class );
	}

	public void testAnonCloser() {
		final XmlElement e = new XmlElement( Symbol.make( "alpha" ), new TreeMap(), new Object[] {} );
		assertEquals( one( e ), interpret( "<alpha></>" ) );
	}

}
