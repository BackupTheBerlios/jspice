package tads2.jetty;


import java.util.Vector;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

public class Parser extends JettyRef {

	// constants for the parse stage we're in:
	public static final int NORMAL_PARSING = 0;
	public static final int DOING_OOPS = 1;
	public static final int DISAMBIGUATING = 2;
	public static final int REQUESTING_DOBJ = 3;
	public static final int REQUESTING_IOBJ = 4;

	public Parser( Jetty jetty ) {
		super( jetty );
	}

	public void run_turn() throws GameOverException {
		try {
			try {
				// start off the turn by updating the status line
				TObject player = getState().get_parser_me();
				if ( player != null ) {
					TValue lv = player.eval_property( RequiredProperties.LOCATION,
						this.arg_array() );
					if ( lv.get_type() == TValue.OBJECT ) {
						TObject loc = getState().lookup_object( lv.get_object() );
						print_statusline( true, true );
						loc.eval_property( RequiredProperties.STATUS_LINE, this.arg_array() );
						print_statusline( false, true );
					}
				}

				// and when we undo for the turn, we'll undo to here:
				getState().set_undo_savepoint();

				Vector input_words = null;
				if ( _tokens != null ) {
					print_error( "Reparsing...", 2 );
					input_words = _tokens;
					_tokens = null;
				} else if ( _unknown != null ) {
					input_words = tokenize_input( DOING_OOPS );
				} else {
					input_words = tokenize_input( NORMAL_PARSING );
				}

				if ( input_words == null ) {
					return;
				}

				for ( int i = 0; i < input_words.size(); i++ ) {
					print_error( "Word: " + input_words.elementAt( i ), 2 );
				}

				// ok, we start parsing here. the default actor for the commands
				// will be Me (or, rather, parserGetMe()), until another is set
				getState().set_actor( getState().get_parser_me() );

				// first we break the line up into individual commands.
				// commands are, as we know, delineated by the special word "."
				// (and it is legal to have the line start/end with one or more periods,
				// which are ignored)
				// this is a first-pass breakup: since we only split on periods,
				// we will assume "north, south" is one command when it's probably
				// actually 2, but this'll get caught and fixed later

				Vector command = new Vector();
				int num_commands = 0;
				for ( int i = 0; i < input_words.size() || command.size() > 0; i++ ) {
					VocabWord vw = ( i < input_words.size() ) ?
						(VocabWord)input_words.elementAt( i ) : null;

					if ( vw == null || vw.is_specword( VocabWord.SPECWORD_THEN ) ) {
						while ( command.size() > 0 ) {
							// if this is not the first command, putOne a blank line first
							if ( num_commands++ > 0 ) {
								print( "\\b" );
							}

							int num_used = parse_command( command );
							for ( int j = 0; j < num_used; j++ ) {
								command.removeElementAt( 0 );
							}
							if ( command.size() > 0 ) {
								if ( ( (VocabWord)command.elementAt( 0 ) ).is_specword( VocabWord.SPECWORD_AND ) ) {
									command.removeElementAt( 0 );
								}
							}
						}
					} else {
						command.addElement( vw );
					}
				}
			} catch ( HaltTurnException hte ) {
				print_error( "HaltTurn caught - ending turn now", 2 );
				getPerror().print( hte.get_errnum(), hte.get_errstr() );
				if ( hte.get_errnum() == ParserError.UNK_WORD ) {
					_unknown = hte.get_errstr();
				}
			}
		} catch ( ReparseException re ) {
			_tokens = re.get_tokens(); // this is fine. we'll use this next turn
		} catch ( ParseException pe ) {
			print_error( "Parse exception (" + pe + ") thrown at bad time.", 1 );
		}
	}

	// takes in a list of vocab words, returns the number of them
	// actually used in the parsing (since the command might come through
	// as 'GO NORTH AND PICK UP THE BALL' which should get parsed in
	// two passes)
	private int parse_command( Vector command )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print_error( "COMMAND:", 2 );
		for ( int i = 0; i < command.size(); i++ ) {
			print_error( " " + ( (VocabWord)command.elementAt( i ) ).get_word(), 2 );
		}

		if ( command.size() == 0 ) {
			return command.size();
		}

		TObject actor = null;
		VocabWord verb = null;
		VocabWord vprep = null;
		ObjectMatch[] dobj = null;
		VocabWord prep = null;
		ObjectMatch[] iobj = null;

		int size = 0; // total length of the command

		// ok, first identify the actor, which'd be (if any) a noun phrase
		// covering the words before the first comma (which could also be
		// an 'and', since those get translated into commas. go figure.)
		for ( int i = 1; i < command.size(); i++ ) {
			VocabWord vw = (VocabWord)command.elementAt( i );
			if ( vw.is_specword( VocabWord.SPECWORD_AND ) ) {
				actor = parse_actor( command, 0, i );
				if ( actor != null )
				// eliminate the phrase which refers to the actor from
				// further parsing, plus the comma:
				{
					for ( int x = 0; x <= i; x++ ) {
						command.removeElementAt( 0 );
					}
				}

				break;
			}
		}

		// so either update the default or look it up, as appropriate
		if ( actor != null ) {
			getState().set_actor( actor );
		} else {
			actor = getState().get_actor();
		}

