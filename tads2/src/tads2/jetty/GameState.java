package tads2.jetty;


import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

public class GameState extends JettyRef {

	public GameState( Jetty jetty ) {
		super( jetty );
	}

	// this is the length after which vocab words can be truncated
	// (although we use all the letters they type)
	public final static int TRUNCATE = 6;

	// max number of undo records to keep:
	public final static int MAX_UNDO = 300; // probably around 30 turns

	// daemon timer values:
	public final static int CONTINUOUS = -999;
	public final static int EXPIRED = 0;

	public void set_version( String v ) {
		_version = v;
	}

	public void set_flags( int f ) {
		_flags = f;
	}

	public void set_timestamp( String t ) {
		_timestamp = t;
	}

	// initialization run after the game file's loaded,
	// called from GameFileParser automatically
	public void init() throws HaltTurnException {
		_parser_me = lookup_required_object( RequiredObjects.ME );
		_command_objects[ 0 ] = _parser_me; // start this off right

		// add a few more specwords things to make it easier later
		if ( !_specwords.containsKey( "," ) ) {
			_specwords.put( ",", "," );
		}
		if ( !_specwords.containsKey( ":" ) ) {
			_specwords.put( ":", "," );
		}
		if ( !_specwords.containsKey( "." ) ) {
			_specwords.put( ".", "." );
		}
		if ( !_specwords.containsKey( "!" ) ) {
			_specwords.put( "!", "." );
		}
		if ( !_specwords.containsKey( "?" ) ) {
			_specwords.put( "?", "." );
		}
		if ( !_specwords.containsKey( ";" ) ) {
			_specwords.put( ";", "." );
		}

		// mark all special vocab words as being special
		for ( Enumeration e = _vocab.elements(); e.hasMoreElements(); ) {
			VocabWord[] vws = (VocabWord[])e.nextElement();
			for ( int i = 0; i < vws.length; i++ ) {
				vws[ i ].set_specword( lookup_specword( vws[ i ].get_word() ) );
			}
		}

		// now we have to set all the contents fields, blah

		// first go through all objects, and set their containing object's contents
		Enumeration objects = _objects.keys();
		for ( Enumeration e = _objects.elements(); e.hasMoreElements(); ) {
			TObject o = (TObject)e.nextElement();
			if ( !o.is_object() || o.is_class() ) {
				continue;
			}

			TValue loc = o.lookup_property( RequiredProperties.LOCATION );

			if ( loc != null && loc.get_type() == TValue.OBJECT ) {
				TObject loc_obj = lookup_object( loc.get_object() );
				// I think it has to have an actual contents property, not one
				// inherited from parent:
				TValue contents = loc_obj.lookup_property( RequiredProperties.CONTENTS,
					false );
				Vector contents_list = null;
				if ( contents == null || contents.get_type() == TValue.DEMAND ) {
					contents_list = new Vector();
					loc_obj.set_property( RequiredProperties.CONTENTS,
						newTValue( TValue.LIST, contents_list ) );
				} else {
					contents_list = contents.get_list();
				}

				contents_list.addElement( newTValue( TValue.OBJECT, o.get_id() ) );
			}

			// and set this object's contents field, if any, to []
			// (in case it's a container that holds no objects initially)
			{
				TValue contents = o.lookup_property( RequiredProperties.CONTENTS, false );
				// the contents == null from above becomes != null because
				// we don't set the contents field to anything here unless
				// it's necessary
				if ( contents != null && contents.get_type() == TValue.DEMAND ) {
					o.set_property( RequiredProperties.CONTENTS,
						newTValue( TValue.LIST, new Vector() ) );
				}
			}
		}
	}

