package com.isti.slinkutil.mseed;

/**
 * Float value codec.
 */
public class FloatCodec extends AbstractDataEncoder {
  /** The number of bytes per sample. */
  public static int BYTES_PER_SAMPLE = 4;

  /**
   * Encode the array of values.
   * @param samples the data points represented as floats.
   * @param samplesLength the samples length.
   * @return the encoded data or null if error.
   */
  public EncodedData encode(int[] samples, int samplesLength) {
    return new FloatEncodedData(samples, samplesLength);
  }
}
