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
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.namespace.Location;
import org.openspice.jspice.main.SuperLoader;
import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.vm_and_compiler.VM;

import java.io.File;

public abstract class Loader {

	private final SuperLoader super_loader;
	private NameSpace current_name_space;

	public Loader( final NameSpace current_ns ) {
		assert current_ns != null;
		this.current_name_space = current_ns;
		this.super_loader = current_ns.getNameSpaceManager().getSuperLoader();
	}

	public JSpiceConf getJSpiceConf() {
		return this.super_loader.getJSpiceConf();
	}

	public VM getVM() {
		return this.super_loader.getVM();
	}

	public void setVM( final VM vm ) {
		this.super_loader.setVM( vm );
	}

	public SuperLoader getSuperLoader() {
		return this.super_loader;
	}

	public NameSpace getCurrentNameSpace() {
		return this.current_name_space;
	}

	protected void bind( final Var.Perm perm, final Object contents ) {
		final Location locn = perm.getLocation();
		locn.setValue( contents );
		locn.makeSet();
	}

	public abstract void loadFile( final File file );

	public abstract void autoloadFile( final File file, final Var.Perm perm, final FacetSet facets );

	public abstract Object autoloadFileForValue( final String name, final File file );
	
}
