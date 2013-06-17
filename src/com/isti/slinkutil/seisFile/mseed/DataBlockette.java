package com.isti.slinkutil.seisFile.mseed;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import com.isti.slinkutil.Utility;

/**
 * DataBlockette.java
 *
 *
 * Created: Thu Apr  8 12:40:56 1999
 *
 * @author Philip Crotwell
 * @version
 */
public abstract class DataBlockette extends Blockette implements Serializable {

  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  /** The bytes. */
  protected byte[] info;

  /** The swap bytes flag is true to swap bytes, false otherwise. */
  protected boolean swapBytes;

  /**
   * Create a data blockette.
   * @param info the bytes.
   * @param swapBytes true to swap bytes, false otherwise.
   */
  public DataBlockette(byte[] info, boolean swapBytes) {
    this.info = info;
    this.swapBytes = swapBytes;
  }

  /**
   * Create an empty data blockette.
   * @param size the size of the blockette in bytes.
   */
  public DataBlockette(int size) {
    this.info = new byte[size];
    System.arraycopy(Utility.intToByteArray(getType()), 2, info, 0, 2);
  }

  /**
   * Get the bytes.
   * @return the bytes.
   */
  public byte[] toBytes() {
    return toBytes((short) 0);
  }

  /**
   * Get the bytes.
   * @param nextOffset the next offset.
   * @return the bytes.
   */
  public byte[] toBytes(short nextOffset) {
    System.arraycopy(Utility.intToByteArray(nextOffset), 2, info, 2, 2);
    return info;
  }

  /** For use by subclasses that want to ensure that they are of a given size.
   * @throws IllegalArgumentException if the size is larger than the number of bytes
   */
  protected void trimToSize(int size) {
    if (info.length < size) {
      throw new IllegalArgumentException("Blockette " + getType()
          + " must have " + size + " bytes, but got " + info.length);
    }
    if (info.length > size) {
      // must be extra junk at end, trim
      byte[] tmp = new byte[size];
      System.arraycopy(info, 0, tmp, 0, size);
      info = tmp;
    }
  }

  /**
   * Write the blockette.
   * @param dos the data output stream.
   * @param nextOffset the next offset.
   * @throws IOException if an I/O Exception occurs.
   */
  public void write(DataOutputStream dos, short nextOffset) throws IOException {
    dos.write(toBytes(nextOffset));
  }

} // DataBlockette