	// add a vocab/object record, storing it with the first six letters of the
	// word as the key
	public void add_vocab( int obj, int prop, int flags, String word,
		String word2 )
		throws HaltTurnException {
		String trunc = ( word.length() > TRUNCATE ) ? word.substring( 0, TRUNCATE ) :
			word;
		VocabWord[] vws = (VocabWord[])_vocab.get( trunc );

		// if the word already exists in here, add to the existing object
		for ( int i = 0; vws != null && i < vws.length; i++ ) {
			if ( vws[ i ].get_word().equals( word ) ) {
				// tell the object we're one of its vocab words:
				( (TObject)lookup_object( obj ) ).add_vocab( vws[ i ] );

				vws[ i ].add_obj( word2, obj, prop );
				return;
			}
		}

		// otherwise, either add the array or add an element to it
		if ( vws == null ) {
			vws = new VocabWord[ 1 ];
		} else {
			VocabWord[] tmp = new VocabWord[ vws.length + 1 ];
			System.arraycopy( vws, 0, tmp, 0, vws.length );
			vws = tmp;
		}

		vws[ vws.length - 1 ] = new VocabWord( this.getJetty(), word, word2, obj, prop );
		// this will never come up in practice, but:
		vws[ vws.length - 1 ].set_specword( lookup_specword( word ) );

		// tell the object we're one of its vocab words:
		( (TObject)lookup_object( obj ) ).add_vocab( vws[ vws.length - 1 ] );

		_vocab.put( trunc, vws );
	}

	public void del_vocab( int obj, int prop, String word, String word2 )
		throws HaltTurnException {
		String trunc = ( word.length() > TRUNCATE ) ? word.substring( 0, TRUNCATE ) :
			word;
		VocabWord[] vws = (VocabWord[])_vocab.get( trunc );
		for ( int i = 0; vws != null && i < vws.length; i++ ) {
			if ( vws[ i ].get_word().equals( word ) ) {
				vws[ i ].remove_obj( word2, obj, prop );
				( (TObject)lookup_object( obj ) ).rem_vocab( vws[ i ] );
				break;
			}
		}
	}

	// look up a vocab word by the first six letters
	public VocabWord lookup_vocab( String word ) {
		String trunc = ( word.length() > TRUNCATE ) ? word.substring( 0, TRUNCATE ) :
			word;
		VocabWord[] vws = (VocabWord[])_vocab.get( trunc );

		VocabWord vw = null;

		for ( int i = 0, cnt = 0; vws != null && i < vws.length; i++ ) {
			// if this's word matches all the letters the player actually typed...
			if ( vws[ i ].get_word().startsWith( word ) ) {
				// if it's an exact match, just use it
				if ( vws[ i ].get_word().equals( word ) ) {
					return vws[ i ];
				}

				cnt++;

				// if there are no other matches so far, just grab the word
				if ( cnt == 1 ) {
					vw = vws[ i ];
				}

				// else if there's one other match so far, set up a merged object,
				// mark it with the truncated flag, and make that the word
				else if ( cnt == 2 ) {
					vw = vw.do_clone();
					vw.set_word( word );
					vw.merge( vws[ i ] );
					vw.truncated( true );
				}

				// otherwise there must be a merged object already, so add this to it
				else {
					vw.merge( vws[ i ] );
				}
			}
		}

		if ( vw == null ) {
			vw = new VocabWord( this.getJetty(), word, VocabWord.UNKNOWN );
			vw.set_specword( lookup_specword( word ) );
		}

		return vw;
	}

	public void add_compound_word( String w1, String w2, String w3 ) {
		_compounds.put( w1 + " " + w2, w3 );
	}

	public String lookup_compound_word( String w1, String w2 ) {
		return (String)_compounds.get( w1 + " " + w2 );
	}

	public void add_specword( char abbr, String word ) {
		_specwords.put( word, String.valueOf( abbr ) );
	}

	public String lookup_specword( String word ) {
		return (String)_specwords.get( word );
	}

	public void add_inheritance( int obj, int loc, int inhloc, int flags,
		int[] superclasses ) {
		int[] arr = new int[ superclasses.length + 3 ];
		arr[ 0 ] = loc;
		arr[ 1 ] = inhloc;
		arr[ 2 ] = flags;
		System.arraycopy( superclasses, 0, arr, 3, superclasses.length );
		_inheritance.put( new Integer( obj ), arr );
	}

