//StaChaNetLoc.java:  Defines the station, channel, network, location.
//
//  9/10/2009 -- [KF]
//

package com.isti.slinkutil;

/**
 * Class StaChaNetLoc defines the station, channel, network, location.
 */
public class StaChaNetLoc extends AbstractStaChaNetLoc {
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
	 *            station code string.
	 * @param channelIdStr
	 *            channel code string.
	 * @param networkCodeStr
	 *            network code string.
	 * @param locationIdStr
	 *            location code string.
	 */
	public StaChaNetLoc(String stationIdStr, String channelIdStr,
			String networkCodeStr, String locationIdStr) {
		this.stationIdStr = trim(stationIdStr);
		this.channelIdStr = trim(channelIdStr);
		this.networkCodeStr = trim(networkCodeStr);
		this.locationIdStr = trim(locationIdStr);
	}

	/**
	 * Returns the channel code string for the message.
	 * 
	 * @return The channel code string.
	 */
	public String getChannelCode() {
		return channelIdStr;
	}

	/**
	 * Returns the location code string for the message.
	 * 
	 * @return The location code string.
	 */
	public String getLocationCode() {
		return locationIdStr;
	}

	/**
	 * Returns the network code string for the message.
	 * 
	 * @return The network code string.
	 */
	public String getNetworkCode() {
		return networkCodeStr;
	}

	/**
	 * Returns the station code string for the message.
	 * 
	 * @return The station code string.
	 */
	public String getStationCode() {
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
			hash = super.hashCode();
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
		return getUniqueId();
	}
}
