package tads2.jetty;


public class InputHandler extends JettyRef {

	public InputHandler( final Jetty jetty, PlatformIO io ) {
		super( jetty );
		_platform_io = io;
	}

	public String read_line() {
		flush(); // flush output first
		input_entered(); // and the next print will start at index 0

		String line = _platform_io.read_line();
		if ( line != null ) {
			return line.trim();
		}

		print_error( "Error with read_line()", 1 );
		return "";
	}

	public String read_key() {
		flush(); // flush output first

		String line = _platform_io.read_key();
		if ( line != null ) {
			return line;
		}

		print_error( "Error with read_key()", 1 );
		return " ";
	}

	private PlatformIO _platform_io;
}
