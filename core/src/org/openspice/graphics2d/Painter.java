package org.openspice.graphics2d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.net.URL;
import java.text.AttributedCharacterIterator;

/**
 * Encapsulates a BufferedImage and its Graphics2D object.
 */
public class Painter {

	//  --- Constructor ---

	private final Graphics2D graphics;
	private final BufferedImage image;

	public Painter( final int width, final int height ) {
		this.image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		this.graphics = this.image.createGraphics();
	}

	public Painter( final String filename ) {
		this( Toolkit.getDefaultToolkit().createImage( filename ) );
	}

	public Painter( final URL url ) {
		this( Toolkit.getDefaultToolkit().createImage( url ) );
	}

	public Painter( final Image i ) {
		final int width = i.getWidth( null );
		final int height = i.getHeight( null );
		this.image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		this.graphics = this.image.createGraphics();
		this.graphics.drawImage( i, 0, 0, null );
	}

	public Painter( final int width, final int height, final Color color ) {
		this( width, height );
//		final Color c = this.graphics.getColor();
		this.graphics.setColor( color );
		this.graphics.fillRect( 0, 0, width, height );
//		this.graphics.setColor( c );
	}

	//	--- Saving Images ---

	private static String format = "png";

	public void setFormat( final String s ) {
		final Iterator it = ImageIO.getImageWritersBySuffix( s );
		if ( it.hasNext() || "gif".equals( s ) ) {
			this.format = s;
		} else {
			throw new RuntimeException( "tbd" );    // todo:
		}
	}

	public void save( final String filename ) {
		try {
			if ( "gif".equals( this.format ) ) {
				throw new RuntimeException( "tbd" );    // todo:
			} else {
				final File f = new File( filename );
				ImageIO.write( image, this.format, f );
			}
		} catch ( IOException ex ) {
			throw new RuntimeException( ex );
		}
	}

	//	---- Drawing operations ----


	public void draw3DRect( int i, int i1, int i2, int i3, boolean b ) {
		graphics.draw3DRect( i, i1, i2, i3, b );
	}

	public void fill3DRect( int i, int i1, int i2, int i3, boolean b ) {
		graphics.fill3DRect( i, i1, i2, i3, b );
	}

	public void draw( Shape shape ) {
		graphics.draw( shape );
	}

	public boolean drawImage( Image image, AffineTransform affineTransform, ImageObserver imageObserver ) {
		return graphics.drawImage( image, affineTransform, imageObserver );
	}

	public void drawImage( BufferedImage bufferedImage, BufferedImageOp bufferedImageOp, int i, int i1 ) {
		graphics.drawImage( bufferedImage, bufferedImageOp, i, i1 );
	}

	public void drawRenderedImage( RenderedImage renderedImage, AffineTransform affineTransform ) {
		graphics.drawRenderedImage( renderedImage, affineTransform );
	}

	public void drawRenderableImage( RenderableImage renderableImage, AffineTransform affineTransform ) {
		graphics.drawRenderableImage( renderableImage, affineTransform );
	}

	public void drawString( String s, int i, int i1 ) {
		graphics.drawString( s, i, i1 );
	}

	public void drawString( String s, float v, float v1 ) {
		graphics.drawString( s, v, v1 );
	}

	public void drawString( AttributedCharacterIterator attributedCharacterIterator, int i, int i1 ) {
		graphics.drawString( attributedCharacterIterator, i, i1 );
	}

	public void drawString( AttributedCharacterIterator attributedCharacterIterator, float v, float v1 ) {
		graphics.drawString( attributedCharacterIterator, v, v1 );
	}

	public void drawGlyphVector( GlyphVector glyphVector, float v, float v1 ) {
		graphics.drawGlyphVector( glyphVector, v, v1 );
	}

	public void fill( Shape shape ) {
		graphics.fill( shape );
	}

	public boolean hit( Rectangle rectangle, Shape shape, boolean b ) {
		return graphics.hit( rectangle, shape, b );
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return graphics.getDeviceConfiguration();
	}

	public void setComposite( Composite composite ) {
		graphics.setComposite( composite );
	}

	public void setPaint( Paint paint ) {
		graphics.setPaint( paint );
	}

	public void setStroke( Stroke stroke ) {
		graphics.setStroke( stroke );
	}

	public void setRenderingHint( RenderingHints.Key key, Object o ) {
		graphics.setRenderingHint( key, o );
	}

	public Object getRenderingHint( RenderingHints.Key key ) {
		return graphics.getRenderingHint( key );
	}

	public void setRenderingHints( Map map ) {
		graphics.setRenderingHints( map );
	}

	public void addRenderingHints( Map map ) {
		graphics.addRenderingHints( map );
	}

	public RenderingHints getRenderingHints() {
		return graphics.getRenderingHints();
	}

	public void translate( int i, int i1 ) {
		graphics.translate( i, i1 );
	}

	public void translate( double v, double v1 ) {
		graphics.translate( v, v1 );
	}

	public void rotate( double v ) {
		graphics.rotate( v );
	}

	public void rotate( double v, double v1, double v2 ) {
		graphics.rotate( v, v1, v2 );
	}

	public void scale( double v, double v1 ) {
		graphics.scale( v, v1 );
	}

	public void shear( double v, double v1 ) {
		graphics.shear( v, v1 );
	}

	public void transform( AffineTransform affineTransform ) {
		graphics.transform( affineTransform );
	}

	public void setTransform( AffineTransform affineTransform ) {
		graphics.setTransform( affineTransform );
	}

