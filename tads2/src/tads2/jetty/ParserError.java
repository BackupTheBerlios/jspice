package tads2.jetty;


public class ParserError extends JettyRef {

	public ParserError( Jetty jetty ) {
		super( jetty );
	}

	public final static int SUCCESS = 0;
	public final static int BAD_PUNCT = 1;
	public final static int UNK_WORD = 2;
	public final static int TOO_MANY_OBJS = 3;
	public final static int ALL_OF = 4;
	public final static int BOTH_OF = 5;
	public final static int OF_NOUN = 6;
	public final static int ART_NOUN = 7;
	public final static int DONT_SEE_ANY = 9;
	public final static int TOO_MANY_OBJS2 = 10;
	public final static int TOO_MANY_OBJS3 = 11;
	public final static int ONE_ACTOR = 12;
	public final static int DONT_KNOW_REF = 13;
	public final static int DONT_KNOW_REF2 = 14;
	public final static int DONT_SEE_REF = 15;
	public final static int DONT_SEE_ANY2 = 16;
	public final static int NO_VERB = 17;
	public final static int DONT_UNDERSTAND = 18;
	public final static int EXTRA_WORDS = 19;
	public final static int NO_TEMPLATE = 23;
	public final static int DONT_RECOGNIZE = 24;
	public final static int NO_MULTI_IO = 25;
	public final static int NO_AGAIN = 26;
	public final static int BAD_AGAIN = 27;
	public final static int NO_MULTI = 28;
	public final static int ANY_OF = 29;
	public final static int ONLY_SEE = 30;
	public final static int CANT_TALK = 31;
	public final static int INT_PPC_INV = 32;
	public final static int INT_PPC_LONG = 33;
	public final static int INT_PPC_LOOP = 34;
	public final static int DONT_SEE_ANY_MORE = 38;
	public final static int DONT_SEE_THAT = 39;
	public final static int NO_NEW_NUM = 40;
	public final static int BAD_DIS_STAT = 41;
	public final static int EMPTY_DISAMBIG = 42;
	public final static int DISAMBIG_RETRY = 43;
	public final static int AMBIGUOUS = 44;
	public final static int TRY_AGAIN = 100;
	public final static int WHICH_PFX = 101;
	public final static int WHICH_COMMA = 102;
	public final static int WHICH_OR = 103;
	public final static int WHICH_QUESTION = 104;
	public final static int DONTKNOW_PFX = 110;
	public final static int DONTKNOW_SPC = 111;
	public final static int DONTKNOW_ANY = 112;
	public final static int DONTKNOW_TO = 113;
	public final static int DONTKNOW_SPC2 = 114;
	public final static int DONTKNOW_END = 115;
	public final static int MULTI = 120;
	public final static int ASSUME_OPEN = 130;
	public final static int ASSUME_CLOSE = 131;
	public final static int ASSUME_SPACE = 132;
	public final static int WHAT_PFX = 140;
	public final static int WHAT_IT = 141;
	public final static int WHAT_TO = 142;
	public final static int WHAT_END = 143;
	public final static int WHAT_THEM = 144;
	public final static int WHAT_HIM = 145;
	public final static int WHAT_HER = 146;
	public final static int WHAT_THEM2 = 147;
	public final static int WHAT_PFX2 = 148;
	public final static int WHAT_TOSPC = 149;
	public final static int MORE_SPECIFIC = 160;
	public final static int NOREACH_MULTI = 200;

	public final static int ADMIN_COMMAND = -1;
	public final static int PREPARSE_ABORT = -2;
	public final static int NULL_INPUT = -3;
	public final static int TOKENIZER_ERROR = -4;
	public final static int DELETED_OBJ_REF = -5;
	public final static int PNP_ERROR = -6;
	public final static int ABORT_THROWN = -7;
	public final static int REACH_ERROR = -8;
	public final static int RESTARTING = -9;
	public final static int TYPE_MISMATCH = -10;

