/**
 * Btime.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package com.isti.slinkutil.seisFile.mseed;

import com.isti.slinkutil.Utility;

/**
 * The blockette time.
 */
public class Btime {

  /**
   * Expects the btime to be a byte array pointing at the beginning of a btime
   * segment
   * @param btime the btime.
   * @return true if the bytes need to be swapped to get a valid year
   */
  public static boolean shouldSwapBytes(byte[] btime) {
    return shouldSwapBytes(btime, 0);
  }

  /**
   * Expects btime to be a byte array pointing at the beginning of a btime
   * segment.
   * 
   * Time capsule: note that year 2056 as a short byte swaps to itself, so whomever
   * is maintaining this code off in the distant future, 49 years from now as
   * I write this in 2007, should find some other header to use for byte swap checking!
   * 
   * Using the jday or tenthmilli doesn't help much as 1 byte swaps to 256, 256 to 1 and 257 to itself.
   * 
   * If mseed was going to support little endian headers they should have put in a damn flag!
   *  - HPC
   * @param btime the btime.
   * @param offset the starting offset.
   * 
   * @return - true if the bytes need to be swapped to get a valid year
   */
  public static boolean shouldSwapBytes(byte[] btime, int offset) {
    int year = Utility.uBytesToInt(btime[0 + offset], btime[1 + offset], false);
    return year < 1960 || year > 2055;
  }

  /** The hour. */
  public int hour = 0;

  /** The julian day. */
  public int jday = 1;

  /** The minute. */
  public int min = 0;

  /** The second. */
  public int sec = 0;

  /** The tenth of milliseconds. */
  public int tenthMilli = 0;

  /** The year. */
  public int year = 1960;

  /**
   * Creates an empty blockette time.
   */
  public Btime() {
  }

  /**
   * Creates a blockette time.
   * @param bytes the bytes.
   */
  public Btime(byte[] bytes) {
    this(bytes, 0);
  }

  /**
   * Creates a blockette time.
   * @param bytes the bytes.
   * @param offset the starting offset.
   */
  public Btime(byte[] bytes, int offset) {
    boolean byteSwapFlag = shouldSwapBytes(bytes, offset);
    year = Utility.uBytesToInt(bytes[offset], bytes[offset + 1], byteSwapFlag);
    jday = Utility.uBytesToInt(bytes[offset + 2], bytes[offset + 3],
        byteSwapFlag);
    hour = bytes[offset + 4] & 0xff;
    min = bytes[offset + 5] & 0xff;
    sec = bytes[offset + 6] & 0xff;
    // bytes[7] is unused (alignment)
    tenthMilli = Utility.uBytesToInt(bytes[offset + 8], bytes[offset + 9],
        byteSwapFlag);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Btime) {
      Btime oBtime = (Btime) o;
      return oBtime.year == year && oBtime.jday == jday && oBtime.hour == hour
          && oBtime.min == min && oBtime.sec == sec
          && oBtime.tenthMilli == tenthMilli;
    }
    return false;
  }

  /**
   * Get the blockette time as bytes.
   * @return the bytes.
   */
  public byte[] getAsBytes() {
    byte[] bytes = new byte[10];
    System.arraycopy(Utility.intToByteArray(year), 2, bytes, 0, 2);
    System.arraycopy(Utility.intToByteArray(jday), 2, bytes, 2, 2);
    System.arraycopy(Utility.intToByteArray(hour), 3, bytes, 4, 1);
    System.arraycopy(Utility.intToByteArray(min), 3, bytes, 5, 1);
    System.arraycopy(Utility.intToByteArray(sec), 3, bytes, 6, 1);
    System.arraycopy(Utility.intToByteArray(tenthMilli), 2, bytes, 8, 2);
    return bytes;
  }

  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + hour;
    result = PRIME * result + jday;
    result = PRIME * result + min;
    result = PRIME * result + sec;
    result = PRIME * result + tenthMilli;
    result = PRIME * result + year;
    return result;
  }

  public String toString() {
    return "BTime(" + year + ":" + jday + ":" + hour + ":" + min + ":" + sec
        + "." + tenthMilli + ")";
  }
}
