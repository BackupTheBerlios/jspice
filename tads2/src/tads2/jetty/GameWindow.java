package tads2.jetty;

// the object that does the actual printing; various bits taken from 
// Matthew Russotto's Zplet code

import java.awt.Component;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.AWTEvent;

public class GameWindow extends Canvas implements PlatformIO {

	public GameWindow( String[] fonts, int[] font_sizes, String[] fg_colors,
		String[] bg_colors ) {
		for ( int i = 0; i < NUM_FONTS; i++ ) {
			if ( fonts[ i ] != null ) {
				_font_names[ i ] = fonts[ i ];
			}
			if ( font_sizes[ i ] != 0 ) {
				_font_sizes[ i ] = font_sizes[ i ];
			}
			if ( fg_colors[ i ] != null ) {
				_fg_color_names[ i ] = fg_colors[ i ];
			}
			if ( bg_colors[ i ] != null ) {
				_bg_color_names[ i ] = bg_colors[ i ];
			}
		}
	}

	public void init_size( int status_width, int main_width, int height,
		ScrollPane scrollbar ) {
		_status_width = status_width;
		_main_width = main_width;
		setSize( main_width, height * SCROLLBACK_PAGES );
		_scrollbar = scrollbar;
	}

	public void set_out( OutputFormatter out ) {
		_out = out;
	}

	// returns the length of this string in whatever units we're using
	public synchronized int size_text( String s ) {
		return _font_metrics[ MAIN ].stringWidth( s );
	}

	public synchronized int size_text( char c ) {
		return _font_metrics[ MAIN ].charWidth( c );
	}

	// prints the string (advancing the cursor as necessary, but not
	// line-wrapping)
	public synchronized void print_text( String s ) {
		_graphics.setFont( _fonts[ MAIN ] );
		_graphics.setColor( _fg_colors[ MAIN ] );
		_graphics.drawString( s, _cursor, _print_line );
		_cursor += _font_metrics[ MAIN ].stringWidth( s );
		// let's not repaint here to try and prevent flicker
	}

	// prints the error message
	public synchronized void print_error( String s ) {
		s = "[" + s + "]";
		_load_error = s;
		_graphics.setFont( _fonts[ MAIN ] );
		_graphics.setColor( _fg_colors[ MAIN ] );
		scroll_window();
		_graphics.drawString( s, _cursor, _print_line );
		_cursor += _font_metrics[ MAIN ].stringWidth( s );
		scroll_window();
		repaint();
	}

	// ends the current line, shifts stuff up to accomodate a new line
	public synchronized void scroll_window() {
		scroll_window( _font_metrics[ MAIN ].getHeight() );
	}

	private synchronized void scroll_window( int scroll_height ) {
		// copy the chunk up:
		int width = getSize().width;
		int height = getSize().height;
		_graphics.copyArea( 0, scroll_height, width, height - scroll_height, 0, -scroll_height );
		// erase the old chunk:
		_graphics.setColor( _bg_colors[ MAIN ] );
		_graphics.fillRect( 0, height - scroll_height, width, scroll_height );
		// reset the cursor:
		_cursor = EDGE_OFFSET;

		// and drop to the bottom of page:
		_scrollbar.setScrollPosition( 0, getSize().height );
		repaint();
	}

	// tell the status line to have this string on it
	public synchronized void set_status_string( String s, boolean left ) {
		if ( left ) {
			_status_left = s;
		} else {
			_status_right = s;
		}

		// request a repaint of the status bar
		_status_line.repaint();
	}

	// read a key from input
	public String read_key() {
		repaint(); // ask for a screen refresh first
		requestFocus(); // and ask for the keyboard focus

		_data.set( null );

		_read_mode = KEY;
		String s = _data.get();
		_read_mode = NONE;

		return s;
	}

