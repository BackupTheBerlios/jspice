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
package org.openspice.jspice.main.manual;

import org.openspice.tools.FileTools;
import org.openspice.tools.ReaderWriterTools;
import org.openspice.vfs.VFile;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FileManualPage implements ManualPage {

	final VFile file;

	public FileManualPage( VFile file ) {
		this.file = file;
	}

	public static final Pattern summary = Pattern.compile( "^Summary:(.*)$" );
	public static final String fail = "No summary available";

	public String getOneLineSummary() {
		try {
			String first_line = null;
			final BufferedReader reader = new BufferedReader( this.file.readContents() );
			for (;;) {
				String line = reader.readLine();
				if ( line == null ) break;
				line = line.trim();
				if ( first_line == null ) first_line = line;
				final Matcher matcher = summary.matcher( line );
				if ( matcher.find() ) {
					return matcher.group( 1 );
				}
			}
			//	Desperation stakes.
			return first_line == null ? fail : first_line;
		} catch ( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public String getContentsAsString() {
		return ReaderWriterTools.readerToString( this.file.readContents() );
	}

}
