package com.isti.slinkutil.seedcodec;

import java.lang.Exception;

/**
 * CodecException.java
 *
 *
 * Created: Fri Nov 22 15:31:06 2002
 *
 * @author <a href="mailto:crotwell@Philip-Crotwells-Computer.local.">Philip Crotwell</a>
 * @version
 */
public class CodecException extends Exception {
  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  /**
   * Create a Codec exception.
   */
  public CodecException() {

  }

  /**
   * Create a Codec exception.
   * @param reason the reason for the exception.
   */
  public CodecException(String reason) {
    super(reason);
  }

}// CodecException
