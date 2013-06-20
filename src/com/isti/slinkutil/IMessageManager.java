package com.isti.slinkutil;

/**
 * The message manager interface.
 * 
 * Messages are processed by calling the 'processMessage(IDigChannel,
 * IDataInfo)' method.
 * 
 * @see #processMessage(IDigChannel, IDataInfo)
 */
public interface IMessageManager {
	/**
	 * Get the current message number and increment the value.
	 * 
	 * @return the SeedLink message number.
	 */
	public IMessageNumber getMessageNumber();

	/**
	 * Processes the given data message, converting it to 'miniSEED' messages,
	 * storing them in the cache and delivering them to any current SeedLink
	 * connections.
	 * 
	 * @param dChanObj
	 *            digitizer-channel object to be associated with the message
	 *            data.
	 * @param dataInfo
	 *            the data information.
	 */
	public void processMessage(IDigChannel dChanObj, IDataInfo dataInfo);

	/**
	 * Processes the given data message, converting it to 'miniSEED' messages,
	 * storing them in the cache and delivering them to any current SeedLink
	 * connections.
	 * 
	 * @param scnlObj
	 *            the SCNL object.
	 * @param dataInfo
	 *            the data information.
	 */
	public void processMessage(IStaChaNetLoc scnlObj, IDataInfo dataInfo);
}