	// note that this automatically calls scroll_window() after reading
	public String read_line() {
		draw_cursor( true );
		repaint(); // ask for a screen refresh first
		requestFocus(); // and ask for the keyboard focus

		_data.set( null );

		_read_mode = LINE;
		String s = _data.get();
		_read_mode = NONE;

		scroll_window();
		return s;
	}

	// returns true if the user hit space (and so it should scroll
	// a full page), false otherwise (so it should scroll a line)
	public boolean more_prompt( String prompt ) {
		print_text( prompt );
		repaint(); // ask for a screen refresh first
		requestFocus(); // and ask for the keyboard focus

		_data.set( null );
		_read_mode = KEY;

		String s = null;

		while ( s == null ) {
			s = _data.get();
			if ( !( s.equals( " " ) || s.equals( "\\n" ) ) ) {
				s = null;
			}
		}

		_read_mode = NONE;

		// reset cursor and blank the line:
		_cursor = EDGE_OFFSET;
		_graphics.setColor( _bg_colors[ MAIN ] );
		int from = _print_line - _font_metrics[ MAIN ].getAscent();
		int height = _font_metrics[ MAIN ].getHeight();
		_graphics.fillRect( 0, from, getSize().width, height );

		return ( s.equals( " " ) );
	}

	public void clear_screen() {
		// to clear the screen but preserve scrollback, we just scroll
		// everything up as with scroll_window() (only a screenful, not
		// a lineful)
		scroll_window( _main_height );
	}

	public void set_style( int style, boolean turn_on ) {
		int new_style = _fonts[ MAIN ].getStyle();
		if ( style == this.BOLD ) {
			new_style = turn_on ? ( new_style | Font.BOLD ) : ( new_style & ~Font.BOLD );
		} else if ( style == this.ITALIC ) {
			new_style = turn_on ? ( new_style | Font.ITALIC ) : ( new_style & ~Font.ITALIC );
		}

		_fonts[ MAIN ] =
			new Font( _fonts[ MAIN ].getName(), new_style, _fonts[ MAIN ].getSize() );
		_font_metrics[ MAIN ] = _graphics.getFontMetrics( _fonts[ MAIN ] );
	}

	// has the gamefile been loaded yet? (ie, display message or not)
	public void loaded( boolean b ) {
		_loaded = b;
	}

	// return a ref to the status line, so the layout manager can stick it in:
	public Component get_status_line() {
		return _status_line;
	}

