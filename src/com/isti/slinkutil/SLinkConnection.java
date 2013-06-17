//SLinkConnection.java:  Manages the connection to a SeedLink client.
//
//  10/1/2009 -- [ET]  Initial version.
//  1/25/2010 -- [ET]  Improved response to TIME command with 'begin_time'
//                     and 'end_time' parameters.
//  2/15/2010 -- [ET]  Modified send loop to close connection after
//                     'WRITE_ERROR_LIMIT' consecutive errors.
// 10/26/2010 -- [ET]  Modified 'setDataTransmitState()' to make it
//                     always notify message queue after no messages
//                     are queued (to fix issue with transaction not
//                     ended if no messages returned).
//

package com.isti.slinkutil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class SLinkConnection manages the connection to a SeedLink client.
 */
public class SLinkConnection
{
  private final SLinkClientsMgr sLinkClientsMgrObj;
  private final Socket clientSocketObj;
  private final String stationIdStr;
  private final String networkIdStr;
  private final int maximumQueueSize;
  private final int messageRetryDelayMS;
  private final InputStream socketInStmObj;
  private final BufferedOutputStream socketOutStmObj;
  private final PrintWriter socketOutWtrObj;
  private final String msgPromptStr;
  private final NotifyThread processingThreadObj;
  private final MessageQueue messageQueueObj;
  private final ArrayList msgStorageListObj = new ArrayList();
  private final Object msgQueueSyncObj;
  private boolean clientSocketOpenFlag = false;
  private int prevSocketInStmVal = 0;
  private boolean msgAddedToQueueFlag = false;
  private int lastMsgNumQueuedVal = -1;
  private boolean queueFullReportedFlag = false;
  private boolean dataTransmitOnFlag = false;
  private boolean storeAddedMsgsFlag = false;
  private int requestedMessageNumber = -1;
  private SLinkTime requestedBeginTime = null;
  private SLinkTime requestedEndTime = null;
  private long transmitBeginTimeMs = 0;
  private long transmitEndTimeMs = 0;
  private Set availableChannelsSet = null;
  private Set selectedChannelsSet = null;
  private final ArrayList selectPatternsList = new ArrayList();
  private static long connectionIdNumber = 0;
         //# of consecutive read errors allowed before closing connection:
  protected static final int READ_ERROR_LIMIT = 10;
         //# of consecutive write errors allowed before closing connection:
  protected static final int WRITE_ERROR_LIMIT = 10;
         //offset value for checking message-number rollover:
  protected static final int MSGNUM_ROLLOVER_CHKOFFS = 100;
         //low-end value for checking message-number rollover:
  protected static final int MSGNUM_ROLLOVRLO_CHKVAL =
                  SLinkMessageNumber.minMsgNumVal + MSGNUM_ROLLOVER_CHKOFFS;
         //high-end value for checking message-number rollover:
  protected static final int MSGNUM_ROLLOVRHI_CHKVAL =
                  SLinkMessageNumber.maxMsgNumVal - MSGNUM_ROLLOVER_CHKOFFS;
         //response-terminator string (CR+LF):
  protected static final String RESPONSE_TERM_STR = "\r\n";
         //bytes for SeedLink message prefix string ("SL"):
  protected static final byte [] SLMSG_PRESTR_ARR = {(byte)'S',(byte)'L'};
         //SeedLink command strings:
  protected static final String HELLO_CMD_STR = "HELLO";
  protected static final String CAT_CMD_STR = "CAT";
  protected static final String BYE_CMD_STR = "BYE";
  protected static final String STATION_CMD_STR = "STATION";
  protected static final String END_CMD_STR = "END";
  protected static final String SELECT_CMD_STR = "SELECT";
  protected static final String DATA_CMD_STR = "DATA";
  protected static final String FETCH_CMD_STR = "FETCH";
  protected static final String TIME_CMD_STR = "TIME";
  protected static final String INFO_CMD_STR = "INFO";
         //SeedLink response strings:
  protected static final String OK_RESP_STR = "OK";
  protected static final String ERROR_RESP_STR = "ERROR";
  protected static final String CMDNOTIMPL_RESP_STR =
                                                  "command not implemented";

  /**
   * Creates the manager object for a SeedLink client connection.
   * @param sLinkClientsMgrObj parent 'SLinkClientsMgr' object.
   * @param clientSocketObj socket object for new client connection.
   * @param stationIdStr station ID string for module.
   * @param networkIdStr network ID string for module.
   * @param maximumQueueSize maximum number of messages allowed to be stored
   * in the queue, or 0 for none.
   * @param messageRetryDelayMS number of milliseconds to wait after a
   * message-send failure before attemping a resend, or 0 to wait
   * indefinitely (until another message is queued).
   * @throws IOException if an error occurs while setting up client
   * socket access.
   */
  public SLinkConnection(SLinkClientsMgr sLinkClientsMgrObj,
           Socket clientSocketObj, String stationIdStr, String networkIdStr,
           int maximumQueueSize, int messageRetryDelayMS) throws IOException
  {
    if(sLinkClientsMgrObj == null || clientSocketObj == null)
      throw new NullPointerException("Null parameter(s)");
    this.sLinkClientsMgrObj = sLinkClientsMgrObj;
    this.clientSocketObj = clientSocketObj;
              //save station-ID string; don't allow null:
    this.stationIdStr = (stationIdStr != null) ? stationIdStr : "";
              //save network-ID string; don't allow null:
    this.networkIdStr = (networkIdStr != null) ? networkIdStr : "";
    this.maximumQueueSize = maximumQueueSize;
    this.messageRetryDelayMS = messageRetryDelayMS;
              //setup input stream for client socket access:
    socketInStmObj = clientSocketObj.getInputStream();
              //setup output stream for client socket access (buffered):
    socketOutStmObj = new BufferedOutputStream(
                                         clientSocketObj.getOutputStream());
              //setup buffered writer for lines-of-character output:
    socketOutWtrObj = new PrintWriter(
                                   new OutputStreamWriter(socketOutStmObj));
              //create ID string for this object (with unique ID number):
    final String objIdStr = "SLinkConn" + (++connectionIdNumber);
    msgPromptStr = objIdStr + ":  ";        //prompt for log messages
    if(LogMgr.isDebugLevel1())    //if mask-bit set then output message
      LogMgr.usrMsgDebug(msgPromptStr + "Opened client socket connection");
              //create thread for processing queue:
    messageQueueObj = new MessageQueue(objIdStr + "Queue");
              //set thread-synchronization object for queue modifications:
    msgQueueSyncObj = messageQueueObj.getThreadSyncLockObj();
    messageQueueObj.startThread();     //startup processing thread
              //create thread for processing client requests:
    processingThreadObj = new NotifyThread(objIdStr + "Proc")
         {
           public void run()
           {
             connectionProcessor();
           }
         };
    processingThreadObj.start();       //startup processing thread
    clientSocketOpenFlag = true;       //indicate client socket is open
  }

