package org.openspice.jspice.alert;

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


public class CulpritList {

	final CulpritList allbutlast;
	final Culprit last;

	private CulpritList( final CulpritList _allbutlast, final String _desc, final Object _arg, final boolean _typed, final boolean _print ) {
		this.allbutlast = _allbutlast;
		this.last = new Culprit( _desc, _arg, _typed, _print );
	}

	private CulpritList( final CulpritList allbutlast1, final String desc1, final Object arg1 ) {
		this.allbutlast = allbutlast1;
		this.last = new Culprit( desc1, arg1 );
	}

	public CulpritList() {
		this.last = null;
		this.allbutlast = null;
	}


	public CulpritList culprit( final String desc, final Object arg ) {
		return new CulpritList( this, desc, arg );
	}

	public CulpritList hint( final String hint_text ) {
		return new CulpritList( this, "hint", hint_text, false, true );
	}

	public CulpritList typedCulprit( final String desc, final Object arg ) {
		return new CulpritList( this, desc, arg, true, false );
	}

	private void doOutput() {
		if ( this.allbutlast != null ) {
			this.allbutlast.doOutput();
		}
		if ( this.last != null ) {
			this.last.output();
		}
	}

	void output() {
		this.doOutput();
		Output.println( "" );
		Output.flushAll();
	}

}


