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

package org.openspice.jspice.lexis;

import org.openspice.jspice.alert.Alert;

import java.io.LineNumberReader;
import java.io.Reader;
import java.io.IOException;

public class SourceCore {
    private int last_char_read = '\n';
    private String print_name;
    private String prompt;
    private LineNumberReader reader;

    SourceCore( final String _origin, final Reader _reader, final String _prompt ) {
        this.print_name = _origin;
        this.reader = new LineNumberReader( _reader );
        this.prompt = _prompt;
    }

    public String getPrintName() {
        return this.print_name;
    }

    public int getLineNumber() {
        return this.reader.getLineNumber();
    }

    private void forcePrompt() {
        if ( prompt != null ) {
            System.out.print( prompt );
            System.out.flush();
        }
    }

	private void clearPrompt() {
		if ( prompt != null ) {
			System.out.println( "" );
		}
	}

    public int rawReadInt() {
        try {
            if ( this.last_char_read == '\n' ) forcePrompt();
            if ( ( this.last_char_read = this.reader.read() ) == -1 ) clearPrompt();
			return this.last_char_read;
        } catch ( IOException ex ) {
            new Alert( "Could not read from input source" ).culprit( "cause", ex.getMessage() ).mishap( 'T' );
        }
    	Alert.unreachable();
        return -1;					//	sop
	}

}
