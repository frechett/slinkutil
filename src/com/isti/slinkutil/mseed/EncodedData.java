//EncodedData.java:  Defines encoded data.
//
//  9/18/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil.mseed;

/**
 * Interface EncodedData defines encoded data.
 */
public interface EncodedData {
  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @return byte array containing the encoded, compressed data.
   */
  public byte[] getEncodedData();

  /**
   * Returns the encoding format.
   * @return the encoding format.
   */
  public byte getEncodingFormat();

  /**
   * Return the number of data samples.
   * @return the number of samples.
   */
  public int getNumSamples();

  /**
   * Determines if the data is full.
   * @return true if the data is full, false otherwise.
   */
  public boolean isFull();
}