  /**
   * Adds the given message to the queue of messages to be sent (if
   * accepted).  The message will be accepted if data transmission is
   * in the "on" state, the message number is greater than the 'previous'
   * message number, and the message channel ID matches an ID specified
   * via the client "SELECT" command.  If a request of historical messages
   * is in progress (via "DATA msgnum" command) then the message will be
   * stored and added after the request has finished.
   * @param msgObj message object to be sent.
   */
  public void addMsgToQueue(MiniSeedMsgHldr msgObj)
  {
    if(dataTransmitOnFlag)             //if transmission on then
      queuePutMessage(msgObj,false);   //put message into queue
    else if(storeAddedMsgsFlag)
    {  //incoming messages should be added to "storage" list
      synchronized(msgStorageListObj)
      {  //grab thread-synchronization lock for list
        if(!dataTransmitOnFlag)
        {  //transmit not re-enabled while waiting for thread-sync lock
          if(storeAddedMsgsFlag)            //if flag still set then
            msgStorageListObj.add(msgObj);  //add to storage list
          return;
        }
      }
         //transmit was re-enabled while waiting for thread-sync lock
      queuePutMessage(msgObj,false);   //put message into queue
    }
  }

  /**
   * Closes the client socket and its streams.
   */
  public void closeConnection()
  {
    if(clientSocketOpenFlag)
    {  //client socket is open
      clientSocketOpenFlag = false;    //indicate client socket not open
      if(LogMgr.isDebugLevel1())  //if mask-bit set then output message
        LogMgr.usrMsgDebug(msgPromptStr + "Closing client socket connection");
                             //remove this connection from manager's list:
      sLinkClientsMgrObj.removeConnection(this);
      processingThreadObj.terminate(); //terminate requests-processing thread
      messageQueueObj.stopThread();    //terminate queue-processing thread
      try
      {       //shutdown client socket streams:
        clientSocketObj.shutdownInput();
        clientSocketObj.shutdownOutput();
      }
      catch(Exception ex)
      {  //error closing streams; log message
        if(LogMgr.isDebugLevel2())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                       "Error shutting down client socket streams:  " + ex);
        }
      }
      try
      {       //close client socket input stream:
        socketInStmObj.close();
      }
      catch(Exception ex)
      {  //error closing streams; log message
        if(LogMgr.isDebugLevel2())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                        "Error closing client socket input stream:  " + ex);
        }
      }
      try
      {       //close client socket output stream:
        socketOutStmObj.close();
      }
      catch(Exception ex)
      {  //error closing streams; log message
        if(LogMgr.isDebugLevel2())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                       "Error closing client socket output stream:  " + ex);
        }
      }
      try
      {       //close client socket:
        clientSocketObj.close();
      }
      catch(IOException ex)
      {  //error closing client socket; log message
        if(LogMgr.isDebugLevel1())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                                     "Error closing client socket:  " + ex);
        }
      }
    }
    else
    {  //client socket not open
      if(LogMgr.isDebugLevel3())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr +
           "Attempted to close client socket connection that was not open");
      }
    }
  }

  /**
   * Receives client requests and generates responses.
   */
  public void connectionProcessor()
  {
    try
    {
      String inLineStr,cmdStr,respStr;
      String [] tokensArr;
      MiniSeedMsgHldr mSeedMsgObj;
      int readErrorCount = 0;
      boolean staHshkModeFlag = false;   //true for STATION handshaking mode
      while(true)
      {  //loop while processing client input
        try
        {          //read line of characters from client:
          inLineStr = readNextInputLine();
          if(processingThreadObj.isTerminated())      //if thread terminated
            return;                                   // then exit method
          if(inLineStr == null)
          {  //end of input steam detected
            if(LogMgr.isDebugLevel1())
            {  //debug-mask bit is set; output debug message
              LogMgr.usrMsgDebug(msgPromptStr +
                                             "Connection closed by client");
            }
            break;           //exit processing loop (and close socket)
          }
          readErrorCount = 0;          //reset error count
        }
        catch(Exception ex)
        {  //error reading from client socket
          if(processingThreadObj.isTerminated())      //if thread terminated
            return;                                   // then exit method
          if(++readErrorCount > READ_ERROR_LIMIT)     //increment error count
          {  //too many errors; log message
            LogMgr.usrMsgWarning(msgPromptStr +
              "Error reading from socket (" + readErrorCount + "):  " + ex);
            LogMgr.usrMsgWarning(msgPromptStr + "Too many read errors (" +
                                  readErrorCount + "), closing connection");
            break;           //exit processing loop (and close socket)
          }
          if(LogMgr.isDebugLevel1())
          {  //debug-mask bit is set; output debug message
            LogMgr.usrMsgDebug(msgPromptStr +
              "Error reading from socket (" + readErrorCount + "):  " + ex);
          }
          processingThreadObj.waitForNotify(1000);    //delay before retry
          inLineStr = null;            //indicate no characters read in
        }

        if(inLineStr != null)
        {  //characters were received from client
          try
          {
            inLineStr = inLineStr.trim();   //trim any trailing CR/LF
            tokensArr = processTokensStr(inLineStr);
            cmdStr = tokensArr[0].toUpperCase(); //get command; upper-case
            if(SELECT_CMD_STR.equals(cmdStr))
            {  //client command is "SELECT"
              setDataTransmitState(false);  //turn off data transmission
              if(LogMgr.isDebugLevel1())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr + "Received \"" +
                                      inLineStr + "\" command from client");
              }
                        //enter pattern arguments; setup response:
              respStr = enterSelectPattern(tokensArr) ? OK_RESP_STR :
                                                             ERROR_RESP_STR;
              if(!sendOutputLine(respStr))  //send response string
                break;  //if error then exit processing loop (close socket)
            }
            else if(STATION_CMD_STR.equals(cmdStr))
            {  //client command is "STATION"
              setDataTransmitState(false);  //turn off data transmission
              if(tokensArr.length > 1 &&
                              stationIdStr.equalsIgnoreCase(tokensArr[1]) &&
                                                    (tokensArr.length < 3 ||
                               networkIdStr.equalsIgnoreCase(tokensArr[2])))
              {  //given argument matches station name and
                 // network-name argument not given or matches
                staHshkModeFlag = true;     //set STATION handshaking mode
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Received valid \"" +
                                      inLineStr + "\" command from client");
                }
                if(!sendOutputLine(OK_RESP_STR))      //send "OK" reply
                  break;  //if error then exit processing loop (close socket)
              }
              else
              {  //given station/network argument(s) not matched
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Received invalid \"" +
                             STATION_CMD_STR + "\" command from client:  " +
                                                                 inLineStr);
                }
                if(!sendOutputLine(ERROR_RESP_STR))   //send "ERROR" response
                  break;  //if error then exit processing loop (close socket)
              }
            }
            else if(DATA_CMD_STR.equals(cmdStr) || TIME_CMD_STR.equals(cmdStr))
            {  //client command is "DATA" or "TIME"
              final boolean argsProcFlag;
              if (DATA_CMD_STR.equals(cmdStr))
              {  //client command is "DATA"
                argsProcFlag = enterDataArgs(tokensArr);
              }
              else if (TIME_CMD_STR.equals(cmdStr))
              {  //client command is "TIME"
                argsProcFlag = enterTimeArgs(tokensArr);
              }
              else
              {
                argsProcFlag = false;
              }
              if(argsProcFlag)
              {  //arguments processed OK
                if(!dataTransmitOnFlag)
                {  //data transmission not on
                  if(staHshkModeFlag)
                  {  //in STATION handshake mode
                    if(LogMgr.isDebugLevel1())
                    {  //debug-mask bit is set; output debug message
                      LogMgr.usrMsgDebug(msgPromptStr + "Received \"" +
                                      inLineStr + "\" command from client");
                    }
                    if(!sendOutputLine(OK_RESP_STR))  //send "OK" reply
                      break;   //if error then exit proc loop (close socket)
                  }
                  else
                  {  //not in STATION handshake mode
                    setDataTransmitState(true);  //turn on data transmission
                    if(LogMgr.isDebugLevel1())
                    {  //debug-mask bit is set; output debug message
                      if(dataTransmitOnFlag)
                      {  //data transmission was turned on
                        LogMgr.usrMsgDebug(msgPromptStr +
                                "Starting data transfer in response to \"" +
                                      inLineStr + "\" command from client");
                      }
                      else
                      {  //data transmission not turned on
                        LogMgr.usrMsgDebug(msgPromptStr + "Received \"" +
                                      inLineStr + "\" command from client");
                      }
                    }
                  }
                }
                else
                {  //data transmission is on
                  if(LogMgr.isDebugLevel1())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(msgPromptStr + "Ignoring \"" +
                                                                 inLineStr +
                            "\" received command during data transmission");
                  }
                }
              }
              else
              {  //error processing arguments
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Received invalid \"" +
                                     cmdStr + "\" command from client:  " +
                                                                 inLineStr);
                }
                if(!sendOutputLine(ERROR_RESP_STR))   //send "ERROR" response
                  break;  //if error then exit processing loop (close socket)
              }
            }
            else if(END_CMD_STR.equals(cmdStr))
            {  //client command is "END"
              if(!dataTransmitOnFlag)
              {  //data transmission not on
                setDataTransmitState(true);      //turn on data transmission
                staHshkModeFlag = false;         //reset handshaking mode
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr +
                                "Starting data transfer in response to \"" +
                                    END_CMD_STR + "\" command from client");
                }
              }
              else
              {  //data transmission is on
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Ignoring \"" +
                                                               END_CMD_STR +
                            "\" received command during data transmission");
                }
              }
            }
            else if(INFO_CMD_STR.equals(cmdStr))
            {  //client command is "INFO"
                        //generate response (via 'level' arg if given):
              if((mSeedMsgObj=sLinkClientsMgrObj.generateInfoResponse(
                      (tokensArr.length > 1) ? tokensArr[1] : null)) != null)
              {  //response successfully generated
                if(LogMgr.isDebugLevel3())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Responding to \"" +
                                      inLineStr + "\" command from client");
                }
                doQueuePutMessage(mSeedMsgObj);  //put resp msg into queue
              }
              else
              {  //unable to generate response
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(msgPromptStr + "Received invalid \"" +
                                INFO_CMD_STR + "\" command from client:  " +
                                                                 inLineStr);
                }
                if(!sendOutputLine(ERROR_RESP_STR))   //send "ERROR" response
                  break;  //if error then exit processing loop (close socket)
              }
            }
            else if(HELLO_CMD_STR.equals(cmdStr))
            {  //client command is "HELLO"; send response
              setDataTransmitState(false);  //turn off data transmission
              if(LogMgr.isDebugLevel2())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr + "Responding to \"" +
                                  HELLO_CMD_STR + "\" command from client");
              }
              if(!sendOutputLine(sLinkClientsMgrObj.getSeedlinkSoftware() +
                  RESPONSE_TERM_STR + sLinkClientsMgrObj.getOrganization()))
              {  //error writing to socket
                break;       //exit processing loop (and close socket)
              }
            }
            else if(BYE_CMD_STR.equals(cmdStr))
            {  //client command is "BYE"
              setDataTransmitState(false);  //turn off data transmission
              staHshkModeFlag = false;      //reset handshaking mode
              if(LogMgr.isDebugLevel1())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr +
                      "Closing connection in response to \"" + BYE_CMD_STR +
                                                  "\" command from client");
              }
              break;         //exit processing loop (and close socket)
            }
            else if(CAT_CMD_STR.equals(cmdStr))
            {  //client command is "CAT"
              // Example of a CAT response:
              // NU ACON  Acoyapa, Nicaragua
              // KO AGRB  AGRB
              // KO ALT   ALT
              // END
              final StringBuffer sb = new StringBuffer();
              SLinkUtilFns.append(sb, networkIdStr, 2);  //append the network
              sb.append(SLinkUtilFns.SPACE_CHAR);
              SLinkUtilFns.append(sb, stationIdStr, 5);  //append the station
              sb.append(SLinkUtilFns.SPACE_CHAR);
              sb.append(stationIdStr);  // append the station since there is no description
              sb.append("\nEND");
              if(!sendOutputLine(sb.toString()))
                break;  //if error then exit processing loop (close socket)
            }
            else if(FETCH_CMD_STR.equals(cmdStr))
            {
              if(LogMgr.isDebugLevel2())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr + "Responding to \"" +
                    inLineStr + "\" command from client (not implemented)");
              }
                             //send not-implemented response:
              if(!sendOutputLine(cmdStr + ' ' + CMDNOTIMPL_RESP_STR))
                break;  //if error then exit processing loop (close socket)
            }
            else
            {  //unrecognized command; send "ERROR" response
              if(inLineStr.length() > 0 && LogMgr.isDebugLevel1())
              {  //non-empty command and debug-mask bit is set; output msg
                LogMgr.usrMsgDebug(msgPromptStr +
                     "Received invalid command from client:  " + inLineStr);
              }
              if(!sendOutputLine(ERROR_RESP_STR))     //send error response
                break;  //if error then exit processing loop (close socket)
            }
          }
          catch(Exception ex)
          {  //some kind of exception error; log it
            LogMgr.usrMsgWarning(msgPromptStr +
                         "Exception error processing client input:  " + ex);
            LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
            if(!sendOutputLine(ERROR_RESP_STR))  //send error response
              break;    //if error then exit processing loop (close socket)
          }
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      LogMgr.usrMsgWarning(msgPromptStr +
                           "Exception error processing connection:  " + ex);
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
    }
    closeConnection();       //close socket connection
  }

  /**
   * Reads and returns the next line of data from the input socket.  A
   * carriage return, linefeed, or both may be used to terminate the line.
   * This methods blocks while waiting for the data.
   * @return The line of data received (not including terminating carriage
   * returns and linefeeds), or null if the end of stream is detected.
   * @throws IOException if an input/output error occurs.
   */
  private String readNextInputLine() throws IOException
  {
    final StringBuffer buff = new StringBuffer();
    int val;
    while(true)
    {  //loop while reading characters from input stream
      if((val=socketInStmObj.read()) == -1)    //read char from input stream
      {  //received value is -1 or end of stream
        prevSocketInStmVal = val;      //save received value
        return null;                   //indicate end of stream
      }
              //if received value is carriage return or linefeed then
              // return received line of data (unless previous value
              // was corresponding CR/LF; then just ignore):
      if(val == (int)'\r')
      {  //received value is carriage return
        if(prevSocketInStmVal != (int)'\n')
          break;   //if previous value not linefeed then exit loop
      }
      else if(val == (int)'\n')
      {  //received value is linefeed
        if(prevSocketInStmVal != (int)'\r')
          break;   //if previous value not carriage return then exit loop
      }
      else         //received value is data
      {
        buff.append((char)(val & 0xFF));    //append char value to buffer
        prevSocketInStmVal = val;           //save received value
      }
    }
    prevSocketInStmVal = val;          //save received value
    return buff.toString();            //return string version of buffer
  }

  /**
   * Enters the given arguments to the SELECT command.
   * @param tokensArr array of SELECT command arguments, starting with
   * the second element in the array.
   * @return true if the arguments were accepted; false if rejected.
   */
  private boolean enterSelectPattern(String [] tokensArr)
  {
    if(tokensArr.length > 1)
    {  //at least one select pattern given
      if(availableChannelsSet == null)
      {  //available-channels set not setup; fetch set now
        if((availableChannelsSet=sLinkClientsMgrObj.getChannelSCNLSet()) ==
                                   null || availableChannelsSet.size() <= 0)
        {  //unable to fetch available-channels set
          availableChannelsSet = null;      //make sure handle is clear
          return false;                     //indicate error
        }
      }
              //make copy of current list of pattern strings:
      final ArrayList newPatsList = new ArrayList(selectPatternsList);
      String patStr;
      int len;
      for(int i=1; i<tokensArr.length; ++i)
      {  //for each new pattern string given
        patStr = tokensArr[i];
        if((len=patStr.length()) > 0)
        {  //pattern string not empty
          if(patStr.indexOf('!') >= 0)      //if negative selector then
            return false;                   //return error (not supported)
          if(len == 1)
          {  //pattern contains single "type" character
            if(patStr.equalsIgnoreCase("D") || patStr.charAt(0) == '?')
              continue;      //if 'D' (data) or wildcard then ignore
            return false;    //if other char then error (not supported)
          }
          if(len >= 3 && patStr.charAt(len-2) == '.')
          {  //pattern ends with "type" char after period
            if(!patStr.substring(len-1).equalsIgnoreCase("D") &&
                                                patStr.charAt(len-1) != '?')
              return false;  //if not 'D' or '?' then error (not supported)
            patStr = patStr.substring(0,len-2);       //remove trailing chars
          }
          newPatsList.add(patStr);     //add pattern string to list
        }
      }
      if(newPatsList.size() > selectPatternsList.size())
      {  //new pattern strings were added to list
                   //create set to receive channel IDs matched below:
        final HashSet newSelChansSet = new HashSet();
        Iterator iterObj;
        boolean matchFlag;
        String chaStr,locStr;
        Object obj;
        IStaChaNetLoc scnlObj;
        for(int i=0; i<newPatsList.size(); ++i)
        {  //for each selector pattern string in list
          patStr = (String)(newPatsList.get(i));
          if((len=patStr.length()) < 2 || len > 5)
            return false;    //if length not between 2 and 5 then error
          if(len < 5)
          {  //length is 2-4
            chaStr = patStr;      //accept string as channel pattern
            locStr = null;        //no location code specified
          }
          else
          {  //length is 5
            chaStr = patStr.substring(2);        //take trailing 3 as chan
            locStr = patStr.substring(0,2);      //take leading 2 as loc
          }
          matchFlag = false;           //true after match to channel ID
          iterObj = availableChannelsSet.iterator();
          while(iterObj.hasNext())
          {  //for each available channel
            if((obj=iterObj.next()) instanceof IStaChaNetLoc)
            {  //SCNL object fetched OK
              scnlObj = (IStaChaNetLoc)obj;
              if(matchPatStr(chaStr,scnlObj.getChannelIdStr()))
              {  //channel ID matched to given channel-selector pattern str
                if(locStr == null ||
                             matchPatStr(locStr,scnlObj.getLocationIdStr()))
                {  //location-selector pattern str not given or matched OK
                  newSelChansSet.add(scnlObj);   //enter matched channel ID
                  matchFlag = true;              //indicate channel-ID match
                  if(LogMgr.isDebugLevel2())
                  {  //debug mask-bit set; output message
                    LogMgr.usrMsgDebug(msgPromptStr +
                                     "Matched SELECT pattern \"" + patStr +
                                    "\" to channel ID \"" + scnlObj + "\"");
                  }
                }
              }
            }
          }
          if(!matchFlag)     //if given pattern matched no channel IDs then
            return false;    //return error
        }
        selectPatternsList.clear();       //enter new patterns list
        selectPatternsList.addAll(newPatsList);
                   //enter new set of selected channel-ID objects:
        selectedChannelsSet = newSelChansSet;
      }
    }
    else
    {  //no select patterns given
      selectPatternsList.clear();      //clear patterns list
      selectedChannelsSet = null;      //clear any selected channel-ID objs
      if(LogMgr.isDebugLevel2())  //if mask-bit set then output message
        LogMgr.usrMsgDebug(msgPromptStr + "Cleared all SELECT patterns");
    }
    return true;
  }

  /**
   * Enters the given arguments to the DATA command.
   * @param tokensArr array of DATA command arguments, starting with
   * the second element in the array.
   * @return true if the arguments were accepted; false if rejected.
   */
  private boolean enterDataArgs(String [] tokensArr)
  {
    if(tokensArr.length <= 1)     //if no arguments then
      return true;                //just return success
    if(tokensArr.length > 3)      //if more than 3 args then
      return false;               //return error

    int messageNumber;
    SLinkTime beginTime = null;
    try
    {         //parse argument as hexidecimal value:
      messageNumber = Integer.parseInt(tokensArr[1],16);
      if (tokensArr.length > 2)
      {
        beginTime = new SLinkTime(tokensArr[2]);
      }
    }
    catch(Exception ex)
    {         //error parsing argument
      if(LogMgr.isDebugLevel1())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr +
                                 "Error processing DATA arguments:  " + ex);
        LogMgr.usrMsgDebug(LogMgr.getStackTraceString(ex));
      }
      return false;
    }
    setDataTransmitState(false);             //turn off data transmit (if on)
    // save requested values
    requestedMessageNumber = messageNumber;
    requestedBeginTime = beginTime;
    requestedEndTime = null;
    return true;
  }

  /**
   * Enters the given arguments to the TIME command.
   * @param tokensArr array of TIME command arguments, starting with
   * the second element in the array.
   * @return true if the arguments were accepted; false if rejected.
   */
  private boolean enterTimeArgs(String [] tokensArr)
  {
    if(tokensArr.length <= 1)     //if no arguments then
      return false;               //return error
    if(tokensArr.length > 3)      //if more than 3 args then
      return false;               //return error

    SLinkTime beginTime = null;
    SLinkTime endTime = null;
    try
    {
      beginTime = new SLinkTime(tokensArr[1]);
      if (tokensArr.length > 2)
      {
        endTime = new SLinkTime(tokensArr[2]);
      }
    }
    catch(Exception ex)
    {  //error parsing argument
      if(ex instanceof IllegalArgumentException)
      {  //error is bad input
        if(LogMgr.isDebugLevel3())
        {  //debug-mask bit is set; output debug message
          LogMgr.usrMsgDebug(msgPromptStr +
                                          "Invalid TIME arguments:  " + ex);
        }
        return false;
      }
      if(LogMgr.isDebugLevel1())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr +
                                 "Error processing TIME arguments:  " + ex);
        LogMgr.usrMsgDebug(LogMgr.getStackTraceString(ex));
      }
      return false;
    }
    if(beginTime.getTime() > endTime.getTime())
    {  //given 'begin' time is after 'end' time
      if(LogMgr.isDebugLevel3())
      {  //debug-mask bit is set; output debug message
        LogMgr.usrMsgDebug(msgPromptStr +
                  "Invalid TIME arguments:  'begin' time after 'end' time");
      }
      return false;
    }
    setDataTransmitState(false);             //turn off data transmit (if on)
    // save requested values
    requestedMessageNumber = -1;
    requestedBeginTime = beginTime;
    requestedEndTime = endTime;
    return true;
  }

  /**
   * Sets data transmission to 'on' or 'off'.
   * @param flgVal true to set data transmission to 'on'; false to set
   * data transmission to 'off'.
   */
  private void setDataTransmitState(boolean flgVal)
  {
    if(flgVal != dataTransmitOnFlag)
    {  //transmit state is changing
      if(flgVal && (requestedMessageNumber >= 0 ||
                                               requestedBeginTime != null ||
                                                  requestedEndTime != null))
      {  //transmission being enabled & msgNum/time request was entered
        try
        {                    //save begin-time value (if given):
          transmitBeginTimeMs = (requestedBeginTime != null) ?
                                           requestedBeginTime.getTime() : 0;
          final boolean futureEndTimeFlag;
          if(requestedEndTime != null)
          {  //end-time was given
            transmitEndTimeMs = requestedEndTime.getTime();     //save value
            if(transmitEndTimeMs > System.currentTimeMillis())
            {  //end-time in future
              futureEndTimeFlag = true;     //indicate end-time in future
              storeAddedMsgsFlag = true;    //set to store incoming messages
            }
            else  //end-time not in future
              futureEndTimeFlag = false;    //indicate no end-time
          }
          else
          {  //no end-time given
            futureEndTimeFlag = false;      //indicate no end-time
            transmitEndTimeMs = 0;          //no end-time value
            storeAddedMsgsFlag = true;      //set to store incoming messages
          }
          if(LogMgr.isDebugLevel1())
          {  //debug-mask bit is set; output debug message
            LogMgr.usrMsgDebug(msgPromptStr +
                                 "Fetching requested messages (reqMsgNum=" +
                                              requestedMessageNumber + ')');
          }
                   //fetch requested messages from cache:
          final MiniSeedMsgHldr [] msgsArr =
                  sLinkClientsMgrObj.requestMessages(requestedMessageNumber,
                                      requestedBeginTime, requestedEndTime);
          requestedMessageNumber = -1;           //clear request values
          requestedBeginTime = null;
          requestedEndTime = null;
          messageQueueObj.clearEvents();    //clear any old messages
          lastMsgNumQueuedVal = -1;         //reset msgNum tracker
          if(LogMgr.isDebugLevel1())
          {  //debug-mask bit is set; output debug message
            LogMgr.usrMsgDebug(msgPromptStr +
                "Entering fetched messages (count=" + msgsArr.length + ')');
          }
                        //enter fetched messages into queue:
          queuePutMessageArr(msgsArr,false);
          synchronized(msgStorageListObj)
          {  //grab thread-synchronization lock for list
            storeAddedMsgsFlag = false;          //clear storage flag
            if(transmitEndTimeMs > 0 && !futureEndTimeFlag)
            {  //request-messages end-time specified and not in future
              msgStorageListObj.clear();         //clear any stored messages
                   //if no messages entered then wake up queue-processing
                   // thread to make sure it's not waiting indefinitely:
              if(lastMsgNumQueuedVal < 0 || msgsArr.length <= 0)
                messageQueueObj.notifyThread();
              if(LogMgr.isDebugLevel2())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr + "Not setting transmit " +
                  "state to 'on' because specified end time not in future");
              }
              return;   //exit method (leaving 'dataTransmitOnFlag'==false)
            }
            if(msgStorageListObj.size() > 0)
            {  //"live" messages were entered into storage during fetch
              if(LogMgr.isDebugLevel1())
              {  //debug-mask bit is set; output debug message
                LogMgr.usrMsgDebug(msgPromptStr +
                           "Received 'live' messages during fetch (count=" +
                             msgStorageListObj.size() + "); entering msgs");
              }
                   //move messages from storage into queue:
              final Iterator iterObj = msgStorageListObj.iterator();
              while(iterObj.hasNext())
                queuePutMessage((MiniSeedMsgHldr)(iterObj.next()),true);
              msgStorageListObj.clear();    //clear storage list
                   //enable transmit here so flag will be ready if
                   // 'addMsgToQueue()' is waiting for thread-sync lock:
              dataTransmitOnFlag = true;
            }
            else if(transmitEndTimeMs > 0)
            {  //no "live" messages entered into storage during fetch
               // and end-time value was given
                   //if no messages entered then wake up queue-processing
                   // thread to make sure it's not waiting indefinitely:
              if(lastMsgNumQueuedVal < 0 || msgsArr.length <= 0)
                messageQueueObj.notifyThread();
            }
          }
        }
        catch(Exception ex)
        {  //some kind of exception error; log it
          storeAddedMsgsFlag = false;       //clear storage flag
          msgStorageListObj.clear();        //clear any stored messages
          requestedMessageNumber = -1;      //clear request values
          requestedBeginTime = null;
          requestedEndTime = null;
          transmitBeginTimeMs = 0;          //no begin-time value
          transmitEndTimeMs = 0;            //no end-time value
          LogMgr.usrMsgWarning(msgPromptStr +
                               "Error fetching requested messages:  " + ex);
        }
      }
      else
      {  //transmission not enabled or no msgNum/time request entered
        transmitBeginTimeMs = 0;            //no begin-time value
        transmitEndTimeMs = 0;              //no end-time value
      }
      dataTransmitOnFlag = flgVal;     //enter new transmit state
    }
  }

  /**
   * Adds the given message to the queue of messages to be sent (if
   * accepted).  The message will be accepted if the message number is
   * greater than the 'previous' message number and the message channel
   * ID matches an ID specified via the client "SELECT" command.
   * @param msgObj message object to be sent.
   * @param lessDbgLogFlag true to log "discarding message" messages
   * at "debug4" level; to log "discarding message" messages at "debug1"
   * level.
   */
  private void queuePutMessage(MiniSeedMsgHldr msgObj,
                                                     boolean lessDbgLogFlag)
  {
    synchronized(msgQueueSyncObj)
    {  //thread-synchronize while using queue and 'lastMsgNumQueuedVal'
      final int msgNum;
      if((msgNum=msgObj.getMessageNumber()) != lastMsgNumQueuedVal)
      {  //message number not same as previous
        if(msgNum < lastMsgNumQueuedVal)
        {  //message number not greater than previous
          if(msgNum > MSGNUM_ROLLOVRLO_CHKVAL ||
                            lastMsgNumQueuedVal < MSGNUM_ROLLOVRHI_CHKVAL)
          {  //message number did not change from high to low (rollover)
            if(lessDbgLogFlag ? LogMgr.isDebugLevel4() :
                                                     LogMgr.isDebugLevel1())
            {  //debug mask-bit set; output message
              LogMgr.usrMsgDebug(msgPromptStr + "Message number (" +
                      msgNum + ") lower than previous; discarding message");
              return;        //reject message
            }
          }
          if(LogMgr.isDebugLevel1())
          {  //debug mask-bit set; output message
            LogMgr.usrMsgDebug(msgPromptStr +
                               "Message number rollover detected (prev=" +
                           lastMsgNumQueuedVal + ", cur=" + msgNum + ')');
          }
        }
            //set local handle to selected-chans set (to be thread safe):
        final Set selChansSet = selectedChannelsSet;
        if(selChansSet == null ||
                        selChansSet.contains(msgObj.getStaChaNetLocObj()))
        {  //no selected-channels set or msg SCNL matches entry in set
          if(LogMgr.isDebugLevel4())
          {  //debug-mask bit is set; output debug message
            LogMgr.usrMsgDebug(msgPromptStr +
                              "Adding message to queue (" + msgNum + ")");
          }
          doQueuePutMessage(msgObj);           //add to queue
          lastMsgNumQueuedVal = msgNum;        //save message number
        }
        else
        {  //no match to selected-channels set
          if(LogMgr.isDebugLevel4())
          {  //debug-mask bit is set; output debug message
            LogMgr.usrMsgDebug(msgPromptStr + "Message (" + msgNum +
                         ") rejected; no match to selected-channels set");
          }
        }
      }
      else
      {  //message number same as previous
        if(lessDbgLogFlag ? LogMgr.isDebugLevel4() : LogMgr.isDebugLevel1())
        {  //debug mask-bit set; output message
          LogMgr.usrMsgDebug(msgPromptStr + "Message number (" + msgNum +
                                ") same as previous; discarding message");
        }
      }
    }
  }

  /**
   * Adds the given array of messages to the queue of messages to be sent.
   * The thread-synchronization lock for the queue is held during the
   * transfer so the queue-processing thread will wait until all the
   * messages are added.
   * @param msgsArr array of message objects to be sent.
   * @param lessDbgLogFlag true to log "discarding message" messages
   * at "debug4" level; to log "discarding message" messages at "debug1"
   * level.
   */
  private void queuePutMessageArr(MiniSeedMsgHldr [] msgsArr,
                                                     boolean lessDbgLogFlag)
  {
    synchronized(msgQueueSyncObj)
    {  //thread-synchronize while using queue
      for(int i=0; i<msgsArr.length; ++i)             //enter messages
        queuePutMessage(msgsArr[i],lessDbgLogFlag);   // into queue
    }
  }

  /**
   * Performs the low-level work of adding a message to the queue.
   * @param msgObj message object to be sent.
   */
  private void doQueuePutMessage(MiniSeedMsgHldr msgObj)
  {
    synchronized(msgQueueSyncObj)
    {  //thread-synchronize while using queue
      if(messageQueueObj.pushEvent(msgObj))     //add message to queue
        queueFullReportedFlag = false;  //if OK then clear report flag
      else
      {  //oldest message dropped because queue full
        if(queueFullReportedFlag)
        {  //queue-full message already reported at warning level
          if(LogMgr.isDebugLevel3())
          {  //debug-mask bit is set; log message
            LogMgr.usrMsgDebug(msgPromptStr +
                     "Message queue full (limit=" + maximumQueueSize +
                                        "); dropping oldest message");
          }
        }
        else
        {  //queue-full message not yet reported at warning level
          LogMgr.usrMsgWarning(msgPromptStr +     //log warning msg
                     "Message queue full (limit=" + maximumQueueSize +
                                     "); dropping oldest message(s)");
          queueFullReportedFlag = true;         //indicate reported
        }
      }
      msgAddedToQueueFlag = true;          //indicate message added
    }
  }

  /**
   * Tests the match between the two strings, with single-character
   * wildcard support on the "pattern" string.
   * @param patStr "pattern" string, where occurrences of the '?' wildcard
   * character are treated as "match any character".
   * @param matchStr "match" string.
   * @return true if match; false if not.
   */
  private boolean matchPatStr(String patStr, String matchStr)
  {
    final int len = patStr.length();
    if(matchStr.length() != len)
      return false;          //if different lengths then no match
    char ch;
    for(int p=0; p<len; ++p)
    {  //for each character in strings
      if((ch=patStr.charAt(p)) != '?')
      {  //pattern string character is not match-any wildcard
        if(Character.toUpperCase(ch) !=
                                  Character.toUpperCase(matchStr.charAt(p)))
        {  //case-insensitive match failed
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Sends the given line of characters to the client.
   * @param outLineStr string of characters to be send.
   * @param crLfFlag true to follow data with carriage return and linefeed;
   * false to not.
   * @return true if successful; false if error.
   */
  private boolean sendOutputLine(String outLineStr, boolean crLfFlag)
  {
    int writeErrorCount = 0;
    while(!processingThreadObj.isTerminated())
    {  //loop if processing thread not terminated and send fails
      try
      {          //send line of characters:
        socketOutWtrObj.print(outLineStr);
        if(crLfFlag)                                  //if flag then
          socketOutWtrObj.print(RESPONSE_TERM_STR);   //send CR+LF
        socketOutWtrObj.flush();                      //send it now
        return true;
      }
      catch(Exception ex)
      {  //error writing to client socket
        ++writeErrorCount;            //increment error count
        LogMgr.usrMsgWarning(msgPromptStr +
            "Error writing to socket (" + writeErrorCount + "):  " + ex);
        if(writeErrorCount > WRITE_ERROR_LIMIT)
        {  //too many errors; log message
          LogMgr.usrMsgWarning(msgPromptStr + "Too many write errors (" +
                                 writeErrorCount + "), closing connection");
          break;           //exit retry loop
        }
        processingThreadObj.waitForNotify(250);       //delay before retry
      }
    }
    return false;
  }

  /**
   * Sends the given line of characters to the client, followed by a
   * carriage return and linefeed.
   * @param outLineStr string of characters to be send.
   * @return true if successful; false if error.
   */
  private boolean sendOutputLine(String outLineStr)
  {
    return sendOutputLine(outLineStr,true);
  }

  /**
   * Converts the given space-separated tokens string into an array of
   * token strings.  The return array will contain at least one element.
   * @param tokensStr space-separated tokens string.
   * @return A new array containing the token strings.
   */
  public static String [] processTokensStr(String tokensStr)
  {
    final ArrayList listObj = new ArrayList();
    if(tokensStr != null)
    {  //non-null token string given
      final int tokensStrLen = tokensStr.length();
      int ePos, sPos = 0;
      String str;
      while(sPos < tokensStrLen)
      {  //for each substring; find next space separator
        if((ePos=tokensStr.indexOf(' ',sPos)) < 0)
          ePos = tokensStrLen;    //if none found then use end-of-string pos
                                  //get delimited substring
        str = tokensStr.substring(sPos,ePos).trim();
        if(str.length() > 0)      //if not empty then
          listObj.add(str);       //add delimited substring to list
        sPos = ePos + 1;          //move past space separator
      }
    }
    if(listObj.size() <= 0)  //if list empty then
      listObj.add("");       //put single empty-string item in list
    return (String [])(listObj.toArray(new String[listObj.size()]));
  }


  /**
   * Class MessageQueue implements the miniSEED-message queue operations.
   */
  protected class MessageQueue extends NotifyEventQueue
  {
    private final String qMsgPromptStr;

    /**
     * Creates the queue.
     * @param threadNameStr name for queue-processing thread.
     */
    public MessageQueue(String threadNameStr)
    {
      super(threadNameStr);
                   //setup local prompt for user messages:
      qMsgPromptStr = threadNameStr + ":  ";
    }

    /**
     * Executing method for queue.
     */
    public void run()
    {              //if debug-mask bit set then output debug message:
      if(LogMgr.isDebugLevel2())
        LogMgr.usrMsgDebug(qMsgPromptStr + "Message queue thread started");
      boolean waitBeforeSendFlag = false;
      int sendErrorCount = 0;
      long retryAfterFailTime = 0;
      long curTimeVal,waitTimeVal;
      try
      {
        Object obj;
        while(!finishRunning())
        {  //loop until thread is terminated
          curTimeVal = System.currentTimeMillis();  //get current time
          if(!isEmpty())
          {  //queue contains objects
            if(!waitBeforeSendFlag ||
                        (msgAddedToQueueFlag && messageRetryDelayMS <= 0))
            {  //not waiting after send-fail or message was just added
               // to queue and currently waiting indefinitely after fail
              msgAddedToQueueFlag = false;     //reset msg-added flag
              if((obj=pullEvent()) instanceof MiniSeedMsgHldr)
              {  //message object found in queue
                final MiniSeedMsgHldr mSeedMsgObj = (MiniSeedMsgHldr)obj;
                if(transmitEndTimeMs > 0 &&
                        mSeedMsgObj.getStartTimeMsVal() > transmitEndTimeMs)
                {  //end-time value was setup and message after end-time
                  if(LogMgr.isDebugLevel1())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(qMsgPromptStr + "Queued message (" +
                                      mSeedMsgObj.getMessageNumber() + ',' +
                                           mSeedMsgObj.getStartTimeMsVal() +
                                  ") after end time (" + transmitEndTimeMs +
                                                   "); ending transaction");
                  }
                  sendTransmitEndIndicator();    //send "END", etc
                }
                if(transmitBeginTimeMs <= 0 ||
                     mSeedMsgObj.getStartTimeMsVal() >= transmitBeginTimeMs)
                {  //begin-time value not setup or msg not before begin-time
                  if(LogMgr.isDebugLevel4())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(qMsgPromptStr + "Sending message (" +
                                      mSeedMsgObj.getMessageNumber() + ')');
                  }
                  try   //send message out client socket:
                  {          //start with "SL" prefix:
                    socketOutStmObj.write(SLMSG_PRESTR_ARR);
                             //send bytes for message-number string:
                    socketOutStmObj.write(
                                        mSeedMsgObj.getSLinkMessageNumber().
                                                      getBytes());
                             //send bytes for minSEED message data:
                    socketOutStmObj.write(mSeedMsgObj.getMessageDataArray());
                    socketOutStmObj.flush();
                    waitBeforeSendFlag = false;       //clear wait flag
                    sendErrorCount = 0;               //clear error count
                  }
                  catch(IOException ex)
                  {  //error sending message; log warning
                    ++sendErrorCount;            //increment error count
                    LogMgr.usrMsgWarning(qMsgPromptStr +
                          "Error sending message (count=" + sendErrorCount +
                                                               "):  " + ex);
                    if(sendErrorCount > WRITE_ERROR_LIMIT)
                    {  //too many errors; log message
                      LogMgr.usrMsgWarning(msgPromptStr +
                                "Too many send errors, closing connection");
                      break;           //exit retry loop
                    }
                    if(LogMgr.isDebugLevel1())
                    {  //debug-mask bit is set; output debug message
                      LogMgr.usrMsgDebug(qMsgPromptStr +
                                         "Pushing message back onto queue");
                    }
                    if(!pushEventBackNoNotify(obj))   //push message back
                    {  //unable to push message back, queue full
                      if(LogMgr.isDebugLevel1())
                      {  //debug-mask bit is set; output debug message
                        LogMgr.usrMsgDebug(qMsgPromptStr +
                          "Message queue full (limit=" + maximumQueueSize +
                                          "); unable to push message back");
                      }
                    }
                    if(messageRetryDelayMS > 0)
                    {  //retry delay was given; set time for next retry
                      retryAfterFailTime = System.currentTimeMillis() +
                                                        messageRetryDelayMS;
                    }
                    waitBeforeSendFlag = true;        //set wait flag
                  }
                }
                else
                {  //begin-time value was setup and msg before begin-time
                  if(LogMgr.isDebugLevel3())
                  {  //debug-mask bit is set; output debug message
                    LogMgr.usrMsgDebug(qMsgPromptStr +
                                               "Ignoring queued message (" +
                                      mSeedMsgObj.getMessageNumber() + ',' +
                                           mSeedMsgObj.getStartTimeMsVal() +
                                                   ") before begin time (" +
                                                 transmitBeginTimeMs + ')');
                  }
                }
              }
              else if(obj != null)
              {  //unexpected object found in queue
                LogMgr.usrMsgWarning(qMsgPromptStr +
                              "Unexpected object type found in queue:  " +
                                                          obj.getClass());
              }       //(if object was pulled from queue then loop now
            }         //  to check for more in queue)
            else
            {  //wait flag set and no message added to queue
              if(retryAfterFailTime > 0)
              {  //retry time is setup; calc wait until next retry
                         //if positive wait time then do delay:
                         // (add +10ms for retry-time-elapsed check)
                if((waitTimeVal=retryAfterFailTime-curTimeVal) > 0)
                {
                  waitForNotify(waitTimeVal + 10);
                  if(retryAfterFailTime > 0 &&
                         System.currentTimeMillis() >= retryAfterFailTime)
                  {  //retry-after-fail time reached
                    waitBeforeSendFlag = false;   //clear wait flag
                  }
                }
              }
              else  //retry time not setup
                waitForNotify();       //wait indefinitely for thread notify
            }
          }
          else
          {  //queue empty
            if(transmitEndTimeMs > 0)
            {  //end-time value was setup
                   //if end-time in future then setup wait until end-time
                   // (add 2 secs for all msgs in window to get through):
              if((waitTimeVal=transmitEndTimeMs-curTimeVal+2000) > 0)
                waitForNotify(waitTimeVal);
              else
              {  //end-time not in future
                if(LogMgr.isDebugLevel1())
                {  //debug-mask bit is set; output debug message
                  LogMgr.usrMsgDebug(qMsgPromptStr + "End time reached (" +
                               transmitEndTimeMs + "); ending transaction");
                }
                sendTransmitEndIndicator();      //send "END", etc
                waitForNotify();       //wait indefinitely for thread notify
              }
            }
            else   //no end-time setup
              waitForNotify();         //wait indefinitely for thread notify
          }
        }
      }
      catch(Exception ex)
      {  //some kind of exception error; log it
        LogMgr.usrMsgWarning(qMsgPromptStr +
                                       "Exception error in thread:  " + ex);
        LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
      }
      stopThread();          //stop this queue-processing thread
              //if debug-mask bit set then output debug message:
      if(LogMgr.isDebugLevel2())
        LogMgr.usrMsgDebug(qMsgPromptStr + "Message queue thread stopped");
      closeConnection();     //close socket connection
    }

    /**
     * Turns off data transmission, sends "END" indicator and clears
     * message queue.
     */
    private void sendTransmitEndIndicator()
    {
      setDataTransmitState(false);          //disable data transmission
      sendOutputLine(END_CMD_STR,false);    //send "END"
      messageQueueObj.clearEvents();        //clear any messages in queue
    }

    /**
     * Returns the object to be used for thread-synchronizations on
     * modifications to the queue.
     * @return The object to be used for thread-synchronizations on
     * modifications to the queue.
     */
    public Object getThreadSyncLockObj()
    {
      return m_queue;
    }
  }
}
