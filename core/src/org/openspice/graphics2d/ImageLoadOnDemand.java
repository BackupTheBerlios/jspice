package org.openspice.graphics2d;

import java.lang.ref.SoftReference;
import java.awt.*;

public abstract class ImageLoadOnDemand extends ImageDelegate {

	private SoftReference image_ref = null;

	abstract protected Image loadImage();

	private Image loadThenGetImage() {
		this.image_ref = new SoftReference( this.loadImage() );
		return this.getImage();
	}

	protected Image getImage() {
		if ( image_ref == null ) {
			return this.loadThenGetImage();
		} else {
			final Image image = (Image)this.image_ref.get();
			if ( image != null ) {
				return image;
			} else {
				return this.loadThenGetImage();
			}
		}
	}

}
