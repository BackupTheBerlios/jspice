package tads2.jetty;

import tads2.jetty.GameTerminal;
import tads2.jetty.Jetty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JettyTerm {

	public static void main( final String[] args ) {
		final GameTerminal term = new GameTerminal();
		try {
			final FileInputStream file = new FileInputStream( args[ 0 ] );
			final Jetty j = new Jetty( term, file );
			if ( j.load() ) {
				j.run();
			}
		} catch ( final FileNotFoundException fnf ) {
			System.err.println( "Error: file not found (" + args[ 0 ] + "): " + fnf );
		}
	}

}
