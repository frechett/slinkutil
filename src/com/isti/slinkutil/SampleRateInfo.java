//SampleRateInfo.java:  Defines the sample rate information.
//
//  9/15/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil;

/**
 * Class SampleRateInfo defines the sample rate information.
 */
public class SampleRateInfo {
	/**
	 * Get the sample rate derived from sample rate factor and the sample rate
	 * multiplier. This was lifted from
	 * edu.sc.seis.seisFile.mseed.DataHeader.getSampleRate().
	 * 
	 * @param factor
	 *            the sample rate factor.
	 * @param multiplier
	 *            the sample rate multiplier.
	 * @return the sample rate
	 */
	public static double getSampleRate(double factor, double multiplier) {
		double sampleRate = 10000.0; // default (impossible) value;
		if ((factor * multiplier) != 0.0) { // in the case of log records
			sampleRate = (java.lang.Math.pow(java.lang.Math.abs(factor),
					(factor / java.lang.Math.abs(factor))) * java.lang.Math
					.pow(java.lang.Math.abs(multiplier),
							(multiplier / java.lang.Math.abs(multiplier))));
		}
		return sampleRate;
	}

	/**
	 * Get the sample rate.
	 * 
	 * @param dataInfo
	 *            the data information.
	 * @return the sample rate.
	 */
	public static double getSampleRate(IDataInfo dataInfo) {
		return getSampleRate(dataInfo.getFirstTimeStamp(),
				dataInfo.getLastTimeStamp(), dataInfo.getNumSamples());
	}

	/**
	 * Get the sample rate.
	 * 
	 * @param startTime
	 *            the start time.
	 * @param endTime
	 *            the end time.
	 * @param numSamples
	 *            the number of samples.
	 * @return the sample rate.
	 */
	public static double getSampleRate(long startTime, long endTime,
			int numSamples) {
		return (numSamples - 1) / ((endTime - startTime) / 1000.0);
	}

	/**
	 * Get the sample rate.
	 * 
	 * @param startTime
	 *            the start time.
	 * @param endTime
	 *            the end time.
	 * @param numSamples
	 *            the number of samples.
	 * @return the sample rate.
	 */
	public static double getSampleRate(SeedTime startTime, SeedTime endTime,
			int numSamples) {
		return getSampleRate(startTime.getTime(), endTime.getTime(), numSamples);
	}

	/** The sample rate factor. */
	private final short factor;

	/** The sample rate multiplier. */
	private final short multiplier;

	/**
	 * Create the sample rate information.
	 * 
	 * @param sampleRate
	 *            the sample rate.
	 */
	public SampleRateInfo(double sampleRate) {
		multiplier = 1;
		if (sampleRate > 0 && sampleRate < 1.) {
			factor = (short) (-1.0 / sampleRate);
		} else {
			factor = (short) sampleRate;
		}
	}

	/**
	 * Create the sample rate information.
	 * 
	 * @param factor
	 *            the sample rate factor.
	 * @param multiplier
	 *            the sample rate multiplier.
	 */
	public SampleRateInfo(short factor, short multiplier) {
		this.factor = factor;
		this.multiplier = multiplier;
	}

	/**
	 * Get the sample rate.
	 * 
	 * @return the sample rate.
	 */
	public double getSampleRate() {
		return getSampleRate(factor, multiplier);
	}

	/**
	 * Get the sample rate factor.
	 * 
	 * @return the sample rate factor.
	 */
	public short getSampleRateFactor() {
		return factor;
	}

	/**
	 * Get the sample rate multiplier.
	 * 
	 * @return the sample rate multiplier.
	 */
	public short getSampleRateMultiplier() {
		return multiplier;
	}

	/**
	 * Get a string representation of the sample rate.
	 * 
	 * @return a string representation of the sample rate.
	 */
	public String toString() {
		return getSampleRate() + " (" + factor + ", " + multiplier + ")";
	}
}
