package com.isti.slinkutil.mseed;

/**
 * The abstract data encoder.
 * All classes extending this class should implement the 'getValueType' method
 * and override the supported encode method.
 */
public abstract class AbstractDataEncoder implements DataEncoder {
  /**
   * The bias offset for use as a constant for the first difference, otherwise
   * set to 0.
   */
  private int bias = 0;

  /**
   * Get the bias.
   * @return the bias.
   */
  public int getBias() {
    return bias;
  }

  /**
   * Set the bias.
   * @param bias offset for use as a constant for the first difference,
   *          otherwise set to 0.
   */
  public void setBias(int bias) {
    this.bias = bias;
  }
}
