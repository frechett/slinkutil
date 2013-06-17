//SLinkClientsMgr.java:  Manages SeedLink-client connections.
//
//  9/29/2008 -- [ET]  Initial version.
//   2/3/2010 -- [ET]  Modified to show program version number as part
//                     of 'HELLO' response.
//

package com.isti.slinkutil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Class SLinkClientsMgr manages SeedLink-client connections.
 */
public class SLinkClientsMgr
{
  /** The library name. */
  public static final String NAME = "SLinkUtil";
  /** The version number. */
  public static final String VERSION = "0.34";
  /** The default SeedLink software string. */
  public static final String DEFAULT_SEEDLINK_SOFTWARE = "SeedLink v3.0 (2009.260)";
  private final String bindAddrStr;
  private final int bindPortNum;
  private final String stationIdStr;
  private final String networkIdStr;
  private final int maximumQueueSize;
  private final int messageRetryDelayMS;
  private final int maxNumConnections;
  private final RequestInfoIntf requestInfoObj;
  private final String seedlinkSoftware;
  private final String organization;
  private final String bindPortIdStr;
  private final String msgPromptStr;
  private final ClientAcceptorThread clientAcceptorThreadObj;
  private final Vector sLinkConnectionList = new Vector();
  private final long mgrStartupTimeMsVal;
  private final String mgrStartupTimeStr;
  private ServerSocket serverListenSocketObj = null;
  private Vector sLinkConnListCopy = new Vector();
  private boolean sLinkConnListChangedFlag = false;
  private final String infoIdCmdResponseStr;
  protected final SimpleDateFormat dateFormatterObj =
                             new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
    /** Level-argument string "ID" for INFO command. */
  public static final String INFOID_ARG_STR = "ID";
    /** Message "number" for INFO-command response ("INFO  "). */
  public static final String INFO_MSGNUM_STR = "INFO  ";

  /**
   * Get the organization.
   * @param name the program or library name.
   * @param version the version.
   * @param stationIdStr the station ID string.
   * @return the organization.
   */
  public static String getOrganization(String name, String version,
      String stationIdStr)  {
    return name + "_v" + version + stationIdStr;
  }

  /**
   * Creates and starts up a SeedLink-clients manager object.
   * @param bindAddrStr host address to be used when binding to the listen
   * port, or null for none.
   * @param bindPortNum port number to be used when binding to the listen
   * port.
   * @param stationIdStr station ID string for module.
   * @param networkIdStr network ID string for module.
   * @param maximumQueueSize maximum number of messages allowed to be stored
   * in each connection queue, or 0 for none.
   * @param messageRetryDelayMS number of milliseconds to wait after a
   * message-send failure before attemping a resend, or 0 to wait
   * indefinitely (until another message is queued).
   * @param maxNumConnections maximum number of simultaneous connections
   * allowed.
   * @param requestInfoObj source object for available-channels information
   * and message requests, or null for none.
   * @param seedlinkSoftware the seedlink software string used in the response
   * string for the "INFO ID" and "HELLO" commands or null for the default.
   * @param organization the organization string used in the response string for
   * the "INFO ID" and "HELLO" commands or null for the default.
   * @throws Exception if the manager cannot be started up because
   * the server listen socket cannot be opened.
   */
  public SLinkClientsMgr(String bindAddrStr, int bindPortNum,
             String stationIdStr, String networkIdStr, int maximumQueueSize,
             int messageRetryDelayMS, int maxNumConnections,
             RequestInfoIntf requestInfoObj,
             String seedlinkSoftware, String organization) throws Exception
  {
    this.bindAddrStr = bindAddrStr;
    this.bindPortNum = bindPortNum;
    this.stationIdStr = stationIdStr;
    this.networkIdStr = networkIdStr;
    this.maximumQueueSize = maximumQueueSize;
    this.messageRetryDelayMS = messageRetryDelayMS;
    this.maxNumConnections = maxNumConnections;
    this.requestInfoObj = requestInfoObj;
    if (seedlinkSoftware == null)
    {
      seedlinkSoftware = DEFAULT_SEEDLINK_SOFTWARE;
    }
    this.seedlinkSoftware = seedlinkSoftware;
    if (organization == null)
    {
      organization = getOrganization(NAME, VERSION, this.stationIdStr);
    }
    this.organization = organization;
                                  //setup ID text for threads:
    bindPortIdStr = ((bindAddrStr != null) ? (bindAddrStr+':') : "") +
                                                                bindPortNum;
              //ID string for this object:
    final String objIdStr = "SLinkClientsMgr";
    msgPromptStr = objIdStr + ":  ";        //prompt for log messages
    openAcceptorSocket();         //open server listen socket
              //create thread for accepting client connections:
    clientAcceptorThreadObj = new ClientAcceptorThread(objIdStr);
             //set startup-time variables for manager:
    long timeMsVal = 0;
    String timeStr = "";
    try
    {
      timeMsVal = System.currentTimeMillis();
      dateFormatterObj.setTimeZone(TimeZone.getTimeZone("GMT"));
      timeStr = dateFormatterObj.format(new Date(timeMsVal));
    }
    catch(Exception ex)
    {}        //if error then use blank value
    mgrStartupTimeMsVal = timeMsVal;        //epoch time
    mgrStartupTimeStr = timeStr;            //string representation
              //setup response string for "INFO ID" command:
    infoIdCmdResponseStr = "<?xml version=\"1.0\"?><seedlink software=\"" +
        seedlinkSoftware + "\" " +
                                    "organization=\"" + organization + "\" " +
                                 "started=\"" + mgrStartupTimeStr + "\" />";
    clientAcceptorThreadObj.start();        //startup acceptor thread
  }

