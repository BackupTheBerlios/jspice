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
package org.openspice.vfs.test;

import org.openspice.vfs.VFolder;
import org.openspice.vfs.VFile;
import org.openspice.vfs.VVolume;
import org.openspice.vfs.zip.ZipVVolume;
import org.openspice.vfs.file.FileVFolder;
import org.openspice.vfs.file.FileVVolume;
import org.openspice.jspice.main.test.SpiceTestBase;
import org.openspice.jspice.conf.JSpiceConf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipFile;

import sun.misc.Resource;
import junit.framework.TestSuite;

public class VFSTest extends SpiceTestBase {

	public VFSTest() {
	}

	public static final TestSuite suite() {
		return new TestSuite( VFSTest.class );
	}

//	public static final void scan( final VFile vfile ) {
//		System.out.println( "file: " + vfile.getNam() + "." + vfile.getExt() );
//	}
//
//	public static final void scan( final VFolder vfolder ) {
//		System.out.println( "folder: " + vfolder.getFullName() );
//		for ( Iterator it = vfolder.listVFolders().iterator(); it.hasNext(); ) {
//			final Object vfobj = it.next();
////			System.out.println( "vfobj = " + vfobj );
//			final VFolder vf = (VFolder)vfobj;
//			scan( vf );
//		}
//		for ( Iterator it = vfolder.listVFiles().iterator(); it.hasNext(); ) {
//			final VFile vf = (VFile)it.next();
//			scan( vf );
//		}
//	}
//
//	public static final void main( final String[] args ) {
//		final VFolder folder = new FileVVolume( new File( "/tmp" ) ).getRootVFolder();
//		scan( folder );
//	}

	public void testFileFVS() {
		final JSpiceConf jconf = new JSpiceConf();
		final VFolder home = jconf.getHome();
		assertEquals( home.getVFile( "jspice", "conf" ).getNam(), "jspice" );
	}

	public void testZipVFS() throws IOException {
		assertTrue( "check zip file exists", new File( "jspice.zip").exists() );
		final VVolume vvol = new ZipVVolume( new ZipFile( "jspice.zip" ) );
		final VFile vfile = vvol.getVFileFromPath( ".jspice/jspice.conf" );
		assertTrue( "vfile not null", vfile != null );
		assertEquals( "check nam", vfile.getNam(), "jspice" );
		assertEquals( "check ext", vfile.getExt(), "conf" );
	}
}
