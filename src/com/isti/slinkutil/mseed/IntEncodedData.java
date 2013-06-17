package com.isti.slinkutil.mseed;

import com.isti.slinkutil.Utility;

/**
 * Integer encoded data.
 */
public class IntEncodedData implements EncodedData {
  /**
   * 32-bit integer data encoding format (used in B1000.)
   */
  public static final byte INT_ENCODING_FORMAT = 3;

  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @param samples the samples.
   * @param numSamples the number of samples.
   * @return byte array containing the encoded, compressed data.
   */
  public static byte[] getEncodedData(final int[] samples, final int numSamples) {
    final byte[] encodedData = new byte[numSamples * IntCodec.BYTES_PER_SAMPLE];
    for (int index = 0; index < numSamples; index++) {
      Utility.intToBytes(samples[index], encodedData, index
          * IntCodec.BYTES_PER_SAMPLE);
    }
    return encodedData;
  }

  /** The number of samples. */
  private final int numSamples;

  /** The samples. */
  private final int[] samples;

  /**
   * Create the encoded data.
   * @param samples the samples.
   * @param numSamples the number of samples.
   */
  public IntEncodedData(final int[] samples, final int numSamples) {
    this.samples = samples;
    this.numSamples = numSamples;
  }

  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @return byte array containing the encoded, compressed data.
   */
  public byte[] getEncodedData() {
    return getEncodedData(samples, numSamples);
  }

  /**
   * Returns the encoding format.
   * @return the encoding format.
   */
  public byte getEncodingFormat() {
    return INT_ENCODING_FORMAT;
  }

  /**
   * Return the number of data samples.
   * @return the number of samples.
   */
  public int getNumSamples() {
    return numSamples;
  }

  /**
   * Determines if the data is full.
   * @return true if the data is full, false otherwise.
   */
  public boolean isFull() {
    return false;
  }
}
