package tads2.jetty;

import java.util.Vector;

public class ReparseException extends Exception
{
  public ReparseException(Vector tokens)
  {
    _tokens = tokens;
  }

  public Vector get_tokens()
  {
    return _tokens;
  }

  public String toString()
  {
    return "reparse";
  }

  Vector _tokens;
}
