package tads2.jetty;

// and yet more general general ids
public class Constants
{
  public static final int INVALID = 0;

  // inputevent() arguments:
  public static final int INPUT_EVENT_KEY        = 1;
  public static final int INPUT_EVENT_TIMEOUT    = 2;
  public static final int INPUT_EVENT_HREF       = 3;
  public static final int INPUT_EVENT_NOTIMEOUT  = 4;
  public static final int INPUT_EVENT_EOF        = 5;


  // inputdialog() arguments:
  // dialogue type:
  public static final int INDLG_OK               = 1;
  public static final int INDLG_OKCANCEL         = 2;
  public static final int INDLG_YESNO            = 3;
  public static final int INDLG_YESNOCANCEL      = 4;

  // icon type:
  public static final int INDLG_ICON_NONE        = 0;
  public static final int INDLG_ICON_WARNING     = 1;
  public static final int INDLG_ICON_INFO        = 2;
  public static final int INDLG_ICON_QUESTION    = 3;
  public static final int INDLG_ICON_ERROR       = 4;

  // label:
  public static final int INDLG_LBL_OK           = 1;
  public static final int INDLG_LBL_CANCEL       = 2;
  public static final int INDLG_LBL_YES          = 3;
  public static final int INDLG_LBL_NO           = 4;


  // gettime() arguments:
  public static final int GETTIME_DATE_AND_TIME  = 1;
  public static final int GETTIME_TICKS          = 2;


  // askfile() arguments:
  // open an existing file for reading:
  public static final int ASKFILE_PROMPT_OPEN    = 1;
  // save to the file:
  public static final int ASKFILE_PROMPT_SAVE    = 2;  

  // and askfile() flags:
  public static final int ASKFILE_EXT_RESULT    = 1;   // extended result codes

  // and askfile() return codes:
  // success - 2nd list element is filename:
  public static final int ASKFILE_SUCCESS        = 0;
  // an error occurred asking for a file:
  public static final int ASKFILE_FAILURE        = 1;    
  // player canceled the file selector:
  public static final int ASKFILE_CANCEL         = 2;

  // and askfile() file types:
  public static final int FILE_TYPE_GAME    = 0; // a game data file (.gam)
  public static final int FILE_TYPE_SAVE    = 1; // a saved game (.sav)
  public static final int FILE_TYPE_LOG     = 2; // a transcript (log) file 
  public static final int FILE_TYPE_DATA    = 4; // data - used for fopen()
  public static final int FILE_TYPE_CMD     = 5; // command input file
  public static final int FILE_TYPE_TEXT    = 7; // text file
  public static final int FILE_TYPE_BIN     = 8; // binary data file
  public static final int FILE_TYPE_UNKNOWN = 11; // unknown file type


  // execCommand() flags
  public static final int EC_HIDE_SUCCESS   = 1;   // hide success messages 
  public static final int EC_HIDE_ERROR     = 2;   // hide error messages 
  public static final int EC_SKIP_VALIDDO   = 4;   // skip dobj validation 
  public static final int EC_SKIP_VALIDIO   = 8;   // skip iobj validation

  // execCommand() return codes
  public static final int EC_SUCCESS        = 0;    // successful completion
  public static final int EC_EXIT           = 1013; // "exit" executed
  public static final int EC_ABORT          = 1014; // "abort" executed
  public static final int EC_ASKDO          = 1015; // "askdo" executed
  public static final int EC_ASKIO          = 1016; // "askio" executed
  public static final int EC_EXITOBJ        = 1019; // "exitobj" executed
  public static final int EC_INVAL_SYNTAX   = 1200; // invalid sentence struct
  public static final int EC_VERDO_FAILED   = 1201; // verDoVerb failed
  public static final int EC_VERIO_FAILED   = 1202; // verIoVerb failed 
  public static final int EC_NO_VERDO       = 1203; // no verDoVerb method 
  public static final int EC_NO_VERIO       = 1204; // no verIoVerb method 
  public static final int EC_INVAL_DOBJ     = 1205; // dobj validation failed
  public static final int EC_INVAL_IOBJ     = 1206; // iobj validation failed 


  // parserGetObj() return codes
  public static final int PO_ACTOR    = 1; // actor
  public static final int PO_VERB     = 2; // deepverb object
  public static final int PO_DOBJ     = 3; // direct object
  public static final int PO_PREP     = 4; // preposition object
  public static final int PO_IOBJ     = 5; // indirect object
  public static final int PO_IT       = 6; // get the "it" object
  public static final int PO_HIM      = 7; // get the "him" object
  public static final int PO_HER      = 8; // get the "her" object
  public static final int PO_THEM     = 9; // get the "them" object


