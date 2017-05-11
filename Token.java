package edu.binghamton.cs571;

/** A simple struct representing a token produced by the scanner */
class Token {
  final Kind kind;  /** what kind of token is this */
  final String lexeme;   /** the actual text of this token */
  final Coords coords;   /** where did this token occur */

  Token(Kind kind, String lexeme, Coords coords) {
    this.kind = kind; this.lexeme = lexeme; this.coords = coords;
  }

  public String toString() {
    return String.format("%s: %s \"%s\"", coords, kind, lexeme);
  }

  static enum Kind {
    /** any miscellaneous char; lexeme contains actual char */
    CHAR,
    /** the "chars" token */
    CHARS,
    /** end-of-file */
    EOF,
    /** a newline char */
    NL
  }

}
