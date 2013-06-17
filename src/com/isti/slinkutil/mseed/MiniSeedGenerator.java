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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.isti.slinkutil.BasicMessageManager;
import com.isti.slinkutil.DataInfo;
import com.isti.slinkutil.IConfigParams;
import com.isti.slinkutil.IDataInfo;
import com.isti.slinkutil.IMessageManager;
import com.isti.slinkutil.IMessageNumber;
import com.isti.slinkutil.ISamples;
import com.isti.slinkutil.LogMgr;
import com.isti.slinkutil.MiniSeedMsgHldr;
import com.isti.slinkutil.SampleRateInfo;
import com.isti.slinkutil.SeedTime;
import com.isti.slinkutil.IStaChaNetLoc;
import com.isti.slinkutil.StaChaNetLoc;
import com.isti.slinkutil.seisFile.mseed.Blockette1000;
import com.isti.slinkutil.seisFile.mseed.Blockette1001;
import com.isti.slinkutil.seisFile.mseed.DataHeader;
import com.isti.slinkutil.seisFile.mseed.DataRecord;
import com.isti.slinkutil.seisFile.mseed.SeedFormatException;

/**
 * Class MiniSeedGenerator creates 'miniSEED' messages.
 */
public class MiniSeedGenerator implements MiniSeedConstants {
  /** The data header type code. */
  private final static char dataHeaderTypeCode = 'D';

  /** The default continuation code. */
  private static final boolean defaultContinuationCode = false;

  /** The maximum number of Steim frames. */
  private final static int maxSteimFrames = SteimCodec
      .getFrames(PREFERRED_MAX_BYTE_LENGTH);
  private final File testOutputDir = null;// new File("output/test");

  /**
   * Creates the miniSEED log record message.
   * @param s the text.
   * @param sequenceNum the sequence number.
   * @param messageNumber the message number.
   * @return the miniSEED message.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  public static MiniSeedMsgHldr createLogRecordMsg(String s, int sequenceNum,
      IMessageNumber messageNumber) throws IOException, SeedFormatException {
    final SeedTime startTime = new SeedTime(messageNumber.getTimeCreated());
    return createLogRecordMsg(s, sequenceNum, messageNumber, startTime);
  }

  /**
   * Creates the miniSEED log record message.
   * @param s the text.
   * @param sequenceNum the sequence number.
   * @param messageNumber the message number.
   * @param startTime the start time.
   * @return the miniSEED message.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  public static MiniSeedMsgHldr createLogRecordMsg(String s, int sequenceNum,
      IMessageNumber messageNumber, SeedTime startTime) throws IOException,
      SeedFormatException {
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
    dataHeader.setStationIdentifier(staChaNetLoc.getStationIdStr());
    dataHeader.setChannelIdentifier(staChaNetLoc.getChannelIdStr());
    dataHeader.setNetworkCode(staChaNetLoc.getNetworkCodeStr());
    dataHeader.setLocationIdentifier(staChaNetLoc.getLocationIdStr());
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

  /** The continuation code. */
  private boolean continuationCode = defaultContinuationCode;

  /** The data encoder. */
  private final DataEncoder dataEncoder;

  /** The data record sequence number. */
  private int dataRecordSequenceNum = 0;

  /** The data to encode. */
  private final int[] dataToEncode;

  /** The data to encode index. */
  private int dataToEncodeIndex = 0;

  /** The data to encode length. */
  private int dataToEncodeLength = 0;

  /** The data to encode data information. */
  private DataInfo dataToEncodeDataInfo = null;

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
   * @param staChaNetLoc the station, channel, network and location.
   * @param codec the codec to use.
   */
  public MiniSeedGenerator(IStaChaNetLoc staChaNetLoc, String codec) {
    this(staChaNetLoc, codec, (IMessageManager) null);
  }