		// ok, so the sentence is either of the form
		// VERB vprep DOBJ vprep PREP IOBJ vprep
		// or
		// VERB vprep PREP IOBJ vprep DOBJ vprep
		//
		// the vprep is a preposition that's actually part of the verb,
		// eg, the 'up' in 'pick up'. There can only be one in the sentence,
		// although it can appear in any of three places (and of course it doesn't
		// have to appear at all).
		//
		// This whole code is a major pain in the ass and has broken numerous
		// times as I try and tweak it to get everything right. Sentences to keep
		// in mind:
		// >JUMP (basic)
		// >EXAMINE BOX (basic)
		// >UNLOCK LOCK WITH KEY (basic)
		// >LOOK IN BOX (verb = 'look in'
		// [this should work even if 'in' is also an adjective])
		// >EXAMINE IN BOX (verb = 'examine', dobj = 'in box')
		// >PICK BOX UP (verb = 'pick up')
		// >PUT FLY IN AMBER IN HOLE IN WALL (dobj = 'fly in amber',
		//                                    iobj = 'hole in wall'

		// there are three parts to the sentence: verb, dobj, iobj
		while ( size < command.size() ) {
			VocabWord word = (VocabWord)command.elementAt( size );
			// the verb is always the first word (since we're post-actor)
			if ( verb == null ) {
				print_error( "verb at " + size, 2 );
				verb = word;
				size++;
				print_error( "verb ends at " + size, 2 );
				continue;
			}

			if ( word.has_objects( VocabWord.PREPOSITION ) ) {
				print_error( "preposition word at " + size, 2 );

				if ( vprep == null && verb.has_objects( VocabWord.VERB, word.get_word() ) ) {
					print_error( "calling it a vprep", 2 );
					vprep = word;
					size++;
					continue;
				}

				if ( prep == null ) {
					// if this might be the dobj (ie, we don't have one yet), and
					// this preposition is the start of a series of 0+ adjectives
					// followed by a noun, then pretend it's not a preposition after all
					int ts = size;
					while ( ( ts < command.size() - 1 ) &&
						( (VocabWord)command.elementAt( ts ) ).has_objects( VocabWord.ADJECTIVE ) &&
						!( (VocabWord)command.elementAt( ts ) ).has_objects( VocabWord.NOUN ) ) {
						ts++;
					}

					if ( dobj != null ||
						!( (VocabWord)command.elementAt( ts ) ).has_objects( VocabWord.NOUN ) ) {
						print_error( "calling it a prep", 2 );
						prep = word;
						size++;
						continue;
					}
				}
			}

			if ( dobj == null || iobj == null ) {
				boolean is_dobj = ( dobj == null );

				print_error( "trying " + ( is_dobj ? "dobj" : "iobj" ) + " at " + size, 2 );
				ObjectMatch[] match = parse_noun_phrase( command, size );
				if ( match != null ) {
					int match_size = 0;
					for ( int i = 0; i < match.length; i++ ) {
						match_size += match[ i ].get_size();
					}

					if ( match_size > 0 ) {
						size += match_size;
						if ( is_dobj ) {
							print_error( "dobj found; ends at " + size, 2 );
							dobj = match;
						} else {
							print_error( "iobj found; ends at " + size, 2 );
							iobj = match;
							if ( !is_dobj && vprep != null && prep == null ) {
								print_error( "(so refiguring vprep as prep)", 2 );
								prep = vprep;
								vprep = null;
							}
						}

						continue;
					}
				}
			}

			// if nobody's handled it thus far, we're out of luck.
			break;
		}

		// uh-oh, we didn't parse all the words:
		if ( size < command.size() ) {
			// it's ok if the next word is 'and', in which case it's a new command:
			if ( ( (VocabWord)command.elementAt( size ) ).is_specword( VocabWord.SPECWORD_AND ) ) {
				// make a new vector, discarding the old one (but our caller still
				// has a handle to it, so s'fine)
				Vector newc = new Vector( size );
				for ( int i = 0; i < size; i++ ) {
					newc.addElement( command.elementAt( i ) );
				}
				command = newc;
			} else {
				print_error( "Oops -- parsed size=" + size + " total size=" +
					command.size(), 2 );
				return call_parse_unknown_verb( command, getState().get_actor(),
					ParserError.EXTRA_WORDS );
			}
		}

		// make sure that if we got a preposition that we also got an iobj:
		if ( prep != null && iobj == null ) {
			return call_parse_unknown_verb( command, getState().get_actor(),
				ParserError.EXTRA_WORDS );
		}

		// if we got a dobj and an iobj, but no prep, that means they gave
		// us the prep-less reversed form (ie, >GIVE GUARD THE CUP)
		if ( dobj != null && iobj != null && prep == null ) {
			ObjectMatch[] tmp = iobj;
			iobj = dobj;
			dobj = tmp;
		}

		String vprep_word = ( vprep == null ) ? null : vprep.get_word();
		if ( verb == null || !verb.has_objects( VocabWord.VERB, vprep_word ) ) {
			int err = ( verb != null && verb.is_unknown() ) ?
				ParserError.UNK_WORD : ParserError.NO_VERB;
			return call_parse_unknown_verb( command, getState().get_actor(), err );
		}

		// run preparse, but not if the verb is unknown
		TObject preparse_cmd = getState().lookup_required_object( RequiredObjects.PREPARSE_CMD );
		if ( preparse_cmd != null ) {
			Vector words = new Vector( size );
			// ShortMark Musante pointed out this wasn't passing a TValue, just a raw
			// string. whoopsies.
			for ( int i = 0; i < size; i++ ) {
				words.addElement( newTValue( TValue.SSTRING, ( (VocabWord)command.elementAt( i ) ).get_word() ) );
			}

			TValue[] args = new TValue[ 1 ];
			args[ 0 ] = newTValue( TValue.LIST, words );
			TValue ret = getRunner().run( preparse_cmd.get_data(), args );

			ret.must_be( TValue.TRUE, TValue.NIL, TValue.LIST );

			// if they return a list, we should use the words in that and reparse
			if ( ret.get_type() == TValue.LIST ) {
				Vector lst = ret.get_list();
				Vector new_words = new Vector();

				for ( int i = 0; i < lst.size(); i++ ) {
					TValue t = (TValue)lst.elementAt( i );
					t.must_be( TValue.SSTRING );
					new_words.addElement( getState().lookup_vocab( t.get_string() ) );
				}

				throw new ReparseException( new_words );
			}

			// whereas if they return nil, we should just give up on this turn
			else if ( ret.get_type() == TValue.NIL ) {
				return command.size();
			}
		}

