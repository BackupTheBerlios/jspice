package tads2.jetty;

public class AskIobjException extends ParseException
{
  public AskIobjException(TObject iobj)
  {
    _iobj = iobj;
  }

  public TObject get_iobj() { return _iobj; }

  public String toString()
  {
    return "askIo(" + _iobj.get_id() + ")";
  }

  private TObject _iobj;
}
