package tads2.jetty;

// this class is just to have a bunch of ids defined for general use
public class RequiredProperties
{
  public static final int INVALID = 0;
  public static final int DOACTION = 1;      // doAction property
  public static final int VERB = 2;      // verb vocabulary property 
  public static final int NOUN = 3;      // noun vocabulary property
  public static final int ADJ = 4;      // adjective vocabulary property
  public static final int PREP = 5;      // preposition vocabulary property
  public static final int ARTICLE = 6;      // article vocabulary property
  public static final int PLURAL = 7;      // plural vocabulary property
  public static final int SDESC = 8;
  public static final int THEDESC = 9;
  public static final int DO_DEFAULT = 10;
  public static final int IO_DEFAULT = 11;
  public static final int IOACTION = 12;
  public static final int LOCATION = 13;
  public static final int VALUE = 14;
  public static final int ROOM_ACTION = 15;
  public static final int ACTOR_ACTION = 16;
  public static final int CONTENTS = 17;
  public static final int TPL = 18;      // special built-in TEMPLATE structure
  public static final int PREP_DEFAULT = 19;
  public static final int VERACTOR = 20;
  public static final int VALID_DO = 21;
  public static final int VALID_IO = 22;
  public static final int LOOK_AROUND = 23;
  public static final int ROOM_CHECK = 24;
  public static final int STATUS_LINE = 25;
  public static final int LOCOK = 26;
  public static final int IS_VIS = 27;
  public static final int CANT_REACH = 28;
  public static final int IS_HIM = 29;
  public static final int IS_HER = 30;
  public static final int ACTION = 31;      // action method
  public static final int VAL_DO_LIST = 32;      // validDoList
  public static final int VAL_IO_LIST = 33;      // validIoList
  public static final int IOBJ_GEN = 34;      // iobjGen
  public static final int DOBJ_GEN = 35;      // dobjGen
  public static final int NILPREP = 36;      // nilPrep
  public static final int REJECT_MDO = 37;      // rejectMultiDobj
  public static final int MOVE_INTO = 38;      // moveInto
  public static final int CONSTRUCT = 39;      // construct
  public static final int DESTRUCT = 40;      // destruct
  public static final int VALID_ACTOR = 41;      // validActor
  public static final int PREF_ACTOR = 42;      // preferredActor
  public static final int IS_EQUIV = 43;      // isEquivalent
  public static final int ADESC = 44;
  public static final int MULTI_SDESC = 45;
  public static final int TPL2 = 46;   // new-style built-in TEMPLATE structure
  public static final int ANYVALUE = 47;   // anyvalue(n) - value to use for '#' with ANY
  public static final int NEW_NUM_OBJ = 48;   // newnumbered(n) - create new numbered object
  public static final int UNKNOWN = 49;  // internal property for unknown words
  public static final int PARSE_UNKNOWN_DOBJ = 50;      // parseUnknownDobj
  public static final int PARSE_UNKNOWN_IOBJ = 51;      // parseUnknownIobj
  public static final int DOBJ_CHECK = 52;      // dobjCheck
  public static final int IOBJ_CHECK = 53;      // iobjCheck
  public static final int VERB_ACTION = 54;     // verbAction
  public static final int DISAMBIG_DO = 55;     // disambigDobj
  public static final int DISAMBIG_IO = 56;     // disambigIobj
  public static final int PREFIX_DESC = 57;     // prefixdesc
  public static final int IS_THEM = 58;         // isThem

  private RequiredProperties() {} // this should never be called
}
