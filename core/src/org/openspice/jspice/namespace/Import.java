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

package org.openspice.jspice.namespace;

import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.NameSpace;

public class Import {
    String 			nickname;
    boolean 		is_qualified;
    NameSpace 		name_space;
    FacetSet 		facet_set;
    FacetSet 		into_facet_set;
	
	Import(
		String 			_nickname,
		boolean 		_is_qualified,
		NameSpace 		_name_space,
		FacetSet 		_facet_set,
		FacetSet 		_into_facet_set
	) {
		this.nickname = _nickname;
		this.is_qualified = _is_qualified;
		this.name_space = _name_space;
		this.facet_set = _facet_set;
		this.into_facet_set = _into_facet_set.addFacet( "<imported>" );
	}
	
	NameSpace getNameSpace() {
		return this.name_space;
	}
	
	FacetSet getFacetSet() {
		return this.facet_set;
	}

	FacetSet getIntoFacetSet() {
		return this.into_facet_set;
	}
}
