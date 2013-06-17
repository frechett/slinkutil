//AbstractMiniSeedMsgCache.java:  Defines an abstract miniSEED message cache.
//
//9/16/2009 -- [KF]  Initial version.
// 2/1/2010 -- [ET]  Added 'synchronized' to declaration for
//                   'requestMessages()' method; added queuing via
//                   'AddMsgToCacheQueue' implementation; modified
//                   'requestMessages()' method to use binary searches.
//

package com.isti.slinkutil;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
* Class AbstractMiniSeedMsgCache defines an abstract miniSEED message cache. The 'add()' or
* 'removeMessages()' method should be called on a periodic basis to remove old
* objects from the cache.
*/
public abstract class AbstractMiniSeedMsgCache implements IMiniSeedMsgCache {
  /** The default wait time in milliseconds. */
  public static final long DEFAULT_WAIT_TIME_MS = 60000;

  /** The wait time in milliseconds. */
  private final long waitTimeMs;

  /**
   * Create the abstract miniSEED message cache.
   * @param waitTimeMs the wait time in milliseconds. This can be set so that
   * messages are removed even if no messages are added.
   */
  public AbstractMiniSeedMsgCache(long waitTimeMs) {
    this.waitTimeMs = waitTimeMs;
  }

  /**
   * Class AddMsgToCacheQueue implements the add-to-cache operation.
   */
  private class AddMsgToCacheQueue extends NotifyEventQueue {
    // local prompt for user messages:
    private static final String QUEUE_NAME_STR = "AddMsgToCacheQueue";
    private static final String qMsgPromptStr = QUEUE_NAME_STR + ":  ";

    /**
     * Creates the queue.
     */
    public AddMsgToCacheQueue() {
      super(QUEUE_NAME_STR); // set queue/thread name
      setDaemonThread(true); // mark daemon thread
    }

    /**
     * Executing method for queue.
     */
    public void run() {
      if (LogMgr.isDebugLevel2()) { // debug-mask bit set; output debug message:
        LogMgr.usrMsgDebug(qMsgPromptStr + "Add-message queue thread started");
      }
      try {
        Object obj;
        while (!finishRunning()) { // loop until thread is terminated
          if (!isEmpty()) { // queue contains objects
            if ((obj = pullEvent()) instanceof MiniSeedMsgHldr) {
              // mini-seed message object found in queue
              synchronized (AbstractMiniSeedMsgCache.this) {
                // thread-synchronize to outer-class instance
                removeMessages(); // remove messages if needed
                saveMessage((MiniSeedMsgHldr) obj); // add msg to queue
              }
            } else if (obj != null) { // unexpected object found in queue
              LogMgr
                  .usrMsgWarning(qMsgPromptStr
                      + "Unexpected object type found in queue:  "
                      + obj.getClass());
              removeMessages(); // remove messages if needed
            }
          } else { // queue empty
            removeMessages(); // remove messages if needed
            // wait for thread notify
            waitForNotify(waitTimeMs);
          }
        }
      } catch (Exception ex) { // some kind of exception error; log it
        LogMgr.usrMsgWarning(qMsgPromptStr + "Exception error in thread:  "
            + ex);
        LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
      }
      if (LogMgr.isDebugLevel2()) { // debug-mask bit set; output debug message:
        LogMgr.usrMsgDebug(qMsgPromptStr + "Add-message queue thread stopped");
      }
    }
  }

  /**
   * Class MsgHldrListComparator implements a comparator for use with a
   * list of 'MiniSeedMsgHldr' objects.  The comparison is against a
   * 'Long' object containing a message-start-time value.
   */
  public static class MsgHldrListComparator implements Comparator {
    /**
     * Compares the two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     * @param obj1 'MiniSeedMsgHldr' or 'Long' object to be compared.
     * @param obj2 'MiniSeedMsgHldr' or 'Long' object to be compared.
     * @return A negative integer, zero, or a positive integer as the
     *           first argument is less than, equal to, or greater than the
     *           second.
     * @throws ClassCastException if the arguments' types prevent them from
     *           being compared by this Comparator.
     */
    public int compare(Object obj1, Object obj2) {
      return (int) (((Number) obj1).longValue() - ((Number) obj2).longValue());
    }
  }

