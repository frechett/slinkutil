//StaChaNetLoc.java:  Defines the station, channel, network, location.
//
//  9/10/2009 -- [KF]
//

package com.isti.slinkutil;

/**
 * Class StaChaNetLoc defines the station, channel, network, location.
 */
public class StaChaNetLoc implements IStaChaNetLoc {
	private static final String EMPTY_STRING = "";

	private static final String separatorText = ".";

	private final String channelIdStr;

	/** Cache the hash code. */
	private int hash = 0;

	private final String locationIdStr;

	private final String networkCodeStr;

	private final String stationIdStr;

	/**
	 * Creates the station, channel, network, location.
	 * 
	 * @param stationIdStr
	 *            station identifier string.
	 * @param channelIdStr
	 *            channel identifier string.
	 * @param networkCodeStr
	 *            network code string.
	 * @param locationIdStr
	 *            location identifier string.
	 */
	public StaChaNetLoc(String stationIdStr, String channelIdStr,
			String networkCodeStr, String locationIdStr) {
		this.stationIdStr = trim(stationIdStr);
		this.channelIdStr = trim(channelIdStr);
		this.networkCodeStr = trim(networkCodeStr);
		this.locationIdStr = trim(locationIdStr);
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument;
	 *         <code>false</code> otherwise.
	 * @see #hashCode()
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IStaChaNetLoc) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	/**
	 * Returns the channel identifier string for the message.
	 * 
	 * @return The channel identifier string.
	 */
	public String getChannelIdStr() {
		return channelIdStr;
	}

	/**
	 * Returns the location identifier string for the message.
	 * 
	 * @return The location identifier string.
	 */
	public String getLocationIdStr() {
		return locationIdStr;
	}

	/**
	 * Returns the network code string for the message.
	 * 
	 * @return The network code string.
	 */
	public String getNetworkCodeStr() {
		return networkCodeStr;
	}

	/**
	 * Returns the station identifier string for the message.
	 * 
	 * @return The station identifier string.
	 */
	public String getStationIdStr() {
		return stationIdStr;
	}

	/**
	 * Returns a hash code value for the object. This method is supported for
	 * the benefit of hash tables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 * 
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
	 * Returns a string representation of the station, channel, network,
	 * location.
	 * 
	 * @return a string representation of the station, channel, network,
	 *         location.
	 */
	public String toString() {
		return stationIdStr + separatorText + channelIdStr + separatorText
				+ networkCodeStr + separatorText + locationIdStr;
	}

	/**
	 * Returns a copy of the string, with leading and trailing whitespace
	 * omitted.
	 * 
	 * @param s
	 *            the string.
	 * @return A copy of the string with leading and trailing white space
	 *         removed, or the string if it has no leading or trailing white
	 *         space.
	 */
	private String trim(String s) {
		if (s == null) {
			return EMPTY_STRING;
		}
		return s.trim();
	}
}
