package edu.binghamton.cs571;

public class UglyRegexpParser {

    Token _lookahead;
    Scanner _scanner;

    UglyRegexpParser(Scanner scanner) {
        _scanner = scanner;
        _lookahead = _scanner.nextToken();
    }

    /**
     * parse a sequence of lines containing ugly-regexp's; for each ugly regexp
     * print out the corresponding standard regexp. If there is an error, print
     * diagnostic and continue with next line.
     */
    public void parse() {
        while (_lookahead.kind != Token.Kind.EOF) {
            try {
                String out = uglyRegexp();
                if (check(Token.Kind.NL)) {
                    System.out.println(out);
                }
                match(Token.Kind.NL);
            } catch (ParseException e) {
                System.err.println(e.getMessage());
                while (_lookahead.kind != Token.Kind.NL) {
                    _lookahead = _scanner.nextToken();
                }
                _lookahead = _scanner.nextToken();
            }
        }
    }

    /**
     * Return standard syntax regexp corresponding to ugly-regexp read from
     * _scanner.
     */
    //IMPLEMENT THIS FUNCTION and any necessary functions it may call.
    /* Parse function for "uglyRegexp"
    uglyRegexp
      : expression uglyRegexpRest
      ;
     */
    public String uglyRegexp() {
        String value = expression();
        return uglyRegexpRest(value);
    }

    /* Parse function for "uglyRegexpRest"
    uglyRegexpRest
      : "." expression uglyRegexpRest
      | EMPTY
      ;
     */
    public String uglyRegexpRest(String ValueSoFar) {
        if (_lookahead.lexeme.equals(".")) { // "." expression uglyRegexpRest
            match(Token.Kind.CHAR, ".");
            String value = expression();
            return uglyRegexpRest("(" + ValueSoFar + value + ")");
        } else { //EMPTY
            return ValueSoFar;
        }
    }

    /* Parse function for "expression"
    expression
      : anUglyTerm expressionRest
      ;
     */
    public String expression() {
        String value = anUglyTerm();
        return expressionRest(value);
    }

    /* Parse function for "expressionRest"
    expressionRest
      : "+" anUglyTerm expressionRest
      | EMPTY
      ;
     */
    public String expressionRest(String ValueSoFar) {
        if (_lookahead.lexeme.equals("+")) { //"+" anUglyTerm expressionRest
            match(Token.Kind.CHAR, "+");
            String value = "|";
            value = value + anUglyTerm();
            return expressionRest("(" + ValueSoFar + value + ")");
        } else { // EMPTY
            return ValueSoFar;
        }
    }

    /* Parse function for "anUglyTerm"
    anUglyTerm
      : "CHARS" anUglyTermTail1 
      | "(" uglyRegexp ")" 
      | "*" anUglyTerm ;
     */
    public String anUglyTerm() {
        String value = "";
        if (_lookahead.lexeme.equals("chars")) { // "CHARS" anUglyTermTail1
            match(_lookahead.kind.CHARS);
            value = "[" + anUglyTermTail1();
            return value;
        } else if (_lookahead.lexeme.equals("(")) { // "(" uglyRegexp ")"
            value = "(";
            match(_lookahead.kind.CHAR, "(");
            value = value + uglyRegexp();
            value = value + ")";
            match(_lookahead.kind.CHAR, ")");
            return value;
        } else if (_lookahead.lexeme.equals("*")) { // "*" anUglyTerm
            match(_lookahead.kind.CHAR, "*");
            value = anUglyTerm();
            value = value + "*";
            return value;
        }
        return value;
    }

    /* Parse function for "anUglyTermTail1"
    anUglyTermTail1
      : "(" CHAR anUglyTermTail2
      ;
     */
    public String anUglyTermTail1() {
        match(_lookahead.kind.CHAR, "(");
        String value = quote(_lookahead.lexeme);
        match(_lookahead.kind.CHAR);
        value = value + anUglyTermTail2();
        return value;
    }

    /* Parse function for "anUglyTermTail2"
    anUglyTermTail2
      : "," CHAR anUglyTermTail2
      | ")"
      ;
     */
    public String anUglyTermTail2() {
        String value = "";
        if (_lookahead.lexeme.equals(",")) { // "," CHAR anUglyTermTail2            
            match(_lookahead.kind.CHAR, ",");
            value = quote(_lookahead.lexeme);
            match(_lookahead.kind.CHAR);
            value = value + anUglyTermTail2();
            return value;
        } else if (_lookahead.lexeme.equals(")")) { // ")"            
            match(_lookahead.kind.CHAR, ")");
            value = "]";
            return value;
        } else {
            match(Token.Kind.CHAR, ")");
        }
        return value;
    }

    //Utility functions which may be useful for parsing or translation
    /**
     * Return s with first char escaped using a '\' if it is non-alphanumeric.
     */
    private static String quote(String s) {
        return (Character.isLetterOrDigit(s.charAt(0))) ? s : "\\" + s;
    }

    /**
     * Return true iff _lookahead.kind is equal to kind.
     */
    private boolean check(Token.Kind kind) {
        return check(kind, null);
    }

    /**
     * Return true iff lookahead kind and lexeme are equal to corresponding
     * args. Note that if lexeme is null, then it is not used in the match.
     */
    private boolean check(Token.Kind kind, String lexeme) {
        return (_lookahead.kind == kind
                && (lexeme == null || _lookahead.lexeme.equals(lexeme)));
    }

    /**
     * If lookahead kind is equal to kind, then set lookahead to next token;
     * else throw a ParseException.
     */
    private void match(Token.Kind kind) {
        match(kind, null);
    }

    /**
     * If lookahead kind and lexeme are not equal to corresponding args, then
     * set lookahead to next token; else throw a ParseException. Note that if
     * lexeme is null, then it is not used in the match.
     */
    private void match(Token.Kind kind, String lexeme) {
        if (check(kind, lexeme)) {
            _lookahead = _scanner.nextToken();
        } else {
            String expected = (lexeme == null) ? kind.toString() : lexeme;
            String message = String.format("%s: syntax error at '%s', expecting '%s'",
                    _lookahead.coords, _lookahead.lexeme,
                    expected);
            throw new ParseException(message);

        }
    }

    private static class ParseException extends RuntimeException {

        ParseException(String message) {
            super(message);
        }
    }

    /**
     * main program: parses and translates ugly-regexp's contained in the file
     * specified by it's single command-line argument.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.format("usage: java %s FILENAME\n",
                    UglyRegexpParser.class
                    .getName());
            System.exit(1);
        }
        Scanner scanner
                = ("-".equals(args[0])) ? new Scanner() : new Scanner(args[0]);
        (new UglyRegexpParser(scanner)).parse();
    }

}
