package org.openspice.jspice.datatypes;
import org.openspice.jspice.lib.MapLib;
import org.openspice.jspice.lib.CastLib;
import org.openspice.jspice.tools.ListTools;
import org.openspice.jspice.tools.Consumer;
import org.openspice.jspice.datatypes.SpiceObject;
import org.openspice.jspice.built_in.inspect.FieldAdder;

import java.util.*;

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

public class Symbol extends SpiceObject implements Comparable {

    private final String symbol;

    private Symbol( final String _symbol ) {
        this.symbol = _symbol.intern();
    }

	public int compareTo( final Object obj ) {
		return this.symbol.compareTo( ((Symbol)obj).symbol );
	}

    public String getInternedString() {
        return this.symbol;
    }

    private static WeakHashMap table = new WeakHashMap();

	public static Symbol make( String sym ) {
        sym = sym.intern();
        Symbol x = (Symbol)table.get( sym );
        if ( x == null ) {
            x = new Symbol( sym );
            table.put( sym, x );
        }
        return x;
    }

	public void addInstanceFields( FieldAdder adder ) {
		adder.add( "string", this.symbol );
	}

	public void showTo( final Consumer cuchar ) {
		cuchar.out( '`' );
		cuchar.outString( this.symbol );
		cuchar.out( '`' );
	}
	
	public void printTo( final Consumer cuchar ) {
		cuchar.outString( this.symbol );
	}

	public List convertToList() {
		return ListTools.convertTo( this.symbol );
	}
	
	public SpiceObject convertFromList( final List list ) {
		return make( (String)ListTools.convertFrom( list, "" ) );
	}
	
	public Map convertToMap() {
		return MapLib.convertTo( this.symbol );
	}
	
	public SpiceObject convertFromMap( final Map map ) {
		return make( (String)MapLib.convertFrom( map, "" ) );
	}

}
