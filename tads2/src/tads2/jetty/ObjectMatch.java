package tads2.jetty;

// a group of VocabWords gets resolved into an ObjectMatch
// (which cover match several objects, or "any box" or "2 of the chairs"
// or even "all")


import java.util.Vector;

public class ObjectMatch extends JettyRef {

	// this is an arbitrary object which we use to mean "the set of all objects"
	private static final Vector EVERYTHING = new Vector();

	// this object is initialized to match all objects, and then this
	// is narrowed down by adding VocabWords
	public ObjectMatch( final Jetty jetty, int size ) {
		this( jetty, size, 1, null );
	}

	public ObjectMatch( final Jetty jetty, int size, int count, Vector objects ) {
		super( jetty );
		_count = count;
		_size = size;
		_words = new Vector();

		if ( objects != null ) {
			_matches = new Vector( objects.size() );
			for ( int i = 0; i < objects.size(); i++ ) {
				_matches.addElement( new VocObj( ( (TObject)objects.elementAt( i ) ), 0 ) );
			}
		} else {
			_matches = EVERYTHING;
		}
	}

	public int get_count() {
		if ( ( _flags & Constants.PRSFLG_ANY ) != 0 ) {
			return 1;
		} else {
			return _count;
		}
	}

	public void set_count( int count ) {
		_count = count;
	}

	public int get_size() {
		return _size;
	}

	public boolean is_ambig() {
		int d = Constants.PRSFLG_ALL | Constants.PRSFLG_PLURAL |
			Constants.PRSFLG_ANY | Constants.PRSFLG_COUNT;
		return ( _flags & d ) == 0;
	}

	// note that "all" would count as zero matches here, and
	// "red" would return the number of possible matches (2, if there was
	// a "red box" and a "red ball", say).
	public int num_matches() {
		return _matches.size();
	}

	public Vector get_matches() {
		if ( _matches == EVERYTHING ) {
			return EVERYTHING;
		}

		Vector v = new Vector( _matches.size() );
		for ( int i = 0; i < _matches.size(); i++ ) {
			v.addElement( ( (VocObj)_matches.elementAt( i ) ).obj );
		}
		return v;
	}

	public int get_match_flag( int idx ) {
		if ( idx < 0 || idx >= _matches.size() ) {
			return -1;
		}

		return ( (VocObj)_matches.elementAt( idx ) ).flags;
	}

	public int get_match_flag( TObject obj ) {
		for ( int i = 0; i < _matches.size(); i++ ) {
			if ( ( (VocObj)_matches.elementAt( i ) ).obj == obj ) {
				return ( (VocObj)_matches.elementAt( i ) ).flags;
			}
		}

		String s = ( obj == null ) ? "null" : Integer.toString( obj.get_id() );
		print_error( "Requested flags for unknown object: " + s, 1 );
		Thread.dumpStack();
		return 0;
	}

	public Vector get_words() {
		return _words;
	}

	public String get_phrase() {
		String ret = "";
		for ( int i = 0; i < _words.size(); i++ ) {
			ret += ( i > 0 ? " " : "" ) + ( (VocabWord)_words.elementAt( i ) ).get_word();
		}
		return ret;
	}

	public int get_flags() {
		return _flags;
	}

	public boolean test_flags( int f ) {
		return ( _flags & f ) != 0;
	}

	public void set_flags( int f ) {
		_flags = f;
	}

	public ObjectMatch[] get_except() {
		return _except;
	}

	public void set_except( ObjectMatch[] except ) {
		if ( _except != null ) {
			for ( int i = 0; i < _except.length; i++ ) {
				_size -= _except[ i ].get_size();
			}
		}

		_except = except;

		if ( _except != null ) {
			for ( int i = 0; i < _except.length; i++ ) {
				_size += _except[ i ].get_size();
			}
		}
	}

	public void inc_size( int s ) {
		_size += s;
	}

