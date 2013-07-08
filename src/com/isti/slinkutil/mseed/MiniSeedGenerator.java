//MiniSeedGenerator.java:  Creates 'miniSEED' messages.
//
//  9/15/2009 -- [KF]  Initial version.
//  9/13/2010 -- [KF]  Added support for Steim2 encoding.
//  9/14/2010 -- [ET]  Increased debug level on "No more data can fit"
//                     message.
//  9/22/2010 -- [KF]  Fixed size of data to encode buffer and
//                     decreased debug level on "No more data can fit"
//                     message.
//  9/28/2010 -- [KF]  Changed the sequence number to be by channel.
// 11/01/2010 -- [KF]  Added blockette 1001.
//

package com.isti.slinkutil.mseed;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.isti.slinkutil.BasicMessageManager;
import com.isti.slinkutil.IConfigParams;
import com.isti.slinkutil.IDataInfo;
import com.isti.slinkutil.IMessageManager;
import com.isti.slinkutil.IMessageNumber;
import com.isti.slinkutil.ISamples;
import com.isti.slinkutil.IStaChaNetLoc;
import com.isti.slinkutil.LogMgr;
import com.isti.slinkutil.MiniSeedMsgHldr;
import com.isti.slinkutil.SampleRateInfo;
import com.isti.slinkutil.SeedTime;
import com.isti.slinkutil.StaChaNetLoc;
import com.isti.slinkutil.seisFile.mseed.Blockette1000;
import com.isti.slinkutil.seisFile.mseed.Blockette1001;
import com.isti.slinkutil.seisFile.mseed.Btime;
import com.isti.slinkutil.seisFile.mseed.DataHeader;
import com.isti.slinkutil.seisFile.mseed.DataRecord;
import com.isti.slinkutil.seisFile.mseed.SeedFormatException;

/**
 * Class MiniSeedGenerator creates 'miniSEED' messages.
 */
public class MiniSeedGenerator implements MiniSeedConstants {
	/** The data header type code. */
	private final static char dataHeaderTypeCode = 'D';

	/** The default I/O clock flags. */
	public final static byte DEFAULT_IO_CLOCK_FLAGS = 32; // Clock locked

	/** The default timing quality. */
	public final static byte DEFAULT_TIMING_QUALITY = 0;

	/** The default continuation code. */
	private static final boolean defaultContinuationCode = false;

	/** The gap threshold in milliseconds. */
	private static long gapThreshold = 1000;

	/** The maximum number of Steim frames. */
	private final static int maxSteimFrames = SteimCodec
			.getFrames(PREFERRED_MAX_BYTE_LENGTH);

