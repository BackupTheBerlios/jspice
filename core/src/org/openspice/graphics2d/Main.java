package org.openspice.graphics2d;

import java.io.File;
import java.awt.*;

public class Main {
	public static void main( final String[] args )  {
//		new Artist( args ).main();
		new ShowOff( "example.jpg" ).main();

//		final ImageFromFile image = new ImageFromFile( new File( "example.gif" ) );
//		System.out.println( "height = " + image.getHeight( null ) );
//
//		final Painter painter = new Painter( 128, 128,  Color.ORANGE );
//		painter.setColor( Color.blue );
//		painter.drawOval( 0, 0, 128, 128 );
//
//		painter.save( "output.png" );
	}
}
