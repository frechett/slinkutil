package com.isti.slinkutil.mseed;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.isti.slinkutil.BasicMessageManager;
import com.isti.slinkutil.ConsoleLogger;
import com.isti.slinkutil.DigChannel;
import com.isti.slinkutil.IConfigParams;
import com.isti.slinkutil.IDataInfo;
import com.isti.slinkutil.IDigChannel;
import com.isti.slinkutil.ILogger;
import com.isti.slinkutil.IMessageManager;
import com.isti.slinkutil.ISamples;
import com.isti.slinkutil.LogMgr;
import com.isti.slinkutil.MiniSeedMsgHldr;
import com.isti.slinkutil.SampleRateInfo;
import com.isti.slinkutil.StaChaNetLoc;

/**
 * MiniSeedGenerator test.
 */
public class MiniSeedGeneratorTest implements IConfigParams, IDataInfo,
		ISamples {
	/**
	 * MiniSeedGenerator test.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		new MiniSeedGeneratorTest().testMiniSeedGenerator();
		System.out.println("Done");
	}

	IConfigParams configParams;
	IDataInfo dataInfo;
	IDigChannel dChanObj;
	int debugMask = ILogger.NO_LEVEL; // ILogger.ALL_LEVEL;
	int dx;
	int max = 1000;
	int maxCount = 100;
	int maxNumSamples;
	IMessageManager messageManager;
	int min = 50;
	int numSamples;
	int[] sampleArray;
	long sampleDuration = 1000; // sample duration in milliseconds
	Object samples;
	int sign = 1;
	int sps = 100; // samples per second
	long startTime, endTime;

	int value = min;

	/**
	 * Create the MiniSeedGenerator test.
	 */
	public MiniSeedGeneratorTest() {
		final File outputDir = new File("output");
		outputDir.mkdirs();
		if (debugMask != ILogger.NO_LEVEL) {
			ILogger logger;
			if ((logger = LogMgr.getLogger()) instanceof ConsoleLogger) {
				((ConsoleLogger) logger).setDebugMask(debugMask);
			}
		}
		maxNumSamples = (int) (sps * (sampleDuration / 1000.));
		sampleArray = new int[maxNumSamples];
		dx = (max - min) / sps;
		configParams = this;
		dataInfo = this;
		// samples = sampleArray;
		samples = this;
		String stationIdStr = "sid";
		String channelIdStr = "cid";
		String networkCodeStr = "net";
		String locationIdStr = "loc";
		dChanObj = new DigChannel(new StaChaNetLoc(stationIdStr, channelIdStr,
				networkCodeStr, locationIdStr));
		BasicMessageManager messageManager = new BasicMessageManager() {
			/**
			 * Processes the miniSEED message.
			 * 
			 * @param miniSeedMsg
			 *            the miniSEED message.
			 */
			public void processMiniSeedMessage(MiniSeedMsgHldr miniSeedMsg) {
				super.processMiniSeedMessage(miniSeedMsg);
				try {
					miniSeedMsg.write(miniSeedMsg.getFile(outputDir));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};
		messageManager.setModConfigParams(configParams);
		this.messageManager = messageManager;
	}

	/**
	 * Get the codec.
	 * 
	 * @return the codec.
	 */
	public String getCodec() {
		// return IConfigParams.STEIM1_MINISEED_CODEC;
		// return IConfigParams.INTEGER_CODEC;
		return IConfigParams.STEIM2_MINISEED_CODEC;
	}

	/**
	 * Get the time stamp of the first sample value measured in milliseconds
	 * since epoch.
	 * 
	 * @return a positive value or zero if no samples.
	 */
	public long getFirstTimeStamp() {
		return startTime;
	}

	/**
	 * Get the time stamp of the last sample value measured in milliseconds
	 * since epoch.
	 * 
	 * @return a positive value or zero if no samples.
	 */
	public long getLastTimeStamp() {
		return endTime;
	}

	/**
	 * Get the maximum cache age in milliseconds.
	 * 
	 * @return the maximum cache age in milliseconds.
	 */
	public int getMaxCacheAge() {
		return 0;
	}

	/**
	 * Get the maximum cache size.
	 * 
	 * @return the maximum cache size.
	 */
	public int getMaxCacheSize() {
		return 0;
	}

	/**
	 * Get the number of samples.
	 * 
	 * @return the number of samples.
	 */
	public int getNumSamples() {
		return numSamples;
	}

	/**
	 * Get the sample at the specified index as an integer.
	 * 
	 * @param index
	 *            the index.
	 * @return the sample.
	 */
	public int getSampleAsInt(int index) {
		return sampleArray[index];
	}

	/**
	 * Get the sample rate.
	 * 
	 * @return the sample rate.
	 */
	public double getSampleRate() {
		return sps;
	}

	/**
	 * Get the samples. This should either be an array of integer values or an
	 * object implementing the 'ISamples' interface.
	 * 
	 * @return the samples.
	 */
	public Object getSamples() {
		return samples;
	}

	/**
	 * Get the selected channel names.
	 * 
	 * @return the selected channel names or null if none.
	 */
	public Set getSelectedChannelNames() {
		return null;
	}

	/**
	 * Test the MiniSeedGenerator.
	 */
	public void testMiniSeedGenerator() {
		final long firstTime = (System.currentTimeMillis() / 1000) * 1000;
		for (int count = 0; maxCount == 0 || count <= maxCount; count++) {
			startTime = firstTime + (count * sampleDuration);
			numSamples = 0;
			if (count < maxCount) {
				for (int i = 0; i < maxNumSamples;) {
					value += dx * sign;
					if (value > max) {
						sign = -1;
					} else if (value < min) {
						sign = 1;
					} else {
						sampleArray[i] = value;
						numSamples++;
						// System.out.println("[" + i + "]: " + data[i]);
						i++;
					}
				}
			}

			if (numSamples == 0) {
				continue;
			}

			try {
				final long timeDiff = Math.round((double) sampleDuration
						/ (double) numSamples * (double) (numSamples - 1));
				endTime = startTime + timeDiff;
				// calculate the sample rate
				double csps = SampleRateInfo.getSampleRate(dataInfo);
				if (csps != sps) {
					System.err.println("calculated sample rate " + csps
							+ " does not match sample rate " + sps + " ("
							+ timeDiff + ")");
				}
				messageManager.processMessage(dChanObj, dataInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			} finally {
				startTime = 0;
				endTime = 0;
			}
			try {
				Thread.sleep(sampleDuration);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
