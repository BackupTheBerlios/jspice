package tads2.jetty;


import java.util.Vector;
import java.io.StringWriter;

// This is the class that takes the output from the game and parses it
// before displaying for the user. Things like escapes ("\n", "\t", "\b") are
// handled as well as html tags (which are just stripped in this version,
// although if someone is insane enough to implement an HTMLOutputFormatter
// subclass I am all for it) and space-compression and so on.

public class OutputFormatter extends JettyRef {

	public final static int DEFAULT_TAB_SIZE = 4; // number of spaces per tab

	public OutputFormatter( final Jetty jetty, PlatformIO io ) {
		super( jetty );
		_platform_io = io;
	}

	public void print( String text )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// possibly filter it first:
		if ( _filter != null ) {
			TValue arg = newTValue( TValue.SSTRING, text );
			TValue ret = getRunner().run( _filter.get_data(), this.arg_array( arg ) );
			if ( ret.get_type() == TValue.SSTRING ) {
				text = ret.get_string();
			}
			// apparently any other return type is allowed & ignored
		}

		// we go through and handle the %-escapes first; why? because we want to
		// skip out right after that if we're doing an outhide()/outcapture()
		text = expand_format_strings( text );

		if ( text.length() == 0 ) {
			return;
		}

		// now, if we're hiding/capturing output, handle those:

		// if there's any outhide() calls going on, just set every one
		// that's happening to true, since they've all now had some output
		// going on (and, of course, don't print anything)
		if ( _outhides.size() > 0 ) {
			for ( int i = 0; i < _outhides.size(); i++ ) {
				_outhides.setElementAt( Boolean.TRUE, i );
			}

			return; // and we're done printing
		}

		// or if there's any outcapture() calls going on, print this string
		// to every one (and don't print anything to stdout).
		// note that nesting makes no difference here: if an outhide surrounds
		// some printed text, it will not be printed, and if there is an
		// outcapture nested within that it will capture no text
		else if ( _outcaptures.size() > 0 ) {
			for ( int i = 0; i < _outcaptures.size(); i++ ) {
				( (StringWriter)_outcaptures.elementAt( i ) ).write( text );
			}
			return; // and we're done printing
		}

