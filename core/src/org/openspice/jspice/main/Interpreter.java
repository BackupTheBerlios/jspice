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

import org.openspice.jspice.expr.Expr;
import org.openspice.jspice.tools.PrintTools;
import org.openspice.jspice.resolve.Resolver;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.alert.AlertException;
import org.openspice.jspice.parse.HoleFinder;
import org.openspice.jspice.parse.SpiceParser;
import org.openspice.jspice.vm_and_compiler.Petrifier;
import org.openspice.jspice.vm_and_compiler.Pebble;
import org.openspice.jspice.vm_and_compiler.VM;
import org.openspice.jspice.namespace.NameSpace;
import org.openspice.jspice.namespace.Var;
import org.openspice.jspice.namespace.FacetSet;
import org.openspice.jspice.lift.Lift;
import org.openspice.jspice.loader.Loader;
import org.openspice.vfs.VItem;
import org.openspice.vfs.VFile;

import java.io.*;
import java.util.List;

public final class Interpreter extends Loader {

	public Interpreter( final NameSpace current_ns ) {
		super( current_ns );
		this.getJSpiceConf();
	}

	private boolean isDebugging() {
		return this.getJSpiceConf().isDebugging();
	}

	public void interpret( final String prompt ) {
		this.interpret( "stdin", new InputStreamReader( System.in ), prompt );
	}
		
	public void interpret( final VFile file ) {
		this.interpret( file.toString(), file.readContents() );
	}

	public final void loadVFile( final VFile file ) {
		this.interpret( file );
	}

	public final void autoloadVFile( final VFile file, final Var.Perm perm, final FacetSet facets ) {
		this.interpret( file );
	}

	public Object autoloadFileForValue( String name, VItem file ) {
		throw Alert.unreachable();
	}

	public void interpret( final String origin, final Reader reader ) {
		this.interpret( origin, reader, null );
	}

	private Pebble makePebble( Expr e, final SpiceParser parser, final Petrifier petrifier ) {
		if ( this.isDebugging() ) {
			System.out.println( "Expression read" );
			e.showTree();
		}

		if ( parser.getHolesUsed() ) {
			//	We did use holes!
			e = HoleFinder.makeTransform().transform( e );
			if ( this.isDebugging() ) {
				System.out.println( "Holes used.  Filling in holes ..." );
				e.showTree();
			}
		}

		Resolver.resolve( this.getSuperLoader(), this.getCurrentNameSpace(), e );
		if ( this.isDebugging() ) {
			System.out.println( "Resolving done" );
		}

		//	Sort out type-3 and partapplys.
		e = Lift.lift( e );
		if ( this.isDebugging() ) {
			System.out.println( "Lifting done" );
			e.showTree();
		}

		final Pebble p = petrifier.petrify( e );
		if ( this.isDebugging() ) {
			System.out.println( "Petrified - prepare for execution" );
		}

		return p;
	}

	private void oneExpr( final SpiceParser parser, final Petrifier petrifier, final boolean show ) {
		parser.clearHolesUsed();
		parser.clearTmp();
		Expr e = parser.readOptExpr();
		if ( e != null && ( parser.tryReadToken( ";" ) != null || parser.peekToken() == null ) ) {
			final Pebble p = this.makePebble( e, parser, petrifier );
			if ( show ) {
				this.getVM().clearAll();
				this.getVM().run( p );
				this.getVM().showAll();
				this.getVM().saveAllResults();
				this.getVM().clearAll();
			} else {
				this.getVM().run( p );
			}
		} else if ( e != null || parser.tryReadToken( ";" ) == null ) {
			//	WRONG parser.peekToken might be null
			new Alert( "Unexpected token" ).culprit( "token", parser.peekToken() ).mishap( 'E');
		}
	}

	public void interpret( final String origin, final Reader reader, final String prompt ) {
		final Petrifier petrifier = new Petrifier();
		SpiceParser parser = new SpiceParser( this, origin, reader, prompt );
		for (;;) {
//		while ( parser.peekToken() != null ) {
			boolean reset = false;
			try {
				if ( parser.peekToken() == null ) break;	//	Horrible.  But exceptions need to be caught.
				this.oneExpr( parser, petrifier, true );
			} catch ( AlertException ex ) {
				//	The problem is reported so force continuation.
				if ( this.isDebugging() ) {
					ex.printStackTrace();
				}
				reset = true;
			} catch ( final Throwable ex ) {
				if ( this.isDebugging() ) {
					ex.printStackTrace();
				}
				new Alert( ex, "Uncaught internal exception", "This exception should have been reported via a mishap" ).
				culprit( "exception type", ex.getClass().getName() ).
				internal_warning();
				reset = true;
			}
			if ( reset ) {
				parser = new SpiceParser( this, origin, reader, prompt );
				reset = false;
				PrintTools.println( "JSpice ready" );
			}
		}
	}

	public void evaluate( final List list, final String origin, final Reader reader, final String prompt ) {
		final VM saved = this.getVM();
		this.setVM( new VM( saved.getJSpiceConf() ) );
		final Petrifier petrifier = new Petrifier();
		SpiceParser parser = new SpiceParser( this, origin, reader, prompt );
		while ( parser.peekToken() != null ) {
			this.oneExpr( parser, petrifier, false );
		}
		this.getVM().copyTo( list );
		this.setVM( saved );
	}

}
 