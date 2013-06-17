//SLinkMessageNumber.java:  Defines a SeedLink message number.
//
//  9/16/2009 -- [KF]
//

package com.isti.slinkutil;

/**
 * Class SLinkMessageNumber defines a SeedLink message number.
 */
public class SLinkMessageNumber implements IMessageNumber {
  /** The maximum message number value. */
  public static final int maxMsgNumVal = 0xFFFFFF;

  /** The minimum message number value. */
  public static final int minMsgNumVal = 0;

  /** Array of "zeros" strings used by 'buildSLMsgNumStr()' method. */
  protected static final String[] SLMSGNUM_ZEROSTR_ARR = { "000000", "00000",
      "0000", "000", "00", "0" };

  /**
   * Converts the given message number to a SeedLink-format message-number
   * string.
   * @param msgNum message number value, 0 to 0xFFFFFF.
   * @return A new 6-character SeedLink-format message-number string.
   */
  public static String buildSLMsgNumStr(int msgNum) {
    return buildSLMsgNumStr(msgNum, null);
  }

  /**
   * Converts the given message number to a SeedLink-format message-number
   * string.
   * @param msgNum message number value, 0 to 0xFFFFFF.
   * @param msgNumStr string version of message number, or null to
   * generate from 'msgNumVal'.
   * @return A new 6-character SeedLink-format message-number string.
   */
  public static String buildSLMsgNumStr(int msgNum, String msgNumStr) {
    if (msgNumStr == null)
      msgNumStr = Integer.toHexString(msgNum).toUpperCase();
    final int len;
    // if less than six characters return string with leading zeros:
    if ((len = msgNumStr.length()) < 6)
      return SLMSGNUM_ZEROSTR_ARR[len] + msgNumStr;
    if (len == 6) // if six characters then
      return msgNumStr; // just return string
    return msgNumStr.substring(0, 6); // if >6 then truncate
  }

  /** The message number value */
  private final int msgNumVal;

  /** Byte array of SeedLink-format-string version of message number. */
  private final byte[] slMsgNumByteArr;

  /** SeedLink-format-string version of message number. */
  private final String slMsgNumStr;

  /** The time the message was created. */
  private final long timeCreated;

  /**
   * Creates the SeedLink message number.
   * @param msgNumVal the message number value.
   */
  public SLinkMessageNumber(int msgNumVal) {
    this(msgNumVal, null);
  }

  /**
   * Creates the SeedLink message number.
   * @param msgNumVal the message number value.
   * @param msgNumStr string version of message number, or null to
   * generate from 'msgNumVal'.
   */
  public SLinkMessageNumber(int msgNumVal, String msgNumStr) {
    timeCreated = System.currentTimeMillis();
    this.msgNumVal = msgNumVal;
    // enter SeedLink-format-string version of message number:
    slMsgNumStr = buildSLMsgNumStr(msgNumVal, msgNumStr);
    // enter byte array of SeedLink-format-string version of message #:
    slMsgNumByteArr = SLinkUtilFns.getBytes(slMsgNumStr);
  }

  /**
   * Get a byte array containing the message number.
   * @return A byte array containing the message number.
   */
  public byte[] getBytes() {
    return slMsgNumByteArr;
  }

  /**
   * Get the message number.
   * @return the message number.
   */
  public int getMessageNumber() {
    return msgNumVal;
  }

  /**
   * Returns the SeedLink-format-string version of the message number.
   * @return The SeedLink-format-string version of the message number.
   */
  public String getSLMsgNumStr() {
    return slMsgNumStr;
  }

  /**
   * Get the time the message was created.
   * @return the time the message was created.
   */
  public long getTimeCreated() {
    return timeCreated;
  }

  /**
   * Returns a string version of the message number.
   * @return A string containing the SeedLink-format-string version
   * of the message number followed by the integer version.
   */
  public String toString() {
    return slMsgNumStr + '/' + msgNumVal;
  }
}
