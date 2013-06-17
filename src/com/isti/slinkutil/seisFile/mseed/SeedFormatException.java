package com.isti.slinkutil.seisFile.mseed;

/**
 * The SEED format exception.
 */
public class SeedFormatException extends Exception {

  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  /**
   * Creates the SEED format exception.
   */
  public SeedFormatException() {
    super();
  }

  /**
   * Creates the SEED format exception.
   * @param s the message.
   */
  public SeedFormatException(String s) {
    super(s);
  }
}
