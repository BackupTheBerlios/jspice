package org.openspice.graphics2d;

import java.awt.*;
import java.awt.event.*;

public class ApplicationFrame extends Frame {

	public ApplicationFrame() {
		this( "ApplicationFrame v1.0" );
	}

	public ApplicationFrame( final String title ) {
		super( title );
		this.createUI( 500, 400 );
	}

	protected void createUI( final int width, final int height ) {
		this.setSize( width, height );
		this.center();

		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( final WindowEvent e ) {
					ApplicationFrame.this.dispose();
					System.exit( 0 );
				}
			}
		);
	}

	public void center() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension frameSize = getSize();
		final int x = ( screenSize.width - frameSize.width ) / 2;
		final int y = ( screenSize.height - frameSize.height ) / 2;
		this.setLocation( x, y );
	}

}