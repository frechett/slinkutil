//SLinkUtilFns.java:  Defines various static SeedLink utility functions.
//
//  10/2/2009 -- [KF]
//

package com.isti.slinkutil;

import java.io.OutputStream;

/**
 * Class SLinkUtilFns various static SeedLink utility functions.
 */
public class SLinkUtilFns {
  /** The space character. */
  public final static char SPACE_CHAR = ' ';

  /**
   * Appends the string padded or truncated to the specified length.
   * @param sb the string buffer.
   * @param s the string.
   * @param length the length.
   */
  public static void append(StringBuffer sb, String s, int length) {
    append(sb, s, length, SPACE_CHAR);
  }

  /**
   * Appends the string padded or truncated to the specified length.
   * @param sb the string buffer.
   * @param s the string.
   * @param length the length.
   * @param padChar the pad character.
   */
  public static void append(StringBuffer sb, String s, int length, char padChar) {
    int i = s.length();
    // truncate if too long
    if (i > length) {
      s = s.substring(0, length);
    }
    // append the string
    sb.append(s);
    // append pad character as needed
    for (; i < length; i++) {
      sb.append(padChar);
    }
  }

  /**
   * Close the output stream quietly.
   * @param os the output stream or null if none.
   */
  public static void close(OutputStream os) {
    try {
      if (os != null)
        os.close();
    } catch (Exception ex) {
    }
  }

  /**
   * Encodes this <tt>String</tt> into a sequence of ASCII bytes.
   * @param s the character string.
   * @return the byte array.
   */
  public static byte[] getBytes(String s) {
    int n;
    byte[] ascii = new byte[s.length()];
    for (int index = 0; index < s.length(); index++) {
      n = (int) (s.charAt(index));
      if (n > 127) {
        n = '?';
      }
      ascii[index] = (byte) n;
    }
    return ascii;
  }

  protected SLinkUtilFns() {
  }
}
