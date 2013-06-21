package com.isti.slinkutil;

/**
 * The logger.
 */
public interface ILogger {
  /** The no level mask value. */
  public static final int NO_LEVEL = 0;

  /** The level 1 mask value. */
  public static final int LEVEL_1 = 0x01;

  /** The level 2 mask value. */
  public static final int LEVEL_2 = 0x02;

  /** The level 3 mask value. */
  public static final int LEVEL_3 = 0x04;

  /** The level 4 mask value. */
  public static final int LEVEL_4 = 0x08;

  /** The all level mask value. */
  public static final int ALL_LEVEL = 0xFFFF;

  /**
   * Checks if the debug-mask bit for "level 1" (0x01) is set.
   * @return true if the debug-mask bit for "level 1" (0x01) is set;
   * false if not.
   */
  public boolean isDebugLevel1();

  /**
   * Checks if the debug-mask bit for "level 2" (0x02) is set.
   * @return true if the debug-mask bit for "level 2" (0x02) is set;
   * false if not.
   */
  public boolean isDebugLevel2();

  /**
   * Checks if the debug-mask bit for "level 3" (0x04) is set.
   * @return true if the debug-mask bit for "level 3" (0x04) is set;
   * false if not.
   */
  public boolean isDebugLevel3();

  /**
   * Checks if the debug-mask bit for "level 4" (0x08) is set.
   * @return true if the debug-mask bit for "level 4" (0x08) is set;
   * false if not.
   */
  public boolean isDebugLevel4();

  /**
   * Posts a debug message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgDebug(String msgStr);

  /**
   * Posts an error message string for this module.  (Indicates a fatal
   * error that will result in an aborted module startup.)  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgError(String msgStr);

  /**
   * Posts an informational message string for this module.  A prompt
   * string containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgInfo(String msgStr);

  /**
   * Posts a log message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgLog(String msgStr);

  /**
   * Posts a warning message string for this module.  A prompt string
   * containing the name of the module is prepended.
   * @param msgStr message string.
   */
  public void usrMsgWarning(String msgStr);
}