		TObject verb_obj = getState().lookup_object( verb.get_first_object( VocabWord.VERB, vprep_word ) );

		TObject prep_obj = null;
		if ( prep != null ) {
			prep_obj = getState().lookup_object( prep.get_first_object( VocabWord.PREPOSITION, null ) );
		}

		{
			TValue verb_arg = newTValue( TValue.OBJECT, verb_obj.get_id() );
			TValue ret = actor.eval_property( RequiredProperties.ROOM_CHECK,
				this.arg_array( verb_arg ) );
			// if roomCheck returns nil, we give up, not running daemons or
			// anything
			if ( !ret.get_logical() ) {
				return command.size();
			}
		}

		try {
			boolean require_dobj = false;
			while ( true ) {
				try {
					int status = getSimulator().do_turn( actor, verb_obj, dobj, prep_obj,
						iobj, require_dobj );
					// if they threw an abort, halt execution on this command line
					if ( status == Constants.EC_ABORT ) {
						throw new HaltTurnException( ParserError.ABORT_THROWN );
					}

					// if we successfully execute, then we can break the loop:
					break;
				} catch ( AskDobjException ade ) {
					print_error( "AskDobj thrown, retrying", 2 );
					// retry execute (ie, don't break loop), asking for a dobj this time
					require_dobj = true;
				} catch ( AskIobjException aie ) {
					print_error( "AskIobj thrown, retrying", 2 );
					// retry execute (ie, don't break loop), asking for an iobj this time
					prep_obj = aie.get_iobj();

					// also, the dobj may have been clarified here:
					if ( dobj == null ) {
						dobj = new ObjectMatch[ 1 ];
						dobj[ 0 ] = getState().get_command_match( 1 );
					}

					if ( dobj != null ) {
						ObjectMatch[] newd = new ObjectMatch[ 1 ];
						newd[ 0 ] = newObjectMatch( 0 );
						newd[ 0 ].add_object( getState().get_command_object( Constants.PO_DOBJ ) );
						newd[ 0 ].copy_words( dobj[ 0 ] );
					}
				} finally {
					// make sure that no output hiding passes to the next turn;
					// this could happen if they forget to close an outhide(),
					// or, as ShortMark Musante noticed, if they do an askio() from within
					// a verify method.
					cancel_outhiding();
				}
			}
		} catch ( HaltTurnException hte ) {
			// some of these we call parseUnknownVerb for, some we just rethrow:
			if ( hte.get_errnum() == ParserError.NO_TEMPLATE ||
				hte.get_errnum() == ParserError.DONT_RECOGNIZE ) {
				return call_parse_unknown_verb( command, actor, hte.get_errnum() );
			} else {
				throw hte;
			}
		}

