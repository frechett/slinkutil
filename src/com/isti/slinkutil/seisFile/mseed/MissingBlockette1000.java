package com.isti.slinkutil.seisFile.mseed;

/**
 * The missing blockette 1000 exception.
 */
public class MissingBlockette1000 extends SeedFormatException {

  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a missing blockette 1000 exception.
   */
  public MissingBlockette1000() {
    super();
  }

  /**
   * Creates a missing blockette 1000 exception.
   * @param s the message.
   */
  public MissingBlockette1000(String s) {
    super(s);
  }
}
