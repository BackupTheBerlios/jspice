package org.openspice.graphics2d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFromFile extends ImageLoadOnDemand {

	private final File file;

	public ImageFromFile( final File _file ) {
		this.file = _file;
	}

	protected Image loadImage() {
		try {
			System.err.println( "loading image: " + this.file );
			return ImageIO.read( this.file );
		} catch ( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}

}
