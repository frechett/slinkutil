package com.isti.slinkutil;

/**
 * The miniSEED message cache. The 'add()' or
* 'removeMessages()' method should be called on a periodic basis to remove old
* objects from the cache.
 */
public interface IMiniSeedMsgCache {
  /**
   * Adds the message to the cache add-message queue.
   * @param miniSeedMsg the message.
   */
  public void add(MiniSeedMsgHldr miniSeedMsg);

  /**
   * Removes messages if needed.
   * @return true if any messages were removed, false otherwise.
   */
  public boolean removeMessages();

  /**
   * Requests 'miniSEED' messages from the cache, starting with the
   * given message number and filtered by the begin and end time.
   * @param msgNumVal starting message number or -1 if any.
   * @param beginTime the begin time or null if none.
   * @param endTime the end time or null if none.
   * @return An array of 'MiniSeedMsgHldr' objects.
   */
  public MiniSeedMsgHldr[] requestMessages(int msgNumVal, SLinkTime beginTime,
      SLinkTime endTime);

  /**
   * Set the maximum message age.
   * @param maximumMessageAge the maximum message age in ms or 0 if none.
   */
  public void setMaximumMessageAge(long maximumMessageAge);

  /**
   * Set the maximum message count.
   * @param maximumMessageCount the maximum message count or 0 if none.
   */
  public void setMaximumMessageCount(int maximumMessageCount);

  /**
   * Starts the add-message-queue processing thread for the cache.
   */
  public void startProcessingThread();

  /**
   * Stops the add-message-queue processing thread for the cache.
   */
  public void stopProcessingThread();
}
