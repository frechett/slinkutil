/**
 * Blockette100.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package com.isti.slinkutil.seisFile.mseed;

import java.io.IOException;
import java.io.Writer;

import com.isti.slinkutil.Utility;

/**
 * The sample rate blockette.
 */
public class Blockette100 extends DataBlockette {

  /** The blockette size. */
  public static final int B100_SIZE = 12;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Create a sample rate blockette.
   */
  public Blockette100() {
    super(B100_SIZE);
  }

  /**
   * Create a sample rate blockette.
   * @param info the bytes.
   * @param swapBytes true to swap bytes, false otherwise.
   */
  public Blockette100(byte[] info, boolean swapBytes) {
    super(info, swapBytes);
    trimToSize(B100_SIZE);
  }

  /**
   * Get the actual sample rate.
   * @return the actual sample rate.
   */
  public float getActualSampleRate() {
    int bits = Utility.bytesToInt(info[4], info[5], info[6], info[7], false);
    return Float.intBitsToFloat(bits);
  }

  /**
   * Get the blockette name.
   * @return the blockette name.
   */
  public String getName() {
    return "Sample Rate Blockette";
  }

  /**
   * Get the blockette size.
   * @return the blockette size.
   */
  public int getSize() {
    return B100_SIZE;
  }

  /**
   * Get the blockette type.
   * @return the blockette type.
   */
  public int getType() {
    return 100;
  }

  /**
   * Sets the actual sample rate.
   * @param actualSampleRate the actual sample rate.
   */
  public void setActualSampleRate(float actualSampleRate) {
    Utility.insertFloat(actualSampleRate, info, 4);
  }

  public void writeASCII(Writer out) throws IOException {
    out.write("Blockette100 " + getActualSampleRate());
  }
}
