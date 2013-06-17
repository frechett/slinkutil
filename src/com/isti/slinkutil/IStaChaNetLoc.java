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
	/**
	 * Returns the channel identifier string for the message.
	 * 
	 * @return The channel identifier string.
	 */
	public String getChannelIdStr();

	/**
	 * Returns the location identifier string for the message.
	 * 
	 * @return The location identifier string.
	 */
	public String getLocationIdStr();

	/**
	 * Returns the network code string for the message.
	 * 
	 * @return The network code string.
	 */
	public String getNetworkCodeStr();

	/**
	 * Returns the station identifier string for the message.
	 * 
	 * @return The station identifier string.
	 */
	public String getStationIdStr();
}
