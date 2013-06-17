//RequestInfoIntf.java:  Defines method(s) for fetching channels information
//                       and requesting messages.
//
//  9/29/2009 -- [ET]
//

package com.isti.slinkutil;

import java.util.Set;

/**
 * Interface RequestInfoIntf defines method(s) for fetching channels
 * information and requesting messages.
 */
public interface RequestInfoIntf
{
  /**
   * Gets a set of SCNL objects for all digitizer channels.
   * @return a set of 'StaChaNetLoc' objects for all digitizer
   * channels.
   */
  public Set getChannelSCNLSet();

  /**
   * Requests 'miniSEED' messages from the cache, starting with the
   * given message number and filtered by the begin and end time.
   * @param msgNumVal starting message number or -1 if any.
   * @param beginTime the begin time or null if none.
   * @param endTime the end time or null if none.
   * @return An array of 'MiniSeedMsgHldr' objects.
   */
  public MiniSeedMsgHldr [] requestMessages(
      int msgNumVal, SLinkTime beginTime, SLinkTime endTime);

  /**
   * Generates the response to the INFO command with the given "level"
   * argument string.
   * @param levelStr "level" argument string (i.e., "ID"), or null for
   * none.
   * @return A new 'MiniSeedMsgHldr' object containing the response,
   * or null if the "level" argument string is invalid.
   */
  public MiniSeedMsgHldr generateInfoResponse(String levelStr);
}
