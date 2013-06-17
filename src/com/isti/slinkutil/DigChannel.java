//DigChannel.java:  Manages information for a digitizer channel.
//
//  9/12/2008 -- [ET]
//  9/23/2011 -- [KF]  Modified to enhance channel parameter.
//

package com.isti.slinkutil;

/**
 * Class DigChannel manages information for a digitizer channel.
 */
public class DigChannel implements IDigChannel {
  /** Index number for channel (1-based). */
  private final int channelNumber;

  /** Cache the hash code. */
  private int hash = 0;

  /** SCNL object. */
  private final IStaChaNetLoc scnlObj;

  /** Select flag for channel. */
  private final boolean selectFlag;

  /**
   * Create a digitizer-channel-information object.
   * @param scnlObj the SCNL object.
   */
  public DigChannel(IStaChaNetLoc scnlObj) {
    this(scnlObj, 1, true);
  }

  /**
   * Create a digitizer-channel-information object.
   * @param scnlObj the SCNL object.
   * @param channelNumber index number for channel (1-based).
   * @param selectFlag select flag for channel.
   */
  public DigChannel(IStaChaNetLoc scnlObj, int channelNumber, boolean selectFlag) {
    this.scnlObj = scnlObj;
    this.channelNumber = channelNumber;
    this.selectFlag = selectFlag;
  }

  /**
   * Create a digitizer-channel-information object.
   * @param stationIdStr station identifier string.
   * @param channelIdStr channel identifier string.
   * @param networkCodeStr network code string.
   * @param locationIdStr location identifier string.
   * @param channelNumber index number for channel (1-based).
   * @param selectFlag select flag for channel.
   */
  public DigChannel(String stationIdStr, String channelIdStr,
      String networkCodeStr, String locationIdStr, int channelNumber,
      boolean selectFlag) {
    this(new StaChaNetLoc(stationIdStr, channelIdStr, networkCodeStr,
        locationIdStr), channelNumber, selectFlag);
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * @param obj the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj argument;
   *         <code>false</code> otherwise.
   * @see #hashCode()
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DigChannel) {
      return toString().equals(obj.toString());
    }
    return false;
  }

  /**
   * Returns the channel identifier string for the digitizer channel.
   * @return The channel identifier string.
   */
  public String getChannelIdStr() {
    return scnlObj.getChannelIdStr();
  }

  /**
   * Returns the channel number for the digitizer channel.
   * @return The channel number.
   */
  public int getChannelNumber() {
    return channelNumber;
  }

  /**
   * Returns the location identifier string for the digitizer channel.
   * @return The location identifier string.
   */
  public String getLocationIdStr() {
    return scnlObj.getLocationIdStr();
  }

  /**
   * Returns the network code string for the digitizer channel.
   * @return The network code string.
   */
  public String getNetworkCodeStr() {
    return scnlObj.getNetworkCodeStr();
  }

  /**
   * Returns the SCNL object for the digitizer channel.
   * @return The 'StaChaNetLoc' object.
   */
  public IStaChaNetLoc getStaChaNetLocObj() {
    return scnlObj;
  }

  /**
   * Returns the station identifier string for the digitizer channel.
   * @return The station identifier string.
   */
  public String getStationIdStr() {
    return scnlObj.getStationIdStr();
  }

  /**
   * Returns a hash code value for the object. This method is supported for the
   * benefit of hashtables such as those provided by
   * <code>java.util.Hashtable</code>.
   * @return a hash code value for this object.
   * @see java.lang.Object#equals(java.lang.Object)
   * @see java.util.Hashtable
   */
  public int hashCode() {
    if (hash == 0) {
      hash = toString().hashCode();
    }
    return hash;
  }

  /**
   * Determines if this channel is selected.
   * @return true if selected, false otherwise.
   */
  public boolean isSelected() {
    return selectFlag;
  }

  /**
   * Returns a string representation of the digitizer channel.
   * @return a string representation of the digitizer channel.
   */
  public String toString() {
    return scnlObj.toString() + " (" + channelNumber + ")";
  }
}
