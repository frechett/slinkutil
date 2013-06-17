//DataEncoder.java:  Defines a data encoder.
//
//  9/18/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil.mseed;

/**
 * Interface DataEncoder defines a data encoder.
 */
public interface DataEncoder {
  /**
   * Encode the array of integer values.
   * @param samples the data points represented as signed integers.
   * @param samplesLength the samples length.
   * @return the encoded data or null if error.
   * @throws UnsupportedOperationException if integer samples are not supported.
   */
  public EncodedData encode(int[] samples, int samplesLength);

  /**
   * Get the bias.
   * @return the bias.
   */
  public int getBias();

  /**
   * Set the bias.
   * @param bias offset for use as a constant for the first difference,
   *          otherwise set to 0.
   */
  public void setBias(int bias);
}