		return command.size();
	}

	// Present a prompt and read a line in from the user, then return a list
	// of the VocabWords it turns into. If null is returned, that means the
	// command's aborted, either by some error or by user choice or whatever.
	// This method will have printed any necessary error messages in that case.
	//
	public Vector tokenize_input( int parse_stage )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TObject cmd_prompt =
			getState().lookup_required_object( RequiredObjects.CMD_PROMPT );

		if ( cmd_prompt == null ) {
			print( "\\b>" );
		} else {
			getRunner().run( cmd_prompt.get_data(),
				this.arg_array( newTValue( TValue.NUMBER,
					parse_stage ) ) );
		}
		String input = read_line();

		TObject cmd_post_prompt =
			getState().lookup_required_object( RequiredObjects.CMD_POST_PROMPT );
		if ( cmd_post_prompt != null ) // this one has no default
		{
			getRunner().run( cmd_post_prompt.get_data(),
				this.arg_array( newTValue( TValue.NUMBER,
					parse_stage ) ) );
		}

		if ( _unknown != null && ( input.startsWith( "o " ) || input.startsWith( "oops " ) ) ) {
			print_error( "Doing oops: unknown=" + _unknown + " old line=" +
				_cur_line, 2 );
			if ( input.startsWith( "o " ) ) {
				input = input.substring( 2 );
			} else {
				input = input.substring( 5 );
			}

			int idx = _cur_line.indexOf( _unknown );
			_cur_line = _cur_line.substring( 0, idx ) + input +
				_cur_line.substring( idx + _unknown.length() );
			input = _cur_line;
			print_error( "New line=" + input, 2 );
		}

		_unknown = null; // clear this, in any case

		if ( input.startsWith( "$$" ) ) {
			String[] commands = {"quit", "verbose", "help", "version", "credits"};

			input = input.substring( 2 ).trim();
			int sp = input.indexOf( ' ' );
			if ( sp == -1 ) {
				sp = input.length();
			}
			String command = input.substring( 0, sp ).toLowerCase();
			if ( command.equals( "" ) ) {
				command = "help";
			}
			String args = input.substring( sp ).trim();

			for ( int i = 0; i < commands.length; i++ ) {
				int len = Math.min( commands[ i ].length(), command.length() );
				if ( commands[ i ].substring( 0, len ).equals( command ) ) {
					command = commands[ i ];
				}
			}

			if ( command.equals( "abend" ) || command.equals( "quit" ) ) {
				print( "\\n[Hot-quit]\\n" );
				throw new GameOverException();
			} else if ( command.equals( "verbose" ) ) {
				try {
					int x = Integer.parseInt( args );
					set_debug_level( x );
					print_error( "Verbosity level set to " + x, x );
				} catch ( NumberFormatException nfe ) {
					print_error( "Error with arg to $$verbose: " + input, 1 );
				}
			} else if ( command.equals( "version" ) ) {
				print( "\\b\\(Jetty, release 1.2 (early Dec '02)\\)\\b" );
			} else if ( command.equals( "credits" ) ) {
				String[] credits = {
					"Jetty: a TADS 2 interpreter written in Java",
					"by Dan Shiovitz (dbs@cs.wisc.edu)",
					"http://www.drizzle.com/~dans/if/jetty/",
					"I can also be contacted via the rec.arts.int-fiction newsgroup.",
					"Thanks is due to a number of people, most notably: ",
					"- Stephen Granade, NK Guy, Iain Merrick, Dan Schmidt, and ",
					"  Emily Short, for beta-testing and suggestions",
					"- Matthew Russotto, for the Zplet source, which was invaluable ",
					"  in demonstrating how applets work",
					"- and, of course, Mike Roberts, for writing TADS, for making the ",
					"  manual and source available, and then for kindly answering my ",
					"  questions when I couldn't figure it out from the previous two",
					"- in addition, after release a number of people made helpful ",
					"  comments and bug reports. Thanks to the following people for ",
					"  gamma-testing: John Cater, Ricardo Dague, Gordon K, Eric Moon, ",
					"  ShortMark Musante, Jim Nelson, Stephen Newton, Lenny Pitts, ",
					"  Hayden Pratt, Ken Ray, Kent Tessman, Rob Wheeler, ",
					"  and Per Wilhelmsson",
					"  (if you should be listed and aren't, send mail)",
					"  ",
					"If you have suggestions or bug reports, please, let me know!",
					"           -- Dan Shiovitz (dbs@cs.wisc.edu)"
				};

				print( "\\n" );
				for ( int i = 0; i < credits.length; i++ ) {
					print( credits[ i ] + "\\n" );
				}
			} else {
				print( "[Known commands (prefix with $$):" );
				for ( int i = 0; i < commands.length; i++ ) {
					print( " " + commands[ i ] );
				}
				print( "]\\b" );
			}

			throw new HaltTurnException( ParserError.ADMIN_COMMAND );
		}

		TObject preparse = getState().lookup_required_object( RequiredObjects.PREPARSE );

		if ( preparse != null ) // this one has no default
		{
			TValue ret = getRunner().run( preparse.get_data(), this.arg_array( newTValue( TValue.SSTRING, input ) ) );

			ret.must_be( TValue.TRUE, TValue.NIL, TValue.SSTRING );

			if ( ret.get_type() == TValue.SSTRING ) {
				input = ret.get_string().trim();
			} else if ( ret.get_type() == TValue.NIL ) {
				throw new HaltTurnException( ParserError.PREPARSE_ABORT );
			}
		}

		if ( input.equals( "" ) ) {
			TObject pardon = getState().lookup_required_object( RequiredObjects.PARDON );
			if ( pardon == null ) {
				print( "I beg your pardon?\\n" );
			} else {
				getRunner().run( pardon.get_data(), this.arg_array() );
			}

			throw new HaltTurnException( ParserError.NULL_INPUT );
		}

		Vector tokens = tokenize_str( input );
		_cur_line = input; // back this up

		return tokens;
	}


	// take in a string and break it up into tokens according to standard
	// parser rules
	public Vector tokenize_str( String str ) throws HaltTurnException {
		StreamTokenizer ist = new StreamTokenizer( new StringReader( str ) );

		ist.quoteChar( '"' );
		ist.wordChars( '-', '-' );
		// this is not entirely correct, as tads does some weird parsing stuff
		// with single-quoted strings, where they're sometimes word chars and
		// sometimes quote chars. But I'm not going to deal with it.
		ist.wordChars( '\'', '\'' );
		ist.ordinaryChar( '.' );
		ist.eolIsSignificant( false );
		ist.lowerCaseMode( true );

		Vector input_words = new Vector( 3 );
		try {
			while ( ist.nextToken() != StreamTokenizer.TT_EOF ) {
				if ( ist.ttype == StreamTokenizer.TT_WORD ||
					ist.ttype == StreamTokenizer.TT_NUMBER ) {
					String word;
					if ( ist.ttype == StreamTokenizer.TT_WORD ) {
						word = ist.sval;
					} else {
						word = String.valueOf( (int)ist.nval );
					}

					if ( input_words.size() > 0 ) {
						int idx = input_words.size() - 1;
						String last = ( (VocabWord)input_words.elementAt( idx ) ).get_word();
						String compound = getState().lookup_compound_word( last, word );
						if ( compound != null ) {
							input_words.removeElementAt( idx );
							word = compound;
						}
					}

					VocabWord v = getState().lookup_vocab( word );
					input_words.addElement( v );
				} else if ( ist.ttype == '"' ) {
					int strobj =
						getState().lookup_required_object( RequiredObjects.STR_OBJ ).get_id();

					VocabWord sw = new VocabWord( this.getJetty(), ist.sval, null, strobj, VocabWord.NOUN );
					sw.is_string( true );
					input_words.addElement( sw );
				} else {
					input_words.addElement( getState().lookup_vocab( String.valueOf( (char)ist.ttype ) ) );
				}
			}
		} catch ( IOException ioe ) {
			throw new HaltTurnException( ParserError.TOKENIZER_ERROR, ioe.toString() );
		}

		return input_words;
	}

	// this pulls the actor out of the command *and* disambiguates it
	private TObject parse_actor( Vector command, int from, int to )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print_error( "Looking for actor in command from=" + from + " to=" +
			to, 2 );

		Vector nc = new Vector( to - from );
		for ( int i = from; i < to; i++ ) {
			nc.addElement( command.elementAt( i ) );
		}

		ObjectMatch[] objs = parse_noun_phrase( nc, 0, null, null, false, false,
			false );
		if ( objs == null ) {
			print_error( "actor didn't parse as a phrase", 2 );
			return null;
		}

		// ok, we got a match
		if ( objs[ 0 ].get_size() != nc.size() ) // some other kind of misparse
		{
			print_error( "actor parsed, but was short (size=" +
				objs[ 0 ].get_size() + ")", 2 );
			return null;
		}

		TObject[] actor =
			getSimulator().do_disambig( Constants.PRO_RESOLVE_ACTOR, objs,
				RequiredProperties.VALID_ACTOR, null,
				null, null, null, false, null, null );
		if ( actor.length > 1 ) {
			throw new HaltTurnException( ParserError.ONE_ACTOR );
		} else {
			return actor[ 0 ];
		}
	}

	// see if the words (starting at 'from') form a noun phrase.
	// the noun phrase can be several SUB-phrases connected with 'and'.
	// if there's a problem with one or more of the phrases, null is returned
	// instead.
	public ObjectMatch[] parse_noun_phrase( Vector command, int from )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		return parse_noun_phrase( command, from, null, null, true, true, false );
	}

	public ObjectMatch[] parse_noun_phrase( Vector command, int from,
		Vector choices )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		return parse_noun_phrase( command, from, choices, null, true, true, false );
	}

	public ObjectMatch[] parse_noun_phrase( Vector command, int from,
		Vector choices, int[] types,
		boolean allow_multi,
		boolean require_match,
		boolean actor_check )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// hit the user hook first:
		{
			ObjectMatch[] om =
				call_parse_noun_phrase( command, from, types, allow_multi,
					require_match, actor_check );
			if ( om != null ) {
				if ( om.length == 0 ) // this isn't a noun phrase after all
				{
					return null;
				} else {
					return om;
				}
			}

			// otherwise, carry on with regular parsing
		}

		if ( actor_check ) {
			allow_multi = require_match = false;
		}

		// a noun phrase is a complex np, followed by zero or more of 'and' + cnp
		Vector v = new Vector();

		while ( true ) {
			VocabWord first = (VocabWord)command.elementAt( from );
			if ( first.is_specword( VocabWord.SPECWORD_THEM ) ) {
				ObjectMatch om = newObjectMatch( 0 );
				om.append_word( first );
				om.set_flags( Constants.PRSFLG_THEM | Constants.PRSFLG_ALL );
				v.addElement( om );
			} else {
				ObjectMatch om = parse_complex_np( command, from, choices,
					require_match, actor_check );
				if ( om == null ) {
					// it's ok to have it not match as a np if it's after the first one
					// (since this could be a case of 'get herring and go north' rather
					// than 'get herring and monkey')
					if ( v.size() == 0 ) {
						return null;
					} else {
						break;
					}
				} else {
					v.addElement( om );
				}
			}

			ObjectMatch last = (ObjectMatch)v.elementAt( v.size() - 1 );
			from += last.get_size(); // this from is one too big if there's no and,

			if ( !allow_multi || from >= command.size() ||
				!( (VocabWord)command.elementAt( from ) ).is_specword( VocabWord.SPECWORD_AND ) ) {
				break;
			}

			last.inc_size( 1 ); // add 1 for the and
			from++;
		}

		ObjectMatch[] ret = new ObjectMatch[ v.size() ];
		v.copyInto( (Object[])ret );

		return ret;
	}

	// a complex np is a simple np plus handling 'all', 'both', and 'any'.
	// also pronouns.
	private ObjectMatch parse_complex_np( Vector command, int from,
		Vector choices, boolean require_match,
		boolean actor_check )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// should call parseNounPhrase first

		// consider special cases:
		// 'all' or 'both' on their own (both only allowed when disambiguating)
		// 'all' or 'both' (optionally followed by 'of') followed by a simple NP
		//    with count > 0
		// 'any' on its own (only when disambiguating)
		// 'any' (plus optional 'of') followed by a simple NP

		boolean disambig = ( choices != null ); // are we in disambiguation mode?

		VocabWord first = (VocabWord)command.elementAt( from );
		if ( !actor_check &&
			( first.is_specword( VocabWord.SPECWORD_ALL ) ||
			first.is_specword( VocabWord.SPECWORD_BOTH ) ||
			first.is_specword( VocabWord.SPECWORD_ANY ) ) ) {
			print_error( "found any/all/both", 2 );

			boolean is_all = first.is_specword( VocabWord.SPECWORD_ALL );
			boolean is_any = first.is_specword( VocabWord.SPECWORD_ANY );
			int flag = is_any ? Constants.PRSFLG_ANY : Constants.PRSFLG_ALL;

			from++;

			ObjectMatch om = null;
			VocabWord found_of = null;

			if ( command.size() >= from + 1 ) {
				found_of = (VocabWord)command.elementAt( from );

				if ( found_of.is_specword( VocabWord.SPECWORD_OF ) ) {
					from++;
				} else {
					found_of = null;
				}

				om = parse_simple_np( command, from, choices, require_match,
					actor_check );
			}

			if ( om == null ) {
				// if we couldn't parse the phrase, it's illegal to have an "of"
				if ( found_of != null ) {
					if ( is_all ) {
						throw new HaltTurnException( ParserError.ALL_OF );
					} else if ( is_any ) {
						throw new HaltTurnException( ParserError.ANY_OF );
					} else {
						throw new HaltTurnException( ParserError.BOTH_OF );
					}
				}

				// any/all/both can be followed by "except":
				if ( is_all && command.size() >= from + 1 &&
					( (VocabWord)command.elementAt( from ) ).is_specword( VocabWord.SPECWORD_BUT ) ) {
					from++;

					print_error( "Returning match w/except", 2 );

					// set this to size=1 to include the "except"
					om = newObjectMatch( 1, 1, choices );
					om.append_word( first );
					om.set_flags( om.get_flags() | flag );
					om.set_except( parse_noun_phrase( command, from, choices ) );
					// now, you would think that tads would choke on it if there
					// was no noun phrase following 'except' (ie, if that
					// parse_noun_phrase returned null). but no, it happily
					// calls it the same as a regular 'all' and goes on, and so do we.
					return om;
				}
			} else {
				// it is kind of stupid to insist that the NP be plural here
				// or else claim we can't parse the sentence, but apparently that's
				// what tads does, so. (this is not a requirement if the thing is
				// "any" -- but it is if it's "any of")
				if ( om.test_flags( Constants.PRSFLG_PLURAL ) ||
					( is_any && found_of == null ) ) {
					print_error( "Returning match w/subphrase", 2 );
					om.set_flags( om.get_flags() | flag );
					if ( found_of != null ) {
						om.prepend_word( found_of );
					}
					om.prepend_word( first );
					return om;
				}
			}

			if ( is_all || disambig ) {
				print_error( "Returning regular match (no subphrase)", 2 );
				ObjectMatch o = newObjectMatch( 0, 1, choices );
				o.append_word( first );
				o.set_flags( o.get_flags() | flag );
				return o;
			} else {
				throw new HaltTurnException( ParserError.DONT_UNDERSTAND );
			}
		} else if ( first.is_specword( VocabWord.SPECWORD_IT ) ||
			first.is_specword( VocabWord.SPECWORD_HIM ) ||
			first.is_specword( VocabWord.SPECWORD_HER ) ) {
			ObjectMatch om = newObjectMatch( 0 );
			om.append_word( first );
			if ( first.is_specword( VocabWord.SPECWORD_IT ) ) {
				om.set_flags( Constants.PRSFLG_IT );
			} else if ( first.is_specword( VocabWord.SPECWORD_HIM ) ) {
				om.set_flags( Constants.PRSFLG_HIM );
			} else {
				om.set_flags( Constants.PRSFLG_HER );
			}
			return om;
		}

		return parse_simple_np( command, from, choices, require_match, actor_check );
	}

	// a simple NP is a core NP optionally followed by 'of' + core NP:
	private ObjectMatch parse_simple_np( Vector command, int from, Vector choices,
		boolean require_match,
		boolean actor_check )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		ObjectMatch core = parse_core_np( command, from, choices, require_match,
			actor_check );
		if ( core == null ) {
			return null;
		}

		if ( command.size() <= ( from + core.get_size() ) ) {
			return core;
		} else {
			VocabWord next = (VocabWord)command.elementAt( from + core.get_size() );
			if ( !next.is_specword( VocabWord.SPECWORD_OF ) ) {
				return core;
			}

			core.append_word( next );
			from += core.get_size();
		}

		ObjectMatch core2 = parse_core_np( command, from, choices, require_match,
			actor_check );
		print_error( "Found 'OF' + another NP, considering merging", 2 );
		// note that we have to do core.merge(core2) and not the other way around
		// because the count and disambig depend on the first one, not the
		// second ("wizards of the castle" is plural, "castle of wizards" singular)
		if ( core2 != null ) {
			core.merge( core2 );
			core.inc_size( 1 ); // add one for the 'of'
			print_error( "merge successful", 2 );
		} else {
			print_error( "merge failed, skipping", 2 );
		}

		return core;
	}

	// a core NP is an optional article, zero or more adjectives, and an
	// optional noun or plural
	private ObjectMatch parse_core_np( Vector command, int from, Vector choices,
		boolean require_match, boolean actor_check )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// first work out what words'll be in this phrase
		boolean article = false;
		boolean disambig = ( choices != null ); // are we in disambiguate mode
		int matchsize = 0;
		int nounpos = -1;  // since there can be a trailing number-adjective

		VocabWord word = (VocabWord)command.elementAt( from );
		if ( word.has_objects( VocabWord.ARTICLE ) ) {
			article = true;
			matchsize++;
		}

		while ( from + matchsize < command.size() ) {
			word = (VocabWord)command.elementAt( from + matchsize );
			if ( word.has_objects( VocabWord.ADJECTIVE ) || word.is_unknown() ) {
				if ( word.has_objects( VocabWord.NOUN ) ||
					word.has_objects( VocabWord.PLURAL ) ||
					word.is_unknown() ) {
					nounpos = matchsize;
				}

				matchsize++;
			} else {
				break;
			}
		}

		if ( from + matchsize < command.size() ) {
			word = (VocabWord)command.elementAt( from + matchsize );

			// during actor checks, quoted strings are not allowed:
			if ( actor_check && word.is_string() ) {
				return null;
			}

			if ( word.has_objects( VocabWord.NOUN ) ||
				word.has_objects( VocabWord.PLURAL ) ||
				word.is_unknown() ||
				// if we're in disambiguation mode, allow 'one'/'ones'
				( disambig &&
				( word.is_specword( VocabWord.SPECWORD_ONE ) ||
				word.is_specword( VocabWord.SPECWORD_ONES ) ) ) ) {
				nounpos = matchsize++;

				// and, maybe, a trailing adjective that's a number:
				if ( from + matchsize < command.size() ) {
					word = (VocabWord)command.elementAt( from + matchsize );
					if ( word.is_number() ) {
						matchsize++;
					}
				}
			} else if ( word.has_objects( VocabWord.ADJECTIVE ) ) {
				matchsize++;
			} else if ( article && matchsize == 1 ) {
				throw new HaltTurnException( ParserError.ART_NOUN );
			} else if ( matchsize == 0 ) {
				return null;
			}
		}

		// loop until either we get a match where all words refer to a single
		// object, or we don't care about that. if we don't satisfy that on the
		// first pass, we have a second (or third, or whatever) chance: if
		// a word in the phrase is also usable as a preposition, we can assume
		// the noun phrase extends only up to the preposition and see if that
		// by itself will work; in addition, if the first word of the phrase is
		// a number, we can assume we're in a count+plural phrase (or singular,
		// if count = 1) situation and reparse for that
		int count = 1;
		boolean tried_as_count = false;
		int unknown_count = 0;
		MATCH: while ( true ) {
			// ok, now actually see if all the words refer to a single object, and
			// complain otherwise
			ObjectMatch match = newObjectMatch( ( article ? 1 : 0 ), count, choices );
			print_error( "Core NP pre-words: " + match, 2 );

			// (but skip an initial article if one exists)
			for ( int pos = article ? 1 : 0; pos < matchsize; pos++ ) {
				word = (VocabWord)command.elementAt( from + pos );

				int types = Constants.PRSTYP_ADJ;
				if ( pos == nounpos ) {
					types |= Constants.PRSTYP_NOUN | Constants.PRSTYP_PLURAL;
				}
				if ( !match.add_word( word, null, types ) ) {
					unknown_count++;
					print_error( "unknown word; count is now " + unknown_count,
						2 );
				}
			}

			// if it's a single word, and that word is a number, always add in
			// numObj, since we might want it.
			if ( matchsize == 1 && ( (VocabWord)command.elementAt( from ) ).is_number() ) {
				match.add_object( getState().lookup_required_object( RequiredObjects.NUM_OBJ ) );
			}

			// consider the case where it matched badly:
			if ( match.num_matches() == 0 || unknown_count > 0 ) {
				// first try reparsing this as having an initial count
				if ( !tried_as_count && ( matchsize > 1 ) &&
					( (VocabWord)command.elementAt( from ) ).is_number() ) {
					VocabWord vw = (VocabWord)command.elementAt( from );
					count = vw.get_number();

					from++;
					matchsize--;
					nounpos--;
					tried_as_count = true;

					print_error( "Reparsing phrase with a count: " + count, 2 );
					continue;
				}

				// then try hacking off a preposition
				for ( int newms = matchsize - 2; newms > ( article ? 1 : 0 ); newms-- ) {
					word = (VocabWord)command.elementAt( newms );
					if ( word.has_objects( VocabWord.PREPOSITION ) ) {
						print_error( "Found a preposition, so shrinking " +
							"matchsize to " + ( newms - 1 ), 2 );
						matchsize = newms - 1;
						continue MATCH; // so we get another chance
					}
				}

				// if neither of those can happen, we're stuck with it:
			}

			// allow it if either it had matches or were all unknown words
			if ( match.num_matches() > 0 || unknown_count == match.get_words().size() ) {
				print_error( "Core NP at end: " + match, 2 );
				if ( tried_as_count ) // set count flag and add 1 for count word size
				{
					// this only was a valid match if the count is 1 or it's plural,
					// though:
					if ( count == 1 || match.test_flags( Constants.PRSFLG_PLURAL ) ) {
						match.set_flags( match.get_flags() | Constants.PRSFLG_COUNT );
						match.inc_size( 1 );
						return match;
					} else {
						// add back word and fall through to matched-nothing case:
						match.prepend_word( (VocabWord)command.elementAt( from - 1 ) );
					}
				} else {
					return match;
				}
			}

			print_error( "Core NP ended up matching no objects", 2 );

			if ( !require_match ) {
				print_error( "...but that's ok", 2 );
				return match;
			}

			String phrase = match.get_phrase();
			throw new HaltTurnException( ParserError.DONT_SEE_ANY, phrase );
		}
	}

	private ObjectMatch[] call_parse_noun_phrase( Vector command, int from, int[] tarr, boolean allow_multi, boolean require_match, boolean actor_check )
		throws ParseException, ReparseException, HaltTurnException, GameOverException
	{
		TObject pnp = getState().lookup_required_object( RequiredObjects.PARSE_NOUN_PHRASE );
		if ( pnp == null ) {
			return null;
		}

		print_error( "Calling parseUnknownVerb()", 2 );

		Vector words = new Vector( command.size() );
		Vector types = new Vector( command.size() );
		for ( int i = 0; i < command.size(); i++ ) {
			VocabWord vw = (VocabWord)command.elementAt( i );
			words.addElement( newTValue( TValue.SSTRING, vw.get_word() ) );
			if ( tarr != null ) {
				types.addElement( newTValue( TValue.NUMBER, tarr[ i ] ) );
			} else {
				types.addElement( newTValue( TValue.NUMBER, vw.get_flags() ) );
			}
		}

		TValue[] args = new TValue[ 5 ];
		args[ 0 ] = newTValue( TValue.LIST, words );
		args[ 1 ] = newTValue( TValue.LIST, types );
		args[ 2 ] = newTValue( TValue.NUMBER, from + 1 ); // tads indexes from 1!
		args[ 3 ] = newTValue( ( require_match ? TValue.TRUE : TValue.NIL ), 0 );
		args[ 4 ] = newTValue( ( actor_check ? TValue.TRUE : TValue.NIL ), 0 );

		TValue ret = getRunner().run( pnp.get_data(), args );
		ret.must_be( TValue.NUMBER, TValue.LIST );

		if ( ret.get_type() == TValue.NUMBER ) {
			int rv = ret.get_number();
			if ( rv == Constants.PNP_USE_DEFAULT ) {
				return null; // this'll tell the parse noun phrase function to continue
			} else {
				throw new HaltTurnException( ParserError.PNP_ERROR );
			}
		} else {
			Vector lst = ret.get_list();
			if ( lst.size() == 0 ) {
				print_error( "List from parseNounPhrase() is size 0", 1 );
				return null;
			}

			int sz = ( (TValue)lst.elementAt( 0 ) ).get_number();
			lst.removeElementAt( 0 );

			if ( sz == from ) // this isn't a noun phrase after all
			{
				return new ObjectMatch[ 0 ];
			}

			ObjectMatch[] om = new ObjectMatch[ 1 ];
			om[ 0 ] = newObjectMatch( 0 );
			for ( int i = from; i < sz; i++ ) {
				om[ 0 ].append_word( ( (VocabWord)command.elementAt( i ) ) );
			}

			Vector excepts = null;

			for ( int i = 0; i < lst.size(); i++ ) {
				TValue tv = (TValue)lst.elementAt( i );
				tv.must_be( TValue.OBJECT, TValue.NIL );

				int flags = 0;
				if ( i + 1 < lst.size() ) {
					TValue tv2 = (TValue)lst.elementAt( i + 1 );
					if ( tv2.get_type() == TValue.NUMBER ) {
						flags = tv2.get_number();
						i++;
					}
				}

				TObject obj;
				if ( tv.get_type() == TValue.NIL ) {
					obj = null;
				} else {
					obj = getState().lookup_object( tv.get_object() );
				}

				// "except" has to be handled special, since we keep it on a
				// SUB-object
				if ( ( flags & Constants.PRSFLG_EXCEPT ) != 0 ) {
					flags &= ~Constants.PRSFLG_EXCEPT;
					ObjectMatch m = newObjectMatch( 0 );
					m.add_object( obj, flags );
					if ( excepts != null ) {
						excepts = new Vector();
					}
					excepts.addElement( m );
				} else {
					om[ 0 ].add_object( obj, flags );
				}
			}

			if ( excepts != null ) {
				ObjectMatch[] ex = new ObjectMatch[ excepts.size() ];
				for ( int i = 0; i < ex.length; i++ ) {
					ex[ i ] = (ObjectMatch)excepts.elementAt( i );
				}
				om[ 0 ].set_except( ex );
			}

			return om;
		}
	}

	private int call_parse_unknown_verb( Vector command, TObject actor, int err )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TObject puv = getState().lookup_required_object( RequiredObjects.PARSE_UNKNOWN_VERB );
		String word = ( (VocabWord)command.elementAt( 0 ) ).get_word();
		if ( puv == null ) {
			if ( err == ParserError.UNK_WORD ) {
				throw new HaltTurnException( err, word );
			} else {
				throw new HaltTurnException( err );
			}
		}

		print_error( "Calling parseUnknownVerb()", 2 );

		Vector words = new Vector( command.size() );
		Vector types = new Vector( command.size() );
		for ( int i = 0; i < command.size(); i++ ) {
			VocabWord vw = (VocabWord)command.elementAt( i );
			words.addElement( newTValue( TValue.SSTRING, vw.get_word() ) );
			types.addElement( newTValue( TValue.NUMBER, vw.get_flags() ) );
		}

		TValue[] args = new TValue[ 4 ];
		args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
		args[ 1 ] = newTValue( TValue.LIST, words );
		args[ 2 ] = newTValue( TValue.LIST, types );
		if ( err == ParserError.UNK_WORD ) {
			args[ 3 ] = newTValue( TValue.NUMBER, ParserError.NO_VERB );
		} else {
			args[ 3 ] = newTValue( TValue.NUMBER, err );
		}

		int count = 0;
		int status = Constants.EC_SUCCESS;
		boolean run_daemons = true;
		try {
			TValue ret = getRunner().run( puv.get_data(), args );

			ret.must_be( TValue.TRUE, TValue.NUMBER, TValue.NIL );
			if ( ret.get_type() == TValue.NIL ) {
				print_error( "parseUnknownVerb() returned nil", 2 );

				if ( err == ParserError.UNK_WORD ) {
					throw new HaltTurnException( err, word );
				} else {
					throw new HaltTurnException( err );
				}
			} else {
				count = ( ret.get_type() == TValue.TRUE ) ? command.size() :
					ret.get_number();
				print_error( "parseUnknownVerb() returned: " + count, 2 );
			}
		} catch ( AbortException a ) {
			print_error( "parseUnknownVerb() called abort()", 2 );
			count = command.size();
			run_daemons = false;
			status = Constants.EC_ABORT;
		} catch ( ExitException e ) {
			print_error( "parseUnknownVerb() called exit()", 2 );
			status = Constants.EC_EXIT;
		} catch ( ExitobjException e ) {
			print_error( "parseUnknownVerb() called exitobj()", 2 );
			status = Constants.EC_EXITOBJ;
		}

		getSimulator().do_end_of_turn( run_daemons, null, null, null, null, null,
			status );

		return count;
	}

	private Vector _tokens = null; // tokens to use next, if we're reparsing
	private String _unknown = null; // if there's an unknown word, what it is
	private String _cur_line = null; // current input line being parsed
}
