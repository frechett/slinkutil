package com.isti.slinkutil.seisFile.mseed;

/**
 * BlocketteUnknown.java
 * 
 * 
 * Created: Mon Apr 5 15:48:51 1999
 * 
 * @author Philip Crotwell
 * @version
 */
import java.io.IOException;
import java.io.Writer;

/**
 * The Blockette UNKNOWN.
 */
public class BlocketteUnknown extends Blockette {

  protected byte[] info;

  protected boolean swapBytes;

  protected int type;

  /**
   * Create the Blockette UNKNOWN.
   * @param info the bytes.
   * @param type the blockette type.
   * @param swapBytes true to swap bytes, false otherwise.
   */
  public BlocketteUnknown(byte[] info, int type, boolean swapBytes) {
    this.info = info;
    this.type = type;
    this.swapBytes = swapBytes;
  }

  public String getName() {
    return "Unknown";
  }

  public int getSize() {
    return info.length;
  }

  /**
   * Get the swap bytes flag.
   * @return true if swapping bytes, false otherwise.
   */
  public boolean getSwapBytes() {
    return swapBytes;
  }

  public int getType() {
    return type;
  }

  public byte[] toBytes() {
    return info;
  }

  public void writeASCII(Writer out) throws IOException {
    out.write("Blockette UNKNOWN");
  }
} // BlocketteUnknown
