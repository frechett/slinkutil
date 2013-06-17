//DataInfo.java:  Defines the data information.
//
//  9/15/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil;

/**
 * Class DataInfo defines the data information.
 */
public class DataInfo implements IDataInfo {
  /** The number of samples. */
  private final int numSamples;

  /** The sample rate. */
  private final double sampleRate;

  /** The samples. */
  private final Object samples;

  /** The start time. */
  private final SeedTime startSeedTime;

  /**
   * Creates the data information with the specified data index.
   * @param dataInfo the data information.
   * @param dataIndex the data index.
   */
  public DataInfo(IDataInfo dataInfo, int dataIndex) {
    this(dataInfo.getStartSeedTime().projectTime(dataIndex,
        dataInfo.getSampleRate()), dataInfo.getSampleRate(), dataInfo
        .getSamples(), dataInfo.getNumSamples());
  }

  /**
   * Creates the miniSEED input.
   * @param startTime message start time.
   * @param sampleRate the sample rate.
   * @param samples the samples. This should either be an array of integer
   * values or an object implementing the 'ISamples' interface.
   * @param numSamples the number of samples.
   * @throws IllegalArgumentException if the samples are invalid.
   */
  public DataInfo(SeedTime startTime, double sampleRate, Object samples,
      int numSamples) throws IllegalArgumentException {
    if (numSamples < 0) {
      throw new IllegalArgumentException("invalid number of samples: "
          + numSamples);
    }
    // if there are samples
    if (numSamples != 0) {
      if (samples == null) {
        throw new IllegalArgumentException("samples may not be null");
      } else if (samples instanceof int[]) {
        final int[] sa = (int[]) samples;
        if (numSamples > sa.length) {
          throw new IllegalArgumentException("number of samples (" + numSamples
              + ") is larger than data length (" + sa.length + ")");
        }
      } else if (!(samples instanceof ISamples)) {
        throw new IllegalArgumentException("unexpected samples: "
            + samples.getClass());
      }
    }
    this.startSeedTime = startTime;
    this.sampleRate = sampleRate;
    this.samples = samples;
    this.numSamples = numSamples;
  }

  /**
   * Get the I/O and clock flags.
   * @return the I/O and clock flags.
   */
  public byte getIoClockFlags() {
    return DEFAULT_IO_CLOCK_FLAGS;
  }

  /**
   * Return the number of samples.
   * @return the number of samples.
   */
  public int getNumSamples() {
    return numSamples;
  }

  /**
   * Return the sample rate.
   * @return the sample rate.
   */
  public double getSampleRate() {
    return sampleRate;
  }

  /**
   * Get the samples. This should either be an array of integer
   * values or an object implementing the 'ISamples' interface.
   * @return the samples.
   */
  public Object getSamples() {
    return samples;
  }

  /**
   * Returns the message start time.
   * @return The message start time.
   */
  public SeedTime getStartSeedTime() {
    return startSeedTime;
  }

  /**
   * Get the current time-quality value.
   * @return the current time-quality value or 0 if none available.
   */
  public byte getTimeQualityValue() {
    return DEFAULT_TIMING_QUALITY;
  }
}