	public AffineTransform getTransform() {
		return graphics.getTransform();
	}

	public Paint getPaint() {
		return graphics.getPaint();
	}

	public Composite getComposite() {
		return graphics.getComposite();
	}

	public void setBackground( Color color ) {
		graphics.setBackground( color );
	}

	public Color getBackground() {
		return graphics.getBackground();
	}

	public Stroke getStroke() {
		return graphics.getStroke();
	}

	public void clip( Shape shape ) {
		graphics.clip( shape );
	}

	public FontRenderContext getFontRenderContext() {
		return graphics.getFontRenderContext();
	}

	public Graphics create() {
		return graphics.create();
	}

	public Graphics create( int i, int i1, int i2, int i3 ) {
		return graphics.create( i, i1, i2, i3 );
	}

	public Color getColor() {
		return graphics.getColor();
	}

	public void setColor( Color color ) {
		graphics.setColor( color );
	}

	public void setPaintMode() {
		graphics.setPaintMode();
	}

	public void setXORMode( Color color ) {
		graphics.setXORMode( color );
	}

	public Font getFont() {
		return graphics.getFont();
	}

	public void setFont( Font font ) {
		graphics.setFont( font );
	}

	public FontMetrics getFontMetrics() {
		return graphics.getFontMetrics();
	}

	public FontMetrics getFontMetrics( Font font ) {
		return graphics.getFontMetrics( font );
	}

	public Rectangle getClipBounds() {
		return graphics.getClipBounds();
	}

	public void clipRect( int i, int i1, int i2, int i3 ) {
		graphics.clipRect( i, i1, i2, i3 );
	}

	public void setClip( int i, int i1, int i2, int i3 ) {
		graphics.setClip( i, i1, i2, i3 );
	}

	public Shape getClip() {
		return graphics.getClip();
	}

	public void setClip( Shape shape ) {
		graphics.setClip( shape );
	}

	public void copyArea( int i, int i1, int i2, int i3, int i4, int i5 ) {
		graphics.copyArea( i, i1, i2, i3, i4, i5 );
	}

	public void drawLine( int i, int i1, int i2, int i3 ) {
		graphics.drawLine( i, i1, i2, i3 );
	}

	public void fillRect( int i, int i1, int i2, int i3 ) {
		graphics.fillRect( i, i1, i2, i3 );
	}

	public void drawRect( int i, int i1, int i2, int i3 ) {
		graphics.drawRect( i, i1, i2, i3 );
	}

	public void clearRect( int i, int i1, int i2, int i3 ) {
		graphics.clearRect( i, i1, i2, i3 );
	}

	public void drawRoundRect( int i, int i1, int i2, int i3, int i4, int i5 ) {
		graphics.drawRoundRect( i, i1, i2, i3, i4, i5 );
	}

	public void fillRoundRect( int i, int i1, int i2, int i3, int i4, int i5 ) {
		graphics.fillRoundRect( i, i1, i2, i3, i4, i5 );
	}

	public void drawOval( int i, int i1, int i2, int i3 ) {
		graphics.drawOval( i, i1, i2, i3 );
	}

	public void fillOval( int i, int i1, int i2, int i3 ) {
		graphics.fillOval( i, i1, i2, i3 );
	}

	public void drawArc( int i, int i1, int i2, int i3, int i4, int i5 ) {
		graphics.drawArc( i, i1, i2, i3, i4, i5 );
	}

	public void fillArc( int i, int i1, int i2, int i3, int i4, int i5 ) {
		graphics.fillArc( i, i1, i2, i3, i4, i5 );
	}

	public void drawPolyline( int[] ints, int[] ints1, int i ) {
		graphics.drawPolyline( ints, ints1, i );
	}

	public void drawPolygon( int[] ints, int[] ints1, int i ) {
		graphics.drawPolygon( ints, ints1, i );
	}

	public void drawPolygon( Polygon polygon ) {
		graphics.drawPolygon( polygon );
	}

	public void fillPolygon( int[] ints, int[] ints1, int i ) {
		graphics.fillPolygon( ints, ints1, i );
	}

	public void fillPolygon( Polygon polygon ) {
		graphics.fillPolygon( polygon );
	}

	public void drawChars( char[] chars, int i, int i1, int i2, int i3 ) {
		graphics.drawChars( chars, i, i1, i2, i3 );
	}

	public void drawBytes( byte[] bytes, int i, int i1, int i2, int i3 ) {
		graphics.drawBytes( bytes, i, i1, i2, i3 );
	}

	public boolean drawImage( Image image, int i, int i1, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, imageObserver );
	}

	public boolean drawImage( Image image, int i, int i1, int i2, int i3, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, i2, i3, imageObserver );
	}

	public boolean drawImage( Image image, int i, int i1, Color color, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, color, imageObserver );
	}

	public boolean drawImage( Image image, int i, int i1, int i2, int i3, Color color, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, i2, i3, color, imageObserver );
	}

	public boolean drawImage( Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, i2, i3, i4, i5, i6, i7, imageObserver );
	}

	public boolean drawImage( Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Color color, ImageObserver imageObserver ) {
		return graphics.drawImage( image, i, i1, i2, i3, i4, i5, i6, i7, color, imageObserver );
	}

	public void dispose() {
		graphics.dispose();
	}

	public void finalize() {
		graphics.finalize();
	}

	public String toString() {
		return graphics.toString();
	}

	public Rectangle getClipBounds( Rectangle rectangle ) {
		return graphics.getClipBounds( rectangle );
	}
}