	public void set_required( int[] required ) {
		_required_objects = required;
	}

	public void add_fmtstr( String word, int prop ) {
		_fmtstrs.put( word, new Integer( prop ) );
	}

	public int lookup_fmtstr( String word ) {
		Integer x = (Integer)_fmtstrs.get( word );
		if ( x == null ) {
			return -1;
		} else {
			return x.intValue();
		}
	}

	public void add_function_object( int id, byte[] data ) {
		if ( _last_object < id ) {
			_last_object = id;
		}

		_objects.put( new Integer( id ), new TObject( this.getJetty(), id, data ) );
	}

	public void add_object_object( int id, int flags, int[] superclasses,
		TProperty[] properties ) {
		if ( _last_object < id ) {
			_last_object = id;
		}

		_objects.put( new Integer( id ),
			new TObject( this.getJetty(), id, flags, superclasses, properties ) );
	}

	// this looks up both "object objects" and "function objects", of course
	public TObject lookup_object( int id ) throws HaltTurnException {
		return lookup_object( id, true );
	}

	public TObject lookup_object( int id, boolean disallow_null )
		throws HaltTurnException {
		TObject t = (TObject)_objects.get( new Integer( id ) );
		if ( t == null && disallow_null ) {
			print_error( "Unknown/deleted object referenced", 1 );
			throw new HaltTurnException( ParserError.DELETED_OBJ_REF );
		}

		return t;
	}

	public TObject lookup_required_object( int reqid ) {
		return (TObject)_objects.get( new Integer( _required_objects[ reqid ] ) );
	}

	public int get_last_object() {
		return _last_object;
	}

	public TValue create_obj( TObject sc )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		int new_id = ++_last_object;
		int flags = 0x16; // 16 = dynamically-created
		TProperty[] props = new TProperty[ 0 ];
		int[] scs = {sc.get_id()};
		TObject obj = new TObject( this.getJetty(), new_id, flags, scs, props );
		_objects.put( new Integer( obj.get_id() ), obj );

		print_error( "dynamically creating object of class=" + scs[ 0 ], 2 );

		// set this object's contents field to []
		obj.set_property( RequiredProperties.CONTENTS,
			newTValue( TValue.LIST, new Vector() ) );

		obj.eval_property( RequiredProperties.CONSTRUCT, this.arg_array() );

		// this new obj has the same vocab as the superclass, of course
		copy_vocab( sc, obj );

		// save an undo record:
		save_newobj_undo( obj );

