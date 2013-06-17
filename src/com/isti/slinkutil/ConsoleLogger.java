package com.isti.slinkutil;

/**
 * The console logger.
 */
public class ConsoleLogger implements ILogger {
  /** The debug mask value. */
  private int debugMask = NO_LEVEL;

  /**
   * Checks the given a debug mask against this module's current mask.
   * @param maskVal mask value to be checked.
   * @return true if there are corresponding bits set, false if not.
   */
  public boolean checkDebugMask(int maskVal) {
    return (debugMask & maskVal) != 0;
  }

  /**
   * Get the debug mask.
   * @return the debug mask.
   */
  public int getDebugMask() {
    return debugMask;
  }

  /**
   * Checks if the debug-mask bit for "level 1" (0x01) is set.
   * @return true if the debug-mask bit for "level 1" (0x01) is set;
   * false if not.
   */
  public boolean isDebugLevel1() {
    return checkDebugMask(LEVEL_1);
  }

  /**
   * Checks if the debug-mask bit for "level 2" (0x02) is set.
   * @return true if the debug-mask bit for "level 2" (0x02) is set;
   * false if not.
   */
  public boolean isDebugLevel2() {
    return checkDebugMask(LEVEL_2);
  }

  /**
   * Checks if the debug-mask bit for "level 3" (0x04) is set.
   * @return true if the debug-mask bit for "level 3" (0x04) is set;
   * false if not.
   */
  public boolean isDebugLevel3() {
    return checkDebugMask(LEVEL_3);
  }

  /**
   * Checks if the debug-mask bit for "level 4" (0x08) is set.
   * @return true if the debug-mask bit for "level 4" (0x08) is set;
   * false if not.
   */
  public boolean isDebugLevel4() {
    return checkDebugMask(LEVEL_4);
  }

  /**
   * Set the debug mask.
   * @param debugMask the debugMask to set
   * @return this logger.
   */
  public ILogger setDebugMask(int debugMask) {
    this.debugMask = debugMask;
    return this;
  }

  /**
   * Posts a debug message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgDebug(String msgStr) {
    System.out.println(msgStr); // send output to console
  }

  /**
   * Posts an error message string for this module.  (Indicates a fatal
   * error that will result in an aborted module startup.)  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgError(String msgStr) {
    System.err.println(msgStr); // send output to console
  }

  /**
   * Posts an informational message string for this module.  A prompt
   * string containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgInfo(String msgStr) {
    System.out.println(msgStr); // send output to console
  }

  /**
   * Posts a log message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgLog(String msgStr) {
    System.out.println(msgStr); // send output to console
  }

  /**
   * Posts a warning message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgWarning(String msgStr) {
    System.err.println(msgStr); // send output to console
  }
}
