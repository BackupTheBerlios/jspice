package org.openspice.jspice.main;

/**
 * Created by IntelliJ IDEA.
 * User: steve
 * Date: Jul 22, 2004
 * Time: 8:22:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class StartPlain {

    public static final void main( final String[] args ) {
		final String[] new_args = new String[ args.length + 2 ];
		System.arraycopy( args, 0, new_args, 2, args.length );
		new_args[ 0 ] = "--banner=off";
		new_args[ 1 ] = "--prompt=";
        new Main().perform( new_args );
    }

}