	/**
	 * Creates the miniSEED log record message.
	 * 
	 * @param s
	 *            the text.
	 * @param sequenceNum
	 *            the sequence number.
	 * @param messageNumber
	 *            the message number.
	 * @return the miniSEED message.
	 * @throws SeedFormatException
	 *             if a SEED format exception occurs.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	public static MiniSeedMsgHldr createLogRecordMsg(String s, int sequenceNum,
			IMessageNumber messageNumber) throws IOException,
			SeedFormatException {
		final SeedTime startTime = new SeedTime(messageNumber.getTimeCreated());
		return createLogRecordMsg(s, sequenceNum, messageNumber, startTime);
	}

	/**
	 * Creates the miniSEED log record message.
	 * 
	 * @param s
	 *            the text.
	 * @param sequenceNum
	 *            the sequence number.
	 * @param messageNumber
	 *            the message number.
	 * @param startTime
	 *            the start time.
	 * @return the miniSEED message.
	 * @throws SeedFormatException
	 *             if a SEED format exception occurs.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	public static MiniSeedMsgHldr createLogRecordMsg(String s, int sequenceNum,
			IMessageNumber messageNumber, SeedTime startTime)
			throws IOException, SeedFormatException {
		final String stationIdStr = "INFO";
		final String channelIdStr = "INF";
		final String networkCodeStr = "SL";
		final String locationIdStr = "  ";
		final IStaChaNetLoc staChaNetLoc = new StaChaNetLoc(stationIdStr,
				channelIdStr, networkCodeStr, locationIdStr);
		final ASCIIEncodedData encodedData = new ASCIIEncodedData(s);
		final int numSamples = encodedData.getNumSamples();
		DataHeader dataHeader = new DataHeader(sequenceNum, dataHeaderTypeCode,
				defaultContinuationCode);
		dataHeader.setStationIdentifier(staChaNetLoc.getStationCode());
		dataHeader.setChannelIdentifier(staChaNetLoc.getChannelCode());
		dataHeader.setNetworkCode(staChaNetLoc.getNetworkCode());
		dataHeader.setLocationIdentifier(staChaNetLoc.getLocationCode());
		dataHeader.setStartBtime(startTime.getBtime());
		dataHeader.setNumBlockettes((byte) 1);
		dataHeader.setNumSamples((short) numSamples);
		dataHeader.setDataBlocketteOffset((byte) PREFERRED_BEGINNING_OF_DATA);

		DataRecord dataRecord = new DataRecord(dataHeader);
		dataRecord.setRecordSize(PREFERRED_DATA_LENGTH);
		dataRecord.setData(encodedData.getEncodedData());

		Blockette1000 b1000 = new Blockette1000();
		b1000.setDataRecordLength(PREFERRED_DATA_RECORD_LENGTH);
		b1000.setEncodingFormat(encodedData.getEncodingFormat());
		b1000.setWordOrder(BIG_ENDIAN_WORD_ORDER);
		dataRecord.addBlockette(b1000);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(
				PREFERRED_DATA_LENGTH);
		final DataOutputStream dos = new DataOutputStream(baos);
		dataRecord.write(dos);
		final byte[] miniSeedData = baos.toByteArray();
		return new MiniSeedMsgHldr(staChaNetLoc, startTime, miniSeedData,
				numSamples, messageNumber);
	}

	/**
	 * Get the delta.
	 * 
	 * @param startTime
	 *            the start time.
	 * @param endTime
	 *            the end time.
	 * @param numSamples
	 *            the number of samples.
	 * @return the delta.
	 */
	public static double getDelta(final long startTime, final long endTime,
			final int numSamples) {
		double delta = 0.;
		if (numSamples > 1) {
			long timeSpan = endTime - startTime;
			delta = (double) timeSpan / (double) (numSamples - 1);
		}
		return delta;
	}

	/**
	 * Get the gap threshold.
	 * 
	 * @return the gap threshold in milliseconds.
	 */
	public static long getGapThreshold() {
		return gapThreshold;
	}

	/**
	 * Get the time stamp for the data with the specified data index.
	 * 
	 * @param dataInfo
	 *            the data information.
	 * @param dataIndex
	 *            the data index.
	 * @return the time stamp.
	 */
	public static long getTimeStamp(final long time, final double delta,
			final int dataIndex) {
		return time + (long) (dataIndex * delta);
	}

	/**
	 * Set the gap threshold.
	 * 
	 * @param gapThreshold
	 *            the gap threshold in milliseconds.
	 */
	public static void setGapThreshold(long gapThreshold) {
		MiniSeedGenerator.gapThreshold = gapThreshold;
	}

	/** The continuation code. */
	private boolean continuationCode = defaultContinuationCode;

	/** The data encoder. */
	private final DataEncoder dataEncoder;

	/** The data record sequence number. */
	private int dataRecordSequenceNum = 0;

	/** The data to encode. */
	private final int[] dataToEncode;

	/** The data to encode delta. */
	private double dataToEncodeDelta;

	/** The data to encode start time. */
	private long dataToEncodeEndTime = 0;

	/** The data to encode index. */
	private int dataToEncodeIndex = 0;

	/** The data to encode length. */
	private int dataToEncodeLength = 0;

	/** The data to encode start time. */
	private long dataToEncodeStartTime = 0;

	/** The maximum data to encode size. */
	private final int maxDataToEncodeSize;

	/** The message manager. */
	private final IMessageManager messageManager;

	/** The number of old samples to save. */
	private int numOldSamplesSave = 0;

	/** The station, channel, network and location. */
	private final IStaChaNetLoc staChaNetLoc;

	/**
	 * Creates the miniSEED generator.
	 * 
	 * @param staChaNetLoc
	 *            the station, channel, network and location.
	 * @param codec
	 *            the codec to use.
	 */
	public MiniSeedGenerator(IStaChaNetLoc staChaNetLoc, String codec) {
		this(staChaNetLoc, codec, (IMessageManager) null);
	}