	// add a vocab word; return true if it's known, false if unknown
	public boolean add_word( VocabWord word, String word2, int types )
		throws HaltTurnException {
		VocabWord other_word = null;
		// if this is a number, add in objects that have vocab '#'
		if ( word.is_number() ) {
			other_word = getState().lookup_vocab( "#" );
		} else if ( word.is_unknown() ) {
			_words.addElement( word );
			_size++;
			return false;
		}

		// if we get a plural and no noun, then disambiguation can be
		// turned off for this noun phrase
		boolean got_noun = false;
		boolean got_plural = false;

		Vector new_list = null;

		// check each possible type and add them to the new list
		for ( int i = 0; i < Constants.vocab_prop_flags.length; i++ ) {
			if ( ( types & Constants.vocab_prop_flags[ i ][ 1 ] ) == 0 ) {
				continue;
			}

			print_error( "Doing " + Constants.vocab_names[ i ] +
				" match of '" + word.get_word() + "'", 2 );

			int flags = 0;
			if ( Constants.vocab_prop_flags[ i ][ 0 ] == VocabWord.ADJECTIVE &&
				( types & Constants.PRSTYP_NOUN ) != 0 ) {
				flags |= Constants.PRSFLG_ENDADJ;
			}

			int[] objs = word.get_objects( Constants.vocab_prop_flags[ i ][ 0 ], word2 );
			if ( other_word != null ) {
				int[] objs2 = other_word.get_objects( Constants.vocab_prop_flags[ i ][ 0 ], word2 );
				if ( objs == null ) {
					objs = objs2;
				} else if ( objs2 != null ) {
					int[] objs3 = new int[ objs.length + objs2.length ];
					System.arraycopy( objs, 0, objs3, 0, objs.length );
					System.arraycopy( objs2, 0, objs3, objs.length, objs2.length );
					objs = objs3;
				}
			}

			// no words? skip it.
			if ( objs == null ) {
				print_error( "(no matching objects)", 2 );
				continue;
			}

			boolean added = false;
			for ( int j = 0; j < objs.length; j++ ) {
				VocObj vo = null;

				if ( _matches == EVERYTHING ) // always add this
				{
					TObject o = getState().lookup_object( objs[ j ] );
					if ( !o.is_class() ) // never count classes in the match
					{
						vo = new VocObj( o, flags );

						if ( new_list == null ) {
							new_list = new Vector();
						}
						new_list.addElement( vo );
						added = true;

						print_error( "adding obj=" + objs[ j ] +
							" since matches=EVERYTHING", 2 );
					} else {
						print_error( "not adding obj=" + objs[ j ] +
							" because it's a class", 2 );
					}
				} else {
					// search list and see if this object is there
					for ( int k = 0; k < _matches.size(); k++ ) {
						vo = (VocObj)_matches.elementAt( k );
						if ( vo.obj != null && vo.obj.get_id() == objs[ j ] ) {
							vo.flags |= flags;

							if ( new_list == null ) {
								new_list = new Vector();
							}
							new_list.addElement( vo );
							added = true;

							print_error( "adding obj=" + objs[ j ] +
								" since it was in matches", 2 );
							break;
						} else {
							vo = null;
						}
					}
				}

				// if the word is marked as truncated, we need to see if
				// this particular word is truncated (ie, the object's words
				// don't exactly match the typed word)
				if ( vo != null && word.is_truncated() ) {
					boolean exact = false;
					Vector words = vo.obj.get_vocab();
					for ( int k = 0; k < words.size(); k++ ) {
						if ( ( (VocabWord)words.elementAt( k ) ).get_word().equals( word.get_word() ) ) {
							exact = true;
							break;
						}
					}

					if ( !exact ) {
						vo.flags |= Constants.PRSFLG_TRUNC;
					}
				}
			}

			if ( added ) {
				if ( Constants.vocab_prop_flags[ i ][ 0 ] == VocabWord.PLURAL ) {
					got_plural = true;
				}
				if ( Constants.vocab_prop_flags[ i ][ 0 ] == VocabWord.NOUN ) {
					got_noun = true;
				}
			}
		}

		// check for a few special words:
		if ( word.is_specword( VocabWord.SPECWORD_ONE ) ) {
			if ( new_list == null ) {
				new_list = _matches;
			}
			got_noun = true;
		} else if ( word.is_specword( VocabWord.SPECWORD_ONES ) ) {
			if ( new_list == null ) {
				new_list = _matches;
			}
			got_plural = true;
		}

		if ( got_plural && !got_noun ) {
			_flags |= Constants.PRSFLG_PLURAL;
		}

		// in the old setup we would return false if new_list was null, but
		// I guess that's not required any more
		if ( new_list == null ) {
			new_list = new Vector( 0 );
		} else {
			// it seems like a good idea to sort the list at this point:
			for ( int i = 1; i < new_list.size(); i++ ) {
				for ( int j = 0; j < i; j++ ) {
					if ( ( (VocObj)new_list.elementAt( i ) ).obj.get_id() <
						( (VocObj)new_list.elementAt( j ) ).obj.get_id() ) {
						Object v = new_list.elementAt( i );
						new_list.setElementAt( new_list.elementAt( j ), i );
						new_list.setElementAt( v, j );
					}
				}
			}
		}

		_matches = new_list;
		_words.addElement( word );
		_size++;

		return true;
	}