	public void print( int errnum )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		print( errnum, null );
	}

	public void print( int errnum, String sub )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		// BAD_DIS_STAT is a real (ie, #defined in adv.t) error, but
		// it shouldn't print a message to the user, so treat it as a
		// special message. ditto NO_NEW_NUM
		if ( errnum < 0 || errnum == BAD_DIS_STAT || errnum == NO_NEW_NUM ) {
			print_error( errstring( errnum ) + ( sub == null ? "" : sub ), 2 );
			return;
		}

		String errstr = errstring( errnum );
		TValue arg1 = newTValue( TValue.NUMBER, errnum );
		TValue arg2 = newTValue( TValue.SSTRING, errstr );
		TValue arg3 = ( sub == null ) ? newTValue( TValue.SSTRING, sub ) : null;

		TObject perror_param = getState().lookup_required_object( RequiredObjects.PARSE_ERROR_PARAM );
		TObject perror = getState().lookup_required_object( RequiredObjects.PARSE_ERROR );
		if ( perror_param != null ) {
			TValue ret = null;
			if ( arg3 != null ) {
				ret = getRunner().run( perror_param.get_data(), this.arg_array( arg1, arg2, arg3 ) );
			} else {
				ret = getRunner().run( perror_param.get_data(), this.arg_array( arg1, arg2 ) );
			}
			if ( ret.get_type() == TValue.SSTRING ) {
				errstr = ret.get_string();
			} else {
				ret.must_be( TValue.NIL );
			}
		} else if ( perror != null ) {
			TValue ret = getRunner().run( perror.get_data(), this.arg_array( arg1, arg2 ) );
			if ( ret.get_type() == TValue.SSTRING ) {
				errstr = ret.get_string();
			} else {
				ret.must_be( TValue.NIL );
			}
		}

		actual_print( errstr, errnum, sub );
	}

	private void actual_print( String errstr, int errnum, String sub )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		int x = errstr.indexOf( "%s" );
		if ( x == -1 ) {
			x = errstr.indexOf( "%d" );
		}
		if ( x == -1 ) {
			x = errstr.indexOf( "%c" );
		}

		String err2 = null;
		if ( x != -1 ) {
			err2 = errstr.substring( x + 2 );
			errstr = errstr.substring( 0, x );
		}

		if ( sub == null && err2 != null ) {
			print_error( "Arg not found for error=" + errnum, 1 );
			sub = "";
		} else if ( sub != null && err2 == null ) {
			print_error( "Arg '" + sub + "' unnecessary for error=" + errnum, 1 );
		}

		if ( err2 == null ) {
			print( errstr );
		} else {
			print( errstr + sub + err2 );
		}

		if ( errnum < 100 || errnum == DONTKNOW_END || errnum == WHAT_END ||
			errnum == WHICH_QUESTION || errnum == ASSUME_CLOSE ||
			errnum == MORE_SPECIFIC ) {
			print( "\\n" );
		}
	}

	private String errstring( int errnum ) {
		switch ( errnum ) {
			case UNK_WORD:
				return "I don't know the word \"%s\".";
			case ART_NOUN:
				return "An article must be followed by a noun.";
			case EXTRA_WORDS:
				return "There are words after your command I couldn't use.";
			case NO_TEMPLATE:
				return "internal error: verb has no action, doAction, or ioAction";
			case DONT_SEE_REF:
				return "I don't see what you're referring to.";
			case DONT_SEE_ANY:
				return "I don't see any %s here.";
			case DONT_SEE_THAT:
				return "You don't see that here.";
			case DONT_SEE_ANY_MORE:
				return "You don't see that here any more.";
			case ONLY_SEE:
				return "I only see %d of those.";
			case NO_VERB:
				return "There's no verb in that sentence!";
			case NO_AGAIN:
				return "There's no command to repeat.";
			case BAD_AGAIN:
				return "You can't repeat that command.";
			case ONE_ACTOR:
				return "You can only speak to one person at a time.";
			case NO_MULTI_IO:
				return "You can't use multiple indirect objects.";
			case NO_MULTI:
				return "You can't use multiple objects with this command.";
			case DONT_RECOGNIZE:
				return "I don't recognize that sentence.";
			case TRY_AGAIN:
				return "Let's try it again: ";
			case WHICH_PFX:
				return "Which %s do you mean, ";
			case WHICH_COMMA:
				return ", ";
			case WHICH_OR:
				return "or ";
			case WHICH_QUESTION:
				return "?";
			case DONT_KNOW_REF:
				return "I don't know what you're referring to with '%s'.";
			case WHAT_PFX:
				return "What do you want to ";
			case WHAT_PFX2:
				return "What do you want ";
			case WHAT_TOSPC:
				return " to ";
			case WHAT_IT:
				return " it ";
			case WHAT_THEM:
				return " them ";
			case WHAT_HIM:
				return " him ";
			case WHAT_HER:
				return " her ";
			case WHAT_TO:
				return "to";
			case WHAT_END:
				return "?";
			case DONT_KNOW_REF2:
				return "I don't know what you're referring to.";
			case DONT_UNDERSTAND:
				return "I don't understand that sentence.";
			case DONTKNOW_PFX:
				return "I don't know how to ";
			case DONTKNOW_SPC:
				return " ";
			case DONTKNOW_ANY:
				return " anything ";
			case DONTKNOW_TO:
				return "to";
			case DONTKNOW_SPC2:
				return " ";
			case DONTKNOW_END:
				return ".";
			case MULTI:
				return ": ";
			case ASSUME_OPEN:
				return "(";
			case ASSUME_SPACE:
				return " ";
			case ASSUME_CLOSE:
				return ")";
			case MORE_SPECIFIC:
				return "You'll have to be more specific about which %s you mean.";
			case BAD_DIS_STAT:
				return "disambigXobj error";
			case NO_NEW_NUM:
				return "error creating new numbered object";
			case ADMIN_COMMAND:
				return "admin command received";
			case PREPARSE_ABORT:
				return "preparse() requested an abort";
			case NULL_INPUT:
				return "empty input command";
			case TOKENIZER_ERROR:
				return "tokenizer error - ";
			case DELETED_OBJ_REF:
				return "deleted object referenced";
			case PNP_ERROR:
				return "parseNounPhrase() returned PNP_ERROR";
			case ABORT_THROWN:
				return "abort statement executed";
			case REACH_ERROR:
				return "cantReach called";
			case RESTARTING:
				return "restart() called";
			case TYPE_MISMATCH:
				return "incorrect type found";
			default:
				print_error( "Unknown ParserError code (" + errnum + ")", 1 );
				return "";
		}
	}

//	private GameState _state;
}
