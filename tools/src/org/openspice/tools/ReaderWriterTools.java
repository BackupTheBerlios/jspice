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
package org.openspice.tools;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ReaderWriterTools {

	public static void readerToWriter( final Reader reader, final Writer writer ) {
		try {
			final char[] cbuf = CharArrayTools.tmpBuffer();
			for (;;) {
				final int n = reader.read( cbuf );
				if ( n == -1 ) break;
				writer.write( cbuf, 0, n );
			}
			writer.close();
			reader.close();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final String readerToString( final Reader reader ) {
		try {
			final StringBuffer b = new StringBuffer();
			final char[] cbuf = CharArrayTools.tmpBuffer();
			for(;;) {
				final int n = reader.read( cbuf );
				if ( n == -1 ) break;
				b.append( cbuf, 0, n );
			}
			return b.toString();
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static final List readerAsCSV( final Reader reader, final String regexp_delimiter ) {
		try {
			final List list = new ArrayList();
			final BufferedReader r = new BufferedReader( reader );
			for(;;) {
				final String s = r.readLine();
				if ( s == null ) break;
				list.add( Arrays.asList( s.split( regexp_delimiter ) ) );
			}
			return list;
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

}