	// just add the word onto the list, no questions asked
	public void append_word( VocabWord word ) {
		_words.addElement( word );
		_size++;
	}

	public void prepend_word( VocabWord word ) {
		_words.insertElementAt( word, 0 );
		_size++;
	}

	// just add this object on
	public void add_object( TObject obj ) {
		add_object( obj, 0 );
	}

	public void add_object( TObject obj, int flags ) {
		if ( _matches == EVERYTHING ) {
			_matches = new Vector();
		}
		for ( int i = 0; i < _matches.size(); i++ ) {
			VocObj vo = (VocObj)_matches.elementAt( i );
			if ( vo.obj == obj ) {
				vo.flags = flags;
				return;
			}
		}

		_matches.addElement( new VocObj( obj, flags ) );
	}

	public boolean merge( ObjectMatch om ) {
		Vector merged = null;
		if ( _matches == EVERYTHING && om._matches == EVERYTHING ) {
			merged = EVERYTHING;
		} else if ( _matches == EVERYTHING ) {
			merged = om._matches;
		} else if ( om._matches == EVERYTHING ) {
			merged = _matches;
		} else {
			for ( int i = 0; i < _matches.size(); i++ ) {
				VocObj vo = (VocObj)_matches.elementAt( i );
				for ( int j = 0; j < om._matches.size(); j++ ) {
					VocObj vo2 = (VocObj)om._matches.elementAt( j );

					if ( vo.obj == vo2.obj ) {
						if ( merged == null ) {
							merged = new Vector();
						}
						merged.addElement( vo );

						// it's not exactly clear what to do with the flags in this
						// case. I think we should or-merge them.
						vo.flags |= vo2.flags;
					}
				}
			}
		}

		if ( merged == null ) {
			return false;
		}

		_matches = merged;

		for ( int i = 0; i < om._words.size(); i++ ) {
			_words.addElement( om._words.elementAt( i ) );
		}

		return true;
	}

	public void copy_words( ObjectMatch o ) {
		_words = (Vector)o._words.clone();
		_size = o._size;
	}

	public String toString() {
		String ret = "count=" + _count;
		ret += " objects=[";

		if ( _matches == EVERYTHING ) {
			ret += "EVERYTHING";
		} else {
			for ( int i = 0; i < _matches.size(); i++ ) {
				VocObj vo = (VocObj)_matches.elementAt( i );
				ret += ( i == 0 ? "" : " " ) +
					( vo.obj == null ? "NIL" : Integer.toString( vo.obj.get_id() ) ) +
					"(" + vo.flags + ")";
			}
		}
		ret += "] flags=" + _flags;
		ret += "\nsize=" + _size + " words=[";
		for ( int i = 0; i < _words.size(); i++ ) {
			ret += ( i == 0 ? "" : " " ) +
				( (VocabWord)_words.elementAt( i ) ).get_word();
		}
		ret += "]";
		return ret;
	}

	// a single object and its flags
	private static class VocObj {

		public TObject obj = null;
		public int flags = 0;

		public VocObj( TObject o, int f ) {
			obj = o;
			flags = f;
		}
	}

	private int _count;
	private int _flags;
	private Vector _matches;
	private ObjectMatch[] _except; // exclude these objects from match

	private int _size;
	private Vector _words; // the vocabwords that make up this match
}
