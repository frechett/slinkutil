//MiniSeedMsgCache.java:  Defines the miniSEED message cache.
//
//  9/16/2009 -- [KF]  Initial version.
//   2/1/2010 -- [ET]  Added 'synchronized' to declaration for
//                     'requestMessages()' method; added queuing via
//                     'AddMsgToCacheQueue' implementation; modified
//                     'requestMessages()' method to use binary searches.
//

package com.isti.slinkutil;

import java.util.LinkedList;
import java.util.List;

/**
 * Class MiniSeedMsgCache defines the miniSEED message cache. The 'add()' or
 * 'removeMessages()' method should be called on a periodic basis to remove old
 * objects from the cache.
 */
public class MiniSeedMsgCache extends AbstractMiniSeedMsgCache {
  /** The message list. */
  private final LinkedList messageList = new LinkedList();

  /**
   * Create the miniSEED message cache.
   */
  public MiniSeedMsgCache() {
    this(DEFAULT_WAIT_TIME_MS);
  }

  /**
   * Create the miniSEED message cache.
   * @param waitTimeMs the wait time in milliseconds. This can be set so that
   * messages are removed even if no messages are added.
   */
  public MiniSeedMsgCache(long waitTimeMs) {
    super(waitTimeMs);
  }

  /**
   * Get the first miniSEED message.
   * @return the first miniSEED message.
   * @throws NoSuchElementException if this cache is empty.
   */
  public synchronized MiniSeedMsgHldr getFirst() {
    return (MiniSeedMsgHldr) messageList.getFirst();
  }

  /**
   * Return the index for the specified message number value.
   * @param msgNumVal the message number or -1 if any.
   * @return the index for the specified message number value or 0 if none.
   */
  private int getIndex(int msgNumVal) {
    if (msgNumVal >= 0) {
      for (int index = 0; index < getNumMessages(); index++) {
        if (getMessage(index).getMessageNumber() == msgNumVal)
          return index;
      }
    }
    return 0;
  }

  /**
   * Get the last miniSEED message.
   * @return the last miniSEED message.
   * @throws NoSuchElementException if this cache is empty.
   */
  public synchronized MiniSeedMsgHldr getLast() {
    return (MiniSeedMsgHldr) messageList.getLast();
  }

  /**
   * Return the message at the specified index.
   * @param index the index of the message to return.
   * @return the message.
   * @throws IndexOutOfBoundsException if the specified index is out of range.
   */
  public synchronized MiniSeedMsgHldr getMessage(int index) {
    return (MiniSeedMsgHldr) messageList.get(index);
  }

  /**
   * Returns the number of messages in the cache.
   * @return the number of messages in the cache.
   */
  public synchronized int getNumMessages() {
    return messageList.size();
  }

  /**
   * Removes messages if needed.
   * @return true if any messages were removed, false otherwise.
   */
  public synchronized boolean removeMessages() {
    boolean removedFlag = false;
    if (getNumMessages() > 0) {
      // if maximum message age exists
      if (getMaximumMessageAge() > 0) {
        // get current time in milliseconds:
        final long currentTime = System.currentTimeMillis();
        final long removeTime = currentTime - getMaximumMessageAge();
        while (getFirst().getTimeCreated() <= removeTime) {
          messageList.removeFirst();
          removedFlag = true;
          if (getNumMessages() <= 0) // exit if no more messages
            break;
        }

        // while maximum message count exists and cache is at or over limit
        while (getMaximumMessageCount() > 0
            && getNumMessages() >= getMaximumMessageCount()) {
          messageList.removeFirst();
          removedFlag = true;
        }
      }
    }
    return removedFlag;
  }

