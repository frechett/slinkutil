package com.isti.slinkutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.isti.slinkutil.mseed.MiniSeedGenerator;

/**
 * Class AbstractMessageManager manages the caching and delivery of 'miniSEED'
 * messages. Classes that extend this class must implement the
 * 'getMessageNumber()' and 'processMiniSeedMessage(MiniSeedMsgHldr)' methods.
 * 
 * Prior to processing messages the 'setModConfigParams' method must be called.
 * 
 * @see #setModConfigParams(IConfigParams)
 */
public abstract class AbstractMessageManager implements IMessageManager {
	/** The map of 'MiniSeedGenerator' objects with 'IStaChaNetLoc' key. */
	private final Map miniSeedGeneratorMap = new HashMap();

	/** The configuration parameters. */
	private IConfigParams modConfigParamsObj = null;

	/**
	 * Gets a set of SCNL objects for all digitizer channels.
	 * 
	 * @return a set of 'StaChaNetLoc' objects for all digitizer channels.
	 */
	public Set getChannelSCNLSet() {
		final Set digChannels = miniSeedGeneratorMap.keySet();
		final HashSet scnlSet = new HashSet(digChannels.size());
		final Iterator it = digChannels.iterator();
		while (it.hasNext()) {
			scnlSet.add(((IDigChannel) it.next()).getStaChaNetLocObj());
		}
		return scnlSet;
	}

	/**
	 * Return the miniSEED generator.
	 * 
	 * @param scnlObj
	 *            the SCNL object.
	 * @return the miniSEED generator.
	 */
	protected MiniSeedGenerator getMiniSeedGenerator(IStaChaNetLoc scnlObj) {
		MiniSeedGenerator miniSeedGenerator = (MiniSeedGenerator) (miniSeedGeneratorMap
				.get(scnlObj));
		if (miniSeedGenerator == null) {
			miniSeedGenerator = new MiniSeedGenerator(scnlObj,
					getModConfigParams().getCodec(), this);
			miniSeedGeneratorMap.put(scnlObj, miniSeedGenerator);
		}
		return miniSeedGenerator;
	}

	/**
	 * Get the configuration parameters.
	 * 
	 * @return the configuration parameters.
	 */
	public IConfigParams getModConfigParams() {
		return modConfigParamsObj;
	}

	/**
	 * Determines if the given channel is selected.
	 * 
	 * @param channelNumber
	 *            index number for channel (1-based).
	 * @param channelIdStr
	 *            ID string for channel.
	 * @param stationNameStr
	 *            station name for channel.
	 * @param networkNameStr
	 *            network name for channel.
	 * @param locationCodeStr
	 *            location code for channel.
	 * @return true if the given channel is selected; false otherwise.
	 */
	public boolean isSelectedChannel(int channelNumber, String channelIdStr,
			String stationNameStr, String networkNameStr, String locationCodeStr) {
		// if the configuration parameters have not been entered
		if (getModConfigParams() == null) {
			LogMgr.usrMsgWarning("AbstractMessageManager:  Module configuration "
					+ "parameters not setup; cannot determine if channel is selected");
			return false;
		}
		if (getModConfigParams().getSelectedChannelNames() == null)
			return true;
		boolean selectFlag = false;
		Iterator it = getModConfigParams().getSelectedChannelNames().iterator();
		while (it.hasNext()) {
			selectFlag = new ChannelMatcher(it.next().toString()).isMatch(
					channelNumber, channelIdStr, stationNameStr,
					networkNameStr, locationCodeStr);
			if (selectFlag)
				break;
		}
		return selectFlag;
	}

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
	public void processMessage(IDigChannel dChanObj, IDataInfo dataInfo)

	{
		if (!dChanObj.isSelected()) { // no match to 'ChannelNames' parameter
										// entries
			if (LogMgr.isDebugLevel4()) { // debug-mask bit is set; output debug
											// message
				LogMgr.usrMsgDebug("AbstractMessageManager.processMessage:  Channel is not selected ("
						+ dChanObj + "); message not delivered");
			}
			return;
		}
		processMessage(dChanObj.getStaChaNetLocObj(), dataInfo);
	}

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
	public void processMessage(IStaChaNetLoc scnlObj, IDataInfo dataInfo) {
		// if the configuration parameters have not been entered
		if (getModConfigParams() == null) {
			LogMgr.usrMsgWarning("AbstractMessageManager:  Module configuration "
					+ "parameters not setup; cannot process message");
			return;
		}

		// get the list of of 'MiniSeedMsgHldr' objects
		try {
			final MiniSeedGenerator miniSeedGenerator = getMiniSeedGenerator(scnlObj);
			final List miniSeedMsgList = miniSeedGenerator
					.getMiniSeedMessages(dataInfo);
			if (miniSeedMsgList.size() > 0)
				processMiniSeedMessages(miniSeedMsgList);
		} catch (Exception ex) {
			removeMiniSeedGenerator(scnlObj);
			LogMgr.usrMsgWarning("AbstractMessageManager error processing message:  "
					+ ex);
			LogMgr.usrMsgWarning("AbstractMessageManager:  SCNL=\"" + scnlObj
					+ "\", startTime=" + dataInfo.getFirstTimeStamp()
					+ ", endTime=" + dataInfo.getLastTimeStamp()
					+ ", numSamples=" + dataInfo.getNumSamples());
			LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
		}
	}

	/**
	 * Processes the miniSEED message.
	 * 
	 * @param miniSeedMsg
	 *            the miniSEED message.
	 */
	protected abstract void processMiniSeedMessage(MiniSeedMsgHldr miniSeedMsg);

	/**
	 * Processes the miniSEED messages.
	 * 
	 * @param miniSeedMsgList
	 *            the list of 'MiniSeedMsg' objects.
	 */
	protected void processMiniSeedMessages(List miniSeedMsgList) {
		Object msgsListObj;
		for (int index = 0; index < miniSeedMsgList.size(); index++) {
			msgsListObj = miniSeedMsgList.get(index);
			if (msgsListObj instanceof MiniSeedMsgHldr) {
				processMiniSeedMessage((MiniSeedMsgHldr) msgsListObj);
			}
		}
	}

	/**
	 * Remove the miniSEED generator.
	 * 
	 * @param scnlObj
	 *            the SCNL object.
	 */
	protected void removeMiniSeedGenerator(IStaChaNetLoc scnlObj) {
		miniSeedGeneratorMap.remove(scnlObj);
	}

	/**
	 * Set the configuration parameters.
	 * 
	 * @param modConfigParamsObj
	 *            the configuration parameters.
	 */
	public void setModConfigParams(IConfigParams modConfigParamsObj) {
		this.modConfigParamsObj = modConfigParamsObj;
	}
}
