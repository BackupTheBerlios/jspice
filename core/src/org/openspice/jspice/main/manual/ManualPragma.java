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


import java.util.Iterator;
import java.util.List;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ManualPragma {

	private void sorry( final String topic ) {
		System.out.println( "Sorry, cannot find any help on '" + topic + "'" );
	}

	private void printTopic( final SearchResult r ) {
		final SearchPhrase hint = r.searchHint();
		final ManualPage page = r.manualPage();
		System.out.println( "found in " + hint );
		System.out.println( page.getContentsAsString() );
	}

	public void help( final Manual manual, final SearchPhrase t ) {
		final List results = manual.search( t );			//	List< SearchResult >
		final int size = results.size();
		if ( size == 0 ) {
			this.sorry( t.toString() );
		} else if ( size == 1 ) {
			this.printTopic( (SearchResult)results.get( 0 ) );
		} else {
			System.out.println( "Several matches for '" + t + "' found.  Please choose :-" );
			int n = 1;
			for ( Iterator it = results.iterator(); it.hasNext(); n++ ) {
				final SearchResult r = (SearchResult)it.next();
				final SearchPhrase hint = r.searchHint();
				final String summary = r.manualPage().getOneLineSummary();
				System.out.println( "[" + n + "] " + hint );
				System.out.println( "  " + summary );
			}
			System.out.print( "Please enter the number of your choice: " );
			System.out.flush();
			try {
				final String line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
				try {
					final int choice = Integer.parseInt( line );
					this.printTopic( (SearchResult)results.get( choice - 1 ) );
				} catch ( NumberFormatException e ) {
					System.out.println( "not a valid choice - ignored" );
				}
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}
	}

}
