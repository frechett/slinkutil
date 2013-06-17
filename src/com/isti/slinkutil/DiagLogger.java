//DiagLogger.java:  Generates periodic diagnostic memory and cache-info
//                  debug log outputs.
//
//  1/29/2010 -- [ET]
//

package com.isti.slinkutil;

/**
 * Class DiagLogger generates periodic diagnostic memory and cache-info
 * debug log outputs.
 */
public class DiagLogger extends NotifyThread
{
  protected final long reportIntervalMSecs;
  protected final MiniSeedMsgCache miniSeedMsgCacheObj;
  protected static final Runtime systemRuntimeObj = Runtime.getRuntime();
  protected static final String msgPromptStr = "DiagLogger:  ";

  /**
   * Creates a diagnostic logger thread.
   * @param intervalSecs number of seconds to delay between reports.
   * @param miniSeedMsgCacheObj message-cache object.
   */
  public DiagLogger(int intervalSecs, MiniSeedMsgCache miniSeedMsgCacheObj)
  {
    super("DiagLogger");          //set thread name
    setDaemon(true);              //mark as daemon thread
    reportIntervalMSecs = intervalSecs * 1000L;
    this.miniSeedMsgCacheObj = miniSeedMsgCacheObj;
  }

  /**
   * Executing method for thread.
   */
  public void run()
  {
    int numMsgs;
    while(true)
    {  //loop until thread terminated
      waitForNotify(reportIntervalMSecs);   //delay between reports
      if(isTerminated())          //if thread terminated
        break;                    //then exit loop
      try
      {
        final long totalVal = systemRuntimeObj.totalMemory();
        final long freeVal = systemRuntimeObj.freeMemory();
        LogMgr.usrMsgDebug(msgPromptStr + "Memory:  total=" + totalVal +
                      ", free=" + freeVal + ", diff=" + (totalVal-freeVal));
      }
      catch(Exception ex)
      {  //error fetching memory information
        LogMgr.usrMsgDebug(msgPromptStr +
                               "Error fetching memory information:  " + ex);
      }
      try
      {
        if((numMsgs=miniSeedMsgCacheObj.getNumMessages()) > 0)
        {  //cache not empty
          LogMgr.usrMsgDebug(msgPromptStr + "Cache:  messageCount=" +
                                             numMsgs + ", firstMsgTime = " +
                          miniSeedMsgCacheObj.getFirst().getStartTimeObj() +
                                                        ", lastMsgTime = " +
                           miniSeedMsgCacheObj.getLast().getStartTimeObj());
        }
        else
          LogMgr.usrMsgDebug(msgPromptStr + "Cache:  messageCount=0");
      }
      catch(Exception ex)
      {  //error fetching cache information
        LogMgr.usrMsgDebug(msgPromptStr +
                                "Error fetching cache information:  " + ex);
      }
    }
  }
}
