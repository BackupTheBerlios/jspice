package tads2.jetty;

// this class handles the world simulator.
// ie, after you've parsed the player's commands to figure out what the
// verb/dobj/prep/iobj are, or at least what the possibilities for them are,
// this disambiguates them and then performs the actual action
//

import java.util.Vector;

public class Simulator extends JettyRef {

	public Simulator( Jetty jetty ) {
		super( jetty );
	}

	// require_dobj = if we should always prompt for a dobj even if the
	//                verb template is sufficient without it
	//                (there's no require_iobj equivalent because you pass
	//                 in a value for prep if you want it to require an iobj)
	public int do_turn( TObject actor, TObject verb, ObjectMatch[] dobj,
		TObject prep, ObjectMatch[] iobj, boolean require_dobj )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print_error( "actor=" + actor.get_id() + " verb=" + verb.get_id(), 2 );

		int status = Constants.EC_SUCCESS;

		// first check if the verb is again:
		boolean again_verb =
			( verb == getState().lookup_required_object( RequiredObjects.AGAIN_VERB ) );

		if ( again_verb ) {
			// retrieve stored values:
			Vector new_values = handle_again();
			actor = (TObject)new_values.elementAt( 0 );
			verb = (TObject)new_values.elementAt( 1 );
			dobj = (ObjectMatch[])new_values.elementAt( 2 );
			prep = (TObject)new_values.elementAt( 3 );
			iobj = (ObjectMatch[])new_values.elementAt( 4 );
		}

		// ok, having determined what the "real" verbs are for this command,
		// work out the template to use:
		// (the has_iobj is prep != null || iobj != null; the former handles
		// the case where an askio() happened, the latter handles the case
		// where someone did >X NOUN NOUN: there is clearly an iobj so it can't
		// pick non-iobj verb templates, but there's no prep so has_iobj would
		// otherwise be false
		int[] template = pick_template( verb, ( require_dobj || dobj != null ),
			( prep != null || iobj != null ), prep );

		if ( template != null ) {
			String t = "";
			for ( int i = 0; i < template.length; i++ ) {
				t += template[ i ] + " ";
			}
			print_error( "Got template: " + t, 2 );
		}

		// ok, and store the matches (this is what objwords() will use, so it
		// has to be done before doDefault is called in fill_in_objs)
		getState().set_command_match( 1, dobj == null ? null : dobj[ 0 ] );
		getState().set_command_match( 2, iobj == null ? null : iobj[ 0 ] );

		boolean dobj_first = false;
		if ( template != null && ( template[ 5 ] & 0x01 ) != 0 ) // VOCTPLFLG_DOBJ_FIRST
		{
			dobj_first = true;
			print_error( "Note: this is a disambigDobjFirst verb", 2 );
		}

		if ( template[ 3 ] > 0 && dobj == null ) {
			boolean two_args = !dobj_first && ( template[ 1 ] != 0 );
			dobj = fill_in_objs( actor, verb, null, null, two_args, template[ 3 ] );
			getState().set_command_match( 1, dobj == null ? null : dobj[ 0 ] );
		}

		if ( template[ 1 ] > 0 ) {
			// we always want to use the prep in the template (ie, even if prep
			// was unset coming into this function)
			prep = getState().lookup_object( template[ 0 ] );
		}

		if ( template[ 1 ] > 0 && iobj == null ) {
			iobj = fill_in_objs( actor, verb, prep, dobj, dobj_first, template[ 1 ] );
			getState().set_command_match( 2, iobj == null ? null : iobj[ 0 ] );
		}

		TObject[] dobj_list = null;
		TObject[] iobj_list = null;
		Vector match_list = new Vector();

		for ( int i = 0; i <= 1; i++ ) {
			if ( dobj != null && ( ( i == 0 && dobj_first ) || ( i == 1 && !dobj_first ) ) ) {
				TObject other = ( iobj_list == null ) ? null : iobj_list[ 0 ];

				dobj_list = do_disambig( Constants.PRO_RESOLVE_DOBJ, dobj, template[ 3 ],
					actor, verb, null, other, false, null,
					match_list );
				if ( dobj_list.length == 0 ) {
					throw new HaltTurnException( ParserError.DONT_KNOW_REF2 );
				} else if ( dobj_list.length > 1 ||
					dobj[ 0 ].test_flags( Constants.PRSFLG_ALL ) ) {
					// check with verb to see if this is ok; it's never ok if
					// disambigDobjFirst is set
					TValue rv = verb.eval_property( RequiredProperties.REJECT_MDO, this.arg_array() );
					if ( rv.get_logical() || dobj_first ) {
						throw new HaltTurnException( ParserError.NO_MULTI );
					}
				}
			} else if ( iobj != null && ( ( i == 0 && !dobj_first ) || ( i == 1 && dobj_first ) ) ) {
				TObject other = ( dobj_list == null ) ? null : dobj_list[ 0 ];

				iobj_list = do_disambig( Constants.PRO_RESOLVE_IOBJ, iobj, template[ 1 ],
					actor, verb, prep, other, false, null, null );
				if ( iobj_list.length == 0 ) {
					throw new HaltTurnException( ParserError.DONT_KNOW_REF2 );
				} else if ( iobj_list.length > 1 ) {
					throw new HaltTurnException( ParserError.NO_MULTI_IO );
				}
			}
		}

		TObject the_iobj = null;
		if ( iobj_list != null ) {
			the_iobj = iobj_list[ 0 ];
		}

		// set pronouns appropriately:
		if ( dobj_list != null ) {
			getState().set_it( Constants.PO_IT, dobj_list[ 0 ] );

			TValue is_him = dobj_list[ 0 ].eval_property( RequiredProperties.IS_HIM,
				this.arg_array() );
			if ( is_him.get_logical() ) {
				getState().set_it( Constants.PO_HIM, dobj_list[ 0 ] );
			}

			TValue is_her = dobj_list[ 0 ].eval_property( RequiredProperties.IS_HER,
				this.arg_array() );
			if ( is_her.get_logical() ) {
				getState().set_it( Constants.PO_HER, dobj_list[ 0 ] );
			}

			getState().set_them( dobj_list );
		}

		status = do_start_of_turn( actor, verb, dobj_list, prep, the_iobj );

		// only do the action if the start of turn check didn't fail:
		if ( status == Constants.EC_SUCCESS ) {
			// determine if we'll want to print object names in the loop:
			boolean print_name = false;
			int print_name_flags = 0;
			if ( dobj_list != null ) {
				if ( dobj_list.length != 1 || !dobj[ 0 ].is_ambig() ) {
					print_name = true;
				}
				if ( dobj_list.length == 1 ) {
					print_name_flags = ( dobj[ 0 ].get_flags() & ( Constants.PRSFLG_ALL | Constants.PRSFLG_ANY | Constants.PRSFLG_THEM ) );
				}
			}

			int max = ( dobj_list == null ) ? 1 : dobj_list.length;
			for ( int i = 0; i < max; i++ ) {
				TObject the_dobj = null;
				if ( dobj_list != null ) {
					the_dobj = dobj_list[ i ];
				}

				// store the parts of the command for later retrieval
				// (by objwords(), parseGetObject())
				getState().set_command_object( Constants.PO_ACTOR, actor );
				getState().set_command_object( Constants.PO_VERB, verb );
				getState().set_command_object( Constants.PO_DOBJ, the_dobj );
				getState().set_command_object( Constants.PO_PREP, prep );
				getState().set_command_object( Constants.PO_IOBJ, the_iobj );

				// and set the dobj matchobj to the correct value each time
				// for objwords() purposes:
				if ( the_dobj != null ) {
					getState().set_command_match( 1, (ObjectMatch)match_list.elementAt( i ) );
				}

				// stick in a newline here if there are multiple objects:
				if ( i != 0 ) {
					print( "\\n" );
				}

				if ( the_dobj != null ) {
					multiobj_prefix( the_dobj, print_name, ( i + 1 ), max, print_name_flags );
				}

				status = do_execute( template, actor, verb, the_dobj, prep, the_iobj,
					false, true, true );

				// if an abort or exit was executed, we don't want to do any
				// more objects:
				if ( status == Constants.EC_EXIT || status == Constants.EC_ABORT ) {
					break;
				}
			}
		}

		boolean run_daemons = ( status != Constants.EC_ABORT );
		do_end_of_turn( run_daemons, actor, verb, dobj_list, prep, the_iobj,
			status );

