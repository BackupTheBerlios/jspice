package tads2.jetty;


// this is the glk of this little project. it's implemented by a class
// to handle the applet code and a class to handle the text version 
public interface PlatformIO
{
  // notify this object when we're initialized and when we're resized
  void set_out(OutputFormatter out);

  // returns the length of this string in whatever units we're using
  int size_text(char c);
  int size_text(String s);

  // prints the string (advancing the print cursor as necessary, but not 
  // line-wrapping)
  void print_text(String s);

  // prints an error message
  void print_error(String s);

  // ends the current line, shifts stuff up to accomodate a new line
  void scroll_window();
  
  // tell the status line to have this string on it
  void set_status_string(String s, boolean left);

  // read a key from input
  String read_key();

  // note that this automatically calls scroll_window() after reading
  String read_line();

  // returns true if the user hit space (and so it should scroll
  // a full page), false otherwise (so it should scroll a line)
  boolean more_prompt(String prompt);

  void clear_screen();

  // set the formatting style of the main window font, to bold or italic
  static final int BOLD = 0;
  static final int ITALIC = 1;
  void set_style(int style, boolean turn_on);
}