  // parseNounPhrase() return codes
  public static final int PNP_ERROR        = 1; //  noun phrase syntax error
  public static final int PNP_USE_DEFAULT  = 2; // use default processing


  // verb.disambigDobj()/disambigIobj() return codes
  public static final int DISAMBIG_CONTINUE    = 1; // continue disambig for list
  public static final int DISAMBIG_DONE        = 2; // list is fully resolved
  public static final int DISAMBIG_ERROR       = 3; // disambiguation failed
  public static final int DISAMBIG_PARSE_RESP  = 4; // parse response string
  public static final int DISAMBIG_PROMPTED    = 5; // prompted for response


  // parser word type flags (for parseUnknownVerb() and parseNounPhrase())
  public static final int PRSTYP_ARTICLE  = 1;  // the, a, an 
  public static final int PRSTYP_ADJ      = 2;  // adjective
  public static final int PRSTYP_NOUN     = 4;  // noun 
  public static final int PRSTYP_PREP     = 8;  // preposition
  public static final int PRSTYP_VERB     = 16; // verb
  public static final int PRSTYP_SPEC     = 32; // special words - "of", etc.
  public static final int PRSTYP_PLURAL   = 64;  // plural
  public static final int PRSTYP_UNKNOWN  = 128; // word is not in dictionary


  // parseNounPhrase flags (in return list)
  public static final int PRSFLG_ALL      = 1;   // "all"
  public static final int PRSFLG_EXCEPT   = 2;   // "except" or "but"
  public static final int PRSFLG_IT       = 4;   // "it"
  public static final int PRSFLG_THEM     = 8;   // "them"
  public static final int PRSFLG_NUM      = 16;  // a number
  public static final int PRSFLG_COUNT    = 32;  // noun phrase uses a count
  public static final int PRSFLG_PLURAL   = 64;  // noun phrase is a plural
  public static final int PRSFLG_ANY      = 128; // noun phrase uses "any"
  public static final int PRSFLG_HIM      = 256;  // "him"
  public static final int PRSFLG_HER      = 512;  // "her"
  public static final int PRSFLG_STR      = 1024; // a quoted string
  public static final int PRSFLG_UNKNOWN  = 2048; // has unknown word
  public static final int PRSFLG_ENDADJ   = 4096; // noun phrase ends in an adj
  public static final int PRSFLG_TRUNC    = 8192; // uses a truncated word


  // parserResolveObjects() arguments
  public static final int PRO_RESOLVE_DOBJ  = 1;  // direct object
  public static final int PRO_RESOLVE_IOBJ  = 2;  // indirect object
  public static final int PRO_RESOLVE_ACTOR = 3;  // actor


  // restore() return codes
  public static final int RESTORE_SUCCESS          = 0; // success
  public static final int RESTORE_FILE_NOT_FOUND   = 1; // file not found
  public static final int RESTORE_NOT_SAVE_FILE    = 2; // not a savegame file
  public static final int RESTORE_BAD_FMT_VSN      = 3; // incompatible version
  public static final int RESTORE_BAD_GAME_VSN     = 4; // bad game or version
  public static final int RESTORE_READ_ERROR       = 5; // error reading file
  public static final int RESTORE_NO_PARAM_FILE    = 6; // no parameter given

  // defined() arguments
  public static final int DEFINED_ANY         = 1; // default behavior
  public static final int DEFINED_DIRECTLY    = 2; // defined directly by obj
  public static final int DEFINED_INHERITS    = 3; // inherited, not defined
  public static final int DEFINED_GET_CLASS   = 4; // get the defining class

  // convert between these two:
  public final static int[][] vocab_prop_flags =
    { { VocabWord.VERB, Constants.PRSTYP_VERB },
      { VocabWord.NOUN, Constants.PRSTYP_NOUN },
      { VocabWord.ADJECTIVE, Constants.PRSTYP_ADJ },
      { VocabWord.PREPOSITION, Constants.PRSTYP_PREP },
      { VocabWord.ARTICLE, Constants.PRSTYP_ARTICLE },
      { VocabWord.PLURAL, Constants.PRSTYP_PLURAL } };
  public final static String[] vocab_names =
  { "verb", "noun", "adjective", "preposition", "article", "plural" };

  public static int vocab_flag(int prop)
  {
    for (int i = 0; i < vocab_prop_flags.length; i++)
      if (vocab_prop_flags[i][0] == prop)
	return vocab_prop_flags[i][1];
    return 0;
  }

  public static int vocab_prop(int flag)
  {
    for (int i = 0; i < vocab_prop_flags.length; i++)
      if (vocab_prop_flags[i][1] == flag)
	return vocab_prop_flags[i][0];
    return -1;
  }

  private Constants() {} // this should never be called
}