  /**
   * Opens the server listen socket.
   * @throws Exception if the listen socket cannot be opened.
   */
  private void openAcceptorSocket() throws Exception
  {
    try
    {
      InetAddress netAddrObj;
      serverListenSocketObj = null;    //initialize handle to server socket
      if(LogMgr.isDebugLevel1())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr +
                          "Binding to listen port at " + bindPortIdStr);
      }
      if(bindAddrStr != null)
      {  //bind host-address was given
        try
        {          //bind to socket at given host and port number:
          netAddrObj = InetAddress.getByName(bindAddrStr);
          serverListenSocketObj =
                                new ServerSocket(bindPortNum,50,netAddrObj);
        }
        catch(UnknownHostException ex)
        {  //unable to locate host
          final String errStr = msgPromptStr + "Unable to locate host \"" +
                                         bindAddrStr + "\" for listen port";
          LogMgr.usrMsgWarning(errStr);          //log error message
          throw ex;
        }
        catch(Exception ex)
        {  //error binding
          final String errStr = msgPromptStr +
                                          "Error binding to listen port (" +
                                                bindPortIdStr + "):  " + ex;
          LogMgr.usrMsgWarning(errStr);          //log error message
          throw ex;
        }
      }
      else
      {  //bind host-address not given
        try
        {      //bind to socket on local host at given port number
          serverListenSocketObj = new ServerSocket(bindPortNum);
        }
        catch(Exception ex)
        {  //error binding
          final String errStr = msgPromptStr +
                                          "Error binding to listen port (" +
                                                bindPortIdStr + "):  " + ex;
          LogMgr.usrMsgWarning(errStr);          //log error message
          throw ex;
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error
      final String errStr = msgPromptStr +
                                    "Error opening acceptor socket:  " + ex;
      LogMgr.usrMsgWarning(errStr);         //log error message
      throw ex;
    }
  }