	/**
	 * Creates the miniSEED generator.
	 * 
	 * @param staChaNetLoc
	 *            the station, channel, network and location.
	 * @param codec
	 *            the codec to use.
	 * @param messageManager
	 *            the message manager or null for default.
	 */
	public MiniSeedGenerator(IStaChaNetLoc staChaNetLoc, String codec,
			IMessageManager messageManager) {
		if (messageManager != null) {
			this.messageManager = messageManager;
		} else {
			this.messageManager = new BasicMessageManager();
		}
		this.staChaNetLoc = staChaNetLoc;
		// Get the maximum number of 32-bit words
		// Each Steim frame contains 1 32-bit control word and 15 32-bit words
		// The first frame in each record contains X0 and XN integration values
		// which takes 2 32-bit words
		final int maxNumberWords = maxSteimFrames * 15 - 2;
		if (IConfigParams.STEIM2_MINISEED_CODEC.equalsIgnoreCase(codec)) {
			// maximum is 7 4-bit differences per 32-bit word
			maxDataToEncodeSize = maxNumberWords * 7;
			dataEncoder = new Steim2Codec(maxSteimFrames);
		} else if (IConfigParams.STEIM1_MINISEED_CODEC.equalsIgnoreCase(codec)) {
			// maximum is 4 8-bit differences per 32-bit word
			maxDataToEncodeSize = maxNumberWords * 4;
			dataEncoder = new Steim1Codec(maxSteimFrames);
		} else if (IConfigParams.FLOAT_CODEC.equalsIgnoreCase(codec)) {
			maxDataToEncodeSize = PREFERRED_MAX_BYTE_LENGTH
					/ FloatCodec.BYTES_PER_SAMPLE;
			dataEncoder = new FloatCodec();
		} else if (IConfigParams.INTEGER_CODEC.equalsIgnoreCase(codec)) {
			maxDataToEncodeSize = PREFERRED_MAX_BYTE_LENGTH
					/ IntCodec.BYTES_PER_SAMPLE;
			dataEncoder = new IntCodec();
		} else {
			throw new IllegalArgumentException("unknown codec: " + codec);
		}
		dataToEncode = new int[maxDataToEncodeSize];
	}

	/**
	 * Create and add the miniSEED message.
	 * 
	 * @param miniSeedMsgList
	 *            the miniSEED message list.
	 * @param encodedData
	 *            the encoded data.
	 * @throws SeedFormatException
	 *             if a SEED format exception occurs.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	protected void addMiniSeedMsg(List miniSeedMsgList,
			final EncodedData encodedData) throws IOException,
			SeedFormatException {
		if (encodedData != null) {
			final MiniSeedMsgHldr miniSeedMsg = createMiniSeedMsg(encodedData,
					dataToEncodeStartTime, dataToEncodeDelta);
			miniSeedMsgList.add(miniSeedMsg);
			// the last encoded value which becomes the bias for the next
			// compression
			final int bias = dataToEncode[miniSeedMsg.getNumSamples() - 1];
			dataEncoder.setBias(bias);
		} else { // could not encode the data
			LogMgr.usrMsgWarning("MiniSeedGenerator.addMiniSeedMsg("
					+ staChaNetLoc + ": could not encode data");
		}
		// if no samples from old buffer to save
		if (numOldSamplesSave == 0) {
			clearDataToEncode(); // clear the data to encode
		} else {
			// determine the index of the old samples
			final int dataIndex = dataToEncodeIndex - numOldSamplesSave;
			if (dataIndex < 0 || dataIndex >= dataToEncode.length) {
				LogMgr.usrMsgWarning("MiniSeedGenerator.addMiniSeedMsg("
						+ staChaNetLoc + "): invalid dataIndex=" + dataIndex
						+ " (dataToEncodeIndex=" + dataToEncodeIndex
						+ " - numOldSamplesSave=" + numOldSamplesSave + ")");
				clearDataToEncode(); // clear the data to encode
				return;
			}
			if (LogMgr.isDebugLevel1()) { // debug-mask bit is set; output debug
				// message
				LogMgr.usrMsgDebug("MiniSeedGenerator.addMiniSeedMsg("
						+ staChaNetLoc + "):" + " dataIndex=" + dataIndex
						+ " (dataToEncodeIndex=" + dataToEncodeIndex
						+ " - numOldSamplesSave=" + numOldSamplesSave + ")");
			}
			// copy the old samples to the start of the buffer
			dataToEncodeIndex = 0;
			System.arraycopy(dataToEncode, dataIndex, dataToEncode,
					dataToEncodeIndex, numOldSamplesSave);
			// determine the start time for the old samples
			dataToEncodeStartTime = getTimeStamp(dataToEncodeStartTime,
					dataToEncodeDelta, dataIndex);
			// set the buffer index
			dataToEncodeIndex = numOldSamplesSave;
			// clear the number of old samples
			numOldSamplesSave = 0;
		}
	}

	/**
	 * Clear the data to encode.
	 */
	protected void clearDataToEncode() {
		dataToEncodeStartTime = 0;
		dataToEncodeEndTime = 0;
		dataToEncodeDelta = 0;
		dataToEncodeIndex = 0;
		numOldSamplesSave = 0;
	}

