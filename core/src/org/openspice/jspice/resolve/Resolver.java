//	Roles of name resolution phase.
//		*	To assert the default facet-set.

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
package org.openspice.jspice.resolve;

import org.openspice.jspice.expr.*;
import org.openspice.jspice.expr.iterators.ExprIterator;
import org.openspice.jspice.expr.cases.*;
import org.openspice.jspice.namespace.*;
import org.openspice.jspice.alert.Alert;
import org.openspice.jspice.main.SuperLoader;
import org.openspice.vfs.VFolder;


/**
 * The entry point is Resolver.resolve.  This works by modifying the
 * tree in-place.
 */
public final class Resolver extends ExprVisitor {

	final SuperLoader super_loader;
    final Env env;
	final NameSpace name_space;

	/**
	 * The main entry point for  the Resolver class.  It resolves
	 * the symbolic names against the internal variable-reference
	 * objects.
	 * @param ns current namespace
	 * @param e expression to resolve.
	 */
	public static final void resolve( final SuperLoader _super_loader, final NameSpace ns, final Expr e ) {
		new Resolver( _super_loader, ns ).visit( e );
	}

	private Resolver( final SuperLoader _super_loader, final NameSpace ns ) {
		this( _super_loader, Env.GLOBAL, ns );
	}
	
	private Resolver( final SuperLoader _super_loader, final Env _env, final NameSpace _ns ) {
		assert _env != null && _ns != null && _super_loader != null;
		this.super_loader = _super_loader;
		this.env = _env;
		this.name_space = _ns;
	}
	
	public Object visitExpr( final Expr e, final Object arg  ) {
		for ( ExprIterator it = e.getAllKids(); it.hasNext(); ) {
			it.next().visit( this );
		}
		return null;
	}
	
	public Object visitBlockExpr( final BlockExpr block, final Object arg ) {
		Resolver res;
		if ( this.env.isGlobal() ) {
			//
			//	This is a really disgusting hack.  Should think
			//	much more carefully about the wisdom of in-place
			//	updates.  Either legitimize or throw out.
			//
			//	Note the fact that LambdaExpr's get updated during
			//	name-resolution is another serious compromise.
			//
			block.updateToLambdaExpr();
			res = this;
		} else {
			res = (
				new Resolver(
					this.super_loader,
					new LocalEnv( this.env ),
					this.name_space
				)
			);
		}
		block.getFirst().visit( res );
		return null;
	}

	
	public Object visitHelloExpr( final HelloExpr hello_expr, final Object arg  ) {
		assert hello_expr != null;
		if ( hello_expr.getFacets() == null ) {
			hello_expr.setFacets( this.name_space.getDefaultFacets() ) ;
		}
		this.env.hello( hello_expr, this.name_space );
		return null;
		
	}

	public Object visitLambdaExpr( final LambdaExpr lambda, final Object arg  ) {
		//System.out.println( "Resolving LAMBDA" );
		final Resolver res = (
			new Resolver(
				this.super_loader,
				new LocalEnv( this.env, lambda ),
				this.name_space
			)
		);
		
		lambda.getBody().visit( res );
		lambda.allocatePositions();
		return null;
	}
	
	public Object visitAnonExpr( final AnonExpr anon, final Object arg  ) {
		return null;
	}
	
    public Object visitNamedExpr( final NamedExpr nme, final Object arg  ) {
		this.env.bind( nme, this.name_space  );
		return null;
	}

	public Object visitImportExpr( final ImportExpr import_expr, final Object arg ) {
		final NameSpace ns = this.name_space;
		final String nickname = import_expr.getNickname();
		final String ipkg_name = import_expr.getPkgName();
		NameSpace ipkg = ns.getNameSpaceManager().get( ipkg_name );
		if ( ipkg == null ) {
			//	Attempt to autoload.
			final VFolder ipkg_folder = this.locatePackage( ipkg_name );
			if ( ipkg_folder == null ) {
				new Alert( "Unknown package" ).culprit( "package", ipkg_name ).mishap();
			} else {
				ipkg = this.loadPackage( ipkg_name, ipkg_folder );
			}
		}
		final boolean qflag = import_expr.isQualified();
		final FacetSet facets = FacetSet.PUBLIC;
		final FacetSet into = FacetSet.NONE;
		ns.addImport( nickname, qflag, ipkg, facets, into );
		return null;
	}

	private final VFolder locatePackage( final String ipkg_name ) {
		return this.super_loader.getJSpiceConf().locatePackage( ipkg_name );
	}

	private final NameSpace loadPackage( final String ipkg_name, final VFolder ipkg_folder ) {
		return this.super_loader.getNameSpaceManager().load_package( ipkg_name, ipkg_folder );
	}

}
