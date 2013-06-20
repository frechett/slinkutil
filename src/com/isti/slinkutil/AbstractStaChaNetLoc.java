package com.isti.slinkutil;

/**
 * Class AbstractStaChaNetLoc defines the station, channel, network, location.
 */
public abstract class AbstractStaChaNetLoc implements IStaChaNetLoc {
	/** The Unique ID separator text. */
	public static final String UNIQUE_ID_SEPARATOR_TEXT = ".";

	/**
	 * Get the unique identifier.
	 * 
	 * @param scnl
	 *            the station, channel, network, location.
	 * @param separatorText
	 *            the separator text.
	 * 
	 * @return the unique identifier.
	 */
	public static String getUniqueId(IStaChaNetLoc scnl, String separatorText) {
		return scnl.getStationCode() + separatorText + scnl.getChannelCode()
				+ separatorText + scnl.getNetworkCode() + separatorText
				+ scnl.getLocationCode();
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
			return getUniqueId().equals(((IStaChaNetLoc) obj).getUniqueId());
		}
		return false;
	}

	/**
	 * Get the unique identifier.
	 * 
	 * @return the unique identifier.
	 */
	public String getUniqueId() {
		return getUniqueId(this, UNIQUE_ID_SEPARATOR_TEXT);
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
		return getUniqueId().hashCode();
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
	public String trim(String s) {
		if (s == null) {
			return EMPTY_STRING;
		}
		return s.trim();
	}
}