	/**
	 * Copy the samples to the data to encode buffer.
	 * 
	 * @param dataInfo
	 *            the data information.
	 * @param dataIndex
	 *            the data index.
	 * @param dataLength
	 *            the data length.
	 */
	protected void copySamples(final IDataInfo dataInfo, final int dataIndex,
			final int dataLength) {
		if (dataInfo.getSamples() instanceof int[]) {
			System.arraycopy(dataInfo.getSamples(), dataIndex, dataToEncode,
					dataToEncodeIndex, dataLength);
		} else {
			final ISamples s = (ISamples) dataInfo.getSamples();
			for (int i = 0; i < dataLength; i++) {
				dataToEncode[dataToEncodeIndex + i] = s
						.getSampleAsInt(dataIndex + i);
			}
		}
	}

	/**
	 * Creates the miniSEED message.
	 * 
	 * @param encodedData
	 *            the encoded data.
	 * @param startTime
	 *            the start time.
	 * @param delta
	 *            the delta.
	 * @return the miniSEED message.
	 * @throws SeedFormatException
	 *             if a SEED format exception occurs.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	protected MiniSeedMsgHldr createMiniSeedMsg(EncodedData encodedData,
			final long startTime, final double delta) throws IOException,
			SeedFormatException {
		final int numSamples = encodedData.getNumSamples();
		final int sequenceNum = getDataRecordSequenceNum();
		final long endTime = getTimeStamp(startTime, delta, numSamples - 1);
		final SeedTime startSeedTime = new SeedTime(startTime);
		final Btime startBtime = startSeedTime.getBtime();
		final SampleRateInfo sampleRateInfo = new SampleRateInfo(
				SampleRateInfo.getSampleRate(startTime, endTime, numSamples));
		final IMessageNumber messageNumber = messageManager.getMessageNumber();
		final DataHeader dataHeader = new DataHeader(sequenceNum,
				dataHeaderTypeCode, continuationCode);
		dataHeader.setStationIdentifier(staChaNetLoc.getStationCode());
		dataHeader.setChannelIdentifier(staChaNetLoc.getChannelCode());
		dataHeader.setNetworkCode(staChaNetLoc.getNetworkCode());
		dataHeader.setLocationIdentifier(staChaNetLoc.getLocationCode());
		dataHeader.setStartBtime(startBtime);
		dataHeader.setIOClockFlags(DEFAULT_IO_CLOCK_FLAGS);
		dataHeader.setSampleRateFactor(sampleRateInfo.getSampleRateFactor());
		dataHeader.setSampleRateMultiplier(sampleRateInfo
				.getSampleRateMultiplier());
		dataHeader.setNumBlockettes((byte) 2);
		dataHeader.setNumSamples((short) numSamples);
		dataHeader.setDataBlocketteOffset((byte) PREFERRED_BEGINNING_OF_DATA);

		final DataRecord dataRecord = new DataRecord(dataHeader);
		dataRecord.setRecordSize(PREFERRED_DATA_LENGTH);
		dataRecord.setData(encodedData.getEncodedData());

		final Blockette1000 b1000 = new Blockette1000();
		b1000.setDataRecordLength(PREFERRED_DATA_RECORD_LENGTH);
		b1000.setEncodingFormat(encodedData.getEncodingFormat());
		b1000.setWordOrder(BIG_ENDIAN_WORD_ORDER);
		dataRecord.addBlockette(b1000);

		final Blockette1001 b1001 = new Blockette1001();
		b1001.setFrameCount((byte) maxSteimFrames);
		b1001.setTimingQuality(DEFAULT_TIMING_QUALITY);
		dataRecord.addBlockette(b1001);
		if (LogMgr.isDebugLevel4()) { // debug-mask bit is set; output debug
										// message
			LogMgr.usrMsgDebug("createMiniSeedMsg:  creating B1001 "
					+ b1001.toString(true));
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream(
				PREFERRED_DATA_LENGTH);
		final DataOutputStream dos = new DataOutputStream(baos);
		dataRecord.write(dos);
		dos.flush();
		final byte[] miniSeedData = baos.toByteArray();
		final MiniSeedMsgHldr miniSeedMsgHldr = new MiniSeedMsgHldr(
				staChaNetLoc, startSeedTime, miniSeedData, numSamples,
				messageNumber);
		return miniSeedMsgHldr;
	}

	/**
	 * Encodes the data.
	 * 
	 * @return the encoded data or null if error.
	 */
	protected EncodedData encodeData() {
		return dataEncoder.encode(dataToEncode, dataToEncodeLength);
	}

