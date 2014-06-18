package com.google.auto.value.processor;

/**
 * Postprocessor that runs over the output of the template engine in order to make it look nicer.
 * Mostly, this involves removing surplus horizontal and vertical space.
 *
 * @author emcmanus@google.com (Éamonn McManus)
 */
class Reformatter {
  static String fixup(String s) {
    s = removeTrailingSpace(s);
    s = compressBlankLines(s);
    s = compressSpace(s);
    return s;
  }

  private static String removeTrailingSpace(String s) {
    // Remove trailing space from all lines. This is mainly to make it easier to find
    // blank lines later.
    if (!s.endsWith("\n")) {
      s += '\n';
    }
    StringBuilder sb = new StringBuilder(s.length());
    int start = 0;
    while (start < s.length()) {
      int nl = s.indexOf('\n', start);
      int i = nl - 1;
      while (i >= start && s.charAt(i) == ' ') {
        i--;
      }
      sb.append(s.substring(start, i + 1)).append('\n');
      start = nl + 1;
    }
    return sb.toString();
  }

  private static String compressBlankLines(String s) {
    // Remove extra blank lines. An "extra" blank line is either a blank line where the previous
    // line was also blank; or a blank line that appears inside parentheses or inside more than one
    // set of braces. This means that we preserve blank lines inside our top-level class, but not
    // within our generated methods.
    StringBuilder sb = new StringBuilder(s.length());
    int braces = 0;
    int parens = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '(':
          parens++;
          break;
        case ')':
          parens--;
          break;
        case '{':
          braces++;
          break;
        case '}':
          braces--;
          break;
        case '\n':
          int j = i + 1;
          while (j < s.length() && s.charAt(j) == '\n') {
            j++;
          }
          if (j > i + 1) {
            if (parens == 0 && braces <= 1) {
              sb.append("\n");
            }
            i = j - 1;
          }
          break;
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private static String compressSpace(String s) {
    // Remove extra spaces. An "extra" space is one that is not part of the indentation at the start
    // of a line, and where the next character is also a space or a right paren or a semicolon
    // or a comma, or the preceding character is a left paren.
    // We can't easily combine this with the removal of trailing whitespace, because in a line with
    // nothing but spaces we would see trailing whitespace as indentation.
    StringBuilder sb = new StringBuilder(s.length());
    boolean indentation = true;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\n':
          indentation = true;
          break;
        case ' ':
          if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '(') {
            continue;
          }
          if (!indentation) {
            // It is safe to look at the next char because the text ends with \n.
            char nextC = s.charAt(i + 1);
            if (" ,;)".indexOf(nextC) >= 0) {
              continue;
            }
          }
          break;
        default:
          indentation = false;
          break;
      }
      sb.append(c);
    }
    return sb.toString();
  }
}