  /**
   * Closes the server listen socket.
   */
  private void closeAcceptorSocket()
  {
    final ServerSocket lServerSockObj;      //set local handle to socket obj
    if((lServerSockObj=serverListenSocketObj) != null)
    {  //server socket is open
      serverListenSocketObj = null;    //release server listen socket object
      if(LogMgr.isDebugLevel1())       //if debug-mask set then output msg
        LogMgr.usrMsgDebug(msgPromptStr + "Closing listen socket");
      try
      {       //close server listen socket:
        lServerSockObj.close();
      }
      catch(IOException ex)
      {  //error closing server socket; log message
        if(LogMgr.isDebugLevel1())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                                     "Error closing listen socket:  " + ex);
        }
      }
    }
  }

  /**
   * Delivers the given message to the connected SeedLink clients.
   * @param msgObj message object to be delivered.
   */
  public void deliverMessage(MiniSeedMsgHldr msgObj)
  {
    try
    {         //get list of 'SLinkConnection' objects:
      final List sLConnList = getSLConnListCopy();
      if(LogMgr.isDebugLevel3())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr + "Delivering msg to clients (" +
                                       sLConnList.size() + "):  " + msgObj);
      }
      if(sLConnList.size() > 0)
      {  //at least one entry in connections list
        Iterator iterObj;
        Object obj;
        iterObj = sLConnList.iterator();
        while(iterObj.hasNext())
        {  //for each 'SLinkConnection' in list; pass along msg object
          if((obj=iterObj.next()) instanceof SLinkConnection)
            ((SLinkConnection)obj).addMsgToQueue(msgObj);
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      LogMgr.usrMsgWarning(msgPromptStr +
                                        "Error delivering message:  " + ex);
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
    }
  }

  /**
   * Processes a new client connection accepted via the server listen
   * socket.
   * @param recvdSocketObj socket object for new client connection.
   * @return true if connection is to be retained and used; false if
   * rejected due to I/O error or too many connections.
   */
  private boolean processNewConnection(Socket recvdSocketObj)
  {
    try
    {
      synchronized(sLinkConnectionList)
      {  //hold thread lock for list until 'changed' flag set
        if(sLinkConnectionList.size() >= maxNumConnections)
        {  //new connection would result in too many
          LogMgr.usrMsgWarning(msgPromptStr +
                "Rejecting new connection; too many current connections (" +
                                          sLinkConnectionList.size() + ')');
          return false;
        }
        sLinkConnectionList.add(  //create connection object and add to list
                       new SLinkConnection(this,recvdSocketObj,stationIdStr,
                        networkIdStr,maximumQueueSize,messageRetryDelayMS));
        sLinkConnListChangedFlag = true;    //indicate list changed
      }
      return true;                //indicate client socket will be used
    }
    catch(Exception ex)
    {  //error setting up socket access
      LogMgr.usrMsgWarning(msgPromptStr +
                              "Error setting up client connection:  " + ex);
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
      return false;
    }
  }

  /**
   * Returns a current copy of the list of 'SLinkConnection' objects.
   * The returned list is thread safe in that in will not be modified
   * when a connection is added or removed.
   * @return A current copy of the list of 'SLinkConnection' objects.
   */
  protected Vector getSLConnListCopy()
  {
    synchronized(sLinkConnectionList)
    {  //grab thread lock for list
      if(sLinkConnListChangedFlag)
      {  //list changed since last call; make new copy of list
        sLinkConnListCopy = new Vector(sLinkConnectionList);
        sLinkConnListChangedFlag = false;        //clear list-changed flag
      }
    }
    return sLinkConnListCopy;
  }

  /**
   * Closes all client sockets and streams.  Also closes the server
   * listen socket.
   */
  public void closeAllSockets()
  {
    try
    {
      if(LogMgr.isDebugLevel1())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr + "Closing all sockets");
      }
              //terminate acceptor thread and close server-listen socket:
      clientAcceptorThreadObj.terminate();
              //close all open connections:
      final Iterator iterObj = getSLConnListCopy().iterator();
      Object obj;
      while(iterObj.hasNext())
      {  //for each 'SLinkConnection' in list; close it
        if((obj=iterObj.next()) instanceof SLinkConnection)
          ((SLinkConnection)obj).closeConnection();
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      LogMgr.usrMsgWarning(msgPromptStr + "Error closing sockets:  " + ex);
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
    }
  }

  /**
   * Removes the given connection from the list.
   * @param sLinkConnObj 'SLinkConnection' object to be removed.
   */
  public void removeConnection(SLinkConnection sLinkConnObj)
  {
    synchronized(sLinkConnectionList)
    {  //hold thread lock for list until 'changed' flag set
      sLinkConnectionList.remove(sLinkConnObj);
      sLinkConnListChangedFlag = true;    //indicate list changed
    }
  }

  /**
   * Returns a set of SCNL objects for all digitizer channels.
   * @return A new HashSet of 'StaChaNetLoc' objects for all digitizer
   * channels, or null if none available.
   */
  public Set getChannelSCNLSet()
  {
    return (requestInfoObj != null) ? requestInfoObj.getChannelSCNLSet() :
                                                                       null;
  }

  /**
   * Get the organization.
   * @return the organization
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * Get the SeedLink software.
   * @return the SeedLink software.
   */
  public String getSeedlinkSoftware() {
    return seedlinkSoftware;
  }

  /**
   * Requests 'miniSEED' messages from the cache, starting with the
   * given message number and filtered by the begin and end time.
   * @param msgNumVal starting message number or -1 if any.
   * @param beginTime the begin time or null if none.
   * @param endTime the end time or null if none.
   * @return An array of 'MiniSeedMsgHldr' objects.
   */
  public MiniSeedMsgHldr [] requestMessages(
      int msgNumVal, SLinkTime beginTime, SLinkTime endTime)
  {
    return (requestInfoObj != null) ? requestInfoObj.requestMessages(msgNumVal,
        beginTime, endTime) : new MiniSeedMsgHldr[0];
  }

  /**
   * Generates the response to the INFO command with the given "level" argument
   * string.
   * @param levelStr "level" argument string (i.e., "ID"), or null for none.
   * @return A new 'MiniSeedMsgHldr' object containing the response, or null if
   *         the "level" argument string is invalid.
   */
  public MiniSeedMsgHldr generateInfoResponse(String levelStr)
  {
    return (requestInfoObj != null) ?
                       requestInfoObj.generateInfoResponse(levelStr) : null;
  }

  /**
   * Returns the integer epoch startup time for the manager.
   * @return The integer epoch startup time for the manager, in milliseconds
   * since 1/1/1970.
   */
  public long getMgrStartupTimeMsVal()
  {
    return mgrStartupTimeMsVal;
  }

  /**
   * Returns the string-format startup time for the manager.
   * @return The string startup time for the manager, in the format
   * yyyy-MM-dd HH:mm:ss'Z'.
   */
  public String getMgrStartupTimeStr()
  {
    return mgrStartupTimeStr;
  }

  /**
   * Returns the response string for the "INFO ID" command.
   * @return The response string for the "INFO ID" command.
   */
  public String getInfoIdCmdResponseStr()
  {
    return infoIdCmdResponseStr;
  }


  /**
   * Class ClientAcceptorThread accepts client connections.
   */
  private class ClientAcceptorThread extends NotifyThread
  {
    protected final String aMsgPromptStr;

    /**
     * Creates the client-acceptor thread.
     * @param threadNameStr name for thread.
     */
    public ClientAcceptorThread(String threadNameStr)
    {
      super(threadNameStr);
                   //setup local prompt for user messages:
      aMsgPromptStr = threadNameStr + ":  ";
    }

    /**
     * Executing method for thread.
     */
    public void run()
    {                             //setup local prompt for user messages:
      if(LogMgr.isDebugLevel2())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(aMsgPromptStr +
                                          "Client acceptor thread started");
      }
      try
      {
        if(serverListenSocketObj != null)
        {  //server-listen socket setup OK
          Socket recvdSocketObj = null;
          while(!isTerminated())
          {  //loop for connection accept while thread not terminated
            if(LogMgr.isDebugLevel1())
            {  //debug-mask bit is set; output debug message
              LogMgr.usrMsgDebug(aMsgPromptStr +
                           "Waiting for next connection at listen port " +
                                                           bindPortIdStr);
            }
            try
            {                  //wait for next connection:
              recvdSocketObj = serverListenSocketObj.accept();
            }
            catch(Exception ex)
            {  //error accepting connection; log message
              if(isTerminated())     //if thread terminated then
                break;               //exit loop (and thread)
              LogMgr.usrMsgWarning(aMsgPromptStr +
                           "Error accepting connection to listen port (" +
                                             bindPortIdStr + "):  " + ex);
              waitForNotify(1000);          //do delay before reattempt
            }
            if(recvdSocketObj != null)
            {  //accepted connection OK
              if(isTerminated())
              {  //thread is terminating
                try
                {     //close socket that was just accepted:
                  recvdSocketObj.close();
                }
                catch(Exception ex)
                {  //error closing socket
                  if(LogMgr.isDebugLevel2())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(aMsgPromptStr +
                                   "Error closing client socket before " +
                                            "thread termination:  " + ex);
                  }
                }
                recvdSocketObj = null;    //clear handle to socket object
                break;                    //exit loop (and thread)
              }
              if(LogMgr.isDebugLevel1())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(aMsgPromptStr + "Processing " +
                                    "connection from remote client at " +
                                  recvdSocketObj.getInetAddress() + ':' +
                                               recvdSocketObj.getPort());
              }
              if(!processNewConnection(recvdSocketObj))
              {  //client socket was rejected
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(aMsgPromptStr + "Closing " +
                      "rejected socket connection from remote client at " +
                                    recvdSocketObj.getInetAddress() + ':' +
                                                 recvdSocketObj.getPort());
                }
                try
                {     //close rejected socket:
                  recvdSocketObj.close();
                }
                catch(Exception ex)
                {  //error closing socket
                  if(LogMgr.isDebugLevel1())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(aMsgPromptStr +
                          "Error closing rejected client socket:  " + ex);
                  }
                }
              }
              recvdSocketObj = null;      //clear handle to socket object
            }
          }
        }
      }
      catch(Exception ex)
      {  //some kind of exception error; log it
        LogMgr.usrMsgWarning(aMsgPromptStr +
                                       "Exception error in thread:  " + ex);
        LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
      }
      closeAcceptorSocket();           //close server socket
      if(LogMgr.isDebugLevel2())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(aMsgPromptStr +
                                          "Client acceptor thread stopped");
      }
    }

    /**
     * Overridden version of 'terminate()' that checks to see if server
     * socket remains open (because acceptor thread is stuck on 'accept()'
     * method) and closes the acceptor socket if so.
     */
    public void terminate()
    {
      super.terminate();               //do thread terminate
              //if server socket didn't get closed (because thread stuck
              // on 'accept()' method) then close socket now:
      if(serverListenSocketObj != null)
        closeAcceptorSocket();
    }
  }
}
