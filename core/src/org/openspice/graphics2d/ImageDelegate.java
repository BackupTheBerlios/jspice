package org.openspice.graphics2d;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public abstract class ImageDelegate extends Image {

	protected abstract Image getImage();

	public int getWidth( final ImageObserver imageObserver ) {
		return this.getImage().getWidth( imageObserver );
	}

	public int getHeight( final ImageObserver imageObserver ) {
		return this.getImage().getHeight( imageObserver );
	}

	public ImageProducer getSource() {
		return this.getImage().getSource();
	}

	public Graphics getGraphics() {
		return this.getImage().getGraphics();
	}

	public Object getProperty( final String s, final ImageObserver imageObserver ) {
		return this.getImage().getProperty( s, imageObserver );
	}

	public Image getScaledInstance( final int i, final int i1, final int i2 ) {
		return this.getImage().getScaledInstance( i, i1, i2 );
	}

	public void flush() {
		this.getImage().flush();
	}


	
}
