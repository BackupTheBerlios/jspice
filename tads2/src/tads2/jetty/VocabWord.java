package tads2.jetty;

// a single vocab word typeable by the player (or a quoted phrase), plus
// its part of speech and other flags

import java.util.Vector;

public class VocabWord extends JettyRef {

	// these ids need to be the same ones as in RequiredProperties.VERB, etc
	public final static int VERB = 2;
	public final static int NOUN = 3;
	public final static int ADJECTIVE = 4;
	public final static int PREPOSITION = 5;
	public final static int ARTICLE = 6;
	public final static int PLURAL = 7;
	public final static int UNKNOWN = -1;

	public static final String SPECWORD_AND = ",";
	public static final String SPECWORD_THEN = ".";
	public static final String SPECWORD_OF = "O";
	public static final String SPECWORD_ALL = "A";
	public static final String SPECWORD_BOTH = "B";
	public static final String SPECWORD_IT = "I";
	public static final String SPECWORD_HIM = "M";
	public static final String SPECWORD_ONE = "N";
	public static final String SPECWORD_ONES = "P";
	public static final String SPECWORD_HER = "R";
	public static final String SPECWORD_THEM = "T";
	public static final String SPECWORD_BUT = "X";
	public static final String SPECWORD_ANY = "Y";

	// use this constructor for doing unknown words
	public VocabWord( final Jetty jetty, String word, int special_type ) {
		this( jetty, word, null, -1, special_type );
	}

	// use this constructor for regular vocab words
	public VocabWord( final Jetty jetty, String word, String word2, int obj, int type ) {
		super( jetty );
		_word = word;
		_word2s = new String[ 1 ];
		_word2s[ 0 ] = word2;
		_types = new int[ 1 ];
		_types[ 0 ] = type;
		if ( obj != -1 ) {
			_objects = new int[ 1 ][ 1 ];
			_objects[ 0 ][ 0 ] = obj;
		} else {
			_objects = new int[ 1 ][ 0 ];
		}

		try {
			_number_value = Integer.parseInt( _word );
		} catch ( NumberFormatException nfe ) {
			_number_value = Integer.MIN_VALUE;
		}
	}

	public String get_word() {
		return _word;
	}

	public void set_word( String w ) {
		_word = w;
	}

	public boolean is_specword() {
		return _specword != null;
	}

	public boolean is_specword( String s ) {
		return _specword != null && _specword.equals( s );
	}

	public boolean is_unknown() {
		return _types[ 0 ] == UNKNOWN && !is_specword();
	}

	public String get_specword() {
		return _specword;
	}

	public boolean is_number() {
		return ( _number_value != Integer.MIN_VALUE );
	}

	public int get_number() {
		return _number_value;
	}

	public void is_string( boolean b ) {
		_is_string = b;
	}

	public boolean is_string() {
		return _is_string;
	}

	public void truncated( boolean b ) {
		_truncated = b;
	}

	public boolean is_truncated() {
		return _truncated;
	}

	// note that this vocab word is the following kind of specialWord
	public void set_specword( String specword ) {
		_specword = specword;
	}

	public boolean has_objects( int type ) {
		return get_objects( type, null ) != null;
	}

	public boolean has_objects( int type, String word2 ) {
		return get_objects( type, word2 ) != null;
	}

	public int get_first_object( int type, String word2 ) throws HaltTurnException {
		int[] objs = get_objects( type, word2 );
		// start from the end, pick the first non-class object
		// (starting from the end is arbitrary but seems more likely to be
		// right since if there are classes they will hopefully be earlier)
		if ( objs != null && objs.length > 0 ) {
			for ( int i = objs.length - 1; i >= 0; i-- ) {
				if ( !getState().lookup_object( objs[ i ] ).is_class() ) {
					return objs[ i ];
				}
			}
		}

		return -1;
	}

	public int[] get_objects( int type, String word2 ) {
		for ( int i = 0; i < _types.length; i++ ) {
			if (
				_types[ i ] == type &&
				( ( _word2s[ i ] == null && word2 == null ) ||
				( _word2s[ i ] != null && _word2s[ i ].equals( word2 ) ) )
			) {
				return _objects[ i ];
			}
		}

		return null;
	}

	// fills in types and word2s with the lists of matches for obj in this
	// word. returns true if it found any matches, false else.
	public boolean get_object( int obj, Vector types, Vector word2s ) {
		boolean found = false;
		for ( int i = 0; i < _objects.length; i++ ) {
			for ( int j = 0; j < _objects[ i ].length; j++ ) {
				if ( _objects[ i ][ j ] == obj ) {
					found = true;
					types.addElement( new Integer( _types[ i ] ) );
					word2s.addElement( _word2s[ i ] );
				}
			}
		}

		return found;
	}

