//NotifyEventQueue.java:  A FIFO event queue with notify-thread support.
//
// 10/15/2008 -- [ET]
//

package com.isti.slinkutil;

import java.util.Vector;

/**
 * Class NotifyEventQueue is a FIFO event queue with notify-thread support.
 * Subclasses need to define a 'run()' method with a processing loop that
 * checks 'finishRunning()' and uses 'waitForEvent()' to retrieve events.
 */
public abstract class NotifyEventQueue implements Runnable
{
    /** Fifo event queue implemented with a vector. */
  protected Vector m_queue = new Vector();

  private Thread m_thread = null;
  private boolean m_daemonFlag = false;
  private final String m_id;
  protected final Object m_stateLock = new byte[0];
    /** Should this thread stop running. */
  private boolean m_isRunning = false;
    /** Should this thread finish pending work and stop running. */
  private boolean m_finishWork = false;
    /** Maximum queue size, or 0 for no limit. */
  private int m_maxQueueSize = 0;


  /**
   * Class NotifyEventQueue is a FIFO event queue with notify-thread
   * support.
   * @param idStr Identification string for this queue and its processing
   * thread.
   */
  public NotifyEventQueue(String idStr)
  {
    m_id = idStr;
  }

  /**
   * Marks this queue's thread as either a daemon thread or a user thread.
   * The Java Virtual Machine exits when the only threads running are all
   * daemon threads.  This method must be called before the thread is
   * started (via the 'startThread()' method).
   * @param flgVal true for a daemon thread; false for user thread.
   */
  public void setDaemonThread(boolean flgVal)
  {
    m_daemonFlag = flgVal;
  }

  /**
   * Sets the maximum queue size allowed.  When the maximum queue size
   * is reached, the oldest entries will be removed as new entries are
   * pushed onto the queue.
   * @param sizeVal maximum queue size, or 0 for no limit.
   */
  public void setMaxQueueSize(int sizeVal)
  {
    synchronized(m_queue)
    {
      m_maxQueueSize = sizeVal;
    }
  }

  /**
   * Returnss the maximum queue size allowed.
   * @return The maximum queue size, or 0 if no limit.
   */
  public int getMaxQueueSize()
  {
    synchronized(m_queue)
    {
      return m_maxQueueSize;
    }
  }

  /**
   * Starts the queue's processing thread.
   */
  public void startThread()
  {
    synchronized(m_stateLock)
    {
      setRunning(true);
      m_thread = new Thread(this,m_id);
      m_thread.setDaemon(m_daemonFlag);
      m_thread.start();
    }
  }

  /**
   * Stops the queue's processing thread.
   */
  public void stopThread()
  {
    synchronized(m_stateLock)
    {
      m_thread = null;
      setRunning(false);
    }
    // wake the thread up if waiting for an event
    notifyThread();
  }

  /**
   * Pushes an event object into the queue.
   * @param event the event object to use.
   * @return false if the oldest object in the queue was removed (because
   * of the maximum-queue-size limit); true if not.
   */
  public boolean pushEvent(Object event)
  {
    synchronized(m_queue)
    {
      final boolean rmFlag;
              //if queue limit setup and >= limit then remove oldest entry:
      if(rmFlag = (m_maxQueueSize > 0 && m_queue.size() >= m_maxQueueSize))
        m_queue.remove(0);
      m_queue.add(event);         //add event-object to Vector
      m_queue.notifyAll();        //notify waiting thread
      return !rmFlag;             //return false if oldest entry removed
    }
  }

  /**
   * Pushes an event object back into the queue at location 0.
   * @param event the event object to use.
   * @return true if the event object was pushed into the queue;
   * false if not (because of the maximum-queue-size limit).
   */
  public boolean pushEventBackNoNotify(Object event)
  {
    synchronized(m_queue)
    {
      if(m_maxQueueSize <= 0 || m_queue.size() < m_maxQueueSize)
      {  //no queue limit setup or queue size is less than limit
        m_queue.add(0,event);     //put object back into queue
        return true;              //indicate pushed OK
      }
      return false;               //indicate queue full
    }
  }

  /**
   * Pushes an event object into the queue but don't issue any
   * notifications.
   * @param event the event object to use.
   * @return false if the oldest object in the queue was removed (because
   * of the maximum-queue-size limit); true if not.
   */
  public boolean pushEventNoNotify(Object event)
  {
    synchronized(m_queue)
    {
      final boolean rmFlag;
              //if queue limit setup and >= limit then remove oldest entry:
      if(rmFlag = (m_maxQueueSize > 0 && m_queue.size() >= m_maxQueueSize))
        m_queue.remove(0);
      m_queue.add(event);         //add event-object to Vector
      return !rmFlag;             //return false if oldest entry removed
    }
  }