		// ok, at last we can do the print for real:
		LOOP: for ( int i = 0; i < text.length(); i++ ) {
			char c = text.charAt( i );

			// skip over high-bit characters (noted by Stephen Newton)
			if ( Character.isISOControl( c ) ) {
				continue;
			}

			// first consider html mode
			if ( _html_mode ) {
				if ( _current_entity != null ) {
					if ( Character.isLetterOrDigit( c ) || c == '#' ) {
						_current_entity.append( c );
					} else {
						// we could be fussy and insist on a ;, but if they are in html
						// mode and have "&foo ", what are we supposed to do?
						process_entity( _current_entity.toString() );
						_current_entity = null;
					}

					continue;
				} else if ( _current_tag != null ) {
					if ( c != '>' ) {
						_current_tag.append( c );
					} else {
						process_tag( _current_tag.toString() );
						_current_tag = null;
					}

					continue;
				} else if ( c == '&' ) {
					_current_entity = new StringBuffer( 5 );
					continue;
				} else if ( c == '<' ) {
					_current_tag = new StringBuffer( 5 );
					continue;
				}
			}

			// stupid hack because we don't want to flush in the middle
			// of a series of hyphens
			if ( _last_printed == '-' && c != '-' ) {
				flush();
				_last_printed = 0;
			}

			// So the escapes are:
			// n, t, b, ^, v, H+, H-, [space], (, ), -, everything else
			if ( c == '\\' ) {
				c = text.charAt( ++i ); // safe -- compiler checks that strings
				// don't end with unescaped slash
				switch ( c ) {
					case 'n':
						{
							print_newline();
						}
						break;

					case 'b':
						{
							print_blankline();
						}
						break;

					case 't':
						{
							print_tab( DEFAULT_TAB_SIZE );
						}
						break;

					case '^':
					case 'v':
						{
							_last_printed = c;
						}
						break;

					case '(':
						{
							if ( !printing_status() ) {
								flush(); // we flush here to ensure the last word is not bolded
								_platform_io.set_style( PlatformIO.BOLD, true );
							}
						}
						break;

					case ')':
						{
							if ( !printing_status() ) {
								// what does normal tads do for nesting and
								// so on? (and mismatched close \)s)
								flush(); // we flush here to ensure the last word is bolded
								_platform_io.set_style( PlatformIO.BOLD, false );
							}
						}
						break;

					case '-':
						{
							add_char( '-', '-' ); // non-breaking hyphen
						}
						break;

					case ' ':
						{
							add_char( ' ', ' ' ); // non-breaking space
						}
						break;

					case 'H':
						{
							// html-toggle mode, which can have + or - as an argument
							// (or any other character, really, which counts as +).
							_html_mode = true;
							if ( ++i < text.length() && text.charAt( i ) == '-' ) {
								_html_mode = false;
							}
						}
						break;

					default:
						{
							add_char( c, '\0' ); // everything else is literal
						}
				}
			} else if ( c == ' ' ) {
				if ( !Character.isWhitespace( _last_printed ) ) {
					add_char( ' ', ' ' );
				}
				flush();
			} else if ( c == '-' ) {
				// possibly-breaking hyphen; but we can't just flush here because
				// we *don't* want to break if we're in the middle of a series of
				// hyphens
				add_char( '-', '-' );
			} else {
				if ( _last_printed == '^' ) {
					c = Character.toUpperCase( c );
				} else if ( _last_printed == 'v' ) {
					c = Character.toLowerCase( c );
				}

				add_char( c, '\0' );
			}
		}
	}

	private void add_char( char c, char last ) {
		_current_word.append( c );
		_last_printed = last;
		wrap_long_words();
	}

	private String expand_format_strings( String text )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TObject actor = getState().get_actor();

		for ( int i = 0; i < text.length(); i++ ) {
			if ( text.charAt( i ) == '\\' ) {
				i++;
				continue;
			} else if ( text.charAt( i ) == '%' ) {
				// find the next %:
				int j;
				for ( j = i + 1; j < text.length(); j++ ) {
					if ( text.charAt( j ) == '%' ) {
						break;
					}
				}

				// if there were no matches, fine, whatever, this isn't a real %.
				if ( j >= text.length() ) {
					return text;
				}

				// if there was another %, then look up the text between here and there
				String fmt = text.substring( i + 1, j );
				int fmtprop = getState().lookup_fmtstr( fmt.toLowerCase() );

				// if this isn't a recognized format string, then skip ahead:
				if ( fmtprop == -1 ) {
					i = j;
					continue;
				}

				// ok, so this is a real format string, so run it, insert the resulting
				// text in the string, and then continue
				int tok = capture_output();
				actor.eval_property( fmtprop, this.arg_array() );
				String fmt_result = uncapture_output( tok );

				// force uppercase if the fmt is uppercased:
				if ( fmt_result.length() > 0 && Character.isUpperCase( fmt.charAt( 0 ) ) ) {
					// if first *two* are uppercased, assume all caps
					if ( Character.isUpperCase( fmt.charAt( 1 ) ) ) {
						fmt_result = fmt_result.toUpperCase();
					}

					// else just caps on the first letter
					else {
						fmt_result = fmt_result.substring( 0, 1 ).toUpperCase() +
							fmt_result.substring( 1 );
					}
				}

				text = text.substring( 0, i ) + fmt_result + text.substring( j + 1 );
				i += fmt_result.length(); // and it gets incremented again by loop
			}
		}

		return text;
	}

	// switch into or out of statusline-printing mode
	// (and when we switch out, actually send the updated status line to the
	// screen)
	public void print_statusline( boolean start, boolean left ) {
		if ( start ) {
			flush();
			_status_line = new StringBuffer( 10 );
			_status_line.append( _last_printed ); // store this here
			return;
		}

		// this always prints to the statusline and is not affected by
		// outhide() or outcapture()
		flush();
		_platform_io.set_status_string( _status_line.toString().substring( 1 ), left );
		_last_printed = _status_line.charAt( 0 ); // and restore it
		_status_line = null;
	}

	// note that it can be relatively expensive to construct the string argument
	// to this function, even if it's not printed. For higher debug levels
	// (3 and 4) you probably want to make sure this function is never
	// called, not just that it doesn't do anything. so wrap the call in an
	// actual if statement.
	public void print_error( String text, int level ) {
		if ( get_debug_level() >= level ) {
			_platform_io.print_error( text );
		}
	}

	// wrap if necessary and print out what we've got so far
	public void flush() {
		if ( !printing_status() ) {
			int sz = _platform_io.size_text( _current_word.toString() );
			if ( _last_column + sz >= _wrap_column ) {
				inc_line();
			}

			_last_column += sz;
			_platform_io.print_text( _current_word.toString() );
			_current_word.setLength( 0 );
		} else {
			_status_line.append( _current_word.toString() );
			_current_word.setLength( 0 );
		}
	}

	// check if this word is longer than the wrap length just by itself:
	private void wrap_long_words() {
		if ( printing_status() ) {
			return;
		}

		String cur = _current_word.toString();
		if ( _platform_io.size_text( cur ) >= _wrap_column ) {
			// print out the first bit:
			String trim = cur;
			while ( _platform_io.size_text( trim ) >= _wrap_column ) {
				trim = trim.substring( 0, trim.length() - 1 );
			}

			_platform_io.print_text( trim );
			inc_line();

			// then truncate the rest:
			String tail = cur.substring( trim.length() );

			if ( _current_word.length() == _wrap_column ) // wrapped exactly?
			{
				_last_printed = '\n';
			}

			_current_word = new StringBuffer( tail );
		}
	}

	private void inc_line() {
		if ( !printing_status() ) {
			// scroll the window up, then decide if we need to print a more prompt:
			_last_column = 0;

			_platform_io.scroll_window();

			// Stephen Newton noted this wasn't pausing early enough
			if ( ++_last_line >= _wrap_line ) {
				if ( _platform_io.more_prompt( "[More]" ) ) {
					_last_line = 0;  // they hit space
				} else {
					_last_line--;    // they hit return, so advance only one line
				}
			}
		}
	}

	private void print_newline() {
		if ( printing_status() ) {
			return;
		}

		flush();
		if ( _last_printed != '\n' ) {
			inc_line();
		}
		_last_printed = '\n';
	}

	private void print_blankline() {
		if ( printing_status() ) {
			return;
		}

		flush();
		// b = a blank line, so one newline if following a newline,
		// two newlines otherwise
		inc_line();
		if ( _last_printed != '\n' ) {
			inc_line();
		}
		_last_printed = '\n';
	}

	private void print_tab( int num_spaces ) {
		flush();

		int sz = _platform_io.size_text( ' ' );

		for ( int i = 0; i < num_spaces && _last_column < _wrap_column; i++ ) {
			_current_word.append( ' ' );
			_last_printed = ' ';
			_last_column += sz;
		}
	}

	// reset the column marker to start of line and clear the line count
	public void input_entered() {
		_last_column = 0;
		_last_line = 0;
		_last_printed = '\n';
	}

	public void set_filter( TObject filter ) {
		_filter = filter;
	}

	public void clear_screen() {
		if ( no_output() || printing_status() ) {
			return;
		}

		_platform_io.clear_screen();
	}

	public void set_platform_io( PlatformIO io ) {
		_platform_io = io;
	}

	public void resize( int columns, int lines ) {
		_wrap_column = columns;
		_wrap_line = lines - 1; // -1, to save room for the [More] line

		_last_column = 0;
		_last_line = 0;
		_current_word.setLength( 0 );
	}

	public int hide_output() {
		flush(); // don't include previous stuff in this hide

		_outhides.addElement( Boolean.FALSE );
		return _outhides.size() - 1;
	}

	public boolean unhide_output( int x ) {
		flush(); // include everything so far in this hide

		if ( _outhides.size() <= x ) {
			return false;
		}

		boolean ret = ( (Boolean)_outhides.elementAt( x ) ).booleanValue();

		// this is sort of bogus, but apparently if you turn off outhiding,
		// it turns off all outhides that are nested within it. I guess
		// that makes sense but I wish it was explicitly stated.
		while ( _outhides.size() > x ) {
			_outhides.removeElementAt( _outhides.size() - 1 );
		}

		return ret;
	}

	public int capture_output() {
		flush(); // don't include previous stuff in this capture

		_outcaptures.addElement( new StringWriter() );
		return _outcaptures.size() - 1;
	}

	public String uncapture_output( int x ) {
		flush(); // include everything so far in this capture

		if ( _outcaptures.size() <= x ) {
			return "";
		}

		String ret = _outcaptures.elementAt( x ).toString();

		while ( _outcaptures.size() > x ) {
			_outcaptures.removeElementAt( _outcaptures.size() - 1 );
		}

		return ret;
	}

	// abort all output hiding/capturing
	public void cancel_outhiding() {
		_outcaptures.removeAllElements();
		_outhides.removeAllElements();
	}

	// returns true if output is not being displayed
	private boolean no_output() {
		return _outcaptures.size() > 0 || _outhides.size() > 0;
	}

	// returns true if we're printing the status line
	private boolean printing_status() {
		return _status_line != null;
	}

	public void print_more_prompt()
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( no_output() || printing_status() ) {
			return;
		}

		_last_line = _wrap_line;

		if ( _platform_io.more_prompt( "[More]" ) ) {
			_last_line = 0;  // they hit space
		} else {
			_last_line--;    // they hit return, so advance only one line
		}
	}

	private void process_entity( String e ) {
		e = e.toLowerCase();
		if ( e.equals( "amp" ) ) {
			add_char( '&', '\0' );
		} else if ( e.equals( "gt" ) ) {
			add_char( '>', '\0' );
		} else if ( e.equals( "lt" ) ) {
			add_char( '<', '\0' );
		} else if ( e.equals( "quot" ) || e.equals( "rdquo" ) || e.equals( "ldquo" ) ) {
			add_char( '\"', '\0' );
		} else if ( e.equals( "lsquo" ) || e.equals( "rsquo" ) ) {
			add_char( '\'', '\0' );
		} else if ( e.equals( "copy" ) ) {
			add_char( '(', '\0' );
			add_char( 'c', '\0' );
			add_char( ')', '\0' );
		} else if ( e.equals( "mdash" ) ) {
			add_char( '-', '-' );
			add_char( '-', '-' );
		} else if ( e.equals( "ndash" ) ) {
			add_char( '-', '-' );
		} else if ( e.equals( "nbsp" ) ) {
			add_char( ' ', ' ' );
		} else {
			print_error( "Unknown entity=" + e, 2 );
			add_char( '?', '\0' );
		}
	}

	private void process_tag( String t ) {
		String tag = t.toLowerCase();

		{
			int ix = tag.indexOf( " " );
			if ( ix != -1 ) {
				tag = tag.substring( 0, ix );
			}
		}

		if ( tag.equals( "p" ) ) {
			print_blankline();
		} else if ( tag.equals( "br" ) ) {
			print_newline();
		} else if ( tag.equals( "b" ) ) {
			if ( !printing_status() ) {
				flush(); // we flush here to ensure the last word is not bolded
				_platform_io.set_style( PlatformIO.BOLD, true );
			}
		} else if ( tag.equals( "/b" ) ) {
			if ( !printing_status() ) {
				flush(); // we flush here to ensure the last word is bolded
				_platform_io.set_style( PlatformIO.BOLD, false );
			}
		} else if ( tag.equals( "i" ) ) {
			if ( !printing_status() ) {
				flush(); // we flush here to ensure the last word is not italicized
				_platform_io.set_style( PlatformIO.ITALIC, true );
			}
		} else if ( tag.equals( "/i" ) ) {
			if ( !printing_status() ) {
				flush(); // we flush here to ensure the last word is italicized
				_platform_io.set_style( PlatformIO.ITALIC, false );
			}
		} else if ( tag.equals( "tab" ) ) {
			int cnt = DEFAULT_TAB_SIZE;
			int ix = t.toLowerCase().indexOf( "multiple=" );
			if ( ix != -1 ) {
				t = t.substring( ix + "multiple=".length() );
				ix = t.indexOf( " " );
				if ( ix != -1 ) {
					t = t.substring( 0, ix );
				}

				try {
					cnt = Integer.parseInt( t );
				} catch ( NumberFormatException nfe ) {
					print_error( "Unknown tab multiple value=" + t, 2 );
					cnt = DEFAULT_TAB_SIZE;
				}
			}

			print_tab( cnt );
		} else if ( tag.equals( "q" ) ) {
			if ( _quote_level % 2 == 0 ) {
				add_char( '"', '\0' );
			} else {
				add_char( '\'', '\0' );
			}

			_quote_level++;
		} else if ( tag.equals( "/q" ) ) {
			_quote_level--;

			if ( _quote_level % 2 == 0 ) {
				add_char( '"', '\0' );
			} else {
				add_char( '\'', '\0' );
			}
		}
	}

	private PlatformIO _platform_io = null;

	private int _wrap_column = 78;
	private int _wrap_line = 20;
	private StringBuffer _current_word = new StringBuffer( 10 );
	// note that this isn't the last character printed, but a token
	// of the significance of the last character printed:
	// ^ and v for force upper/lower, a space for a space, \n for a
	// newline/blank line, . for a period, \0 for insignificant
	private char _last_printed = 0;
	private int _last_column = 0;
	private int _last_line = 0;

	private Vector _outhides = new Vector( 1 );
	private Vector _outcaptures = new Vector( 1 );

	private TObject _filter = null;

	private StringBuffer _status_line = null;

	private boolean _html_mode = false;
	private StringBuffer _current_entity = null;
	private StringBuffer _current_tag = null;
	private int _quote_level = 0;
}
