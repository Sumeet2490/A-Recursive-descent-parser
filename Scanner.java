package edu.binghamton.cs571;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * A very crude scanner: simple returns the tokens defined in Token.Kind.
 */
class Scanner {

    private final BufferedReader _reader;
    private String _line;
    private Coords _coords; //coordinates of end of last token

    /**
     * Create a scanner for reading from filename
     */
    Scanner(String fileName) {
        this(openFile(fileName), new Coords(fileName));
    }

    /**
     * Create a scanner for reading from stdin.
     */
    Scanner() {
        this(new BufferedReader(new InputStreamReader(System.in)),
                new Coords("<stdin>"));
    }

    /**
     * Create a scanner for reader with previous coordinates coords.
     */
    Scanner(BufferedReader reader, Coords coords) {
        _reader = reader;
        _coords = coords;
        _line = null;
    }

    //Single fixed lexeme recognized.
    private static final String CHARS_LEXEME = "chars";
    private static final int CHARS_LEN = CHARS_LEXEME.length();

    /**
     * Return next token for this scanner.
     */
    Token nextToken() {
        Token token = null;
        Coords coords = _coords;
        int lineN = _coords.lineN;
        int colN = _coords.colN;
        if (_line == null || colN >= _line.length()) {
            _line = getLine();
            if (_line != null) {
                lineN++;
                colN = 0;
            }
        }
        if (_line == null) {
            token = new Token(Token.Kind.EOF, "<EOF>", coords);
        } else {
            char c = _line.charAt(colN);
            while (c == ' ' || c == '\t') { //skip linear whitespace
                colN++;
                c = _line.charAt(colN);
            }
            coords = new Coords(_coords.fileName, lineN, colN);
            if (c == '\n') {
                token = new Token(Token.Kind.NL, "\n", coords);
                colN += 1;
            } else if (_line.length() >= colN + CHARS_LEN
                    && _line.substring(colN, colN + CHARS_LEN).equals(CHARS_LEXEME)) {
                token = new Token(Token.Kind.CHARS, CHARS_LEXEME, coords);
                colN += CHARS_LEN;
            } else {
                token = new Token(Token.Kind.CHAR, String.valueOf(c), coords);
                colN += 1;
            }
        }
        _coords = new Coords(_coords.fileName, lineN, colN);
        return token;
    }

    /**
     * Return next line read from _reader (ensure line terminated with '\n').
     */
    private String getLine() {
        String line = null;
        try {
            line = _reader.readLine();
            if (line == null) {
                _reader.close();
            } else {
                line = line + "\n"; //readLine() does not include terminating "\n"
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return line;
    }

    private static BufferedReader openFile(String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reader;
    }

    /**
     * Simple test program for scanner
     */
    public static void main(String[] args) {
        String test = "chars(1, 2)\n"
                + "chars(a, 1, 2) * + . chars\n"
                + "char\n"
                + "\n";
        BufferedReader reader = new BufferedReader(new StringReader(test));
        Scanner scanner = new Scanner(reader, new Coords("<testString>"));
        Token t;
        do {
            t = scanner.nextToken();
            System.out.println(t);
        } while (t.kind != Token.Kind.EOF);
    }

}
