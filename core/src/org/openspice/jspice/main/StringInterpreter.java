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
package org.openspice.jspice.main;

import org.openspice.jspice.conf.JSpiceConf;
import org.openspice.jspice.vm_and_compiler.VM;

import java.io.StringReader;
import java.util.List;

public class StringInterpreter {

	JSpiceConf jspice_conf;

	public StringInterpreter() {
		this.jspice_conf = new JSpiceConf();
		this.jspice_conf.setIsDebugging( true );		//	todo: This is far too crude.
	}

	public List interpret( final String s ) {
		final SuperLoader super_loader = new SuperLoader( this.jspice_conf );
		final Interpreter interpreter = new Interpreter( super_loader.getNameSpace( "spice.interactive_mode" ) );
		interpreter.interpret(
			"a string",
			new StringReader( s ),
			null
		);
		final VM vm = interpreter.getVM();
		return vm.getAllResults();
	}



}
