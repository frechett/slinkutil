package com.isti.slinkutil.seisFile.mseed;

import java.io.IOException;
import java.io.Writer;

/**
 * Superclass of all seed blockettes. The actual blockettes do not store either
 * their blockette type or their length in the case of ascii blockettes or next
 * blockettes offset in the case of data blockettes as these are either already
 * known (ie type) or may change after reading due to data changes. Instead each
 * of these values are calculated based on the data.
 */
public abstract class Blockette {

  /**
   * Parse the blockette.
   * @param type the blockette type.
   * @param bytes the bytes for the blockette.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the blockette.
   * @throws IOException if an I/O Exception occurs.
   */
  public static Blockette parseBlockette(int type, byte[] bytes,
      boolean swapBytes) throws IOException {
    switch (type) {
    case 100:
      return new Blockette100(bytes, swapBytes);
    case 200:
      return new Blockette200(bytes, swapBytes);
    case 1000:
      return new Blockette1000(bytes, swapBytes);
    case 2000:
      return new Blockette2000(bytes, swapBytes);
    default:
      return new BlocketteUnknown(bytes, type, swapBytes);
    }
  }

  /**
   * Get the blockette name.
   * @return the blockette name.
   */
  public abstract String getName();

  /**
   * Get the blockette size.
   * @return the blockette size.
   */
  public abstract int getSize();

  /**
   * Get the blockette type.
   * @return the blockette type.
   */
  public abstract int getType();

  /**
   * Get the blockette bytes.
   * @return the blockette bytes.
   */
  public abstract byte[] toBytes();

  /**
   * Get a string representation of the blockette.
   * @return the string representation of the blockette.
   */
  public String toString() {
    return toString(false);
  }

  /**
   * Get a string representation of the blockette.
   * @param verbose true for verbose, false otherwise.
   * @return the string representation of the blockette.
   */
  public String toString(boolean verbose) {
    String s = getType() + ": " + getName();
    if (verbose) {
      final byte[] byteArray = toBytes();
      if (byteArray != null && byteArray.length > 0) {
        s += " [" + byteArray[0];
        for (int i = 1; i < byteArray.length; i++) {
          s += "," + byteArray[i];
        }
        s += "]";
      }
    }
    return s;
  }

  /**
   * Method writeASCII
   * @param out a Writer
   * @throws IOException if an I/O Exception occurs.
   */
  public abstract void writeASCII(Writer out) throws IOException;
}