  /**
   * Creates the miniSEED generator.
   * @param staChaNetLoc the station, channel, network and location.
   * @param codec the codec to use.
   * @param messageManager the message manager or null for default.
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
   * @param miniSeedMsgList the miniSEED message list.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  protected void addMiniSeedMsg(final List miniSeedMsgList) throws IOException,
      SeedFormatException {
    final EncodedData encodedData = encodeData();
    addMiniSeedMsg(miniSeedMsgList, encodedData);
  }

  /**
   * Create and add the miniSEED message.
   * @param miniSeedMsgList the miniSEED message list.
   * @param encodedData the encoded data.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  protected void addMiniSeedMsg(List miniSeedMsgList,
      final EncodedData encodedData) throws IOException, SeedFormatException {
    if (encodedData != null) {
      final MiniSeedMsgHldr miniSeedMsg = createMiniSeedMsg(encodedData);
      miniSeedMsgList.add(miniSeedMsg);
      // the last encoded value which becomes the bias for the next compression
      final int bias = dataToEncode[miniSeedMsg.getNumSamples() - 1];
      dataEncoder.setBias(bias);
    } else { // could not encode the data
      LogMgr.usrMsgWarning("MiniSeedGenerator.addMiniSeedMsg(" + staChaNetLoc
          + ": could not encode data");
    }
    // if no samples from old buffer to save
    if (numOldSamplesSave == 0) {
      clearDataToEncode(); // clear the data to encode
    } else {
      // determine the index of the old samples
      final int dataIndex = dataToEncodeIndex - numOldSamplesSave;
      if (dataIndex < 0 || dataIndex >= dataToEncode.length) {
        LogMgr.usrMsgWarning("MiniSeedGenerator.addMiniSeedMsg(" + staChaNetLoc
            + "): invalid dataIndex=" + dataIndex + " (dataToEncodeIndex="
            + dataToEncodeIndex + " - numOldSamplesSave=" + numOldSamplesSave
            + ")");
        clearDataToEncode(); // clear the data to encode
        return;
      }
      if (LogMgr.isDebugLevel1()) { // debug-mask bit is set; output debug
        // message
        LogMgr.usrMsgDebug("MiniSeedGenerator.addMiniSeedMsg(" + staChaNetLoc
            + "):" + " dataIndex=" + dataIndex + " (dataToEncodeIndex="
            + dataToEncodeIndex + " - numOldSamplesSave=" + numOldSamplesSave
            + ")");
      }
      // copy the old samples to the start of the buffer
      dataToEncodeIndex = 0;
      System.arraycopy(dataToEncode, dataIndex, dataToEncode,
          dataToEncodeIndex, numOldSamplesSave);
      // determine the start time for the old samples
      dataToEncodeDataInfo = new DataInfo(dataToEncodeDataInfo, dataIndex);
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
    dataToEncodeDataInfo = null;
    dataToEncodeIndex = 0;
    numOldSamplesSave = 0;
  }

  /**
   * Copy the samples to the data to encode buffer.
   * @param dataInfo the data information.
   * @param dataIndex the data index.
   * @param dataLength the data length.
   */
  protected void copySamples(final IDataInfo dataInfo, final int dataIndex,
      final int dataLength) {
    if (dataInfo.getSamples() instanceof int[]) {
      System.arraycopy(dataInfo.getSamples(), dataIndex, dataToEncode,
          dataToEncodeIndex, dataLength);
    } else {
      final ISamples s = (ISamples) dataInfo.getSamples();
      for (int i = 0; i < dataLength; i++) {
        dataToEncode[dataToEncodeIndex + i] = s.getSampleAsInt(dataIndex + i);
      }
    }
  }

  /**
   * Creates the miniSEED message.
   * @param encodedData the encoded data.
   * @return the miniSEED message.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  protected MiniSeedMsgHldr createMiniSeedMsg(EncodedData encodedData)
      throws IOException, SeedFormatException {
    final int numSamples = encodedData.getNumSamples();
    final int sequenceNum = getDataRecordSequenceNum();
    final IDataInfo dataInfo = dataToEncodeDataInfo;
    final IMessageNumber messageNumber = messageManager.getMessageNumber();
    final byte timingQuality = dataInfo.getTimeQualityValue();
    DataHeader dataHeader = new DataHeader(sequenceNum, dataHeaderTypeCode,
        continuationCode);
    dataHeader.setStationIdentifier(staChaNetLoc.getStationIdStr());
    dataHeader.setChannelIdentifier(staChaNetLoc.getChannelIdStr());
    dataHeader.setNetworkCode(staChaNetLoc.getNetworkCodeStr());
    dataHeader.setLocationIdentifier(staChaNetLoc.getLocationIdStr());
    dataHeader.setStartBtime(dataInfo.getStartSeedTime().getBtime());
    dataHeader.setIOClockFlags(dataInfo.getIoClockFlags());
    SampleRateInfo sampleRateInfo = new SampleRateInfo(dataInfo.getSampleRate());
    dataHeader.setSampleRateFactor(sampleRateInfo.getSampleRateFactor());
    dataHeader
        .setSampleRateMultiplier(sampleRateInfo.getSampleRateMultiplier());
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

    Blockette1001 b1001 = new Blockette1001();
    b1001.setFrameCount((byte) maxSteimFrames);
    b1001.setTimingQuality(timingQuality);
    dataRecord.addBlockette(b1001);
    if (LogMgr.isDebugLevel4()) { // debug-mask bit is set; output debug message
      LogMgr.usrMsgDebug("createMiniSeedMsg:  creating B1001 "
          + b1001.toString(true));
    }

    final ByteArrayOutputStream baos = new ByteArrayOutputStream(
        PREFERRED_DATA_LENGTH);
    DataOutputStream dos = new DataOutputStream(baos);
    dataRecord.write(dos);
    dos.flush();
    final byte[] miniSeedData = baos.toByteArray();
    MiniSeedMsgHldr miniSeedMsgHldr = new MiniSeedMsgHldr(staChaNetLoc,
        dataInfo.getStartSeedTime(), miniSeedData, numSamples, messageNumber);
    // TODO test output
    if (testOutputDir != null) {
      testOutputDir.mkdirs();
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
          miniSeedMsgHldr.getFile(testOutputDir)));
      dos = new DataOutputStream(bos);
      dataRecord.write(dos);
      dos.flush();
    }
    return miniSeedMsgHldr;
  }

  /**
   * Encodes the data.
   * @return the encoded data or null if error.
   */
  protected EncodedData encodeData() {
    return dataEncoder.encode(dataToEncode, dataToEncodeLength);
  }

