//MessageManager.java:  Manages the caching and delivery of 'miniSEED'
//                      messages.
//
//  10/7/2009 -- [KF]  Initial version.
// 10/14/2009 -- [ET]  Added more debug output to 'processMessage()'
//                     method.
// 10/15/2009 -- [KF]  Changed 'miniSeedGeneratorMap' to use 'DigChannel' key.
//   2/1/2010 -- [ET]  Added 'getMiniSeedMsgCache()' method; added call to
//                     "miniSeedMsgCacheObj.stopProcessingThread()".
//  9/28/2010 -- [KF]  Changed the sequence number to be by channel.
// 10/27/2010 -- [ET]  Added "get/setTimeQualityValue()" methods.
//  9/23/2011 -- [KF]  Modified to enhance channel parameter.
//

package com.isti.slinkutil;

import com.isti.slinkutil.mseed.MiniSeedGenerator;

/**
 * Class SLinkMessageManager manages the caching and delivery of 'miniSEED'
 * messages.
 */
public class SLinkMessageManager extends AbstractMessageManager implements
    RequestInfoIntf {
  /** The message number. */
  private int messageNumber = 0;

  /** The miniSEED message cache. */
  private final IMiniSeedMsgCache miniSeedMsgCacheObj;

  // SeedLink clients manager for module:
  protected SLinkClientsMgr sLinkClientsMgrObj = null;

  /**
   * Create the message manager with the default miniSEED message cache.
   */
  public SLinkMessageManager() {
    this((IMiniSeedMsgCache) null);
  }

  /**
   * Create the message manager.
   * @param miniSeedMsgCacheObj the miniSEED message cache or null for the default.
   */
  public SLinkMessageManager(IMiniSeedMsgCache miniSeedMsgCacheObj) {
    if (miniSeedMsgCacheObj == null) {
      miniSeedMsgCacheObj = new MiniSeedMsgCache();
    }
    this.miniSeedMsgCacheObj = miniSeedMsgCacheObj;
  }

  /**
   * Generates the response to the INFO command with the given "level"
   * argument string.
   * @param levelStr "level" argument string (i.e., "ID"), or null for
   * none.
   * @return A new 'MiniSeedMsgHldr' object containing the response,
   * or null if the "level" argument string is invalid (or an error
   * occurred).
   */
  public MiniSeedMsgHldr generateInfoResponse(String levelStr) {
    try {
      if (levelStr == null || levelStr.length() <= 0
          || SLinkClientsMgr.INFOID_ARG_STR.equalsIgnoreCase(levelStr)) { // command
                                                                          // is
                                                                          // "INFO"
                                                                          // or
                                                                          // "INFO ID"
        return MiniSeedGenerator.createLogRecordMsg(
            sLinkClientsMgrObj.getInfoIdCmdResponseStr(), 0,
            new SLinkMessageNumber(0, SLinkClientsMgr.INFO_MSGNUM_STR));
      }
    } catch (Exception ex) { // some kind of exception error; log it
      LogMgr.usrMsgWarning("SLinkMessageManager:  Error generating 'INFO' "
          + "command response:  " + ex);
    }
    // command is not "INFO" or "INFO ID" (or error)
    return null; // indicate invalid argument (or error)
  }

  /**
   * Get the current message number and increment the value.
   * @return the SeedLink message number.
   */
  public IMessageNumber getMessageNumber() {
    if (messageNumber > SLinkMessageNumber.maxMsgNumVal) {
      messageNumber = SLinkMessageNumber.minMsgNumVal;
    }
    return new SLinkMessageNumber(messageNumber++);
  }

  /**
   * Returns the miniSEED message cache.
   * @return The miniSEED message cache object.
   */
  public IMiniSeedMsgCache getMiniSeedMsgCache() {
    return miniSeedMsgCacheObj;
  }

  /**
   * Processes the miniSEED message.
   * @param miniSeedMsg the miniSEED message.
   */
  protected void processMiniSeedMessage(MiniSeedMsgHldr miniSeedMsg) {
    // add the message to the cache
    miniSeedMsgCacheObj.add(miniSeedMsg);
    // deliver the message
    if (sLinkClientsMgrObj != null) {
      sLinkClientsMgrObj.deliverMessage(miniSeedMsg);
    }
  }

  /**
   * Requests 'miniSEED' messages from the cache, starting with the
   * given message number and filtered by the begin and end time.
   * @param msgNumVal starting message number or -1 if any.
   * @param beginTime the begin time or null if none.
   * @param endTime the end time or null if none.
   * @return An array of 'MiniSeedMsgHldr' objects.
   */
  public MiniSeedMsgHldr[] requestMessages(int msgNumVal, SLinkTime beginTime,
      SLinkTime endTime) {
    return miniSeedMsgCacheObj.requestMessages(msgNumVal, beginTime, endTime);
  }

  /**
   * Shuts down this message manager.
   */
  public void shutdown() {
    miniSeedMsgCacheObj.stopProcessingThread(); // stop queue-proc thread
  }

  /**
   * Starts up this message manager.
   * @param cParamObj module configuration parameters object to be used.
   * @param clientsMgrObj SeedLink clients manager to be used.
   */
  public void startup(IConfigParams cParamObj, SLinkClientsMgr clientsMgrObj) {
    if (cParamObj == null || clientsMgrObj == null)
      throw new NullPointerException("Null parameter(s)");
    setModConfigParams(cParamObj); // save config-parameters object
    sLinkClientsMgrObj = clientsMgrObj; // save clients-manager object
    miniSeedMsgCacheObj.setMaximumMessageAge(cParamObj.getMaxCacheAge());
    miniSeedMsgCacheObj.setMaximumMessageCount(cParamObj.getMaxCacheSize());
    miniSeedMsgCacheObj.startProcessingThread(); // start queue-proc thread
  }
}
