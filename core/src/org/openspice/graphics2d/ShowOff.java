package org.openspice.graphics2d;

import java.awt.*;
//import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
//import java.util.Random;

import com.sun.image.codec.jpeg.*;

import javax.imageio.ImageIO;


public class ShowOff extends Component {

	private ImageDelegate mImage;
//	private Font mFont;
//	private String mMessage;
//	private int mSplit;
//	private TextLayout mLayout;

	public ShowOff( final String filename ) {
		// The image is loaded either from this
		//   default filename or the first command-
		//   line argument.
		// The second command-line argument specifies
		//   what string will be displayed. The third
		//   specifies at what point in the string the
		//   background color will change.
//
//		//	Program parameters.
//		String filename = null;
//		String message = "Java2D";
//		int split = 4;
//
//		if ( args.length <= 0 ) {
//			System.err.println( "java ShowOff [filename [message [split]]]" );
//		} else {
//			if ( args.length > 0 ) filename = args[ 0 ];
//			if ( args.length > 1 ) message = args[ 1 ];
//			if ( args.length > 2 ) split = Integer.parseInt( args[ 2 ] );
//
//		}



		try {
			final Image image = ImageIO.read( new File( "example.jpg" ) );
			this.mImage = (
				new ImageDelegate() {
					public Image getImage() { return image; }
				}
			);
		this.setSize( mImage.getWidth( null ), mImage.getHeight( null ) );
		} catch ( Exception _ ) {
			throw new RuntimeException( _ );
		}



		/*try {
			// Get the specified image.
			final InputStream in = new FileInputStream( filename );
			assert in != null;
			final JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( in );
			this.mImage = decoder.decodeAsBufferedImage();
			in.close();
//			// Create a font.
//			this.mFont = new Font( "Serif", Font.PLAIN, 116 );
//			// Save the message and split.
//			this.mMessage = message;
//			this.mSplit = split;
//			// Set our size to match the image's size.
			this.setSize( mImage.getWidth(), mImage.getHeight() );
		} catch ( IOException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		} catch ( ImageFormatException e ) {
			throw new RuntimeException( e );	//To change body of catch statement use Options | File Templates.
		}*/
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

		// Turn on antialiasing.
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g2.drawImage( this.mImage.getImage(), 0, 0, null );
//		drawBackground( g2 );
//		drawImageMosaic( g2 );
//		drawText( g2 );
	}

//	protected void drawBackground( final Graphics2D g2 ) {
//		// Draw circles of different colors.
//		final int side = 45;
//		final int width = getSize().width;
//		final int height = getSize().height;
//		final Color[] colors = {Color.yellow, Color.cyan, Color.orange,
//						  Color.pink, Color.magenta, Color.lightGray};
//		for ( int y = 0; y < height; y += side ) {
//			for ( int x = 0; x < width; x += side ) {
//				Ellipse2D ellipse = new Ellipse2D.Float( x, y, side, side );
//				final int index = ( x + y ) / side % colors.length;
//				g2.setPaint( colors[ index ] );
//				g2.fill( ellipse );
//			}
//		}
//	}

//	protected void drawImageMosaic( Graphics2D g2 ) {
//		// Break the image up into tiles. Draw each
//		//   tile with its own transparency, allowing
//		//   the background to show through to varying
//		//   degrees.
//		final int side = 36;
//		final int width = mImage.getWidth();
//		final int height = mImage.getHeight();
//		for ( int y = 0; y < height; y += side ) {
//			for ( int x = 0; x < width; x += side ) {
//				// Calculate an appropriate transparency value.
//				final float xBias = ( float )x / ( float )width;
//				final float yBias = ( float )y / ( float )height;
//				final float alpha = 1.0f - Math.abs( xBias - yBias );
//				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, alpha ) );
//				// Draw the subimage.
//				final int w = Math.min( side, width - x );
//				final int h = Math.min( side, height - y );
//				final BufferedImage tile = mImage.getSubimage( x, y, w, h );
//				g2.drawImage( tile, x, y, null );
//			}
//		}
//		// Reset the composite.
//		g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER ) );
//	}

//	protected void drawText( Graphics2D g2 ) {
//		// Find the bounds of the entire string.
//		final FontRenderContext frc = g2.getFontRenderContext();
//		this.mLayout = new TextLayout( mMessage, mFont, frc );
//		// Find the dimensions of this component.
//		final int width = getSize().width;
//		final int height = getSize().height;
//		// Place the first full string, horizontally centered,
//		//   at the bottom of the component.
//		final Rectangle2D bounds = mLayout.getBounds();
//		final double x = ( width - bounds.getWidth() ) / 2;
//		final double y = height - bounds.getHeight();
//		drawString( g2, x, y, 0 );
//		// Now draw a second version, anchored to the right side
//		//   of the component and rotated by -PI / 2.
//		drawString( g2, width - bounds.getHeight(), y, -Math.PI / 2 );
//	}
//
//	protected void drawString( Graphics2D g2,
//							   double x, double y, double theta ) {
//		// Transform to the requested location.
//		g2.translate( x, y );
//		// Rotate by the requested angle.
//		g2.rotate( theta );
//		// Draw the first part of the string.
//		final String first = this.mMessage.substring( 0, mSplit );
//		final float width = drawBoxedString( g2, first, Color.white, Color.red, 0 );
//		// Draw the second part of the string.
//		final String second = this.mMessage.substring( mSplit );
//		drawBoxedString( g2, second, Color.blue, Color.white, width );
//		// Undo the transformations.
//		g2.rotate( -theta );
//		g2.translate( -x, -y );
//	}
//
//	protected float drawBoxedString( final Graphics2D g2, final String s, final Color c1, final Color c2, final double x ) {
//		// Calculate the width of the string.
//		final FontRenderContext frc = g2.getFontRenderContext();
//		final TextLayout subLayout = new TextLayout( s, mFont, frc );
//		final float advance = subLayout.getAdvance();
//		// Fill the background rectangle with a gradient.
//		GradientPaint gradient = new GradientPaint( ( float )x, 0, c1, (float)( x + advance ), 0, c2 );
//		g2.setPaint( gradient );
//		final Rectangle2D bounds = this.mLayout.getBounds();
//		final Rectangle2D back = new Rectangle2D.Double( x, 0, advance, bounds.getHeight() );
//		g2.fill( back );
//		// Draw the string over the gradient rectangle.
//		g2.setPaint( Color.white );
//		g2.setFont( this.mFont );
//		g2.drawString( s, (float)x, (float)-bounds.getY() );
//		return advance;
//	}
}
