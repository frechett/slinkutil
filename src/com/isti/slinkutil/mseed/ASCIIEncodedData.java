//ASCIIEncodedData.java:  Defines ASCII encoded data.
//
//  9/29/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil.mseed;

import com.isti.slinkutil.SLinkUtilFns;

/**
 * Class ASCIIEncodedData defines ASCII encoded data.
 */
public class ASCIIEncodedData implements EncodedData {
  /**
   * ASCII encoding format (used in B1000.)
   */
  public static final byte ASCII_ENCODING_FORMAT = 0;

  /** The encoded data. */
  private final byte[] encodedData;

  /**
   * Creates a ASCII Codec.
   * @param s the text.
   */
  public ASCIIEncodedData(String s) {
    encodedData = SLinkUtilFns.getBytes(s);
  }

  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @return byte array containing the encoded, compressed data.
   */
  public byte[] getEncodedData() {
    return encodedData;
  }

  /**
   * Returns the encoding format.
   * @return the encoding format.
   */
  public byte getEncodingFormat() {
    return ASCII_ENCODING_FORMAT;
  }

  /**
   * Return the number of data samples.
   * @return the number of samples.
   */
  public int getNumSamples() {
    return encodedData.length;
  }

  /**
   * Determines if the data is full.
   * @return true if the data is full, false otherwise.
   */
  public boolean isFull() {
    return true;
  }
}