  /**
   * Return the data record sequence number.
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
   * @param dataInfo the data information.
   * @return the list of 'MiniSeedMsgHldr' objects.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O exception occurs.
   */
  public List getMiniSeedMessages(final IDataInfo dataInfo) throws IOException,
      SeedFormatException {

    final List miniSeedMsgList = new ArrayList();
    final int numSamples = dataInfo.getNumSamples();
    final SeedTime startTime, nextStartTime;

    // if there was previous data
    if (isPreviousDataAvailable()) {
      // determine if there is a gap if there is new data
      boolean gapFlag = false;
      if (numSamples > 0) {
        startTime = dataInfo.getStartSeedTime();
        nextStartTime = dataToEncodeDataInfo.getStartSeedTime().projectTime(
            dataToEncodeIndex, dataToEncodeDataInfo.getSampleRate());
        if (!startTime.equals(nextStartTime)) {
          gapFlag = true;
        }
      } else {
        startTime = null;
        nextStartTime = null;
      }

      // if there is a gap or there is no data
      if (gapFlag || numSamples == 0) {
        // create the miniSEED and add it to the list
        addMiniSeedMsg(miniSeedMsgList);
        if (LogMgr.isDebugLevel2()) { // debug-mask bit is set; output debug
          // message
          LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
              + staChaNetLoc + "):  " + "gap=" + gapFlag + ", numSamples="
              + numSamples + ", startTime=" + startTime + ", nextStartTime="
              + nextStartTime);
        }
      }
    }

    // if there is new data
    if (numSamples > 0) {

      // for all of the data
      for (int dataIndex = 0; dataIndex < numSamples;) {
        // determine how much more data can fit in the buffer
        final int dataAvailable = dataToEncode.length - dataToEncodeIndex;
        // if no data is available
        if (dataAvailable == 0) {
          // create the miniSEED and add it to the list
          addMiniSeedMsg(miniSeedMsgList);
          if (LogMgr.isDebugLevel1()) { // debug-mask bit is set; output debug
            // message
            // NOTE: This should not happen if 'dataToEncode' is sized correctly
            LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
                + staChaNetLoc + "):  "
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
          LogMgr.usrMsgWarning("MiniSeedGenerator copySamples(" + staChaNetLoc
              + "):  numSamples=" + numSamples + ", dataIndex=" + dataIndex
              + ", dataToEncode.length=" + dataToEncode.length
              + ", dataToEncodeIndex=" + dataToEncodeIndex + ", dataLength="
              + dataLength);
          throw ex;
        }

        // if there was no previous data
        if (!isPreviousDataAvailable()) {
          dataToEncodeDataInfo = new DataInfo(dataInfo, dataIndex);
        }

        // add the length to the data index
        dataIndex += dataLength;

        // encode the data
        final EncodedData encodedData = encodeData();
        if (encodedData != null) {
          final int numEncodedSamples = encodedData.getNumSamples();
          boolean fullFlag = false;
          // if there is more data than can fit in a single data record,
          // we definitely have a full packet
          if (numEncodedSamples < dataToEncodeLength) {
            fullFlag = true;
            // otherwise if encoded data is determined to be full
          } else if (encodedData.isFull()) {
            if (numEncodedSamples == dataToEncodeLength) {
              if (LogMgr.isDebugLevel3()) { // debug-mask bit is set; output
                // debug message
                LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
                    + staChaNetLoc + "): data is full");
              }
              fullFlag = true;
            } else {
              LogMgr.usrMsgWarning("MiniSeedGenerator.getMiniSeedMessages("
                  + staChaNetLoc
                  + "): invalid data is full: numEncodedSamples="
                  + numEncodedSamples + ", dataToEncodeLength="
                  + dataToEncodeLength);
            }
          }

          if (fullFlag) { // if data is full
            // determine the number of unused samples
            final int unusedSamples = dataToEncodeLength - numEncodedSamples;
            // if unused samples are new samples
            if (unusedSamples <= dataIndex) {
              // subtract the unused samples
              dataIndex -= unusedSamples;
            } else {
              // determine the number of old samples to save
              numOldSamplesSave = unusedSamples - dataIndex;
              if (LogMgr.isDebugLevel1()) { // debug-mask bit is set; output
                // debug message
                LogMgr.usrMsgDebug("MiniSeedGenerator.getMiniSeedMessages("
                    + staChaNetLoc + "):" + " numOldSamplesSave="
                    + numOldSamplesSave + " (unusedSamples=" + unusedSamples
                    + " - dataIndex=" + dataIndex + "), numEncodedSamples="
                    + numEncodedSamples + ", dataToEncodeIndex="
                    + dataToEncodeIndex + ", numSamples=" + numSamples
                    + ", dataLength=" + dataLength);
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
   * @return true if previous data is available, false otherwise.
   */
  protected boolean isPreviousDataAvailable() {
    return dataToEncodeDataInfo != null && dataToEncodeIndex > 0;
  }
}
