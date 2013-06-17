package com.isti.slinkutil.mseed;

/**
 * Integer value codec.
 */
public class IntCodec extends AbstractDataEncoder {
  /** The number of bytes per sample. */
  public static int BYTES_PER_SAMPLE = 4;

  /**
   * Encode the array of integer values.
   * @param samples the data points represented as signed integers.
   * @param samplesLength the samples length.
   * @return the encoded data or null if error.
   * @throws UnsupportedOperationException if integer samples are not supported.
   */
  public EncodedData encode(int[] samples, int samplesLength) {
    return new IntEncodedData(samples, samplesLength);
  }
}
