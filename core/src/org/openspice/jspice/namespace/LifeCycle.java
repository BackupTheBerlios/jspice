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

import org.openspice.jspice.alert.Alert;
import org.openspice.vfs.VItem;

import java.io.File;

abstract class LifeCycle {

	/**
	 * Checks whether or not the location is ready to be referenced.  If the variable is
	 * ready for autoloading, this will trigger the autoload itself.  If wantsValue is true
	 * then the location is going to be accessed and must therefore be fully, properly set.
	 * The return value indicates that the location has fully completed its lifeCycle.
	 * If the location is not ready an AlertException will be thrown.
	 *
	 * @param wantsValue flag indicating location is about to be accessed
	 * @return the location has fully completed its lifecycle.
	 */
	abstract boolean lifeCycleCheck( final boolean wantsValue );

	protected boolean unSet() {
		return true;
	}

	static final class Set extends LifeCycle {

		boolean lifeCycleCheck( final boolean wantsValue ) {
			return true;
		}

		protected boolean unSet() {
			return false;
		}

	}

	//	Not complete - we would have to provide enough stuff for reasonable error-reporting.
	static final class Forward extends LifeCycle {

		final Var.Perm perm;

		public Forward( Var.Perm perm ) {
			this.perm = perm;
		}
		boolean lifeCycleCheck( final boolean wantsValue ) {
			if ( wantsValue ) {
				throw new Alert( "Accessing forward-declared variable before it is defined" ).culprit( "variable", this.perm ).mishap();
			} else {
				return false;
			}
		}

	}

	static final class AutoloadInProgress extends LifeCycle {

		final Var.Perm perm;

		public AutoloadInProgress( Var.Perm perm ) {
			this.perm = perm;
		}

		boolean lifeCycleCheck( final boolean wantsValue ) {
			if ( wantsValue ) {
				throw new Alert( "Accessing variable while autoload is in progress" ).culprit( "variable", this.perm ).mishap();
			} else {
				return false;
			}
		}

	}

	static final class AutoloadReady extends LifeCycle {

		final NameSpace 	namespace;
		final Var.Perm 		perm;
		final VItem			file;
		final FacetSet		facet;

		public AutoloadReady( final NameSpace _namespace, final Var.Perm perm, final FacetSet facet, final VItem file ) {
			this.namespace = _namespace;
			this.perm = perm;
			this.file = file;
			this.facet = facet;
		}

		boolean lifeCycleCheck( final boolean wantsValue ) {
			//	ShortMark location as in-progress.
			final Location locn = this.perm.getLocation();
			locn.lifeCycle = new AutoloadInProgress( this.perm );

			//	Now force auto loading.
			this.namespace.getSuperLoader().autoloadFile( this.file, this.namespace, this.perm, this.facet );

			//	Verify that autoloading worked.
			if ( locn.lifeCycle.unSet() ) {
				new Alert( "Autoload failed to bind variable" ).culprit( "variable", this.perm ).mishap();
			} else if ( !this.perm.getFacetSet().match( facet ) ) {
				new Alert( "Autoload putOne variable in wrong facet" ).
				culprit( "variable", this.perm ).
				culprit( "target facet", facet ).
				culprit( "loaded facet(s)", this.perm.getFacetSet() ).
				mishap();
			}

			return true;
		}

	}

	public static final LifeCycle SET = new Set();
;
}

