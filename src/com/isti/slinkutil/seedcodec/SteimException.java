package com.isti.slinkutil.seedcodec;

/**
 *  A type of exception specific to problems encountered with
 *  Steim compression.
 *
 *  @author Robert Casey
 *  @version 11/20/2002
 */
public class SteimException extends CodecException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Create a Steim exception.
   */
  public SteimException() {
    super();
  }

  /**
   * Create a Steim exception.
   * @param s the reason for the exception.
   */
  public SteimException(String s) {
    super(s);
  }
}
