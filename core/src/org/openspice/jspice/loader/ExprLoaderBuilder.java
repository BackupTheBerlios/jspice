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
package org.openspice.jspice.loader;

import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.Interpreter;
import org.openspice.vfs.VFile;

import java.util.ArrayList;

public class ExprLoaderBuilder extends ObjectLoaderBuilder {


	static class ExprLoader extends ObjectLoader {

		private ExprLoader( final NameSpace ns ) {
			super( ns );
		}

		public Object fileValueFromVFile( final String name, final VFile file ) {
			final Interpreter intr = new Interpreter( this.getCurrentNameSpace() );
			final ArrayList answer = new ArrayList();
			intr.evaluate( answer, file.getUniqueID().toString(), file.readContents(), null );
			if ( answer.isEmpty() ) {
				new Alert( "No results from expression file" ).culprit( "file", file ).mishap();
			} else if ( answer.size() > 1 ) {
				new Alert( "Too many results from expression file" ).culprit( "file", file ).culprit( "results list", answer ).mishap();
			}
			return answer.get( 0 );
		}

	}

	public ObjectLoader newObjectLoader( NameSpace current_ns ) {
		return new ExprLoader( current_ns );
	}

}