  /**
   * Pulls an event from the queue.  If the queue is empty then this
   * method will return null.
   * @return the event object, or null if none are available.
   */
  public Object pullEvent()
  {
    synchronized(m_queue)
    {
      if(m_queue.size() == 0)
      {
        return null;
      }
      return m_queue.remove(0);
    }
  }

  /**
   * Returns the number of event objects in the queue.
   * @return the number of event objects in the queue.
   */
  public int getQueueSize()
  {
    return m_queue.size();
  }

  /**
   * Indicates if the event queue is empty.
   * @return true if the queue is empty; false if not.
   */
  public boolean isEmpty()
  {
    return (m_queue.size() == 0);
  }

  /**
   * Indicates if the event queue is full (as per the limit set via
   * the 'setMaxQueueSize()' method.)
   * @return true if the event queue is full; false if not.
   */
  public boolean isQueueFull()
  {
    return (m_maxQueueSize > 0 && m_queue.size() >= m_maxQueueSize);
  }

  /**
   * Determines if this queue's thread has been stopped.
   * @return false if 'stopThread()' has been called; true if not.
   */
  public boolean isRunning()
  {
    synchronized(m_stateLock)
    {
      return (m_isRunning && m_thread != null && m_thread.isAlive());
    }
  }

  /**
   * Sets the running status of the queue's thread.
   * @param value true for running; false for stopped.
   */
  protected void setRunning(final boolean value)
  {
    synchronized(m_stateLock)
    {
      m_isRunning = value;
    }
  }

  /**
   * Determines if 'finishWorkAndStopThread()' has been called.
   * @return true if 'finishWorkAndStopThread()' has been called;
   * false if not.
   */
  protected boolean shouldFinishWork()
  {
    synchronized(m_stateLock)
    {
      return m_finishWork;
    }
  }

  /**
   * Notifies our event processing thread that the queue has events
   * to process.
   * Invokes notify() on the queue object.
   */
  public void notifyThread()
  {
    synchronized(m_queue)
    {
      m_queue.notifyAll();
    }
  }

  /**
   * Waits for the queue to have events.
   * Invokes wait() on the queue object if running.
   * @return  Event if available, null otherwise.
   */
  public Object waitForEvent()
  {
    return waitForEvent(0);
  }

  /**
   * Waits for the queue to have events.
   * Invokes wait() on the queue object if running.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread-notify, or 0 to wait indefinitely.
   * @return  Event if available, null otherwise.
   */
  public Object waitForEvent(long waitTimeMs)
  {
    return checkForEvent(waitTimeMs) ? pullEvent() : null;
  }

  /**
   * Waits for a thread-notify on the queue, up to the given number
   * of milliseconds.
   * Invokes wait() on the queue object if running.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread-notify, or 0 to wait indefinitely.
   * @return The number of elements in the queue.
   */
  public int waitForNotify(long waitTimeMs)
  {
    // synchronize on the queue
    synchronized(m_queue)
    {
      if(isRunning())
      {
        try
        {
          m_queue.wait(waitTimeMs);
        }
        catch(final InterruptedException iex)
        {
            // ignore
        }
      }
      return m_queue.size();
    }
  }

  /**
   * Waits for a thread-notify on the queue.
   * Invokes wait() on the queue object if running.
   * @return The number of elements in the queue.
   */
  public int waitForNotify()
  {
    return waitForNotify(0);
  }

  /**
   * Waits for the queue to have events.
   * Invokes wait() on the queue object if running.
   * @return true if the queue is not empty, false if empty..
   */
  public boolean checkForEvent()
  {
    return checkForEvent(0);
  }

  /**
   * Waits for the queue to have events.
   * Invokes wait() on the queue object if running.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread-notify, or 0 to wait indefinitely.
   * @return true if the queue is not empty, false if empty..
   */
  public boolean checkForEvent(long waitTimeMs)
  {
    // synchronize on the queue
    synchronized(m_queue)
    {
      if ( m_queue.isEmpty() && isRunning() )
      {
        try
        {
          m_queue.wait(waitTimeMs);
        }
        catch ( final InterruptedException iex )
        {
            // ignore
        }
      }
      return !(isEmpty());
    }
  }

  /**
   * Indicates if processing thread should exit.
   * @return true if processing thread should exit; false if not.
   */
  public boolean finishRunning()
  {
    if(shouldFinishWork())
    {
      synchronized(m_queue)
      {
        if(m_queue.isEmpty())
        {
          return true;
        }
      }
    }
    return !isRunning();
  }

  /**
   * Finishes pending work and stops the thread.
   */
  public void finishWorkAndStopThread()
  {
    synchronized(m_stateLock)
    {
      m_finishWork = true;
    }
         // wake the thread up if waiting for an event
    notifyThread();
  }

  /**
   * Clears all events from the queue.
   */
  public void clearEvents()
  {
    synchronized(m_queue)
    {
      m_queue.clear();
    }
  }

  /**
   * Returns the queue name.
   * @return The queue name.
   */
  public final String toString()
  {
    return m_id;
  }
}
