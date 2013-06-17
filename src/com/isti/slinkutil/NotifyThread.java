//NotifyThread.java:  Extension of 'Thread' that adds wait/notify
//                    and terminate functionality.
//
// 10/15/2008 -- [ET]
//

package com.isti.slinkutil;

/**
 * Class NotifyThread extends 'Thread' to add wait/notify and terminate
 * functionality.
 */
public class NotifyThread extends Thread
{
  /** The default terminate wait time in milliseconds. */
  public static int DEFAULT_TERMINATE_WAIT_TIME = 100;
  /** Flag set true if the thread should be terminated. */
  protected boolean terminateFlag = false;
  /** For autonumbering anonymous threads. */
  private static int threadInitNumber = 0;
  /** Thread synchronization object used for 'wait' and 'notify'. */
  private final Object threadWaitSyncObject = new Object();
  /** Flag set true after 'notifyThread()' method called. */
  private boolean threadWaitNotifyFlag = false;
  /** Start sync object. */
  private final Object startSyncObj = new Object();
  /** Started flag: true if the thread was already started, false otherwise. */
  private boolean startedFlag = false;


  /**
   * Allocates a new <code>NotifyThread</code> object so that it has
   * <code>target</code> as its run object, has the specified
   * <code>name</code> as its name, and belongs to the thread group
   * referred to by <code>group</code>.
   * <p>
   * If <code>group</code> is <code>null</code>, the group is
   * set to be the same ThreadGroup as
   * the thread that is creating the new thread.
   *
   * <p>If there is a security manager, its <code>checkAccess</code>
   * method is called with the ThreadGroup as its argument.
   * This may result in a SecurityException.
   * <p>
   * If the <code>target</code> argument is not <code>null</code>, the
   * <code>run</code> method of the <code>target</code> is called when
   * this thread is started. If the target argument is
   * <code>null</code>, this thread's <code>run</code> method is called
   * when this thread is started.
   * <p>
   * The priority of the newly created thread is set equal to the
   * priority of the thread creating it, that is, the currently running
   * thread. The method <code>setPriority</code> may be used to
   * change the priority to a new value.
   * <p>
   * The newly created thread is initially marked as being a daemon
   * thread if and only if the thread creating it is currently marked
   * as a daemon thread. The method <code>setDaemon </code> may be used
   * to change whether or not a thread is a daemon.
   *
   * @param      group     the thread group.
   * @param      target   the object whose <code>run</code> method is called.
   * @param      name     the name of the new thread.
   * @exception  SecurityException  if the current thread cannot create a
   *               thread in the specified thread group.
   */
  public NotifyThread(ThreadGroup group, Runnable target, String name)
  {
    super(group,target,name);
  }

  /**
   * Allocates a new <code>IstiNotifyThread</code> object. This constructor
   * has the same effect as <code>IstiNotifyThread(null, target, name)</code>.
   *
   * @param   target   the object whose <code>run</code> method is called.
   * @param   name     the name of the new thread.
   */
  public NotifyThread(Runnable target, String name)
  {
    this(null, target, name);
  }

  /**
   * Allocates a new <code>NotifyThread</code> object. This constructor
   * has the same effect as <code>NotifyThread(null, null, name)</code>.
   *
   * @param   name   the name of the new thread.
   */
  public NotifyThread(String name)
  {
    this(null, null, name);
  }

  /**
   * Allocates a new <code>NotifyThread</code> object. This constructor has
   * the same effect as <code>NotifyThread(group, null, name)</code>
   *
   * @param      group   the thread group.
   * @param      name    the name of the new thread.
   * @exception  SecurityException  if the current thread cannot create a
   *               thread in the specified thread group.
   */
  public NotifyThread(ThreadGroup group, String name)
  {
    this(group, null, name);
  }

  /**
   * Returns (and increments) the next thread number for autonumbering
   * anonymous threads.
   * @return the next thread number.
   */
  protected static synchronized int nextThreadNum()
  {
    return threadInitNumber++;
  }

  /**
   * Starts this thread if the thread is not terminated and not already alive.
   */
  public void start()
  {
    synchronized(startSyncObj)
    {
      if (startedFlag)  //exit if the thread was already started
        return;
      startedFlag = true;
    }
    //if the thread is not terminated and not already alive then start thread
    if (!terminateFlag && !isAlive())
      super.start();
  }

