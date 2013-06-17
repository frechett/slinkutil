package com.isti.slinkutil.seisFile.mseed;

/**
 * Blockette1000.java
 *
 *
 * Created: Fri Apr  2 14:51:42 1999
 *
 * @author Philip Crotwell
 * @version
 */

import java.io.IOException;
import java.io.Writer;

/**
 * The data only SEED blockette.
 */
public class Blockette1000 extends DataBlockette {
  /** The blockette size. */
  public static final int B1000_SIZE = 8;
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an empty data only SEED blockette.
   */
  public Blockette1000() {
    super(B1000_SIZE);
  }

  /**
   * Creates a data only SEED blockette.
   * @param info the bytes.
   * @param swapBytes true to swap bytes, false otherwise.
   */
  public Blockette1000(byte[] info, boolean swapBytes) {
    super(info, swapBytes);
    trimToSize(B1000_SIZE);
  }

  /**
   * Get the value of dataRecordLengthByte.
   * @return Value of dataRecordLengthByte.
   */
  public int getDataRecordLength() {
    if (getDataRecordLengthByte() < 31) {
      return (0x01 << getDataRecordLengthByte());
    } else {
      throw new RuntimeException("Data Record Length exceeds size of int");
    }
  }

  /**
   * Get the value of dataRecordLengthByte.
   * @return Value of dataRecordLengthByte.
   */
  public byte getDataRecordLengthByte() {
    return info[6];
  }

  /**
   * Get the value of encodingFormat.
   * @return Value of encodingFormat.
   */
  public byte getEncodingFormat() {
    return info[4];
  }

  /**
   * Get the blockette name.
   * @return the blockette name.
   */
  public String getName() {
    return "Data Only SEED Blockette";
  }

  /**
   * Get the value of reserved.
   * @return Value of reserved.
   */
  public byte getReserved() {
    return info[7];
  }

  /**
   * Get the blockette size.
   * @return the blockette size.
   */
  public int getSize() {
    return B1000_SIZE;
  }

  /**
   * Get the blockette type.
   * @return the blockette type.
   */
  public int getType() {
    return 1000;
  }

  /**
   * Get the value of wordOrder.
   * @return Value of wordOrder.
   */
  public byte getWordOrder() {
    return info[5];
  }

  /**
   * Determine if the data is big endian.
   * @return true if big endian, false if little endian.
   */
  public boolean isBigEndian() {
    if (info[5] == 1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Determine if the data is little endian.
   * @return true if little endian, false if big endian.
   */
  public boolean isLittleEndian() {
    return !isBigEndian();
  }

  /**
   * Set the value of dataRecordLength.
   * @param v  Value to assign to dataRecordLength.
   */
  public void setDataRecordLength(byte v) {
    info[6] = v;
  }

  /**
   * Set the value of encodingFormat.
   * @param v  Value to assign to encodingFormat.
   */
  public void setEncodingFormat(byte v) {
    info[4] = v;
  }

  /**
   * Set the value of reserved.
   * @param v  Value to assign to reserved.
   */
  public void setReserved(byte v) {
    info[7] = v;
  }

  /**
   * Set the value of wordOrder.
   * @param v  Value to assign to wordOrder.
   */
  public void setWordOrder(byte v) {
    info[5] = v;
  }

  public String toString() {
    return super.toString() + "  format=" + getEncodingFormat();
  }

  public void writeASCII(Writer out) throws IOException {
    out.write("Blockette1000 " + getEncodingFormat() + " " + getWordOrder()
        + " " + getDataRecordLengthByte());
  }

} // Blockette1000