		return status;
	}

	// this returns a vector because it contains both TObjects and ObjectMatch[]s
	// I am not really in favor of this in general but it seems the cleanest way
	// here.
	private Vector handle_again()
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		Vector ret = new Vector( 5 );

		TObject actor = getState().get_command_object( Constants.PO_ACTOR );
		TObject verb = getState().get_command_object( Constants.PO_VERB );
		TObject the_dobj = getState().get_command_object( Constants.PO_DOBJ );
		TObject prep = getState().get_command_object( Constants.PO_PREP );
		TObject the_iobj = getState().get_command_object( Constants.PO_IOBJ );

		ObjectMatch[] dobj = null;
		ObjectMatch[] iobj = null;

		if ( the_dobj != null ) {
			ObjectMatch o = getState().get_command_match( 1 );
			dobj = new ObjectMatch[ 1 ];
			dobj[ 0 ] = newObjectMatch( 0 );
			dobj[ 0 ].add_object( the_dobj );
			dobj[ 0 ].copy_words( o );
		}

		if ( the_iobj != null ) {
			ObjectMatch o = getState().get_command_match( 2 );
			iobj = new ObjectMatch[ 1 ];
			iobj[ 0 ] = newObjectMatch( 0 );
			iobj[ 0 ].add_object( the_iobj );
			iobj[ 0 ].copy_words( o );
		}

		// and of course these have to be valid:
		if ( verb == null || actor == null ) {
			throw new HaltTurnException( ParserError.NO_AGAIN );
		} else {
			TValue tv = actor.eval_property( RequiredProperties.VALID_ACTOR,
				this.arg_array() );
			if ( !tv.get_logical() ) {
				throw new HaltTurnException( ParserError.BAD_AGAIN );
			}
		}

		for ( int o = 0; o < 2; o++ ) {
			if ( o == 0 && the_dobj == null ) {
				continue;
			}
			if ( o == 1 && the_iobj == null ) {
				continue;
			}

			TObject obj = ( o == 0 ) ? the_dobj : the_iobj;

			TValue[] args = new TValue[ 3 ];
			args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
			args[ 1 ] = newTValue( TValue.OBJECT, obj.get_id() );
			args[ 2 ] = newTValue( TValue.NUMBER, 1 );
			TValue tv;
			if ( o == 0 ) {
				tv = verb.eval_property( RequiredProperties.VALID_DO, args );
			} else {
				tv = verb.eval_property( RequiredProperties.VALID_IO, args );
			}
			if ( !tv.get_logical() &&
				obj != getState().lookup_required_object( RequiredObjects.NUM_OBJ ) &&
				obj != getState().lookup_required_object( RequiredObjects.STR_OBJ ) ) {
				throw new HaltTurnException( ParserError.BAD_AGAIN );
			}
		}

		ret.addElement( actor );
		ret.addElement( verb );
		ret.addElement( dobj );
		ret.addElement( prep );
		ret.addElement( iobj );

		return ret;
	}

	public int[] pick_template( TObject verb, boolean has_dobj, boolean has_iobj,
		TObject prep )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// if the prep is unset, work out what it should be (this doesn't
		// necessarily mean it'll be used, but if it is, we need to know it)
		if ( prep == null ) {
			TValue dp = verb.eval_property( RequiredProperties.PREP_DEFAULT,
				this.arg_array() );
			if ( dp.get_type() == TValue.OBJECT ) {
				prep = getState().lookup_object( dp.get_object() );
			} else {
				dp.must_be( TValue.NIL );
				// this is a really, really vile hack:
				VocabWord vw = getState().lookup_vocab( "to" );
				int t = vw.get_first_object( VocabWord.PREPOSITION, null );
				prep = getState().lookup_object( t );
			}
		}

		print_error( "Using prep=" + prep.get_id(), 2 );

		// if there's no dobj and no iobj and the verb has an action(actor) method,
		// just use that:
		if ( !has_dobj && !has_iobj &&
			verb.lookup_property( RequiredProperties.ACTION ) != null ) {
			int[] t = {0, 0, 0, 0, 0, 0};
			return t;
		}

		// we're going to loop until we find a template or the obj has no
		// superclasses
		boolean first_time = true;
		int[] template = null;

		while ( true ) {
			TObject[] source = new TObject[ 1 ];
			TValue tpl = verb.lookup_property( RequiredProperties.TPL2, true,
				first_time, source );

			// Try and allow old-style templates, for Dan Greenlee
			if ( tpl == null ) {
				tpl = verb.lookup_property( RequiredProperties.TPL, true,
					first_time, source );
			}

			// once we're out of templates, no use looping further:
			if ( tpl == null ) {
				break;
			}

			Vector templates = tpl.get_template();

			for ( int i = 0; templates != null && i < templates.size(); i++ ) {
				// order is: prep verIo io verDo do flags
				int[] t = (int[])templates.elementAt( i );

				String tt = "";
				for ( int j = 0; j < t.length; j++ ) {
					tt += t[ j ] + " ";
				}
				print_error( "Read template: " + tt, 2 );

				// you want to prefer a template that matches your dobj/iobj
				// settings, although it's not a fatal error if the template
				// wants a dobj/iobj and you don't have one (but the converse
				// *is* an fatal error):
				if ( ( t[ 1 ] < 1 && has_iobj ) || ( t[ 3 ] < 1 && has_dobj ) ) {
					print_error( "Rejecting because dobj/iobj exists", 2 );
					continue;
				}

				// or if there's a non-matching prep, thatsa no good either
				else if ( t[ 0 ] > 0 && t[ 0 ] != prep.get_id() ) {
					print_error( "Rejecting because prep doesn't match", 2 );
					continue;
				}

				// now we're more in the area of priority, not error.
				// if there is no iobj, prefer dobj-only but allow dobj+iobj
				// else, only allow dobj+iobj

				if ( !has_iobj ) {
					if ( t[ 1 ] < 1 ) {
						// this is the preferred match, dobj-only
						return t;
					} else {
						// this is the less-preferred match, but we'll allow it
						print_error( "Allowing verb+dobj+iobj template for only dobj given", 2 );
						template = t;
					}
				} else {
					// well, we know this template must allow iobjs and has the right
					// preposition, so go with it:
					return t;
				}
			}

			// if we got here without returning, it's onto the superclass:
			verb = source[ 0 ];
			first_time = false;
		}

		// if we got a template, it's less-preferred, but we use it anyway:
		if ( template != null ) {
			return template;
		} else {
			// but if not, bah.
			print_error( "No matching templates at all", 2 );
			throw new HaltTurnException( ParserError.DONT_RECOGNIZE );
		}
	}

	// the dobj/iobj is unknown: first try and fill it in with doDefault
	// and so on, then ask the player
	// two_args = should the verify method take actor + dobj/iobj, or just
	//            actor
	private ObjectMatch[] fill_in_objs( TObject actor, TObject verb, TObject prep,
		ObjectMatch[] dobj, boolean two_args,
		int prop )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print_error( "Object not known, trying to fill it in", 2 );

		TValue[] def_args;
		int def_prop;

		if ( prep == null ) // then it's the dobj that's unknown
		{
			print_error( "Checking doDefault", 2 );
			def_args = this.arg_array( newTValue( TValue.OBJECT, actor.get_id() ),
				newTValue( TValue.NIL, 0 ),
				newTValue( TValue.NIL, 0 ) );
			def_prop = RequiredProperties.DO_DEFAULT;
		} else {
			print_error( "Checking ioDefault", 2 );
			def_args = this.arg_array( newTValue( TValue.OBJECT, actor.get_id() ),
				newTValue( TValue.OBJECT, prep.get_id() ) );
			def_prop = RequiredProperties.IO_DEFAULT;
		}

		TValue def = verb.eval_property( def_prop, def_args );
		def.must_be( TValue.NIL, TValue.LIST );
		if ( def.get_type() == TValue.LIST ) {
			TObject keep = null;
			// when two_args is true, the second arg is null because we don't know
			// it yet; this really happens in standard tads. you might have, eg,
			// lock: item
			//   verDoUnlockWith(actor, iobj) = { if (iobj.someproperty) "hello"; }
			// and you do >UNLOCK, it will give an error because it will pass
			// nil as the value for iobj, and then try and eval nil.someproperty,
			// which is an error.
			TValue[] ver_args = new TValue[ two_args ? 2 : 1 ];
			ver_args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
			if ( two_args ) {
				ver_args[ 1 ] = newTValue( TValue.NIL, 0 );
			}

			Vector possibles = def.get_list();
			print_error( "Checking list size=" + possibles.size(), 2 );

			for ( int i = 0; i < possibles.size(); i++ ) {
				int obj = ( (TValue)possibles.elementAt( i ) ).get_object();
				TObject t = getState().lookup_object( obj );

				int hide = hide_output();
				eval_verb_verify( verb, t, null, null, prop, ver_args );
				if ( !unhide_output( hide ) ) {
					if ( keep == null ) {
						keep = t;
					} else {
						// oops, it's ambiguous: set keep to null and quit immediately
						keep = null;
						break;
					}
				}
			}

			if ( keep != null ) {
				print_error( "Found a single obj that works!", 2 );

				// ok, we found a single object that works. this is good 'nuff.
				// print out "(the <dobj>)" / "(<prep> the <iobj>)", so the player
				// will know:
				TObject parse_default =
					getState().lookup_required_object( RequiredObjects.PARSE_DEFAULT );

				if ( parse_default != null ) {
					TValue arg1 = newTValue( TValue.OBJECT, keep.get_id() );
					TValue arg2 = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
						newTValue( TValue.OBJECT, prep.get_id() );
					getRunner().run( parse_default.get_data(),
						this.arg_array( arg1, arg2 ) );
				} else {
					getPerror().print( ParserError.ASSUME_OPEN );
					if ( prep != null ) {
						prep.eval_property( RequiredProperties.SDESC, this.arg_array() );
						getPerror().print( ParserError.ASSUME_SPACE );
					}
					keep.eval_property( RequiredProperties.THEDESC,
						this.arg_array() );
					getPerror().print( ParserError.ASSUME_CLOSE );
				}

				ObjectMatch[] om = new ObjectMatch[ 1 ];
				om[ 0 ] = newObjectMatch( 0 );
				om[ 0 ].add_object( keep );
				return om;
			} else {
				print_error( "Can't find just a single obj that works, asking player", 2 );
			}
		}

		TObject askobj_indir = getState().lookup_required_object( RequiredObjects.PARSE_ASKOBJ_INDIRECT );
		TObject askobj_actor = getState().lookup_required_object( RequiredObjects.PARSE_ASKOBJ_ACTOR );
		TObject askobj = getState().lookup_required_object( RequiredObjects.PARSE_ASKOBJ );

		if ( prep != null && askobj_indir != null ) {
			Vector info_list = new Vector();
			for ( int i = 0; i < dobj.length; i++ ) {
				Vector single = new Vector();

				Vector words_list = new Vector();
				Vector words = dobj[ i ].get_words();
				for ( int j = 0; j < words.size(); j++ ) {
					words_list.addElement( newTValue( TValue.SSTRING, ( (VocabWord)words.elementAt( j ) ).get_word() ) );
				}
				single.addElement( newTValue( TValue.LIST, words_list ) );

				Vector objs_list = new Vector();
				Vector objs = dobj[ i ].get_matches();
				for ( int j = 0; j < objs.size(); j++ ) {
					objs_list.addElement( newTValue( TValue.OBJECT, ( (TObject)objs.elementAt( j ) ).get_id() ) );
				}

				Vector flags_list = new Vector( objs_list.size() );
				for ( int j = 0; j < objs_list.size(); j++ ) {
					flags_list.addElement( newTValue( TValue.NUMBER, dobj[ i ].get_match_flag( j ) ) );
				}

				info_list.addElement( newTValue( TValue.LIST, single ) );
			}

			TValue arg1 = newTValue( TValue.OBJECT, actor.get_id() );
			TValue arg2 = newTValue( TValue.OBJECT, verb.get_id() );
			TValue arg3 = newTValue( TValue.OBJECT, prep.get_id() );
			TValue arg4 = newTValue( TValue.LIST, info_list );
			getRunner().run( askobj_indir.get_data(),
				this.arg_array( arg1, arg2, arg3, arg4 ) );
		} else if ( askobj_actor != null ) {
			TValue arg1 = newTValue( TValue.OBJECT, actor.get_id() );
			TValue arg2 = newTValue( TValue.OBJECT, verb.get_id() );
			TValue arg3 = ( prep == null ) ? null :
				newTValue( TValue.OBJECT, prep.get_id() );
			if ( arg3 == null ) {
				getRunner().run( askobj_actor.get_data(),
					this.arg_array( arg1, arg2 ) );
			} else {
				getRunner().run( askobj_actor.get_data(),
					this.arg_array( arg1, arg2, arg3 ) );
			}
		} else if ( askobj != null ) {
			TValue arg1 = newTValue( TValue.OBJECT, verb.get_id() );
			TValue arg2 = ( prep == null ) ? null :
				newTValue( TValue.OBJECT, prep.get_id() );
			if ( arg2 == null ) {
				getRunner().run( askobj.get_data(), this.arg_array( arg1 ) );
			} else {
				getRunner().run( askobj.get_data(), this.arg_array( arg1, arg2 ) );
			}
		} else {
			// "what do you want <the actor> to" vs "what do you want to":
			if ( actor != getState().get_parser_me() ) {
				getPerror().print( ParserError.WHAT_PFX2 );
				actor.eval_property( RequiredProperties.THEDESC, this.arg_array() );
				getPerror().print( ParserError.WHAT_TOSPC );
			} else {
				getPerror().print( ParserError.WHAT_PFX );
			}

			// "<verb>":
			verb.eval_property( RequiredProperties.SDESC, this.arg_array() );

			// if we're asking for an iobj, we need to add "<the dobj> <prep>"
			if ( prep != null ) {
				// ok, so here's the scoop: if there are multiple matches, use "them".
				// else if the match has count > 1 or is an all-match, use "them".
				// else ask each possibility if it's "isThem", "isHim", and "isHer"
				// if any one of those gets a yes from everybody, use that. else
				// use "it".
				int pronoun = 0;
				if ( dobj.length > 1 || dobj[ 0 ].get_count() > 1 ||
					dobj[ 0 ].test_flags( Constants.PRSFLG_ALL ) ) {
					pronoun = ParserError.WHAT_THEM;
				}

				int them_count = 0, him_count = 0, her_count = 0;
				Vector objs = dobj[ 0 ].get_matches();

				for ( int i = 0; i < objs.size(); i++ ) {
					TObject t = (TObject)objs.elementAt( i );
					TValue[] args = this.arg_array();
					if ( t.eval_property( RequiredProperties.IS_THEM, args ).get_logical() ) {
						them_count++;
					}
					if ( t.eval_property( RequiredProperties.IS_HIM, args ).get_logical() ) {
						him_count++;
					}
					if ( t.eval_property( RequiredProperties.IS_HER, args ).get_logical() ) {
						her_count++;
					}
				}

				if ( them_count == objs.size() ) {
					pronoun = ParserError.WHAT_THEM;
				} else if ( him_count == objs.size() ) {
					pronoun = ParserError.WHAT_HIM;
				} else if ( her_count == objs.size() ) {
					pronoun = ParserError.WHAT_HER;
				} else {
					pronoun = ParserError.WHAT_IT;
				}

				getPerror().print( pronoun );
				prep.eval_property( RequiredProperties.SDESC, this.arg_array() );
			}

			// "?":
			getPerror().print( ParserError.WHAT_END );
		}

		Vector toks = getParser().tokenize_input( prep == null ?
			Parser.REQUESTING_DOBJ :
			Parser.REQUESTING_IOBJ );

		ObjectMatch[] spec = getParser().parse_noun_phrase( toks, 0 );

		int spec_sz = 0;
		if ( spec != null ) {
			for ( int j = 0; j < spec.length; j++ ) {
				spec_sz += spec[ j ].get_size();
			}
		}

		if ( spec_sz == toks.size() ) {
			// parsed the phrase successfully, so.
			return spec;
		} else {
			// if we couldn't parse the player's response as a noun phrase,
			// halt this disambiguation totally and reparse this as
			// a normal input line
			print_error( "Failed to parse DOBJ/IOBJ", 2 );
			throw new ReparseException( toks );
		}
	}


	// having gotten all the preliminary stuff out of the way, we can actually
	// do the action (well, once we do all the roomAction and iobjCheck and
	// verifies etc etc)
	public int do_execute( int[] template, TObject actor, TObject verb,
		TObject dobj, TObject prep, TObject iobj,
		boolean recursive, boolean check_valid_do,
		boolean check_valid_io )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TValue actor_arg = newTValue( TValue.OBJECT, actor.get_id() );
		TValue verb_arg = newTValue( TValue.OBJECT, verb.get_id() );
		TValue dobj_arg = ( dobj == null ) ? newTValue( TValue.NIL, 0 ) :
			newTValue( TValue.OBJECT, dobj.get_id() );
		TValue prep_arg = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
			newTValue( TValue.OBJECT, prep.get_id() );
		TValue iobj_arg = ( iobj == null ) ? newTValue( TValue.NIL, 0 ) :
			newTValue( TValue.OBJECT, iobj.get_id() );

		if ( get_debug_level() >= 2 ) {
			String s = "executing: actor=" + actor.get_id() + " verb=" + verb.get_id();
			s += " dobj=" + ( ( dobj == null ) ? "null" : Integer.toString( dobj.get_id() ) );
			s += " prep=" + ( ( prep == null ) ? "null" : Integer.toString( prep.get_id() ) );
			s += " iobj=" + ( ( iobj == null ) ? "null" : Integer.toString( iobj.get_id() ) );
			print_error( s, 2 );
		}

		int status = Constants.EC_SUCCESS;

		// we have to check this again, annoyingly, since this might come
		// from execCommand
		boolean dobj_first = false;
		if ( template != null && ( template[ 5 ] & 0x01 ) != 0 ) // VOCTPLFLG_DOBJ_FIRST
		{
			dobj_first = true;
			print_error( "Note: this is a disambigDobjFirst verb", 2 );
		}
		try {
			TObject numobj = getState().lookup_required_object( RequiredObjects.NUM_OBJ );
			TObject strobj = getState().lookup_required_object( RequiredObjects.STR_OBJ );
			// validate the dobj if we want to:
			if ( dobj != null && dobj != numobj && dobj != strobj && check_valid_do ) {
				TValue[] args =
					this.arg_array( actor_arg, dobj_arg, newTValue( TValue.NUMBER, 1 ) );
				TValue tv = verb.eval_property( RequiredProperties.VALID_DO, args );
				if ( !tv.get_logical() ) {
					Vector v = new Vector( 1 );
					v.addElement( dobj );
					ObjectMatch o = getState().get_command_match( 1 );
					complain_about_invalids( v, actor, verb, prep, true, recursive,
						o.get_phrase() );
					return Constants.EC_INVAL_DOBJ;
				}
			}

			// and the iobj:
			if ( iobj != null && iobj != numobj && iobj != strobj && check_valid_io ) {
				TValue[] args =
					this.arg_array( actor_arg, iobj_arg, newTValue( TValue.NUMBER, 1 ) );
				TValue tv = verb.eval_property( RequiredProperties.VALID_IO, args );
				if ( !tv.get_logical() ) {
					Vector v = new Vector( 1 );
					v.addElement( iobj );
					ObjectMatch o = getState().get_command_match( 2 );
					complain_about_invalids( v, actor, verb, prep, false, recursive,
						o.get_phrase() );
					return Constants.EC_INVAL_IOBJ;
				}
			}

			// verb.verbAction(actor, do, prep, io)
			TValue[] args =
				this.arg_array( actor_arg, dobj_arg, prep_arg, iobj_arg );
			verb.eval_property( RequiredProperties.VERB_ACTION, args );

			// actor.actorAction(verb, do, prep, io)
			args = this.arg_array( verb_arg, dobj_arg, prep_arg, iobj_arg );
			actor.eval_property( RequiredProperties.ACTOR_ACTION, args );

			// actor.location.roomAction(actor, verb, do, prep, io)
			{
				// look up the location
				TValue locv = actor.eval_property( RequiredProperties.LOCATION,
					this.arg_array() );
				locv.must_be( TValue.OBJECT, TValue.NIL );
				if ( locv.get_type() == TValue.OBJECT ) {
					TObject loc = getState().lookup_object( locv.get_object() );
					args = this.arg_array( actor_arg, verb_arg, dobj_arg,
						prep_arg, iobj_arg );
					// do the roomAction
					loc.eval_property( RequiredProperties.ROOM_ACTION, args );
				}
			}

			// generic checks on the iobj:
			if ( iobj != null ) {
				// io.iobjCheck(actor, verb, dobj, prep)
				args = this.arg_array( actor_arg, verb_arg, dobj_arg, prep_arg );
				iobj.eval_property( RequiredProperties.IOBJ_CHECK, args );

				if ( iobj.lookup_property( template[ 1 ] ) == null ) {
					// io.iobjGen(actor, verb, dobj, prep)
					args = this.arg_array( actor_arg, verb_arg, dobj_arg, prep_arg );
					iobj.eval_property( RequiredProperties.IOBJ_GEN, args );
				}
			}

			// and now the same for dobj:
			if ( dobj != null ) {
				// do.dobjCheck(actor, verb, iobj, prep)
				args = this.arg_array( actor_arg, verb_arg, iobj_arg, prep_arg );
				dobj.eval_property( RequiredProperties.DOBJ_CHECK, args );

				if ( dobj.lookup_property( template[ 3 ] ) == null ) {
					// do.dobjGen(actor, verb, iobj, prep)
					args = this.arg_array( actor_arg, verb_arg, iobj_arg, prep_arg );
					dobj.eval_property( RequiredProperties.DOBJ_GEN, args );
				}
			}

			// the verIoFoo test
			if ( iobj != null ) {
				print_error( "Trying iobj verify", 2 );

				int hide = hide_output();

				// io.verIo<Verb>(actor)
				if ( dobj_first ) {
					args = this.arg_array( actor_arg, dobj_arg );
				} else {
					args = this.arg_array( actor_arg );
				}

				eval_verb_verify( verb, null, prep, iobj, template[ 1 ], args );

				// if there was output, then it didn't pass the verify
				// so run the verify *again* to produce the output for the
				// player
				if ( unhide_output( hide ) ) {
					if ( eval_verb_verify( verb, null, prep, iobj, template[ 1 ], args ) ) {
						return Constants.EC_VERIO_FAILED;
					} else {
						return Constants.EC_NO_VERIO;
					}
				}
			}

			// the verDoFoo test
			if ( dobj != null ) {
				print_error( "Trying dobj verify", 2 );

				int hide = hide_output();

				// do.verDo<Verb>(actor) or do.verDo<Verb>(actor, iobj)
				if ( iobj != null && !dobj_first ) {
					args = this.arg_array( actor_arg, iobj_arg );
				} else {
					args = this.arg_array( actor_arg );
				}

				eval_verb_verify( verb, dobj, null, null, template[ 3 ], args );

				// if there was output, then it didn't pass the verify
				// so run the verify again to produce the output
				if ( unhide_output( hide ) ) {
					if ( eval_verb_verify( verb, dobj, null, null, template[ 3 ], args ) ) {
						return Constants.EC_VERDO_FAILED;
					} else {
						return Constants.EC_NO_VERDO;
					}
				}
			}

			// and, at last, do the frickin' action
			print_error( "Passed verify, doing stuff", 2 );

			if ( iobj != null ) {
				print_error( "target=iobj #" + iobj.get_id(), 2 );
				args = this.arg_array( actor_arg, dobj_arg );
				iobj.eval_property( template[ 2 ], args );
			} else if ( dobj != null ) {
				print_error( "target=dobj #" + dobj.get_id(), 2 );
				args = this.arg_array( actor_arg );
				dobj.eval_property( template[ 4 ], args );
			} else {
				print_error( "target=<nil>, doing actor(action)", 2 );
				// finally, do the actual verb.action(actor) call
				args = this.arg_array( actor_arg );
				verb.eval_property( RequiredProperties.ACTION, args );
			}
		} catch ( ExitobjException ee ) {
			status = Constants.EC_EXITOBJ;
			print_error( "exitobj called", 2 );
		} catch ( ExitException ee ) {
			status = Constants.EC_EXIT;
			print_error( "exit called", 2 );
		} catch ( AbortException pe ) {
			status = Constants.EC_ABORT;
			print_error( "abort called", 2 );
		}

		// regardless of the exit status we want to run postAction, but only
		// if this is not recursive (ie, don't do it when this is called
		// from execCommand())
		TObject postaction = getState().lookup_required_object( RequiredObjects.POST_ACTION );
		if ( postaction != null && !recursive ) {
			print_error( "Running postAction", 2 );

			TValue[] args = new TValue[ 6 ];
			args[ 0 ] = ( actor == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, actor.get_id() );
			args[ 1 ] = ( verb == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, verb.get_id() );
			args[ 2 ] = ( dobj == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, dobj.get_id() );
			args[ 3 ] = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, prep.get_id() );
			args[ 4 ] = ( iobj == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, iobj.get_id() );
			args[ 5 ] = newTValue( TValue.NUMBER, status );

			getRunner().run( postaction.get_data(), args );
		}

		return status;
	}

	public int do_start_of_turn( TObject actor, TObject verb, TObject[] dobjs,
		TObject prep, TObject iobj )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		int status = Constants.EC_SUCCESS;

		TObject precommand = getState().lookup_required_object( RequiredObjects.PRE_COMMAND );
		if ( precommand != null ) {
			print_error( "Running precommand", 2 );

			TValue[] args = new TValue[ 5 ];
			args[ 0 ] = ( actor == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, actor.get_id() );
			args[ 1 ] = ( verb == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, verb.get_id() );
			if ( dobjs != null ) {
				Vector dobj_list = new Vector();
				for ( int i = 0; i < dobjs.length; i++ ) {
					dobj_list.addElement( newTValue( TValue.OBJECT, dobjs[ i ].get_id() ) );
				}
				args[ 2 ] = newTValue( TValue.LIST, dobj_list );
			} else {
				args[ 2 ] = newTValue( TValue.NIL, 0 );
			}
			args[ 3 ] = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, prep.get_id() );
			args[ 4 ] = ( iobj == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, iobj.get_id() );

			try {
				getRunner().run( precommand.get_data(), args );
			} catch ( ExitException ee ) {
				status = Constants.EC_EXIT;
			} catch ( AbortException ae ) {
				status = Constants.EC_ABORT;
			}
		}

		return status;
	}

	public void do_end_of_turn( boolean run_daemons_and_fuses, TObject actor,
		TObject verb, TObject[] dobjs, TObject prep,
		TObject iobj, int status )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( run_daemons_and_fuses ) {
			getState().run_daemons_and_fuses( true, true );
		}

		TObject ec = getState().lookup_required_object( RequiredObjects.END_COMMAND );
		if ( ec != null ) {
			TValue[] args = new TValue[ 6 ];
			args[ 0 ] = ( actor == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, actor.get_id() );
			args[ 1 ] = ( verb == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, verb.get_id() );
			Vector dobj_list = new Vector();
			if ( dobjs != null ) {
				for ( int i = 0; i < dobjs.length; i++ ) {
					dobj_list.addElement( newTValue( TValue.OBJECT, dobjs[ i ].get_id() ) );
				}
			}
			args[ 2 ] = newTValue( TValue.LIST, dobj_list );
			args[ 3 ] = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, prep.get_id() );
			args[ 4 ] = ( iobj == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, iobj.get_id() );
			args[ 5 ] = newTValue( TValue.NUMBER, status );

			getRunner().run( ec.get_data(), args );
		}
	}

	public TObject[] do_disambig( int type, ObjectMatch[] match, int prop,
		TObject actor, TObject verb, TObject prep,
		TObject other, boolean silent, int[] status,
		Vector match_list )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		Vector ret = new Vector();

		if ( status != null ) {
			status[ 0 ] = ParserError.SUCCESS;
		}

		TObject numobj =
			getState().lookup_required_object( RequiredObjects.NUM_OBJ );
		TObject strobj =
			getState().lookup_required_object( RequiredObjects.STR_OBJ );

		for ( int i = 0; i < match.length; i++ ) {
			if ( match[ i ].test_flags( Constants.PRSFLG_THEM ) ) {
				TObject[] them = getState().lookup_them();
				if ( them == null ) {
					throw new HaltTurnException( ParserError.DONT_KNOW_REF,
						match[ i ].get_phrase() );
				}
				for ( int j = 0; j < them.length; j++ ) {
					ret.addElement( them[ j ] );
					if ( match_list != null ) {
						match_list.addElement( match[ i ] );
					}
				}

				continue;
			} else if ( match[ i ].test_flags( Constants.PRSFLG_IT | Constants.PRSFLG_HIM |
				Constants.PRSFLG_HER ) ) {
				int p = -1;
				if ( match[ i ].test_flags( Constants.PRSFLG_IT ) ) {
					p = Constants.PO_IT;
				} else if ( match[ i ].test_flags( Constants.PRSFLG_HIM ) ) {
					p = Constants.PO_HIM;
				} else {
					p = Constants.PO_HER;
				}

				TObject pronoun = getState().lookup_it( p );
				if ( pronoun == null ) {
					throw new HaltTurnException( ParserError.DONT_KNOW_REF,
						match[ i ].get_phrase() );
				}
				ret.addElement( pronoun );
				if ( match_list != null ) {
					match_list.addElement( match[ i ] );
				}

				continue;
			}

			// if it's an all-match with no items (ie, it really means "all", not
			// "all boxes" or something), call doDefault or ioDefault as appropriate
			// to get the items
			if ( match[ i ].test_flags( Constants.PRSFLG_ALL ) &&
				match[ i ].num_matches() == 0 ) {
				print_error( "This appears to be an all-match", 2 );

				// before proceeding on further, if there's an 'except' as part of this
				// match, disambiguate it:
				Vector skip = new Vector();
				if ( match[ i ].get_except() != null ) {
					print_error( "'except' found, running disambig", 2 );
					TObject[] exc = do_disambig( type, match[ i ].get_except(), prop,
						actor, verb, prep, other, silent,
						status, null );

					// what to do if the except-match needs disambiguation but silent
					// is set to true? well, apparently standard TADS just ignores this
					// and pretends it returned ParserError.SUCCESS. So that's what we'll
					// do.
					if ( status != null && status[ 0 ] == ParserError.AMBIGUOUS ) {
						status[ 0 ] = ParserError.SUCCESS;
					}

					for ( int j = 0; j < exc.length; j++ ) {
						skip.addElement( exc[ j ] );
					}

					print_error( "Found " + skip.size() + " except items", 2 );
				}

				TValue[] def_args;
				int def_prop;
				if ( type == Constants.PRO_RESOLVE_DOBJ ) {
					print_error( "Checking doDefault", 2 );
					TValue p = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
						newTValue( TValue.OBJECT, prep.get_id() );
					TValue o = ( other == null ) ? newTValue( TValue.NIL, 0 ) :
						newTValue( TValue.OBJECT, other.get_id() );
					def_args =
						this.arg_array( newTValue( TValue.OBJECT, actor.get_id() ), p, o );
					def_prop = RequiredProperties.DO_DEFAULT;
				} else // this is always iobj, since actor-resolution doesn't allow "all"
				{
					// (but in fact iobj-resolution doesn't allow it either)
					print_error( "error: 'all' used as iobj somehow?", 1 );

					print_error( "Checking ioDefault", 2 );
					TValue p = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
						newTValue( TValue.OBJECT, prep.get_id() );
					def_args =
						this.arg_array( newTValue( TValue.OBJECT, actor.get_id() ), p );
					def_prop = RequiredProperties.IO_DEFAULT;
				}

				Vector objs = new Vector();

				TValue def = verb.eval_property( def_prop, def_args );
				def.must_be( TValue.NIL, TValue.LIST );
				if ( def.get_type() == TValue.LIST ) {
					Vector v = def.get_list();
					for ( int j = 0; j < v.size(); j++ ) {
						int objid = ( (TValue)v.elementAt( j ) ).get_object();
						TObject obj = getState().lookup_object( objid );
						// add anything in this list that's not in our exclude list
						if ( !skip.contains( obj ) ) {
							objs.addElement( obj );
						}
					}
				}

				// if we didn't get any items, complain:
				if ( objs.size() == 0 ) {
					throw new HaltTurnException( ParserError.DONT_SEE_REF );
				} else {
					// it's a bug in tads that this doesn't get called for "all",
					// I think, as removing duplicates and (more importantly)
					// handling numbered objects ought to be done here too.
					// I think it doesn't hurt anything to include this check here,
					// but it is non-standard:
					clean_up_disambig_list( objs, match[ i ], actor, verb );
				}

				for ( int j = 0; j < objs.size(); j++ ) {
					ret.addElement( objs.elementAt( j ) );
					if ( match_list != null ) {
						match_list.addElement( match[ i ] );
					}
				}

				continue; // and onto the next match.
			}

			// ok, it's a regular (non-all) phrase. we check for unknown words,
			// and then use validXoList, validXo, and verify methods to shrink
			// the list. oh, and the ENDADJ and TRUNCATE flags if all else fails.
			// once we've done all that, we either call disambigXobj or ask
			// the player or give up, as appropriate.

			// first we disqualify unknown words:
			Vector words = match[ i ].get_words();
			String unknown = null;
			for ( int j = 0; j < words.size(); j++ ) {
				VocabWord w = (VocabWord)words.elementAt( j );

				if ( w.is_unknown() && !w.is_number() ) {
					unknown = w.get_word();
					break;
				}
			}

			if ( unknown != null ) {
				if ( type == Constants.PRO_RESOLVE_ACTOR ) {
					throw new HaltTurnException( ParserError.UNK_WORD, unknown );
				}

				print_error( "unknown word detected", 2 );
				// we'll call parseUnknownDobj (or Iobj) to handle this:
				TValue[] args = new TValue[ 4 ];
				Vector words_list = new Vector();
				for ( int j = 0; j < words.size(); j++ ) {
					words_list.addElement( newTValue( TValue.SSTRING, ( (VocabWord)words.elementAt( j ) ).get_word() ) );
				}
				args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
				args[ 1 ] = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
					newTValue( TValue.OBJECT, prep.get_id() );
				args[ 2 ] = ( other == null ) ? newTValue( TValue.NIL, 0 ) :
					newTValue( TValue.OBJECT, other.get_id() );
				args[ 3 ] = newTValue( TValue.LIST, words_list );

				int uprop = ( type == Constants.PRO_RESOLVE_DOBJ ) ?
					RequiredProperties.PARSE_UNKNOWN_DOBJ :
					RequiredProperties.PARSE_UNKNOWN_IOBJ;

				TValue rv = verb.eval_property( uprop, args );

				if ( rv.get_type() == TValue.TRUE ) {
					// then the method has dealt with this entirely:
					continue;
				} else if ( rv.get_type() == TValue.OBJECT ) {
					// then the method wants this as the resolution
					ret.addElement( getState().lookup_object( rv.get_object() ) );
					if ( match_list != null ) {
						match_list.addElement( match[ i ] );
					}
					continue;
				} else if ( rv.get_type() == TValue.LIST ) {
					// then the method wants these objects as the resolution
					Vector v = rv.get_list();
					for ( int j = 0; j < v.size(); j++ ) {
						int o = ( (TValue)v.elementAt( j ) ).get_object();
						ret.addElement( getState().lookup_object( o ) );
						if ( match_list != null ) {
							match_list.addElement( match[ i ] );
						}
					}
					continue;
				} else {
					rv.must_be( TValue.NIL ); // do normal processing, namely:
				}

				throw new HaltTurnException( ParserError.UNK_WORD, unknown );
			}

			// unknowns dealt with, onto resolution:
			Vector initial_list = match[ i ].get_matches();
			Vector valid_list = null;
			Vector verified_list = null;

			print_error( "Starting validation check (size=" + initial_list.size() + ")", 2 );

			TValue[] args = new TValue[ 3 ];
			args[ 0 ] = ( actor == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, actor.get_id() );
			args[ 1 ] = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, prep.get_id() );
			args[ 2 ] = ( other == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, other.get_id() );

			if ( type == Constants.PRO_RESOLVE_DOBJ ||
				type == Constants.PRO_RESOLVE_IOBJ ) {
				TValue rv;
				if ( type == Constants.PRO_RESOLVE_DOBJ ) {
					rv = verb.eval_property( RequiredProperties.VAL_DO_LIST, args );
				} else {
					rv = verb.eval_property( RequiredProperties.VAL_IO_LIST, args );
				}

				if ( rv.get_type() != TValue.LIST ) {
					print_error( "validXoList returned non-list", 2 );
					valid_list = initial_list;
				} else {
					Vector lst = rv.get_list();
					valid_list = new Vector();
					print_error( "validXoList returned list (size=" +
						lst.size() + ")", 2 );
					for ( int j = 0; j < lst.size(); j++ ) {
						TValue t = (TValue)lst.elementAt( j );

						// For Per Wilhelmsson, have this allow (and skip) nils
						if ( t.get_type() == TValue.NIL ) {
							continue;
						}

						TObject obj = getState().lookup_object( t.get_object() );
						if ( initial_list.contains( obj ) ) {
							valid_list.addElement( obj );
						}
					}
				}
			} else {
				valid_list = initial_list;
			}

			print_error( "doing validXo checks (size=" + valid_list.size() +
				")", 2 );
			for ( int j = 0; j < valid_list.size(); j++ ) {
				TObject curobj = (TObject)valid_list.elementAt( j );
				// always strip these out, and we'll add them back later:
				if ( curobj == numobj || curobj == strobj ) {
					valid_list.removeElementAt( j-- );
				} else {
					// args[0] is set to actor, which is what we want.
					args[ 1 ] = newTValue( TValue.OBJECT, curobj.get_id() );
					args[ 2 ] = newTValue( TValue.NUMBER, j + 1 );
					TValue rv;
					if ( type == Constants.PRO_RESOLVE_DOBJ ) {
						rv = verb.eval_property( RequiredProperties.VALID_DO, args );
					} else if ( type == Constants.PRO_RESOLVE_IOBJ ) {
						rv = verb.eval_property( RequiredProperties.VALID_IO, args );
					} else {
						rv = curobj.eval_property( RequiredProperties.VALID_ACTOR, this.arg_array() );
					}

					if ( !rv.get_logical() ) {
						valid_list.removeElementAt( j-- );
					}
				}
			}

			verified_list = new Vector( valid_list.size() );
			TValue[] ver_args;
			if ( other != null ) {
				print_error( "creating two-arg array", 2 );
				ver_args = new TValue[ 2 ];
				ver_args[ 0 ] = args[ 0 ]; // ie, the actor
				ver_args[ 1 ] = newTValue( TValue.OBJECT, other.get_id() );
			} else {
				print_error( "creating one-arg array", 2 );
				ver_args = new TValue[ 1 ];
				ver_args[ 0 ] = args[ 0 ]; // ie, the actor
			}

			print_error( "doing logical checks (prop=" + prop + " size=" + valid_list.size() + ")", 2 );
			for ( int j = 0; j < valid_list.size(); j++ ) {
				TObject tobj = (TObject)valid_list.elementAt( j );

				if ( type == Constants.PRO_RESOLVE_DOBJ ||
					type == Constants.PRO_RESOLVE_IOBJ ) {
					int hide = hide_output();

					if ( type == Constants.PRO_RESOLVE_DOBJ ) {
						eval_verb_verify( verb, tobj, null, null, prop, ver_args );
					} else {
						eval_verb_verify( verb, null, prep, tobj, prop, ver_args );
					}

					if ( !unhide_output( hide ) ) {
						verified_list.addElement( tobj );
					}
				} else {
					// actor resolution:
					TValue rv = tobj.eval_property( RequiredProperties.PREF_ACTOR,
						this.arg_array() );

					if ( rv.get_logical() ) {
						verified_list.addElement( tobj );
					}
				}
			}

			print_error( "done with checks (size=" + verified_list.size() + ")", 2 );

			// strobj and numobj are a special case (unfortunately)
			// basically, if they're included in the initial noun list,
			// and the final list is empty, add them to the final noun list.
			if ( verified_list.size() == 0 ) {
				if ( initial_list.contains( numobj ) ) {
					int n = ( (VocabWord)words.elementAt( 0 ) ).get_number();
					TValue v = newTValue( TValue.NUMBER, n );
					verified_list.addElement( numobj );
					numobj.set_property( RequiredProperties.VALUE, v );
					print_error( "adding numobj back in to list", 2 );
				}

				if ( initial_list.contains( strobj ) ) {
					TValue v = newTValue( TValue.SSTRING,
						( (VocabWord)words.elementAt( 0 ) ).get_word() );
					verified_list.addElement( strobj );
					strobj.set_property( RequiredProperties.VALUE, v );
					print_error( "adding strobj back in to list", 2 );
				}
			}

			// finally, having done all that, choose between the verified list and
			// the valid list (preferring the former)
			Vector objects = verified_list;
			if ( objects.size() == 0 ) {
				objects = valid_list;
			}

			Vector equivs = new Vector(); // objects which have isEquivalents

			// now do the user callout to disambigDobj or disambigIobj:
			String[] response = new String[ 1 ];
			int code = call_disambig_xobj( type, verb, actor, prep, other, prop,
				match[ i ], objects, silent, response );

			if ( code == Constants.DISAMBIG_ERROR ) {
				throw new HaltTurnException( ParserError.BAD_DIS_STAT );
			} else if ( code == Constants.DISAMBIG_DONE ) {
				// just use these objects, and we're done
				for ( int j = 0; j < objects.size(); j++ ) {
					ret.addElement( objects.elementAt( j ) );
					if ( match_list != null ) {
						match_list.addElement( match[ i ] );
					}
				}
				continue;
			} else if ( code == Constants.DISAMBIG_CONTINUE ) {
				// ok, so go through as normal and remove less-preferrable matches (ie,
				// all-adjectives or use truncated words). If some are less preferred
				// than others, use only the more-preferred ones
				Vector more_pref = new Vector();
				for ( int j = 0; j < objects.size(); j++ ) {
					int f = match[ i ].get_match_flag( (TObject)objects.elementAt( j ) );
					if ( ( f & ( Constants.PRSFLG_ENDADJ | Constants.PRSFLG_TRUNC ) ) == 0 ) {
						more_pref.addElement( objects.elementAt( j ) );
					}
				}

				if ( more_pref.size() > 0 ) {
					objects = more_pref;
					print_error( "dropping less-preferred objects from list", 2 );
				}

				// the last thing to do is to strip out equivalent objects.
				// two objects are equivalent if their first superclass is the same
				// and they both have isEquivalent = true.
				// the actual definition in tads code is "a and b are equivalent if
				// they have the same superclass and a.isEquivalent is true." note that
				// this is not commutative, and so you get weird things if
				// a.isEquivalent is true but b.isEquivalent isn't. this definition
				// is slightly easier to implement than the real one, so I'll do that.
				//
				// also, note that we only eliminate duplicates if they're not asking
				// for a plural -- if they say "GET ALL COINS" they don't want to
				// just get one coin, even if they're all equivalent.
				// we just call this the same as being non-ambiguous, since it
				// doesn't matter for "any" if duplicates are eliminated or not
				if ( match[ i ].is_ambig() ) {
					for ( int j = 0; j < objects.size(); j++ ) {
						TObject obj1 = (TObject)objects.elementAt( j );
						TValue eq = obj1.eval_property( RequiredProperties.IS_EQUIV,
							this.arg_array() );
						if ( !eq.get_logical() ) {
							continue;
						}

						int[] scs1 = obj1.get_superclasses();
						if ( scs1.length < 1 ) {
							continue;
						}

						for ( int k = j + 1; k < objects.size(); k++ ) {
							TObject obj2 = (TObject)objects.elementAt( k );
							int[] scs2 = obj2.get_superclasses();
							if ( scs2.length < 1 ) {
								continue;
							}

							// first superclasses match, so they're equivalent
							if ( scs1[ 0 ] == scs2[ 0 ] ) {
								print_error( "dropping equivalent object", 2 );

								// add obj1 to the list of objects-with-equivalents:
								if ( !equivs.contains( obj1 ) ) {
									equivs.addElement( obj1 );
								}

								// and remove obj2 from the objects list
								objects.removeElementAt( k );
								k--;
							}
						}
					}
				}
			}

			if ( objects.size() == 0 ) {
				// nothing at all matched: this could be either because
				// no objects are visible, or because they're visible but not
				// reachable.
				complain_about_invalids( initial_list, actor, verb, prep,
					( type == Constants.PRO_RESOLVE_DOBJ ), false,
					match[ i ].get_phrase() );
			} else {
				clean_up_disambig_list( objects, match[ i ], actor, verb );
			}

			// ok, we've done all the disambiguation possible without asking the
			// player. see what we've got:
			int count = match[ i ].is_ambig() ? match[ i ].get_count() : objects.size();
			boolean ambiguous = ( objects.size() > count );

			print_error( "count required=" + count + " ambig=" + ambiguous +
				" object count=" + objects.size(), 2 );
			if ( ambiguous ) {
				// if they requested silence, we can't ask the player to disambiguate.
				// since we still need disambiguation, we are stuck.
				if ( silent ) {
					status[ 0 ] = ParserError.AMBIGUOUS;
					TObject[] r = new TObject[ objects.size() ];
					objects.copyInto( r );
					return r;
				}

				objects = which_do_you_mean( objects, count, match[ i ].get_phrase(),
					( code != Constants.DISAMBIG_PROMPTED ),
					response[ 0 ], equivs );
			} else if ( match[ i ].test_flags( Constants.PRSFLG_COUNT |
				Constants.PRSFLG_ANY ) ) {
				print_error( "count/any set, adjusting size down from " +
					objects.size() + " to " + match[ i ].get_count(), 2 );
				if ( match[ i ].get_count() > objects.size() ) {
					throw new HaltTurnException( ParserError.ONLY_SEE,
						Integer.toString( objects.size() ) );
				} else {
					while ( match[ i ].get_count() < objects.size() ) {
						objects.removeElementAt( objects.size() - 1 );
					}
				}
			}

			for ( int j = 0; j < objects.size(); j++ ) {
				ret.addElement( objects.elementAt( j ) );
				if ( match_list != null ) {
					match_list.addElement( match[ i ] );
				}
			}
		}

		TObject[] ra = new TObject[ ret.size() ];
		ret.copyInto( ra );
		return ra;
	}

	private void clean_up_disambig_list( Vector objects, ObjectMatch match,
		TObject actor, TObject verb )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// make sure there're no duplicates (not an isEquivalent duplicate, but
		// the same exact object appearing twice). this check avoids the infamous
		// "Which foo do you mean, the foo or the foo?" bug
		for ( int i = 0; i < objects.size(); i++ ) {
			print_error( "checking object=" + objects.elementAt( i ), 2 );
			for ( int j = i + 1; j < objects.size(); j++ ) {
				if ( objects.elementAt( i ) == objects.elementAt( j ) ) {
					print_error( "dropping duplicate object", 2 );
					objects.removeElementAt( j );
					j--;
				}
			}
		}

		// and do the numbered object check:
		for ( int i = 0; i < objects.size(); i++ ) {
			TObject obj = (TObject)objects.elementAt( i );
			if ( !obj.is_numbered() ) {
				continue;
			}

			print_error( "numbered object found=" + obj.get_id(), 2 );

			TValue[] values;

			if ( match.test_flags( Constants.PRSFLG_ANY | Constants.PRSFLG_COUNT |
				Constants.PRSFLG_ALL ) ) {
				int count = match.get_count() - 1;
				print_error( "any/all/count found, count=" + ( count + 1 ), 2 );
				// we want to create count-1 copies of this object (calling anyvalue(1)
				// to get their number), leaving the original object in place
				// why? don't ask me.
				// since we're going to yank the original object out of the list,
				// insert it a second time so it'll be in at the end
				objects.insertElementAt( obj, i );

				// and then send in the clones:
				values = new TValue[ count ];
				TValue[] args = this.arg_array( newTValue( TValue.NUMBER, 1 ) );
				for ( int j = 0; j < count; j++ ) {
					TValue av = obj.eval_property( RequiredProperties.ANYVALUE, args );
					values[ j ] = newTValue( TValue.NUMBER, av.get_number() );
				}
			} else if ( match.test_flags( Constants.PRSFLG_PLURAL ) ) {
				print_error( "plural found", 2 );
				// create one object with count = nil
				values = new TValue[ 1 ];
				values[ 0 ] = newTValue( TValue.NIL, 0 );
			} else {
				// create one object with a number that must have been specified
				Vector words = match.get_words();
				int num = Integer.MIN_VALUE;
				for ( int j = 0; j < words.size(); j++ ) {
					VocabWord vw = (VocabWord)words.elementAt( j );
					if ( vw.is_number() ) {
						num = vw.get_number();
					}
				}

				print_error( "single found, num=" + num, 2 );

				// no number specified? oops
				if ( num == Integer.MIN_VALUE ) {
					throw new HaltTurnException( ParserError.MORE_SPECIFIC,
						match.get_phrase() );
				}

				values = new TValue[ 1 ];
				values[ 0 ] = newTValue( TValue.NUMBER, num );
			}

			// remove the original object:
			objects.removeElementAt( i );

			// and add in the correct number of copies:
			for ( int j = 0; j < values.length; j++ ) {
				TValue[] args = new TValue[ 3 ];
				args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
				args[ 1 ] = newTValue( TValue.OBJECT, verb.get_id() );
				args[ 2 ] = values[ j ];

				TValue rv = obj.eval_property( RequiredProperties.NEW_NUM_OBJ, args );
				if ( rv.get_type() == TValue.NIL ) {
					throw new HaltTurnException( ParserError.NO_NEW_NUM );
				} else {
					TObject copy = getState().lookup_object( rv.get_object() );
					objects.insertElementAt( copy, i++ );
				}
			}
		}
	}

	// given a list, ask the player "which do you mean, the foo or the bar?"
	// and handle stuff like 'both' and so on, of course
	private Vector which_do_you_mean( Vector objects, int count, String phrase,
		boolean initial_prompt, String response,
		Vector equivs )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print_error( "Trying to disambiguate a list of size=" +
			objects.size() + " for count=" + count, 2 );

		boolean repeat = false;

		DISAMBIG: while ( true ) {
			// if they already gave us a response, we should use that string:
			if ( response == null && ( repeat || initial_prompt ) ) {
				TObject parse_disambig =
					getState().lookup_required_object( RequiredObjects.PARSE_DISAMBIG );
				if ( parse_disambig != null ) {
					TValue sv = newTValue( TValue.SSTRING, phrase );
					// Eric Moon and John Cater pointed out this wasn't passing
					// a tads list. Whoops.
					Vector lst = new Vector( objects.size() );
					for ( int i = 0; i < objects.size(); i++ ) {
						lst.addElement( newTValue( TValue.OBJECT,
							( (TObject)objects.elementAt( i ) ).get_id() ) );
					}
					TValue lv = newTValue( TValue.LIST, lst );
					getRunner().run( parse_disambig.get_data(), this.arg_array( sv, lv ) );
				} else {
					// grr using the error facility for normal messages
					if ( repeat ) {
						getPerror().print( ParserError.TRY_AGAIN );
					} else {
						repeat = true;
					}

					getPerror().print( ParserError.WHICH_PFX, phrase );
					for ( int j = 0; j < objects.size(); j++ ) {
						TObject obj = (TObject)objects.elementAt( j );
						int prp = ( equivs.contains( obj ) ) ? RequiredProperties.ADESC :
							RequiredProperties.THEDESC;
						obj.eval_property( prp, this.arg_array() );

						if ( j < objects.size() - 1 ) {
							getPerror().print( ParserError.WHICH_COMMA );
						}

						if ( j == objects.size() - 2 ) {
							getPerror().print( ParserError.WHICH_OR );
						}
					}
					getPerror().print( ParserError.WHICH_QUESTION );
				}
			}

			Vector toks = null;
			if ( response != null ) {
				// then just parse their response:
				toks = getParser().tokenize_str( response );
				response = null; // so next time we will ask the question
			} else {
				toks = getParser().tokenize_input( Parser.DISAMBIGUATING );
			}

			// read in their response and see if it parses as a noun phrase:
			ObjectMatch[] match = getParser().parse_noun_phrase( toks, 0, objects );
			if ( match == null ) // can't parse as a noun phrase?
			{
				throw new ReparseException( toks );
			} else {
				// make sure the whole string was parsed into this noun phrase:
				int sz = 0;
				if ( match != null ) {
					for ( int i = 0; i < match.length; i++ ) {
						sz += match[ i ].get_size();
					}
				}

				// if not, retry as a sentence
				if ( sz < toks.size() ) {
					throw new ReparseException( toks );
				}
			}

			print_error( "NP disambig matches: " + match.length, 2 );

			// so the way it works is, if the number of matches is greater than 1,
			// then if any are ambiguous, you just try again with the original
			// object list (if none are equivalent you return that list and you're
			// done). if the number of matches is equal to 1, then if it is
			// ambiguous you try again with its object list, otherwise you return
			// its list.
			Vector ret_list = new Vector();
			for ( int i = 0; i < match.length; i++ ) {
				if ( match[ i ].is_ambig() &&
					match[ i ].num_matches() > match[ i ].get_count() ) {
					if ( match.length == 1 ) {
						objects = match[ i ].get_matches();
					}
					continue DISAMBIG;
				} else {
					Vector v = match[ i ].get_matches();
					for ( int j = 0; j < v.size() && j < match[ i ].get_count(); j++ ) {
						ret_list.addElement( v.elementAt( j ) );
					}
				}
			}

			// got here? great, we're done
			return ret_list;
		}
	}

	// returns true if the verify method is defined at all, false if it
	// has to use the built-in default
	private boolean eval_verb_verify( TObject verb, TObject dobj, TObject prep,
		TObject iobj, int prop, TValue[] args )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TObject obj = ( dobj == null ) ? iobj : dobj;
		if ( obj.lookup_property( prop ) != null ) {
			obj.eval_property( prop, args );
			return true;
		}

		TObject parse_err = getState().lookup_required_object( RequiredObjects.PARSE_ERROR2 );
		if ( parse_err != null ) {
			TValue[] err_args = new TValue[ 4 ];
			err_args[ 0 ] = newTValue( TValue.OBJECT, verb.get_id() );
			err_args[ 1 ] = ( dobj != null ) ? newTValue( TValue.OBJECT, dobj.get_id() ) :
				newTValue( TValue.NIL, 0 );
			err_args[ 2 ] = ( prep != null ) ? newTValue( TValue.OBJECT, prep.get_id() ) :
				newTValue( TValue.NIL, 0 );
			err_args[ 3 ] = ( iobj != null ) ? newTValue( TValue.OBJECT, iobj.get_id() ) :
				newTValue( TValue.NIL, 0 );

			getRunner().run( parse_err.get_data(), err_args );
		} else {
			getPerror().print( ParserError.DONTKNOW_PFX );
			verb.eval_property( RequiredProperties.SDESC, this.arg_array() );

			if ( dobj != null ) {
				getPerror().print( ParserError.DONTKNOW_SPC );
				dobj.eval_property( RequiredProperties.THEDESC, this.arg_array() );
			} else {
				getPerror().print( ParserError.DONTKNOW_ANY );
				prep.eval_property( RequiredProperties.SDESC, this.arg_array() );
				getPerror().print( ParserError.DONTKNOW_SPC2 );
				iobj.eval_property( RequiredProperties.THEDESC, this.arg_array() );
			}
			getPerror().print( ParserError.DONTKNOW_END );
		}

		return false;
	}

	// print a prefix for this object, if we want to (it's in a list of
	// objects being acted on)
	private void multiobj_prefix( TObject obj, boolean want_to, int index,
		int max, int flags )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( obj.lookup_property( RequiredProperties.PREFIX_DESC ) != null ) {
			TValue[] args = new TValue[ 4 ];
			args[ 0 ] = newTValue( want_to ? TValue.TRUE : TValue.NIL, 0 );
			args[ 1 ] = newTValue( TValue.NUMBER, index );
			args[ 2 ] = newTValue( TValue.NUMBER, max );
			args[ 3 ] = newTValue( TValue.NUMBER, flags );
			obj.eval_property( RequiredProperties.PREFIX_DESC, args );
		} else if ( obj.lookup_property( RequiredProperties.MULTI_SDESC ) != null ) {
			if ( want_to ) {
				obj.eval_property( RequiredProperties.MULTI_SDESC, this.arg_array() );
				getPerror().print( ParserError.MULTI );
			}
		} else {
			if ( want_to ) {
				obj.eval_property( RequiredProperties.SDESC, this.arg_array() );
				getPerror().print( ParserError.MULTI );
			}
		}
	}

	// objs didn't pass the validXo check -- complain either that you
	// can't see them or can't reach them, as appropriate
	private void complain_about_invalids( Vector objs, TObject actor,
		TObject verb, TObject prep,
		boolean is_dobj, boolean recursive,
		String phrase )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		TValue actor_arg = null;
		if ( actor != null ) {
			actor_arg = newTValue( TValue.OBJECT, actor.get_id() );
		} else {
			actor_arg = newTValue( TValue.NIL, 0 );
		}

		TValue[] actor_arglist = this.arg_array( actor_arg );

		Vector vis_list = new Vector();
		for ( int i = 0; i < objs.size(); i++ ) {
			TObject obj = (TObject)objs.elementAt( i );
			TValue rv = obj.eval_property( RequiredProperties.IS_VIS, actor_arglist );
			if ( rv.get_logical() ) {
				vis_list.addElement( obj );
			}
		}

		if ( vis_list.size() == 0 ) {
			if ( recursive ) {
				throw new HaltTurnException( ParserError.DONT_SEE_THAT );
			} else {
				throw new HaltTurnException( ParserError.DONT_SEE_ANY, phrase );
			}
		}

		// see if this defines cantReach on the verb, or if we have to use
		// the per-object cantReach:
		if ( verb.lookup_property( RequiredProperties.CANT_REACH ) != null ) {
			Vector lst = new Vector( vis_list.size() );
			for ( int i = 0; i < vis_list.size(); i++ ) {
				lst.addElement( newTValue( TValue.OBJECT,
					( (TObject)vis_list.elementAt( i ) ).get_id() ) );
			}
			TValue lv = newTValue( TValue.LIST, lst );
			TValue prep_arg = ( prep == null ) ? newTValue( TValue.NIL, 0 ) :
				newTValue( TValue.OBJECT, prep.get_id() );

			TValue nil = newTValue( TValue.NIL, 0 );

			TValue[] args;
			if ( is_dobj ) {
				args = this.arg_array( actor_arg, lv, prep_arg, nil );
			} else {
				args = this.arg_array( actor_arg, nil, prep_arg, lv );
			}

			verb.eval_property( RequiredProperties.CANT_REACH, args );
		} else {
			for ( int i = 0; i < objs.size(); i++ ) {
				TObject obj = (TObject)objs.elementAt( i );
				if ( objs.size() > 1 ) {
					multiobj_prefix( obj, true, i, objs.size(), 0 );
				}
				obj.eval_property( RequiredProperties.CANT_REACH, actor_arglist );
			}
		}

		throw new HaltTurnException( ParserError.REACH_ERROR );
	}

	private int call_disambig_xobj( int type, TObject verb, TObject actor,
		TObject prep, TObject other, int prop,
		ObjectMatch match, Vector objects,
		boolean silent, String[] response )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( type == Constants.PRO_RESOLVE_ACTOR ) {
			return Constants.DISAMBIG_CONTINUE;
		}

		int dprop = ( type == Constants.PRO_RESOLVE_DOBJ ) ?
			RequiredProperties.DISAMBIG_DO : RequiredProperties.DISAMBIG_IO;

		// if the verb doesn't define this, skip out
		if ( verb.lookup_property( dprop ) == null ) {
			return Constants.DISAMBIG_CONTINUE;
		}

		Vector words = match.get_words();
		Vector word_list = new Vector( words.size() );
		for ( int i = 0; i < words.size(); i++ ) {
			word_list.addElement( newTValue( TValue.SSTRING, ( (VocabWord)words.elementAt( i ) ).get_word() ) );
		}

		Vector obj_list = new Vector( objects.size() );
		for ( int i = 0; i < objects.size(); i++ ) {
			obj_list.addElement( newTValue( TValue.OBJECT, ( (TObject)objects.elementAt( i ) ).get_id() ) );
		}

		Vector flag_list = new Vector( objects.size() );
		for ( int i = 0; i < objects.size(); i++ ) {
			flag_list.addElement( newTValue( TValue.NUMBER, match.get_match_flag( (TObject)objects.elementAt( i ) ) ) );
		}

		int count = match.is_ambig() ? match.get_count() : obj_list.size();
		boolean ambiguous = ( obj_list.size() > count );

		TValue[] args = new TValue[ 10 ];
		args[ 0 ] = newTValue( TValue.OBJECT, actor.get_id() );
		args[ 1 ] = prep == null ? newTValue( TValue.NIL, 0 ) :
			newTValue( TValue.OBJECT, prep.get_id() );
		args[ 2 ] = other == null ? newTValue( TValue.NIL, 0 ) :
			newTValue( TValue.OBJECT, other.get_id() );
		args[ 3 ] = newTValue( TValue.PROPERTY, prop );
		args[ 4 ] = newTValue( TValue.LIST, word_list );
		args[ 5 ] = newTValue( TValue.LIST, obj_list );
		args[ 6 ] = newTValue( TValue.LIST, flag_list );
		args[ 7 ] = newTValue( TValue.NUMBER, count );
		args[ 8 ] = newTValue( ( ambiguous ? TValue.TRUE : TValue.NIL ), 0 );
		args[ 9 ] = newTValue( ( silent ? TValue.TRUE : TValue.NIL ), 0 );

		TValue ret = verb.eval_property( dprop, args );

		if ( ret.get_type() == TValue.NIL ) {
			return Constants.DISAMBIG_CONTINUE;
		} else if ( ret.get_type() == TValue.NUMBER ) {
			int code = ret.get_number();
			if ( code == Constants.DISAMBIG_CONTINUE ||
				code == Constants.DISAMBIG_DONE ||
				code == Constants.DISAMBIG_ERROR ||
				code == Constants.DISAMBIG_PROMPTED ) {
				return code;
			} else {
				print_error( "incorrect return code from disambigXobj: " +
					code, 1 );
				return Constants.DISAMBIG_CONTINUE;
			}
		}

		Vector lst = ret.get_list();
		if ( lst.size() == 0 ) {
			print_error( "incorrect list size from disambigXobj: " +
				lst.size(), 1 );
			return Constants.DISAMBIG_CONTINUE;
		}

		int code = Constants.DISAMBIG_CONTINUE;

		TValue first = (TValue)lst.elementAt( 0 );
		if ( first.get_type() == TValue.NUMBER ) {
			code = first.get_number();
			lst.removeElementAt( 0 );

			if ( code == Constants.DISAMBIG_PARSE_RESP ) {
				if ( lst.size() != 1 ) {
					print_error( "incorrect list size from disambigXobj " +
						"for DISAMBIG_PARSE_RESP", 1 );
					return Constants.DISAMBIG_CONTINUE;
				} else {
					String rep = ( (TValue)lst.elementAt( 0 ) ).get_string();
					response[ 0 ] = rep;
					return Constants.DISAMBIG_PARSE_RESP;
				}
			} else if ( !( code == Constants.DISAMBIG_CONTINUE ||
				code == Constants.DISAMBIG_DONE ||
				code == Constants.DISAMBIG_ERROR ||
				code == Constants.DISAMBIG_PROMPTED ) ) {
				print_error( "incorrect return code from disambigXobj: " +
					code, 1 );
				code = Constants.DISAMBIG_CONTINUE;
			}

			if ( lst.size() == 1 ) {
				return code;
			}
		} else {
			first.must_be( TValue.OBJECT );
		}

		// ok, we replace the object list with a new one of our own devising
		objects.removeAllElements();
		match.set_flags( 0 );
		for ( int i = 0; i < lst.size(); i++ ) {
			int objid = ( (TValue)lst.elementAt( i ) ).get_object();

			int flags = 0;
			if ( i + 1 < lst.size() ) {
				TValue tv2 = (TValue)lst.elementAt( i + 1 );
				if ( tv2.get_type() == TValue.NUMBER ) {
					flags = tv2.get_number();
					i++;
				}
			}

			match.set_flags( flags & ( Constants.PRSFLG_ALL | Constants.PRSFLG_ANY |
				Constants.PRSFLG_PLURAL ) );
			flags &= ( Constants.PRSFLG_ENDADJ | Constants.PRSFLG_TRUNC );

			TObject obj = getState().lookup_object( objid );
			objects.addElement( obj );

			// so if the flags get looked up later, they'll be right:
			match.add_object( obj, flags );
		}

		return code;
	}
}