  /**
   * Performs a thread-notify and terminates this thread if the thread
   * is not terminated and alive.
   */
  public void terminate()
  {
    if(!terminateFlag && isAlive())
    {    //thread is not terminated and is alive
      terminateFlag = true;            //set terminate flag
      notifyThread();                  //wake up 'waitforNotify()' method
      waitForTerminate();              //wait for thread to terminate
    }
  }

  /**
   * Tests if this thread is terminated.
   *
   * @return  <code>true</code> if this thread is terminated;
   *          <code>false</code> otherwise.
   */
  public boolean isTerminated()
  {
    return terminateFlag;
  }

  /**
   * Performs a thread wait.
   * If a notify has occurred since the last call to 'waitForNotify()'
   * or 'clearThreadWaitNotifyFlag()' then this method will return
   * immediately.
   */
  public void waitForNotify()
  {
    waitForNotify(0);
  }

  /**
   * Performs a thread wait, up to the given timeout value.
   * If a notify has occurred since the last call to 'waitForNotify()'
   * or 'clearThreadWaitNotifyFlag()' then this method will return
   * immediately.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread-notify, or 0 to wait indefinitely.
   * @return true if the wait-timeout value was reached; false if a
   * thread-notify, thread-interrupt or thread-terminate occurred.
   */
  public boolean waitForNotify(long waitTimeMs)
  {
    try
    {
      synchronized(threadWaitSyncObject)
      {  //grab thread synchronization lock object
        if(!isTerminated())
        {     //thread has not been terminated
          if(!threadWaitNotifyFlag)
          {   //'notifyThread()' method not called
                   //wait until specified time, notify or interrupt:
            threadWaitSyncObject.wait(waitTimeMs);
            if(!threadWaitNotifyFlag)  //if notify was not called then
              return true;             //indicate thread-wait finished
          }
          threadWaitNotifyFlag = false;
        }
      }
    }
    catch(InterruptedException ex)
    {    //thread was interrupted
    }
    return false;       //indicate thread-wait did not finish
  }

  /**
   * Performs a thread-notify.  This will "wake up" the 'waitForNotify()'
   * method.
   */
  public void notifyThread()
  {
    synchronized(threadWaitSyncObject)
    {    //grab thread synchronization lock object
      threadWaitNotifyFlag = true;          //indicate notify called
      threadWaitSyncObject.notifyAll();     //notify on object
    }
  }

  /**
   * Clears the thread-wait notify flag.  This will clear any previous
   * "notifies" set via the 'notifyThread()' method.
   */
  public void clearThreadWaitNotifyFlag()
  {
    synchronized(threadWaitSyncObject)
    {
      threadWaitNotifyFlag = false;
    }
  }

  /**
   * Determines if the thread was already started.
   * @return true if the thread was already started, false otherwise.
   */
  public boolean wasStarted()
  {
    return startedFlag;
  }

  /**
   * Waits (up to 100 milliseconds) for the thread to terminate.
   */
  public void waitForTerminate()
  {
    waitForTerminate(DEFAULT_TERMINATE_WAIT_TIME);
  }

  /**
   * Waits (up to 'waitTimeMs' milliseconds) for the thread to terminate.
   * @param waitTimeMs the maximum number of milliseconds to wait for
   * the thread to terminate, or 0 to wait indefinitely.
   */
  public void waitForTerminate(long waitTimeMs)
  {
    try { join(waitTimeMs); }  //wait for thread to terminate
    catch(InterruptedException ex) {}
  }

  /**
   * This method is deprecated because it will not be interrupted by
   * the 'notifyThread()' method.  The 'waitForNotify()' method should
   * be used instead for thread-sleep delays.
   * Causes the currently executing thread to sleep (temporarily cease
   * execution) for the specified number of milliseconds. The thread
   * does not lose ownership of any monitors.
   *
   * @param      millis   the length of time to sleep in milliseconds.
   * @exception  InterruptedException if another thread has interrupted
   *             the current thread.  The <i>interrupted status</i> of the
   *             current thread is cleared when this exception is thrown.
   * @see        java.lang.Object#notify()
   * @deprecated Use 'waitForNotify()' instead.
   */
  public static void sleep(long millis) throws InterruptedException
  {
    Thread.sleep(millis);
  }
}