	protected synchronized void processKeyEvent( KeyEvent e ) {
		// we don't care about key-up events:
		if ( e.getID() == KeyEvent.KEY_RELEASED ) {
			return;
		}

		if ( _read_mode == LINE ) {
			if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
				draw_cursor( false );

				String command = _cur_command.toString();
				_data.set( command );
				_cur_command.setLength( 0 );

				_history_index = -1; // and the working command is now reset

				if ( command.length() > 0 ) {
					// add this to history if it doesn't match the last command:
					if ( _command_history.size() == 0 ||
						!_command_history.elementAt( 0 ).equals( command ) ) {
						_command_history.insertElementAt( command, 0 );
					}

					// and cap history off at whatever size
					if ( _command_history.size() > HISTORY_SIZE ) {
						_command_history.removeElementAt( _command_history.size() - 1 );
					}
				}
			} else if ( e.getKeyCode() == KeyEvent.VK_UP ||
				e.getKeyCode() == KeyEvent.VK_DOWN ) {
				if ( ( e.getKeyCode() == KeyEvent.VK_UP &&
					( _history_index < -1 ||
					_history_index >= _command_history.size() - 1 ) ) ||
					( e.getKeyCode() == KeyEvent.VK_DOWN &&
					( _history_index < 0 ||
					_history_index >= _command_history.size() ) ) ) {
					return; // index is out of range, whoops.
				}

				// first erase the current command:
				draw_cursor( false );
				_graphics.setFont( _fonts[ INPUT ] );
				_graphics.setColor( _bg_colors[ MAIN ] );
				String cur = ( _history_index == -1 ) ? _cur_command.toString() :
					(String)_command_history.elementAt( _history_index );
				_cursor -= _font_metrics[ INPUT ].stringWidth( cur );
				_graphics.drawString( cur, _cursor, _print_line );

				// back up partial command as necessary:
				if ( _history_index == -1 ) {
					_partial_command = cur;
				}

				if ( e.getKeyCode() == KeyEvent.VK_UP ) {
					_history_index++;
				} else {
					_history_index--;
				}

				// retrieve partial command as necessary:
				if ( _history_index == -1 ) {
					cur = _partial_command;
				} else {
					cur = (String)_command_history.elementAt( _history_index );
				}

				// and draw the resulting command:
				_graphics.setColor( _fg_colors[ INPUT ] );
				_graphics.drawString( cur, _cursor, _print_line );
				_cursor += _font_metrics[ INPUT ].stringWidth( cur );
				draw_cursor( true );

				// and stick it into the string buffer:
				_cur_command.setLength( 0 );
				_cur_command.append( cur );
			} else if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE &&
				_cur_command.length() > 0 ) {
				draw_cursor( false );

				// blank out the last character:
				char c = _cur_command.charAt( _cur_command.length() - 1 );
				int width = _font_metrics[ INPUT ].charWidth( c );
				int from = _print_line - _font_metrics[ INPUT ].getAscent();
				_cursor -= width;
				_graphics.setColor( _bg_colors[ MAIN ] );
				_graphics.fillRect( _cursor, from, width, _font_metrics[ INPUT ].getHeight() );
				draw_cursor( true );

				// and remove it from the buffer:
				_cur_command.setLength( _cur_command.length() - 1 );

				_history_index = -1; // and the working command is now reset
			} else if ( e.getID() == KeyEvent.KEY_TYPED &&
				!Character.isISOControl( e.getKeyChar() ) ) {
				draw_cursor( false );

				// and draw it:
				_graphics.setFont( _fonts[ INPUT ] );
				_graphics.setColor( _fg_colors[ INPUT ] );
				String c = "" + e.getKeyChar();
				_graphics.drawString( c, _cursor, _print_line );
				_cursor += _font_metrics[ INPUT ].stringWidth( c ); // and advance cursor

				draw_cursor( true );

				_history_index = -1; // and the working command is now reset
				_cur_command.append( e.getKeyChar() );
			}
		} else if ( _read_mode == KEY ) {
			if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
				_data.set( "[bksp]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_DELETE ) {
				_data.set( "[del]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_DOWN ) {
				_data.set( "[down]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_END ) {
				_data.set( "[end]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
				_data.set( "\\n" );
			} else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
				_data.set( "[esc]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F1 ) {
				_data.set( "[f1]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F2 ) {
				_data.set( "[f2]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F3 ) {
				_data.set( "[f3]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F4 ) {
				_data.set( "[f4]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F5 ) {
				_data.set( "[f5]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F6 ) {
				_data.set( "[f6]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F7 ) {
				_data.set( "[f7]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F8 ) {
				_data.set( "[f8]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F9 ) {
				_data.set( "[f9]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_F10 ) {
				_data.set( "[f10]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_HOME ) {
				_data.set( "[home]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_INSERT ) {
				_data.set( "[ins]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) {
				_data.set( "[left]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ) {
				_data.set( "[page down]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_PAGE_UP ) {
				_data.set( "[page up]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
				_data.set( "[right]" );
			} else if ( e.getKeyCode() == KeyEvent.VK_TAB ) {
				_data.set( "\\t" );
			} else if ( e.getKeyCode() == KeyEvent.VK_UP ) {
				_data.set( "[up]" );
			} else {
				char c = e.getKeyChar();

				if ( c != KeyEvent.CHAR_UNDEFINED &&
					!Character.isISOControl( e.getKeyChar() ) ) {
					if ( e.isAltDown() ) {
						_data.set( "[alt-" + Character.toLowerCase( c ) + "]" );
					} else if ( e.isControlDown() ) {
						// as far as I can tell, control is handled in a weird way
						// not like how it should be. of course, it is equally possible
						// I am just screwing something up. anyway, I'm just going to
						// allow ctrl+a through ctrl+z:
						if ( c >= 1 && c <= 26 ) {
							_data.set( "[ctrl-" + (char)( c - 1 + 'a' ) + "]" );
						}
					} else {
						_data.set( new Character( c ).toString() );
					}
				}
			}
		} else       // just ignore the keypress
		{
			return;
		}

		// repaint only the last line, natch:
		int line_height = _font_metrics[ INPUT ].getHeight();
		int print_start = _print_line - _font_metrics[ INPUT ].getAscent();
		repaint( 0, print_start, getSize().width, line_height );
	}

	// on some platforms (read: mac) apparently you need to explicitly
	// request focus when the window is clicked on
	protected synchronized void processMouseEvent( MouseEvent e ) {
		requestFocus();
		super.processMouseEvent( e );
	}

	// draw the (line) cursor at the location of the (internal print) cursor
	// this is the only place where the input bg color is used
	private void draw_cursor( boolean draw ) {
		int from = _print_line - _font_metrics[ INPUT ].getAscent();
		int width = _font_metrics[ INPUT ].stringWidth( " " );
		int height = _font_metrics[ INPUT ].getAscent() +
			_font_metrics[ INPUT ].getDescent();

		if ( draw ) {
			_graphics.setColor( _bg_colors[ INPUT ] );
			_graphics.fillRect( _cursor, from, width, height );
			_cursor += width;
		} else {
			_graphics.setColor( _bg_colors[ MAIN ] );
			_cursor -= width;
			_graphics.fillRect( _cursor, from, width, height );
		}
	}

	// I am informed by Ricardo Dague that the default update() method clears
	// the screen all the time, which leads to flicker
	public void update( Graphics g ) {
		paint( g );
	}

	// this bit yanked (vaguely) from Ian McFarland's article on java.sun.com:
	public void paint( Graphics g ) {
		// recheck size each time, rebuild if resized
		if ( _buffer == null || _buffer.getWidth( this ) != getSize().width ||
			_buffer.getHeight( this ) != getSize().height ) {
			reset();
		}

		if ( _buffer == null ) // apparently sometimes reset() is called too early
		{
			return;
		}

		if ( !_loaded ) {
			g.setColor( _bg_colors[ MAIN ] );
			g.fillRect( 0, 0, getSize().width, getSize().height );
			g.setColor( _fg_colors[ MAIN ] );
			String s = "Loading game, please be patient";
			if ( _load_error != null ) {
				s = _load_error;
				g.setFont( new Font( "Courier", Font.PLAIN, 12 ) );
			} else {
				g.setFont( new Font( "Courier", Font.PLAIN, 24 ) );
			}

			g.drawString( s, EDGE_OFFSET, getSize().height - _main_height / 2 );
			_scrollbar.setScrollPosition( 0, getSize().height );
			return;
		}

		// and copy the buffer over onto the real graphics object
		g.drawImage( _buffer, 0, 0, this );
	}

	public synchronized void reset() {
		// let's ignore keyboard events for a bit here:
		disableEvents( AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK );

		final Dimension sz = this.getSize();
//	  System.err.println( "sz = " + sz );
		_buffer = createImage( sz.width, sz.height );

		_graphics = _buffer.getGraphics();

		// use that graphics object to set up all the font/color info:
		for ( int i = 0; i < NUM_FONTS; i++ ) {
			// this is a bad hack, and getFontList() is deprecated besides.
			// nevertheless, I don't know a good way to say "see if this
			// font is supported, and if not, return null"
			String[] font_list;
			try {
				font_list = getToolkit().getFontList();
			} catch ( NullPointerException np ) {
				// Nick Montfort identified a problem with java 1.4.1 on the mac;
				// getFontList() doesn't handle certain situations involving bad
				// fonts on the machine and crashes. As an attempted fix, let's
				// default to using some other font list:
				String[] df = {"Monospaced", "SansSerif"};
				font_list = new String[ df.length ];
				System.arraycopy( df, 0, font_list, 0, df.length );
			}

			StringTokenizer names = new StringTokenizer( _font_names[ i ], ";," );
			FONTS: while ( names.hasMoreTokens() && _fonts[ i ] == null ) {
				String f = names.nextToken().trim();
				for ( int j = 0; j < font_list.length; j++ ) {
					if ( font_list[ j ].equals( f ) ) {
						_fonts[ i ] = new Font( f, Font.PLAIN, _font_sizes[ i ] );
						break FONTS;
					}
				}
			}

			// dig up some default or something
			if ( _fonts[ i ] == null ) {
				_fonts[ i ] = new Font( "not found", Font.PLAIN, _font_sizes[ i ] );
				System.err.println( "Can't find a font, making up one" );
			}

			_font_metrics[ i ] = _graphics.getFontMetrics( _fonts[ i ] );
			_bg_colors[ i ] = lookup_color( _bg_color_names[ i ] );
			_fg_colors[ i ] = lookup_color( _fg_color_names[ i ] );
		}

		_print_line = getSize().height - EDGE_OFFSET -
			Math.max( _font_metrics[ MAIN ].getMaxDescent(),
				_font_metrics[ INPUT ].getMaxDescent() );

		// clear the screen:
		_graphics.setColor( _bg_colors[ MAIN ] );
		_graphics.fillRect( 0, 0, getSize().width, getSize().height );

		// adjust our size values to this new size, keeping the main/status
		// width difference the same:
		_main_width = getSize().width - ( _status_width - _main_width );
		_status_width = getSize().width;
		_main_height = getSize().height / SCROLLBACK_PAGES;
		_status_height = _font_metrics[ STATUS ].getHeight();

		// let the output formatter object know about its new size:
		if ( _out != null ) {
			int lines = _main_height / _font_metrics[ MAIN ].getHeight();
			_out.resize( _main_width - EDGE_OFFSET * 2, lines );
		}

		// and now we're ready to start accepting keyboard events again
		enableEvents( AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK );

		invalidate(); // oh, and since we got resized, please re-lay-out us
		_status_line.invalidate(); // and our status line
	}

	// grumble grumble can't read from system properties
	private Color lookup_color( String color ) {
		color = color.toLowerCase();

		if ( color.startsWith( "#" ) ) {
			return Color.decode( color );
		} else if ( color.equals( "black" ) ) {
			return Color.black;
		} else if ( color.equals( "blue" ) ) {
			return Color.blue;
		} else if ( color.equals( "cyan" ) ) {
			return Color.cyan;
		} else if ( color.equals( "darkgray" ) ) {
			return Color.darkGray;
		} else if ( color.equals( "gray" ) ) {
			return Color.gray;
		} else if ( color.equals( "green" ) ) {
			return Color.green;
		} else if ( color.equals( "lightgray" ) ) {
			return Color.lightGray;
		} else if ( color.equals( "magenta" ) ) {
			return Color.magenta;
		} else if ( color.equals( "orange" ) ) {
			return Color.orange;
		} else if ( color.equals( "pink" ) ) {
			return Color.pink;
		} else if ( color.equals( "red" ) ) {
			return Color.red;
		} else if ( color.equals( "white" ) ) {
			return Color.white;
		} else if ( color.equals( "yellow" ) ) {
			return Color.yellow;
		} else {
			// I dunno, should raise some kind of error here:
			System.err.println( "Unknown color: '" + color + "'" );
			return Color.black;
		}
	}

	// drawing values:
	private Image _buffer;
	private Graphics _graphics;

	private static final int STATUS = 0;
	private static final int MAIN = 1;
	private static final int INPUT = 2;
	private static final int NUM_FONTS = 3;

	// the actual objects used for drawing:
	private Font[] _fonts = new Font[ NUM_FONTS ];
	private FontMetrics[] _font_metrics = new FontMetrics[ NUM_FONTS ];
	private Color[] _bg_colors = new Color[ NUM_FONTS ];
	private Color[] _fg_colors = new Color[ NUM_FONTS ];

	// and the values to initialize them:
	private String[] _font_names =
		{"Monospaced", "SansSerif", "Monospaced"};
	private int[] _font_sizes = {12, 12, 14};
	private String[] _fg_color_names =
		{"white", "black", "black"};
	private String[] _bg_color_names =
		{"blue", "white", "black"};

	private int _status_width = 0;
	private int _status_height = 0;
	private int _main_width = 0;
	private int _main_height = 0;

	private ScrollPane _scrollbar = null;

	// the current thing to print in the statusline:
	private String _status_left = "";
	private String _status_right = "";

	// and the statusline itself:
	private StatusLine _status_line = new StatusLine();

	// how many pixels or whatever to stay away from the left & right edges:
	private static final int EDGE_OFFSET = 5;

	// total size of scrollback
	private static final int SCROLLBACK_PAGES = 2;

	// if this is false, display a loading message
	private boolean _loaded = false;

	// the output formatter, to contact when we resize
	private OutputFormatter _out = null;

	private int _cursor = EDGE_OFFSET;      // where we are on the line
	private int _print_line = 0;  // where in the image we print to

	private final static int NONE = 0;
	private final static int LINE = 1;
	private final static int KEY = 2;
	private int _read_mode = NONE;
	private Data _data = new Data();

	private StringBuffer _cur_command = new StringBuffer( 80 );
	private final static int HISTORY_SIZE = 10;
	private Vector _command_history = new Vector( HISTORY_SIZE + 1 );
	private int _history_index = -1;
	// we hang onto the command in progress when you scroll through the history:
	private String _partial_command = null;

	class StatusLine extends Canvas {

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getPreferredSize() {
			// I dunno, just pick something here:
			if ( _font_metrics[ STATUS ] == null ) {
				return new Dimension( 0, 0 );
			}

			int width = Integer.MAX_VALUE; // is there a "as big as possible"?
			int height = _font_metrics[ STATUS ].getHeight();
			return new Dimension( width, height );
		}

		public void update( Graphics g ) {
			paint( g );
		}

		public void paint( Graphics g ) {
			if ( _font_metrics[ STATUS ] == null ) {
				// blank the area (ie, the main window color, not the status color)
				g.setColor( _bg_colors[ MAIN ] );
				g.fillRect( 0, 0, getSize().width, getSize().height );
				return;
			}

			// blank the line
			g.setColor( _bg_colors[ STATUS ] );
			int height = _font_metrics[ STATUS ].getHeight();
			g.fillRect( 0, 0, getSize().width, height );

			// draw left and right (at appropriate offsets)
			g.setFont( _fonts[ STATUS ] );
			g.setColor( _fg_colors[ STATUS ] );
			g.drawString( _status_left, EDGE_OFFSET, _font_metrics[ STATUS ].getAscent() );
			int right_start = getSize().width - EDGE_OFFSET -
				_font_metrics[ STATUS ].stringWidth( _status_right );
			g.drawString( _status_right, right_start, _font_metrics[ STATUS ].getAscent() );
			// we no longer do a thin margin here because the scrollpanel
			// seems to supply a line
		}
	}

	String _load_error = null; // if an error is raised during load

	private static class Data {

		private String txt = null;

		public synchronized String get() {
			while ( txt == null ) {
				try {
					wait();
				} catch ( InterruptedException ie ) {
				} // nobody ever does stuff with this
			}
			return txt;
		}

		public synchronized void set( String s ) {
			txt = s;
			notify();
		}
	}
}
