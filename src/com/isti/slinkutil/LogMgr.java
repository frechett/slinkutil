//LogMgr.java:  Manages log-message output.
//
//  9/30/2008 -- [ET]  Initial version.
//

package com.isti.slinkutil;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Class LogMgr manages log-message output.
 */
public class LogMgr {
  /** The logger. */
  private static ILogger logger = new ConsoleLogger();

  /**
   * Get the logger.
   * @return the the logger.
   */
  public static ILogger getLogger() {
    return logger;
  }

  /**
   * Fetches stack trace data from a throwable and returns it in a string.
   * @param throwObj the throwable object to use.
   * @return A string containing the stack trace data.
   */
  public static String getStackTraceString(Throwable throwObj) {
    String retStr = "";
    // create string-writer to receive data:
    final StringWriter strWtrObj = new StringWriter();
    try { // wrap print-writer around string-writer:
      final PrintWriter prtWtrObj = new PrintWriter(strWtrObj);
      // get stack trace data:
      throwObj.printStackTrace(new PrintWriter(prtWtrObj));
      prtWtrObj.flush(); // flush data out of print writer
      // convert stream to string; strip trailing newline:
      retStr = strWtrObj.toString();
      int p = retStr.length();
      if (--p > 0 && retStr.charAt(p) <= '\r') { // trailing char is CR (13) or
                                                 // lower
        if (p > 0 && retStr.charAt(p - 1) <= '\r')
          --p; // next char in is CR (13) or lower
        retStr = retStr.substring(0, p);
      }
    } catch (Exception ex) {
    } // ignore any exceptions
    return retStr; // return stack trace data
  }

  /**
   * Checks if the debug-mask bit for "level 1" (0x01) is set.
   * @return true if the debug-mask bit for "level 1" (0x01) is set;
   * false if not.
   */
  public static boolean isDebugLevel1() {
    return logger.isDebugLevel1();
  }

  /**
   * Checks if the debug-mask bit for "level 2" (0x02) is set.
   * @return true if the debug-mask bit for "level 2" (0x02) is set;
   * false if not.
   */
  public static boolean isDebugLevel2() {
    return logger.isDebugLevel2();
  }

  /**
   * Checks if the debug-mask bit for "level 3" (0x04) is set.
   * @return true if the debug-mask bit for "level 3" (0x04) is set;
   * false if not.
   */
  public static boolean isDebugLevel3() {
    return logger.isDebugLevel3();
  }

  /**
   * Checks if the debug-mask bit for "level 4" (0x08) is set.
   * @return true if the debug-mask bit for "level 4" (0x08) is set;
   * false if not.
   */
  public static boolean isDebugLevel4() {
    return logger.isDebugLevel4();
  }

  /**
   * Set the logger.
   * @param logger the logger.
   */
  public static void setLogMgr(ILogger logger) {
    LogMgr.logger = logger;
  }

  /**
   * Posts a debug message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public static void usrMsgDebug(String msgStr) {
    logger.usrMsgDebug(msgStr);
  }

  /**
   * Posts an error message string for this module.  (Indicates a fatal
   * error that will result in an aborted module startup.)  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public static void usrMsgError(String msgStr) {
    logger.usrMsgError(msgStr);
  }

  /**
   * Posts an informational message string for this module.  A prompt
   * string containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public static void usrMsgInfo(String msgStr) {
    logger.usrMsgInfo(msgStr);
  }

  /**
   * Posts a log message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public static void usrMsgLog(String msgStr) {
    logger.usrMsgLog(msgStr);
  }

  /**
   * Posts a warning message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public static void usrMsgWarning(String msgStr) {
    logger.usrMsgWarning(msgStr);
  }

  // private constructor; static access only
  private LogMgr() {
  }
}
