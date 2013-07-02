package com.isti.slinkutil;

/**
 * The data information.
 */
public interface IDataInfo {
	/** The default I/O clock flags. */
	public final static byte DEFAULT_IO_CLOCK_FLAGS = 32; // Clock locked

	/** The default timing quality. */
	public final static byte DEFAULT_TIMING_QUALITY = 0;

	/**
	 * Get the I/O and clock flags.
	 * 
	 * @return the I/O and clock flags.
	 */
	public byte getIoClockFlags();

	/**
	 * Get the number of samples.
	 * 
	 * @return the number of samples.
	 */
	public int getNumSamples();

	/**
	 * Get the sample rate.
	 * 
	 * @return the sample rate.
	 */
	public double getSampleRate();

	/**
	 * Get the samples. This should either be an array of integer values or an
	 * object implementing the 'ISamples' interface.
	 * 
	 * @return the samples.
	 */
	public Object getSamples();

	/**
	 * Get the start SEED time.
	 * 
	 * @return the start SEED time.
	 */
	public SeedTime getStartSeedTime();

	/**
	 * Get the current time-quality value.
	 * 
	 * @return the current time-quality value or 0 if none available.
	 */
	public byte getTimeQualityValue();
}
