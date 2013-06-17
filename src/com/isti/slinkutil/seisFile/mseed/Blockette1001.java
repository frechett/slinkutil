//Blockette1001:  Creates blockette 1001.
//
// 11/01/2010 -- [KF]  Initial version.
//

package com.isti.slinkutil.seisFile.mseed;

import java.io.IOException;
import java.io.Writer;

/**
 * The data extension blockette.
 */
public class Blockette1001 extends DataBlockette {
  /** The blockette size. */
  public static final int B1001_SIZE = 8;

  /** The frame count index. */
  private static final int frameCountIndex = 7;

  /** The microsecond index. */
  private static final int microSecondsIndex = 5;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** The timing quality index. */
  private static final int timingQualityIndex = 4;

  /**
   * Create the blockette 1001.
   */
  public Blockette1001() {
    super(B1001_SIZE);
  }

  /**
   * Get the frame count.
   * @return the frame count.
   */
  public byte getFrameCount() {
    return info[frameCountIndex];
  }

  /**
   * Get the number of microseconds.
   * @return the number of microseconds.
   */
  public byte getMicroSeconds() {
    return info[microSecondsIndex];
  }

  /**
   * Get the name.
   * @return the name.
   */
  public String getName() {
    return "Data Extension Blockette";
  }

  /**
   * Get the size.
   * @return the size.
   */
  public int getSize() {
    return B1001_SIZE;
  }

  /**
   * Get the timing quality.
   * @return the timing quality.
   */
  public byte getTimingQuality() {
    return info[timingQualityIndex];
  }

  /**
   * Get the type.
   * @return the type.
   */
  public int getType() {
    return 1001;
  }

  /**
   * Set the frame count.
   * @param b the frame count (maximum of 63).
   */
  public void setFrameCount(byte b) {
    info[frameCountIndex] = b;
  }

  /**
   * Set the number of microseconds.
   * @param b the number of microseconds (0-99).
   */
  public void setMicroSeconds(byte b) {
    info[microSecondsIndex] = b;
  }

  /**
   * Set the timing quality.
   * @param b the timing quality (0-100%).
   */
  public void setTimingQuality(byte b) {
    info[timingQualityIndex] = b;
  }

  /**
   * Write the ASCII.
   * @param out the output.
   */
  public void writeASCII(Writer out) throws IOException {
    out.write("Blockette1001 " + getTimingQuality());
  }
}
