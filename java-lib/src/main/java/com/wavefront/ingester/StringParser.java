package com.wavefront.ingester;

/**
 * A lightweight re-usable parser custom-tailored to suit all of our supported line protocols.
 *
 * @author vasily@wavefront.com
 */
public class StringParser {
  private int currentIndex = 0;
  private String input = null;
  private String peek = null;

  public StringParser() {
  }

  /**
   * @param input string to parse at instance creation
   */
  public StringParser(String input) {
    parse(input);
  }

  /**
   * Re-sets the parser with a new string to be parsed.
   *
   * @param input string to parse
   */
  public void parse(String input) {
    this.input = input;
    this.currentIndex = 0;
  }

  /**
   * Retrieves the next available token, but does not advance the further, so multiple
   * calls to peek() return the same value. The value is cached so performance
   * penalty is negligible.
   *
   * @return next available token or null if end of line was reached
   */
  public String peek() {
    if (input == null) throw new IllegalStateException("parse() must be called first");
    if (peek == null) {
      peek = advance();
    }
    return peek;
  }

  /**
   * Checks whether there are more tokens available in the string.
   *
   * @return true if more tokens available
   */
  public boolean hasNext() {
    return peek() != null;
  }

  /**
   * Retrieves the next available token and advances further.
   *
   * @return next available token or null if end of line was reached
   */
  public String next() {
    String token = peek();
    peek = null;
    return token;
  }

  private String advance() {
    while (currentIndex < input.length() && Character.isWhitespace(input.charAt(currentIndex))) {
      // skip whitespace if any
      currentIndex++;
    }
    if (currentIndex >= input.length()) return null;
    char currentChar = input.charAt(currentIndex);
    if (isQuote(currentChar)) {
      return parseAsQuoted(currentChar);
    } else if (isTokenCharacter(currentChar)) {
      currentIndex++;
      return Character.toString(currentChar);
    } else {
      return parseAsNonQuoted();
    }
  }

  private String parseAsQuoted(char quoteChar) {
    int matchingQuoteIndex = currentIndex + 1;
    boolean hasEscapedQuotes = false;
    boolean isEscapedQuote;
    do {
      int index = input.indexOf(quoteChar, matchingQuoteIndex);
      if (index == -1) throw new RuntimeException("Unmatched quote character: (" + quoteChar + ")");
      matchingQuoteIndex = index + 1;
      isEscapedQuote = input.charAt(index - 1) == '\\';
      if (isEscapedQuote) hasEscapedQuotes = true;
    } while (isEscapedQuote);
    String unquoted = input.substring(currentIndex + 1, matchingQuoteIndex - 1);
    currentIndex = matchingQuoteIndex;
    if (!hasEscapedQuotes) return unquoted;
    return unquoted.replace("\\" + quoteChar, Character.toString(quoteChar));
  }

  private String parseAsNonQuoted() {
    int indexOfSeparator = indexOfAnySeparator(input, currentIndex);
    int endOfToken = indexOfSeparator == -1 ? input.length() : indexOfSeparator;
    String result = input.substring(currentIndex, endOfToken);
    currentIndex = endOfToken;
    return result;
  }

  private static boolean isTokenCharacter(char ch) {
    return ch == '=' || ch == '#';
  }

  private static boolean isQuote(char ch) {
    return ch == '\"' || ch == '\'';
  }

  private static int indexOfAnySeparator(String input, int startIndex) {
    for (int i = startIndex; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == ' ' || ch == '\t' || ch == '=') return i;
    }
    return -1;
  }
}
