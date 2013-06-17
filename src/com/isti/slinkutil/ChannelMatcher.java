//ChannelMatcher:  Channel matcher for a digitizer channel.
//
//  9/21/2011 -- [KF]
//

package com.isti.slinkutil;

/**
 * Class ChannelMatcher is a channel matcher for a digitizer channel.
 */
public class ChannelMatcher {
  /** Null string for location code ("--"). */
  public static final String LOC_NULL_STR = "--";

  /** The channel number prefix text. */
  private final static String CHANNEL_NUMBER_PREFIX_TEXT = "dig1:";

  /** The SCN separator text. */
  private final static String SCNL_SEPARATOR = ".";

  /** Number for matching all. */
  private final static int ALL_NUMBER = -1;

  /** Text for matching all. */
  private final static String ALL_TEXT = "*";

  /** Index number for channel (1-based). */
  public final int channelNumber;

  /** ID string for channel. */
  public final String channelIdStr;

  /** Station name for channel. */
  public final String stationNameStr;

  /** Network name for channel. */
  public final String networkNameStr;

  /** Location code for channel. */
  public final String locationCodeStr;

  /**
   * Create a channel matcher object.
   * @param s the channel name matching text.
   */
  public ChannelMatcher(String s) {
    int channelNumber = ALL_NUMBER;
    String channelIdStr = ALL_TEXT;
    String stationNameStr = ALL_TEXT;
    String networkNameStr = ALL_TEXT;
    String locationCodeStr = ALL_TEXT;
    int endIndex = s.indexOf(SCNL_SEPARATOR);

    // if channel number specified
    if (s.startsWith(CHANNEL_NUMBER_PREFIX_TEXT)) {
      try {
        channelNumber = Integer.parseInt(s.substring(CHANNEL_NUMBER_PREFIX_TEXT
            .length()));
      } catch (NumberFormatException ex) {
        channelNumber = Integer.MAX_VALUE; // don't match anything
        LogMgr.usrMsgWarning("ChannelMatcher:  Invalid channel name (" + s
            + ")");
      }
    } else if (endIndex == -1) { // no separator found
      channelIdStr = s; // bare channel
    } else {
      // determine the SCNL
      int beginIndex = 0;
      int sIndex = 0;
      String[] sA = new String[4];
      while (sIndex < sA.length) {
        sA[sIndex++] = s.substring(beginIndex, endIndex);
        if (endIndex >= s.length())
          break;
        beginIndex = endIndex + 1;
        // find next separator
        endIndex = s.indexOf(SCNL_SEPARATOR, beginIndex);
        if (endIndex == -1) { // if no more separators
          endIndex = s.length(); // use the rest of the text
        }
      }

      switch (sIndex) {
      case 2: // CH.LOC
        channelIdStr = sA[0];
        locationCodeStr = sA[1];
        break;
      case 3: // CH.NET.LOC
        channelIdStr = sA[0];
        networkNameStr = sA[1];
        locationCodeStr = sA[2];
        break;
      case 4: // STA.CH.NET.LOC
        stationNameStr = sA[0];
        channelIdStr = sA[1];
        networkNameStr = sA[2];
        locationCodeStr = sA[3];
        break;
      }
    }

    this.channelNumber = channelNumber;
    this.channelIdStr = channelIdStr;
    this.stationNameStr = stationNameStr;
    this.networkNameStr = networkNameStr;
    this.locationCodeStr = locationCodeStr;
  }

  /**
   * Determines if there is a match.
   * @param channelNumber index number for channel (1-based).
   * @param channelIdStr ID string for channel.
   * @param stationNameStr station name for channel.
   * @param networkNameStr network name for channel.
   * @param locationCodeStr location code for channel.
   * @return true if match, false otherwise.
   */
  public boolean isMatch(int channelNumber, String channelIdStr,
      String stationNameStr, String networkNameStr, String locationCodeStr) {
    // if not matching all channel numbers
    if (this.channelNumber != ALL_NUMBER)
      // match if channel number matches
      return this.channelNumber == channelNumber;
    // match if S C N L all match
    if (!isMatch(this.channelIdStr, channelIdStr))
      return false;
    if (!isMatch(this.stationNameStr, stationNameStr))
      return false;
    if (!isMatch(this.networkNameStr, networkNameStr))
      return false;
    if (LOC_NULL_STR.equals(this.locationCodeStr) ? !isEmpty(locationCodeStr)
        : !isMatch(this.locationCodeStr, locationCodeStr))
      return false;
    return true;
  }

  /**
   * Determines if the text is empty.
   * @param s the text or null if none.
   * @return true if the text is empty, false otherwise.
   */
  private static boolean isEmpty(String s) {
    return s == null || s.trim().length() == 0;
  }

  /**
   * Determines if there is a match.
   * @param s the channel matcher text.
   * @param dcs the digital channel text.
   * @return true if match, false otherwise.
   */
  private static boolean isMatch(String s, String dcs) {
    return s.equals(ALL_TEXT) || s.equals(dcs);
  }
}
