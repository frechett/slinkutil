package com.isti.slinkutil.mseed;

import com.isti.slinkutil.Utility;

/**
 * Float encoded data.
 */
public class FloatEncodedData implements EncodedData {
  /**
   * IEEE floating point data encoding format (used in B1000.)
   */
  public static final byte FLOAT_ENCODING_FORMAT = 4;

  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @param samples the samples.
   * @param numSamples the number of samples.
   * @return byte array containing the encoded, compressed data.
   */
  public static byte[] getEncodedData(final float[] samples,
      final int numSamples) {
    final byte[] encodedData = new byte[numSamples
        * FloatCodec.BYTES_PER_SAMPLE];
    for (int index = 0; index < numSamples; index++) {
      Utility.intToBytes(Utility.floatToIntBits(samples[index]), encodedData,
          index * FloatCodec.BYTES_PER_SAMPLE);
    }
    return encodedData;
  }

  /**
   * Return the compressed byte representation of the data for inclusion in a
   * data record.
   * @param samples the samples.
   * @param numSamples the number of samples.
   * @return byte array containing the encoded, compressed data.
   */
  public static byte[] getEncodedData(final Object samples, final int numSamples) {
    if (samples instanceof float[]) {
      return getEncodedData((float[]) samples, numSamples);
    } else {
      return IntEncodedData.getEncodedData((int[]) samples, numSamples);
    }
  }

  /** The number of samples. */
  private final int numSamples;

  /** The samples. */
  private final Object samples;

  /**
   * Create the float encoded data.
   * @param samples the samples.
   * @param numSamples the number of samples.
   */
  public FloatEncodedData(final float[] samples, final int numSamples) {
    this.samples = samples;
    this.numSamples = numSamples;
  }

  /**
   * Create the float encoded data.
   * @param samples the samples.
   * @param numSamples the number of samples.
   */
  public FloatEncodedData(final int[] samples, final int numSamples) {
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
    return FLOAT_ENCODING_FORMAT;
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
