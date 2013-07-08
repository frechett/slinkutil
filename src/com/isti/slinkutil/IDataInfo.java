package com.isti.slinkutil;

/**
 * The data information.
 */
public interface IDataInfo {
	/**
	 * Get the time stamp of the first sample value measured in milliseconds
	 * since epoch.
	 * 
	 * @return a positive value or zero if no samples.
	 */
	public long getFirstTimeStamp();

	/**
	 * Get the time stamp of the last sample value measured in milliseconds
	 * since epoch.
	 * 
	 * @return a positive value or zero if no samples.
	 */
	public long getLastTimeStamp();

	/**
	 * Get the number of samples.
	 * 
	 * @return the number of samples.
	 */
	public int getNumSamples();

	/**
	 * Get the samples. This should either be an array of integer values or an
	 * object implementing the 'ISamples' interface.
	 * 
	 * @return the samples.
	 */
	public Object getSamples();
}