	/**
	 * Return the data record sequence number.
	 * 
	 * @return the data record sequence number.
	 */
	protected int getDataRecordSequenceNum() {
		if (dataRecordSequenceNum >= 999999) {
			dataRecordSequenceNum = 0;
		}
		return ++dataRecordSequenceNum;
	}

	/**
	 * Gets the miniSEED messages.
	 * 
	 * @param dataInfo
	 *            the data information.
	 * @return the list of 'MiniSeedMsgHldr' objects.
	 * @throws SeedFormatException
	 *             if a SEED format exception occurs.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	public List getMiniSeedMessages(final IDataInfo dataInfo)
			throws IOException, SeedFormatException {

		final List miniSeedMsgList = new ArrayList();
		final int numSamples = dataInfo.getNumSamples();

		// if there was previous data
		if (isPreviousDataAvailable()) {
			// determine if there is a gap if there is new data
			long gapLength = 0;
			boolean gapFlag = false;
			if (numSamples > 0) {
				// check for gap based upon difference from last end time and
				// start time
				gapLength = dataToEncodeEndTime - dataInfo.getFirstTimeStamp();
				if (gapLength > gapThreshold) {
					gapFlag = true;
				}
			}

			// if there is a gap or there is no new data
			if (gapFlag || numSamples == 0) {
				// create the miniSEED and add it to the list
				addMiniSeedMsg(miniSeedMsgList, encodeData());
				if (LogMgr.isDebugLevel1()) { // debug-mask bit is set; output
												// debug
					// message
					LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
							+ staChaNetLoc + "):  " + "gap=" + gapFlag + " ("
							+ gapLength + "), numSamples=" + numSamples
							+ ", startTime=" + dataInfo.getFirstTimeStamp()
							+ ", endTime=" + dataInfo.getLastTimeStamp());
				}
			}
		}

		// if there is new data
		if (numSamples > 0) {
			// save the end time
			dataToEncodeEndTime = dataInfo.getLastTimeStamp();

			// for all of the data
			for (int dataIndex = 0; dataIndex < numSamples;) {
				// determine how much more data can fit in the buffer
				final int dataAvailable = dataToEncode.length
						- dataToEncodeIndex;
				// if no data is available
				if (dataAvailable == 0) {
					// create the miniSEED and add it to the list
					addMiniSeedMsg(miniSeedMsgList, encodeData());
					if (LogMgr.isDebugLevel1()) { // debug-mask bit is set;
													// output debug
						// message
						// NOTE: This should not happen if 'dataToEncode' is
						// sized correctly
						LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
								+ staChaNetLoc
								+ "):  "
								+ "No more data can fit in the buffer, miniSEED created");
					}
					continue;
				}
				int dataLength = numSamples - dataIndex;
				if (dataLength > dataAvailable) {
					dataLength = dataAvailable;
				}

				try {
					// copy the data into the 'dataToEncode' buffer
					copySamples(dataInfo, dataIndex, dataLength);
					dataToEncodeLength = dataToEncodeIndex + dataLength;
				} catch (RuntimeException ex) {
					LogMgr.usrMsgWarning("MiniSeedGenerator copySamples("
							+ staChaNetLoc + "):  numSamples=" + numSamples
							+ ", dataIndex=" + dataIndex
							+ ", dataToEncode.length=" + dataToEncode.length
							+ ", dataToEncodeIndex=" + dataToEncodeIndex
							+ ", dataLength=" + dataLength);
					throw ex;
				}

				// if there was no previous data
				if (!isPreviousDataAvailable()) {
					dataToEncodeDelta = getDelta(dataInfo.getFirstTimeStamp(),
							dataInfo.getLastTimeStamp(),
							dataInfo.getNumSamples());
					dataToEncodeStartTime = getTimeStamp(
							dataInfo.getFirstTimeStamp(), dataToEncodeDelta,
							dataIndex);
				} else {
					// calculate the delta
					dataToEncodeDelta = getDelta(dataToEncodeStartTime,
							dataToEncodeEndTime, dataToEncodeLength);
				}

				// add the length to the data index
				dataIndex += dataLength;

				// encode the data
				final EncodedData encodedData = encodeData();
				if (encodedData != null) {
					final int numEncodedSamples = encodedData.getNumSamples();
					boolean fullFlag = false;
					// if there is more data than can fit in a single data
					// record,
					// we definitely have a full packet
					if (numEncodedSamples < dataToEncodeLength) {
						fullFlag = true;
						// otherwise if encoded data is determined to be full
					} else if (encodedData.isFull()) {
						if (numEncodedSamples == dataToEncodeLength) {
							if (LogMgr.isDebugLevel3()) { // debug-mask bit is
															// set; output
								// debug message
								LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
										+ staChaNetLoc + "): data is full");
							}
							fullFlag = true;
						} else {
							LogMgr.usrMsgWarning("MiniSeedGenerator.getMiniSeedMessages("
									+ staChaNetLoc
									+ "): invalid data is full: numEncodedSamples="
									+ numEncodedSamples
									+ ", dataToEncodeLength="
									+ dataToEncodeLength);
						}
					}

					if (fullFlag) { // if data is full
						// determine the number of unused samples
						final int unusedSamples = dataToEncodeLength
								- numEncodedSamples;
						// if unused samples are new samples
						if (unusedSamples <= dataIndex) {
							// subtract the unused samples
							dataIndex -= unusedSamples;
						} else {
							// determine the number of old samples to save
							numOldSamplesSave = unusedSamples - dataIndex;
							if (LogMgr.isDebugLevel1()) { // debug-mask bit is
															// set; output
								// debug message
								LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
										+ staChaNetLoc
										+ "):"
										+ " numOldSamplesSave="
										+ numOldSamplesSave
										+ " (unusedSamples="
										+ unusedSamples
										+ " - dataIndex="
										+ dataIndex
										+ "), numEncodedSamples="
										+ numEncodedSamples
										+ ", dataToEncodeIndex="
										+ dataToEncodeIndex
										+ ", numSamples="
										+ numSamples
										+ ", dataLength="
										+ dataLength);
							}
							// none of the new samples are used
							dataIndex = 0;
						}
						// create and add the miniSEED message
						addMiniSeedMsg(miniSeedMsgList, encodedData);
					} else {
						dataToEncodeIndex += dataLength;
					}
				} else { // could not encode the data
					clearDataToEncode(); // clear the data to encode
					LogMgr.usrMsgWarning("MiniSeedGenerator.getMiniSeedMessages("
							+ staChaNetLoc + "): could not encode data");
				}
			}
		}
		return miniSeedMsgList;
	}

	/**
	 * Determines if previous data is available.
	 * 
	 * @return true if previous data is available, false otherwise.
	 */
	protected boolean isPreviousDataAvailable() {
		return dataToEncodeStartTime != 0 && dataToEncodeIndex > 0;
	}
}