		return newTValue( TValue.OBJECT, obj.get_id() );
	}

	private void copy_vocab( TObject copy, TObject obj ) {
		Vector vocab = copy.get_vocab();
		Vector types = new Vector();
		Vector word2s = new Vector();

		for ( int i = 0; i < vocab.size(); i++ ) {
			VocabWord vw = (VocabWord)vocab.elementAt( i );

			if ( vw.get_object( copy.get_id(), types, word2s ) ) {
				obj.add_vocab( vw );
				for ( int j = 0; j < types.size(); j++ ) {
					int t = ( (Integer)types.elementAt( j ) ).intValue();
					print_error( "adding vocab=" + vw.get_word() +
						" type=" + t, 2 );
					vw.add_obj( (String)word2s.elementAt( j ), obj.get_id(), t );
				}
			} else {
				print_error( "Vocab word '" + vw.get_word() +
					"' not found in copy=" + copy.get_id(), 1 );
			}

			types.removeAllElements();
			word2s.removeAllElements();
		}
	}

	public void delete_obj( TObject obj )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		delete_obj( obj, true );
	}

	public void delete_obj( TObject obj, boolean save_undo )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( save_undo ) {
			obj.eval_property( RequiredProperties.DESTRUCT, this.arg_array() );
		}

		for ( int i = 0; i < _singular_pronouns.length; i++ ) {
			if ( _singular_pronouns[ i ] == obj ) {
				_singular_pronouns[ i ] = null;
			}
		}

		if ( _plural_pronoun != null ) {
			for ( int i = 0; i < _plural_pronoun.length; i++ ) {
				if ( _plural_pronoun[ i ] == obj ) {
					_plural_pronoun = null;
					break;
				}
			}
		}

		for ( int i = 0; i < _command_objects.length; i++ ) {
			if ( _command_objects[ i ] == obj ) {
				// null out actor and verb so it'll complain if they do an 'again'
				_command_objects[ 0 ] = _command_objects[ 1 ] = null;
				break;
			}
		}

		// delete all the vocab records for this object
		Vector vocab = obj.get_vocab();
		Vector types = new Vector();
		Vector word2s = new Vector();
		for ( int i = 0; i < vocab.size(); i++ ) {
			VocabWord vw = (VocabWord)vocab.elementAt( i );

			if ( vw.get_object( obj.get_id(), types, word2s ) ) {
				for ( int j = 0; j < types.size(); j++ ) {
					vw.remove_obj( (String)word2s.elementAt( j ), obj.get_id(),
						( (Integer)types.elementAt( j ) ).intValue() );
				}
			} else {
				print_error( "Vocab word '" + vw.get_word() +
					"' not found in obj=" + obj.get_id(), 1 );
			}

			types.removeAllElements();
			word2s.removeAllElements();
		}

		// and lastly, delete it from the hash
		_objects.remove( new Integer( obj.get_id() ) );

		if ( save_undo ) {
			save_delobj_undo( obj );
		}
	}

	public TObject lookup_it( int which ) {
		if ( which == Constants.PO_IT ) {
			return _singular_pronouns[ 0 ];
		}
		if ( which == Constants.PO_HIM ) {
			return _singular_pronouns[ 1 ];
		}
		if ( which == Constants.PO_HER ) {
			return _singular_pronouns[ 2 ];
		}

		print_error( "lookup_it called with which=" + which, 1 );
		return null;
	}

	public void set_it( int which, TObject p ) {
		if ( p != null && ( p == lookup_required_object( RequiredObjects.NUM_OBJ ) ||
			p == lookup_required_object( RequiredObjects.STR_OBJ ) ) ) {
			p = null;
		}

		if ( which == Constants.PO_IT ) {
			_singular_pronouns[ 0 ] = p;
		} else if ( which == Constants.PO_HIM ) {
			_singular_pronouns[ 1 ] = p;
		} else if ( which == Constants.PO_HER ) {
			_singular_pronouns[ 2 ] = p;
		} else {
			print_error( "set_it called with which=" + which, 1 );
		}
	}

	public TObject[] lookup_them() {
		return _plural_pronoun;
	}

	public void set_them( TObject[] p ) {
		_plural_pronoun = p;
	}

	// use PO_ACTOR, etc constants in Constants.java
	public TObject get_command_object( int which ) {
		if ( which <= 0 || which > _command_objects.length ) {
			print_error( "get_command_object called with which=" + which, 1 );
			return null;
		}

		return _command_objects[ which - 1 ];
	}

	public void set_command_object( int which, TObject obj ) {
		if ( which <= 0 || which > _command_objects.length ) {
			print_error( "set_command_object called with which=" + which, 1 );
			return;
		}

		_command_objects[ which - 1 ] = obj;
	}

	// convenience methods:
	public TObject get_actor() {
		return get_command_object( Constants.PO_ACTOR );
	}

	public void set_actor( TObject o ) {
		set_command_object( Constants.PO_ACTOR, o );
	}

	public ObjectMatch get_command_match( int which ) {
		if ( which <= 0 || which > _command_matches.length ) {
			print_error( "get_command_match called with which=" + which, 1 );
			return null;
		}

		return _command_matches[ which - 1 ];
	}

	public void set_command_match( int which, ObjectMatch obj ) {
		if ( which <= 0 || which > _command_matches.length ) {
			print_error( "set_command_match called with which=" + which, 1 );
			return;
		}

		_command_matches[ which - 1 ] = obj;
	}

	public Vector[] get_rex_groups() {
		return _rex_groups;
	}

	public void set_rex_groups( Vector[] groups ) {
		_rex_groups = groups;
	}

	public void add_daemon( int obj, int prop, TValue value, int time ) {
		Daemon d = new Daemon( obj, prop, value, time );
		_daemons.addElement( d );
		save_setdaemon_undo( d );
	}

	public void remove_daemon( int obj, int prop, TValue value ) {
		for ( int i = 0; i < _daemons.size(); i++ ) {
			Daemon d = (Daemon)_daemons.elementAt( i );
			if ( d.object == obj && d.property == prop &&
				( value == null ? d.value == null : value.equals( d.value ) ) ) {
				save_remdaemon_undo( d );
				_daemons.removeElementAt( i-- );
			}
		}
	}

	public int lookup_fuse( int obj, int prop, TValue value ) {
		for ( int i = 0; i < _daemons.size(); i++ ) {
			Daemon d = (Daemon)_daemons.elementAt( i );
			if ( d.object == obj && ( prop == -1 && d.value.equals( value ) ||
				prop != -1 && d.property == prop ) ) {
				return ( d.time == CONTINUOUS ) ? -1 : d.time;
			}
		}

		return -1;
	}

	// burns down fuses by 1 turn, maybe runs them
	public void burn_fuses( boolean run )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		for ( int i = 0; i < _daemons.size(); i++ ) {
			Daemon d = (Daemon)_daemons.elementAt( i );

			if ( d.time == CONTINUOUS || d.time == EXPIRED ) {
				continue;
			}

			save_burn_undo( d, d.time );

			if ( --d.time == 0 ) {
				d.time = EXPIRED;
			}
		}

		if ( run ) {
			run_daemons_and_fuses( false, true );
		}
	}

	// runs daemons and expired fuses (also removing the expired fuses)
	public void run_daemons_and_fuses( boolean run_daemons, boolean run_fuses )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// http://nothings.org/computer/iterate.html
		Vector ds = (Vector)_daemons.clone();

		for ( int i = 0; i < ds.size(); i++ ) {
			Daemon d = (Daemon)ds.elementAt( i );

			if ( !( ( d.time == CONTINUOUS && run_daemons ) ||
				( d.time == EXPIRED && run_fuses ) ) ) {
				continue;
			}

			if ( d.time == EXPIRED ) {
				_daemons.removeElement( d );
			}

			TObject obj = lookup_object( d.object );

			if ( d.property == -1 ) {
				getRunner().run( obj.get_data(), this.arg_array( d.value ) );
			} else {
				obj.eval_property( d.property, this.arg_array() );
			}
		}
	}

	public void set_parser_me( TObject actor ) {
		_parser_me = actor;
	}

	public TObject get_parser_me() {
		return _parser_me;
	}

	public void set_preinit( int p ) {
		_preinit = p;
	}

	public TObject get_preinit() {
		return (TObject)_objects.get( new Integer( _preinit ) );
	}

	public boolean run_preinit() {
		// 0x04: FIOFPRE - preinit needs to be run after reading game
		return ( ( _flags & 0x04 ) != 0 );
	}

	// undo a turn; return true if there was anything to undo
	public boolean do_undo()
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( _undo_records.size() == 0 ) {
			return false;
		}

		UNDO: while ( _undo_records.size() > 0 ) {
			UndoRecord u =
				(UndoRecord)_undo_records.elementAt( _undo_records.size() - 1 );
			_undo_records.removeElementAt( _undo_records.size() - 1 );

			if ( u.type == UndoRecord.SAVE_POINT ) {
				break;
			} else if ( u.type == UndoRecord.PROPERTY ) {
				if ( u.value == null ) {
					u.obj.del_property( u.num );
				} else {
					u.obj.set_property( u.num, u.value, false );
				}
			} else if ( u.type == UndoRecord.FUSE_BURN ) {
				// fix the time:
				u.daemon.time = u.num;

				// check if it's still in the list:
				for ( int i = 0; i < _daemons.size(); i++ ) {
					if ( _daemons.elementAt( i ) == u.daemon ) // deliberately using == here
					{
						continue UNDO;
					}
				}

				// no, so add it:
				_daemons.addElement( u.daemon );
			} else if ( u.type == UndoRecord.SET_DAEMON ) {
				// to undo a setdaemon, you delete it
				for ( int i = 0; i < _daemons.size(); i++ ) {
					if ( _daemons.elementAt( i ) == u.daemon ) // deliberately using == here
					{
						_daemons.removeElementAt( i-- );
					}
				}
			} else if ( u.type == UndoRecord.REM_DAEMON ) {
				// to undo a remdaemon, you add it
				_daemons.addElement( u.daemon );
			} else if ( u.type == UndoRecord.NEW_OBJECT ) {
				delete_obj( u.obj, false );
			} else if ( u.type == UndoRecord.DEL_OBJECT ) {
				// stick it back in the hash
				_objects.put( new Integer( u.obj.get_id() ), u.obj );

				// tell vocab they have it as a possibility again
				TObject sc = lookup_object( u.obj.get_superclasses()[ 0 ] );
				copy_vocab( sc, u.obj );
			} else if ( u.type == UndoRecord.SET_ME ) {
				_parser_me = u.obj;
			} else if ( u.type == UndoRecord.ADD_WORD || u.type == UndoRecord.DEL_WORD ) {
				String word = u.word;
				String word2 = null;
				int x = word.indexOf( ' ' );
				if ( x != -1 ) {
					word2 = word.substring( x + 1 );
					word = word.substring( 0, x );
				}

				if ( u.type == UndoRecord.ADD_WORD ) {
					del_vocab( u.obj.get_id(), u.num, word, word2 );
				} else {
					add_vocab( u.obj.get_id(), u.num, 0, word, word2 );
				}
			} else {
				print_error( "Unknown undo record type found: " + u.type, 1 );
			}
		}

		return true;
	}

	// set a savepoint; the next undo will undo up to here and no further back
	public void set_undo_savepoint() {
		// the first time, don't actually do anything, just instantiate the
		// vector (this will mean that undoing on the first turn will correctly
		// say "sorry, nothing to undo")
		if ( _undo_records == null ) {
			_undo_records = new Vector( MAX_UNDO );
		} else {
			add_undo( new UndoRecord( UndoRecord.SAVE_POINT ) );
		}
	}

	// save an undo record for a TObject.set_property() call
	public void save_prop_undo( TObject obj, int prop, TValue value ) {
		UndoRecord u = new UndoRecord( UndoRecord.PROPERTY );
		u.obj = obj;
		u.num = prop;
		// keep a copy of the value, not the real thing:
		u.value = ( value == null ) ? null : value.do_clone();
		add_undo( u );
	}

	// save an undo record for a fuse burning once
	private void save_burn_undo( Daemon d, int time ) {
		UndoRecord u = new UndoRecord( UndoRecord.FUSE_BURN );
		u.daemon = d;
		u.num = time;
		add_undo( u );
	}

	// save an undo record for setting a daemon/fuse/notify
	private void save_setdaemon_undo( Daemon d ) {
		UndoRecord u = new UndoRecord( UndoRecord.SET_DAEMON );
		u.daemon = d;
		add_undo( u );
	}

	// save an undo record for removing a daemon/fuse/notify
	private void save_remdaemon_undo( Daemon d ) {
		UndoRecord u = new UndoRecord( UndoRecord.REM_DAEMON );
		u.daemon = d;
		add_undo( u );
	}

	// save an undo record for creating a dynamic object
	private void save_newobj_undo( TObject obj ) {
		UndoRecord u = new UndoRecord( UndoRecord.NEW_OBJECT );
		u.obj = obj;
		add_undo( u );
	}

	// save an undo record for deleting a dynamic object
	private void save_delobj_undo( TObject obj ) {
		UndoRecord u = new UndoRecord( UndoRecord.DEL_OBJECT );
		u.obj = obj;
		add_undo( u );
	}

	// save an undo record for parserSetMe()
	public void save_setme_undo( TObject obj ) {
		UndoRecord u = new UndoRecord( UndoRecord.SET_ME );
		u.obj = obj;
		add_undo( u );
	}

	// save an undo record for adding a vocab word to an object
	public void save_addword_undo( int obj, int prop, String word )
		throws HaltTurnException {
		UndoRecord u = new UndoRecord( UndoRecord.ADD_WORD );
		u.obj = lookup_object( obj );
		u.num = prop;
		u.word = word;
		add_undo( u );
	}

	// save an undo record for removing a vocab word from an object
	public void save_delword_undo( int obj, int prop, String word )
		throws HaltTurnException {
		UndoRecord u = new UndoRecord( UndoRecord.DEL_WORD );
		u.obj = lookup_object( obj );
		u.num = prop;
		u.word = word;
		add_undo( u );
	}

	public void do_restart()
		throws HaltTurnException, ParseException, ReparseException,
		GameOverException {
		// first we'll reset the vocab:
		for ( int i = _restart_vocab_records.size() - 1; i >= 0; i-- ) {
			UndoRecord u = (UndoRecord)_restart_vocab_records.elementAt( i );
			if ( u.type == UndoRecord.ADD_WORD || u.type == UndoRecord.DEL_WORD ) {
				String word = u.word;
				String word2 = null;
				int x = word.indexOf( ' ' );
				if ( x != -1 ) {
					word2 = word.substring( x + 1 );
					word = word.substring( 0, x );
				}

				if ( u.type == UndoRecord.ADD_WORD ) {
					del_vocab( u.obj.get_id(), u.num, word, word2 );
				} else {
					add_vocab( u.obj.get_id(), u.num, 0, word, word2 );
				}
			}
		}

		// then the properties:
		Vector dynamic = new Vector();
		for ( Enumeration e = _restart_prop_records.keys(); e.hasMoreElements(); ) {
			TObject o = (TObject)e.nextElement();
			TProperty[] props = (TProperty[])_restart_prop_records.get( o );
			for ( int i = 0; i < props.length; i++ ) {
				o.set_property( props[ i ].id, props[ i ].value, false );
			}

			if ( o.is_dynamic() ) {
				dynamic.addElement( o );
			}
		}

		// blow away dynamic objects:
		for ( int i = 0; i < dynamic.size(); i++ ) {
			delete_obj( (TObject)dynamic.elementAt( i ), false );
		}

		// and a couple other things need to get wiped also:
		_daemons.removeAllElements();
		_parser_me = lookup_required_object( RequiredObjects.ME );
		for ( int i = 0; i < _singular_pronouns.length; i++ ) {
			_singular_pronouns[ i ] = null;
		}
		_plural_pronoun = null;
		for ( int i = 0; i < _command_objects.length; i++ ) {
			_command_objects[ i ] = null;
		}
		_command_objects[ 0 ] = _parser_me; // but the actor is reset to this
		for ( int i = 0; i < _command_matches.length; i++ ) {
			_command_matches[ i ] = null;
		}
		_undo_records = null;
		_restart_vocab_records.removeAllElements();
		// this test seems pointless to me, but I get an error running it as
		// an applet under windows if I don't have it
		if ( _restart_prop_records.size() > 0 ) {
			_restart_prop_records = new Hashtable( _restart_prop_records.size() );
		}
	}

	private void save_prop_restart( TObject obj, int prop, TValue value ) {
		// the theory is that you modify some properties a lot and some not very
		// much. therefore I am using an array here rather than a vector in the
		// hopes that it'll quickly grow to its final size and then stop
		TProperty[] props = (TProperty[])_restart_prop_records.get( obj );

		for ( int i = 0; props != null && i < props.length; i++ ) {
			// if there is a saved value, we're done:
			if ( props[ i ].id == prop ) {
				return;
			}
		}

		if ( props == null ) {
			props = new TProperty[ 1 ];
		} else {
			TProperty[] tmp = new TProperty[ props.length + 1 ];
			System.arraycopy( props, 0, tmp, 0, props.length );
			props = tmp;
		}

		props[ props.length - 1 ] = new TProperty( prop, value );
		_restart_prop_records.put( obj, props );
	}

	// add an undo record, unless we're in the init phase (ie, no savepoints set)
	// if the records become too numerous, ditch an older one.
	private void add_undo( UndoRecord u ) {
		if ( _undo_records != null ) {
			_undo_records.addElement( u );
			if ( _undo_records.size() > MAX_UNDO ) {
				_undo_records.removeElementAt( 0 );
			}

			// and save restart information if necessary:
			if ( u.type == UndoRecord.PROPERTY ) {
				save_prop_restart( u.obj, u.num, u.value );
			} else if ( u.type == UndoRecord.ADD_WORD || u.type == UndoRecord.DEL_WORD ) {
				_restart_vocab_records.addElement( u );
			}
		}
	}

	private static class Daemon {

		public int object;
		public int property;
		public TValue value;
		public int time;

		public boolean equals( Object dobj ) {
			if ( !( dobj instanceof Daemon ) ) {
				return false;
			}
			Daemon d = (Daemon)dobj;

			return this.object == d.object && this.property == d.property &&
				( this.value == null ? d.value == null : this.value.equals( d.value ) );
		}

		public Daemon( int o, int p, TValue v, int t ) {
			object = o;
			property = p;
			value = v;
			time = t;
		}
	}

	private static class UndoRecord {

		public static final int SAVE_POINT = 0;
		public static final int PROPERTY = 1;
		public static final int FUSE_BURN = 2;
		public static final int SET_DAEMON = 3;
		public static final int REM_DAEMON = 4;
		public static final int NEW_OBJECT = 5;
		public static final int DEL_OBJECT = 6;
		public static final int SET_ME = 7;
		public static final int ADD_WORD = 8;
		public static final int DEL_WORD = 9;

		public int type;
		public TObject obj;
		public int num;
		public Daemon daemon;
		public TValue value;
		public String word;

		public UndoRecord( int t ) {
			type = t;
		}
	}

	private String _version;
	private int _flags;
	private String _timestamp;
	private Hashtable _vocab = new Hashtable( 200 );
	private Hashtable _compounds = new Hashtable( 20 );
	private Hashtable _specwords = new Hashtable( 10 );
	private Hashtable _fmtstrs = new Hashtable( 10 );
	private Hashtable _inheritance = new Hashtable( 200 );
	private Hashtable _objects = new Hashtable( 200 ); // for "object" = "objects and functions"
	private int _last_object = 0;
	private int[] _required_objects; // for "object" = "objects and functions"
	private int _preinit; // preinit isn't a standard required object, sigh
	private Vector _daemons = new Vector( 10 ); // this is also fuses, notifies
	private TObject _parser_me = null; // for parserGet/SetMe()
	private TObject[] _singular_pronouns = new TObject[ 3 ];
	private TObject[] _plural_pronoun = null;
	private TObject[] _command_objects = new TObject[ 5 ];
	private ObjectMatch[] _command_matches = new ObjectMatch[ 2 ];
	private Vector[] _rex_groups = null;

	private Vector _undo_records = null;

	private Vector _restart_vocab_records = new Vector();
	private Hashtable _restart_prop_records = new Hashtable();
}