  /** The empty message array. */
  private static final MiniSeedMsgHldr[] emptyMsgArray = new MiniSeedMsgHldr[0];

  /** Comparator used by 'getListIndexForTimeVal()' method. */
  private static final MsgHldrListComparator msgHldrListCompObj = new MsgHldrListComparator();

  /**
   * Get the empty message array.
   * @return the empty message array.
   */
  public static MiniSeedMsgHldr[] getEmptyMsgArray() {
    return emptyMsgArray;
  }

  /**
   * Returns the index of the message in the list for the given time value.
   * @param listObj list of 'MiniSeedMsgHldr' objects to search.
   * @param timeVal time value, in milliseconds since 1/1/1970.
   * @param firstMatchFlag true to return the index of the first message
   * with a time value greater than or equal to the given time value;
   * false to return the index after the last message with a time value
   * greater than or equal to the given time value.
   * @return The index value.
   */
  public static int getListIndexForTimeVal(List listObj, long timeVal,
      boolean firstMatchFlag) {
    int idx; // search for time value:
    if ((idx = Collections.binarySearch(listObj, new Long(timeVal),
        msgHldrListCompObj)) < 0) { // not exact match to time value
      idx = -idx - 1; // convert insertion-point value
    } else { // exact match to time value found; check adjacent items
      if (firstMatchFlag) { // looking for "begin" item
        while (idx > 0
            && ((MiniSeedMsgHldr) (listObj.get(idx - 1))).getStartTimeMsVal() == timeVal) {
          // previous item is also exact time match
          --idx; // select previous item index
        }
      } else { // looking for "end" item + 1
        final int listSize = listObj.size();
        while (idx < listSize
            && ((MiniSeedMsgHldr) (listObj.get(idx))).getStartTimeMsVal() == timeVal) {
          // next item is also exact time match
          ++idx; // select next item index (+1)
        }
      }
    }
    return idx;
  }

  /** Add-message queue for cache. */
  private final AddMsgToCacheQueue addMsgToCacheQueueObj = new AddMsgToCacheQueue();

  /** The maximum message age or 0 if none. */
  private long maximumMessageAge = 0;

  /** The maximum message count or 0 if none. */
  private int maximumMessageCount = 0;

  /**
   * Adds the message to the cache add-message queue.
   * @param miniSeedMsg the message.
   */
  public void add(MiniSeedMsgHldr miniSeedMsg) {
    if (!addMsgToCacheQueueObj.pushEvent(miniSeedMsg))
      LogMgr.usrMsgWarning("AbstractMiniSeedMsgCache:  Add-message queue full");
  }

  /**
   * Return the maximum message age.
   * @return the maximum message age or 0 if none.
   */
  public synchronized long getMaximumMessageAge() {
    return maximumMessageAge;
  }

  /**
   * Return the maximum message count.
   * @return the maximum message count or 0 if none.
   */
  public synchronized int getMaximumMessageCount() {
    return maximumMessageCount;
  }

  /**
   * Saves the message.
   * @param miniSeedMsg the message.
   */
  public abstract void saveMessage(MiniSeedMsgHldr miniSeedMsg);

  /**
   * Set the maximum message age.
   * @param maximumMessageAge the maximum message age in ms or 0 if none.
   */
  public synchronized void setMaximumMessageAge(long maximumMessageAge) {
    this.maximumMessageAge = maximumMessageAge;
    removeMessages(); // remove messages if needed
  }

  /**
   * Set the maximum message count.
   * @param maximumMessageCount the maximum message count or 0 if none.
   */
  public synchronized void setMaximumMessageCount(int maximumMessageCount) {
    this.maximumMessageCount = maximumMessageCount;
    // also use value for add-message-queue maximum:
    addMsgToCacheQueueObj.setMaxQueueSize(maximumMessageCount);
    removeMessages(); // remove messages if needed
  }

  /**
   * Starts the add-message-queue processing thread for the cache.
   */
  public void startProcessingThread() {
    addMsgToCacheQueueObj.startThread();
  }

  /**
   * Stops the add-message-queue processing thread for the cache.
   */
  public void stopProcessingThread() {
    addMsgToCacheQueueObj.stopThread();
  }
}
