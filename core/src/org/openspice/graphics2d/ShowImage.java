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
package org.openspice.graphics2d;

import java.awt.*;

public class ShowImage extends Component {

	private Image image;

	public ShowImage( final Image _image ) {
		this.image = _image;
	}

	public void main() {
		final ApplicationFrame f = new ApplicationFrame( "ShowOff v1.0" );
		f.setLayout( new BorderLayout() );
		f.add( this, BorderLayout.CENTER );
		f.setSize( f.getPreferredSize() );
		f.center();
		f.setResizable( false );
		f.setVisible( true );
	}

	public void paint( final Graphics g ) {
		final Graphics2D g2 = (Graphics2D)g;
//		final MediaTracker t = new MediaTracker( this );
//		t.addImage( this.image, MediaTracker.COMPLETE );
//		try {
//			t.waitForAll();
//		} catch ( InterruptedException ex ) {
//			throw new RuntimeException( ex );
//		}
		g2.drawImage( this.image, 0, 0, null );
	}

	public Dimension getPreferredSize() {
		return new Dimension( image.getWidth( null ), image.getHeight( null ) );
	}

}