  /**
   * Requests 'miniSEED' messages from the cache, starting with the
   * given message number and filtered by the begin and end time.
   * @param msgNumVal starting message number or -1 if any.
   * @param beginTime the begin time or null if none.
   * @param endTime the end time or null if none.
   * @return An array of 'MiniSeedMsgHldr' objects.
   */
  public synchronized MiniSeedMsgHldr[] requestMessages(int msgNumVal,
      SLinkTime beginTime, SLinkTime endTime) {
    if (LogMgr.isDebugLevel2()) {
      LogMgr.usrMsgDebug("CACHE_DEBUG:  Entered 'requestMessages()'");
      LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Params:  msgNumVal="
          + msgNumVal + ", beginTime=" + beginTime + ", endTime=" + endTime);
      LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  First cache msg:  "
          + getFirst());
      LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Last cache msg:  " + getLast());
    }
    if (getNumMessages() <= 0) // if cache empty then
      return getEmptyMsgArray(); // no messages
    if (LogMgr.isDebugLevel2())
      LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Getting index for msgNum");
    // start with the message number or first if none
    int index = getIndex(msgNumVal);
    // get list starting with index (or entire list if none):
    List subList = (index > 0) ? messageList.subList(index, getNumMessages())
        : messageList;
    if (beginTime != null || endTime != null) { // begin-time or end-time was
                                                // given; get time values
      final long beginTimeMsVal = (beginTime != null) ? beginTime.getTime()
          : Long.MIN_VALUE;
      final long endTimeMsVal = (endTime != null) ? endTime.getTime()
          : Long.MAX_VALUE;
      if (LogMgr.isDebugLevel2()) {
        LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Getting first/last "
            + "time vals");
      }
      // get the first and last time
      final long firstTimeMsVal = getFirst().getStartTimeMsVal();
      final long lastTimeMsVal = getLast().getStartTimeMsVal();
      // if end time is before the first time or the begin time
      // is after the last time then return empty array
      if (endTimeMsVal < firstTimeMsVal || beginTimeMsVal > lastTimeMsVal)
        return getEmptyMsgArray(); // no messages
      int fromIndex = -1; // default to none
      int toIndex = getNumMessages(); // default to last index
      // if any message number, begin time is less than or equal to
      // the first time or end time is after or equal to the last time
      if (index != 0 || beginTimeMsVal > firstTimeMsVal
          || endTimeMsVal < lastTimeMsVal) { // message number given and not all
                                             // msgs between 'begin' and 'end'
        if (LogMgr.isDebugLevel2()) {
          LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Finding messages; "
              + "index=" + index + ", beginTimeMsVal=" + beginTimeMsVal
              + ", endTimeMsVal=" + endTimeMsVal + ", firstTimeMsVal="
              + firstTimeMsVal + ", lastTimeMsVal=" + lastTimeMsVal
              + ", subList.size=" + subList.size());
        }
        // get index for first msg >= begin-time value:
        fromIndex = (beginTime != null) ? getListIndexForTimeVal(subList,
            beginTimeMsVal, true) : 0;
        // get index after last msg <= end-time value:
        toIndex = (endTime != null) ? getListIndexForTimeVal(subList,
            endTimeMsVal, false) : subList.size();
        if (LogMgr.isDebugLevel2()) {
          LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Finished finding "
              + "messages");
        }
        if (fromIndex >= toIndex) // if no messages between from/to
          return getEmptyMsgArray(); // then return no messages
        if (LogMgr.isDebugLevel2()) {
          LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Building return "
              + "subList; fromIndex=" + fromIndex + ", toIndex=" + toIndex);
        }
        // 'from' not at beginning or 'to' not at end; create sub-list:
        if (fromIndex > 0 || toIndex < subList.size())
          subList = messageList.subList(fromIndex, toIndex);
      }
    }
    if (LogMgr.isDebugLevel2()) {
      LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Building return array; "
          + "size=" + subList.size());
    }
    final MiniSeedMsgHldr[] msgArr = (MiniSeedMsgHldr[]) (subList
        .toArray(getEmptyMsgArray()));
    if (LogMgr.isDebugLevel2()) {
      if (msgArr.length > 0) {
        LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  First ret msg [0]:  "
            + msgArr[0]);
        LogMgr.usrMsgDebug("CACHE_DEBUG_reqMsgs:  Last ret msg ["
            + (msgArr.length - 1) + "]:  " + msgArr[msgArr.length - 1]);
      }
      LogMgr.usrMsgDebug("CACHE_DEBUG:  Exiting 'requestMessages()'");
    }
    return msgArr;
  }

  /**
   * Saves the message.
   * @param miniSeedMsg the message.
   */
  public void saveMessage(MiniSeedMsgHldr miniSeedMsg) {
    messageList.add(miniSeedMsg);
  }
}