	public void add_obj( String word2, int obj, int type ) {
		for ( int i = 0; i < _types.length; i++ ) {
			if ( _types[ i ] == type &&
				( _word2s[ i ] == null ? word2 == null : _word2s[ i ].equals( word2 ) ) ) {
				for ( int j = 0; j < _objects[ i ].length; j++ ) {
					if ( _objects[ i ][ j ] == obj ) {
						return; // already added
					}
				}

				int[] o = new int[ _objects[ i ].length + 1 ];
				System.arraycopy( _objects[ i ], 0, o, 0, _objects[ i ].length );
				o[ _objects[ i ].length ] = obj;
				_objects[ i ] = o;
				return;
			} else if ( _types[ i ] == UNKNOWN ) {
				_word2s[ i ] = word2;
				_types[ i ] = type;
				int[] o = new int[ 1 ];
				o[ 0 ] = obj;
				_objects[ i ] = o;
				return;
			}
		}

		int[] t = new int[ _types.length + 1 ];
		System.arraycopy( _types, 0, t, 0, _types.length );
		t[ _types.length ] = type;
		_types = t;

		String[] w = new String[ _word2s.length + 1 ];
		System.arraycopy( _word2s, 0, w, 0, _word2s.length );
		w[ _word2s.length ] = word2;
		_word2s = w;

		int[][] o = new int[ _objects.length + 1 ][];
		System.arraycopy( _objects, 0, o, 0, _objects.length );
		o[ _objects.length ] = new int[ 1 ];
		o[ _objects.length ][ 0 ] = obj;
		_objects = o;
	}

	public void remove_obj( String word2, int obj, int type ) {
		for ( int i = 0; i < _types.length; i++ ) {
			if ( _types[ i ] == type &&
				( _word2s[ i ] == null ? word2 == null : _word2s[ i ].equals( word2 ) ) ) {
				for ( int j = 0; i < _objects[ i ].length; j++ ) {
					if ( _objects[ i ][ j ] == obj ) {
						// copy the last entry down over this, then shrink the array
						_objects[ i ][ j ] = _objects[ i ][ _objects[ i ].length - 1 ];
						int[] o = new int[ _objects[ i ].length - 1 ];
						System.arraycopy( _objects[ i ], 0, o, 0, o.length );
						_objects[ i ] = o;
						return;
					}
				}
			}
		}
	}

	public int get_flags() {
		int flags = 0;

		for ( int i = 0; i < Constants.vocab_prop_flags.length; i++ ) {
			if ( get_objects( Constants.vocab_prop_flags[ i ][ 0 ], null ) != null ) {
				flags |= Constants.vocab_prop_flags[ i ][ 1 ];
			}
		}

		if ( is_unknown() ) {
			flags |= Constants.PRSTYP_UNKNOWN;
		}
		if ( is_specword() ) {
			flags |= Constants.PRSTYP_SPEC;
		}

		return flags;
	}

	public VocabWord do_clone() {
		VocabWord vw = new VocabWord( this.getJetty(), _word, null, -1, -1 );
		vw._word2s = new String[ _word2s.length ];
		System.arraycopy( _word2s, 0, vw._word2s, 0, _word2s.length );
		vw._types = new int[ _types.length ];
		System.arraycopy( _types, 0, vw._types, 0, _types.length );

		vw._objects = new int[ _objects.length ][];
		for ( int i = 0; i < vw._objects.length; i++ ) {
			vw._objects[ i ] = new int[ _objects[ i ].length ];
			System.arraycopy( _objects[ i ], 0, vw._objects[ i ], 0, _objects[ i ].length );
		}

		vw._specword = _specword;
		vw._number_value = _number_value;
		vw._is_string = _is_string;
		vw._truncated = _truncated;

		return vw;
	}

	public void merge( VocabWord vw ) {
		// inefficient but easy:
		for ( int i = 0; i < vw._objects.length; i++ ) {
			for ( int j = 0; j < vw._objects[ i ].length; j++ ) {
				add_obj( vw._word2s[ i ], vw._objects[ i ][ j ], vw._types[ i ] );
			}
		}
	}

	public String toString() {
		String[] type_names = {"?", "??", "verb", "noun", "adj", "prep", "art",
							   "plu"};

		String ret = _word + " (";
		if ( is_truncated() ) {
			ret += "TRUNCATED ";
		}

		if ( is_specword() ) {
			ret += "SPECIAL=" + _specword + " ";
		}

		if ( _types[ 0 ] == UNKNOWN ) {
			ret += "UNKNOWN";
		} else {
			for ( int i = 0; i < _types.length; i++ ) {
				ret += ( ( i != 0 ) ? " " : "" ) + "[" + type_names[ _types[ i ] ];
				if ( _word2s[ i ] != null ) {
					ret += " w/'" + _word2s[ i ] + "'";
				}
				ret += ":";
				for ( int j = 0; j < _objects[ i ].length; j++ ) {
					ret += " " + _objects[ i ][ j ];
				}
				ret += "]";
			}
		}

		return ret + ")";
	}

	String _word;
	String[] _word2s;
	int[] _types;
	int[][] _objects;
	String _specword;
	int _number_value;
	boolean _is_string;
	boolean _truncated;
}
