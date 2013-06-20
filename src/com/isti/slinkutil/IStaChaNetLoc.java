//StaChaNetLoc.java:  Defines the station, channel, network, location.
//
//  9/10/2009 -- [KF]
//

package com.isti.slinkutil;

/**
 * Interface StaChaNetLoc defines the station, channel, network, location. The
 * {@link #equals(Object)} and {@link #hashCode()} method should be provided to
 * ensure the correct handling of hash table keys.
 */
public interface IStaChaNetLoc {
	/** An empty string. */
	public static final String EMPTY_STRING = "";

	/**
	 * Returns the channel code.
	 * 
	 * @return The channel code string.
	 */
	public String getChannelCode();

	/**
	 * Returns the location code.
	 * 
	 * @return The location code string.
	 */
	public String getLocationCode();

	/**
	 * Returns the network code.
	 * 
	 * @return The network code string.
	 */
	public String getNetworkCode();

	/**
	 * Returns the station code.
	 * 
	 * @return The station code string.
	 */
	public String getStationCode();

	/**
	 * Get the unique identifier.
	 * 
	 * @return the unique identifier.
	 */
	public String getUniqueId();
}
