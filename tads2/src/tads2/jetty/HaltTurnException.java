package tads2.jetty;

public class HaltTurnException extends ParseException
{
  public HaltTurnException(int errnum)
  {
    _errnum = errnum;
    _errstr = null;
  }

  public HaltTurnException(int errnum, String errstr)
  {
    _errnum = errnum;
    _errstr = errstr;
  }

  public int get_errnum() { return _errnum; }
  public String get_errstr() { return _errstr; }

  public String toString()
  {
    return "halt turn (code=" + _errnum + 
      (_errstr == null ? ")" : "/" + _errstr + ")");
  }

  private int _errnum;      // the ParseError code to halt with
  private String _errstr;   // some arg for it
}
