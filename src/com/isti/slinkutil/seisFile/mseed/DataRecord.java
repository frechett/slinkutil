package com.isti.slinkutil.seisFile.mseed;

/**
 * DataRecord.java
 * 
 * 
 * Created: Thu Apr 8 13:52:27 1999
 * 
 * @author Philip Crotwell
 * @version
 */
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;

import com.isti.slinkutil.Utility;

/**
 * The data record.
 */
public class DataRecord extends SeedRecord implements Serializable {

  /**
  * 
  */
  private static final long serialVersionUID = 1L;

  /**
   * allows setting of a default record size, making reading of miniseed that
   * lack a Blockette1000. Compression is still unknown, but at least the
   * record can be read in and manipulated. A value of 0 for defaultRecordSize
   * means there must be a blockette 1000 or a MissingBlockette1000 will be
   * thrown.
   * 
   * If an exception is thrown and the underlying stream supports it, the stream
   * will be reset to its state prior to any bytes being read. The buffer in the
   * underlying stream must be large enough buffer any values read prior to the
   * exception. A buffer sized to be the largest seed record expected is sufficient
   * and so 4096 is a reasonable buffer size.
   * @param inStream the data input.
   * @param defaultRecordSize the default record size.
   * @return the data record.
   * @throws IOException if an I/O Exception occurs.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public static DataRecord read(DataInput inStream, int defaultRecordSize)
      throws IOException, SeedFormatException {
    boolean resetOnError = inStream instanceof DataInputStream
        && ((InputStream) inStream).markSupported();
    if (resetOnError) {
      ((InputStream) inStream).mark(4096);
    }
    try {
      ControlHeader header = ControlHeader.read(inStream);
      if (header instanceof DataHeader) {
        return readDataRecord(inStream, (DataHeader) header, defaultRecordSize);
      } else {
        throw new SeedFormatException(
            "Found a control header in a miniseed file");
      }
    } catch (SeedFormatException e) {
      if (resetOnError) {
        ((InputStream) inStream).reset();
      }
      throw e;
    } catch (IOException e) {
      if (resetOnError) {
        ((InputStream) inStream).reset();
      }
      throw e;
    } catch (RuntimeException e) {
      if (resetOnError) {
        ((InputStream) inStream).reset();
      }
      throw e;
    }
  }

  /**
   * Read the data record.
   * @param inStream the data input stream.
   * @return the data record.
   * @throws IOException if an I/O Exception occurs.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public static DataRecord read(DataInputStream inStream) throws IOException,
      SeedFormatException {
    return read(inStream, 0);
  }

  protected static DataRecord readDataRecord(DataInput inStream,
      DataHeader header, int defaultRecordSize) throws IOException,
      SeedFormatException {
    boolean swapBytes = header.flagByteSwap();
    /*
     * Assert.isTrue(header.getDataBlocketteOffset()>= header.getSize(),
     * "Offset to first blockette must be larger than the header size");
     */
    DataRecord dataRec = new DataRecord(header);
    // read garbage between header and blockettes
    if (header.getDataBlocketteOffset() != 0) {
      byte[] garbage = new byte[header.getDataBlocketteOffset()
          - header.getSize()];
      if (garbage.length != 0) {
        inStream.readFully(garbage);
      }
    }
    byte[] blocketteBytes;
    int currOffset = header.getDataBlocketteOffset();
    if (header.getDataBlocketteOffset() == 0) {
      currOffset = header.getSize();
    }
    int type, nextOffset;
    for (int i = 0; i < header.getNumBlockettes(); i++) {
      // get blockette type (first 2 bytes)
      byte hibyteType = inStream.readByte();
      byte lowbyteType = inStream.readByte();
      type = Utility.uBytesToInt(hibyteType, lowbyteType, swapBytes);
      // System.out.println("Blockette type "+type);
      byte hibyteOffset = inStream.readByte();
      byte lowbyteOffset = inStream.readByte();
      nextOffset = Utility.uBytesToInt(hibyteOffset, lowbyteOffset, swapBytes);
      // account for the 4 bytes above
      currOffset += 4;
      if (nextOffset != 0) {
        blocketteBytes = new byte[nextOffset - currOffset];
      } else if (header.getDataOffset() > currOffset) {
        blocketteBytes = new byte[header.getDataOffset() - currOffset];
      } else {
        blocketteBytes = new byte[0];
      }
      inStream.readFully(blocketteBytes);
      if (nextOffset != 0) {
        currOffset = nextOffset;
      } else {
        currOffset += blocketteBytes.length;
      }
      // fix so blockette has full bytes
      byte[] fullBlocketteBytes = new byte[blocketteBytes.length + 4];
      System.arraycopy(blocketteBytes, 0, fullBlocketteBytes, 4,
          blocketteBytes.length);
      fullBlocketteBytes[0] = hibyteType;
      fullBlocketteBytes[1] = lowbyteType;
      fullBlocketteBytes[2] = hibyteOffset;
      fullBlocketteBytes[3] = lowbyteOffset;
      Blockette b = Blockette.parseBlockette(type, fullBlocketteBytes,
          swapBytes);
      dataRec.addBlockette(b);
      if (nextOffset == 0) {
        break;
      }
    }
    int recordSize = defaultRecordSize;
    try {
      recordSize = ((Blockette1000) dataRec.getUniqueBlockette(1000))
          .getDataRecordLength();
    } catch (MissingBlockette1000 e) {
      if (defaultRecordSize == 0) {
        // no default
        throw e;
      }
      // otherwise use default
      recordSize = defaultRecordSize;
    }
    dataRec.RECORD_SIZE = recordSize;
    // read garbage between blockettes and data
    if (header.getDataOffset() != 0) {
      byte[] garbage = new byte[header.getDataOffset() - currOffset];
      if (garbage.length != 0) {
        inStream.readFully(garbage);
      }
    }
    byte[] timeseries;
    if (header.getDataOffset() == 0) {
      // data record with no data, so gobble up the rest of the record
      timeseries = new byte[recordSize - currOffset];
    } else {
      timeseries = new byte[recordSize - header.getDataOffset()];
    }
    inStream.readFully(timeseries);
    dataRec.setData(timeseries);
    return dataRec;
  }

  protected byte[] data;

  int RECORD_SIZE = 4096;

  byte ZERO_BYTE = 0;

  /**
   * Create the data record.
   * @param header the data header.
   */
  public DataRecord(DataHeader header) {
    super(header);
  }

  /**
   * Create the data record.
   * @param record the data record.
   */
  public DataRecord(DataRecord record) {
    super(new DataHeader(record.getHeader().getSequenceNum(), record
        .getHeader().getTypeCode(), record.getHeader().isContinuation()));
    RECORD_SIZE = record.RECORD_SIZE;
    getHeader().setActivityFlags(record.getHeader().getActivityFlags());
    getHeader().setChannelIdentifier(record.getHeader().getChannelIdentifier());
    getHeader().setDataBlocketteOffset(
        (short) record.getHeader().getDataBlocketteOffset());
    getHeader().setDataOffset((short) record.getHeader().getDataOffset());
    getHeader().setDataQualityFlags(record.getHeader().getDataQualityFlags());
    getHeader().setIOClockFlags(record.getHeader().getIOClockFlags());
    getHeader().setLocationIdentifier(
        record.getHeader().getLocationIdentifier());
    getHeader().setNetworkCode(record.getHeader().getNetworkCode());
    getHeader().setNumSamples((short) record.getHeader().getNumSamples());
    getHeader().setSampleRateFactor(
        (short) record.getHeader().getSampleRateFactor());
    getHeader().setSampleRateMultiplier(
        (short) record.getHeader().getSampleRateMultiplier());
    getHeader().setStartBtime(record.getHeader().getStartBtime());
    getHeader().setStationIdentifier(record.getHeader().getStationIdentifier());
    getHeader().setTimeCorrection(record.getHeader().getTimeCorrection());
    try {
      setData(record.getData());
      for (int j = 0; j < record.getBlockettes().length; j++) {
        addBlockette(record.getBlockettes()[j]);
      }
    } catch (SeedFormatException e) {
      throw new RuntimeException(
          "Shouldn't happen as record was valid and we are copying it");
    }
  }

  public void addBlockette(Blockette b) throws SeedFormatException {
    if (b == null) {
      throw new IllegalArgumentException("Blockette cannot be null");
    }
    if (b instanceof DataBlockette || b instanceof BlocketteUnknown) {
      super.addBlockette(b);
      getHeader().setNumBlockettes((byte) (getHeader().getNumBlockettes() + 1));
    } else {
      throw new SeedFormatException(
          "Cannot add non-data blockettes to a DataRecord " + b.getType());
    }
    recheckDataOffset();
  }

  /**
   * Get the data from this data header unparsed, is as a byte array in
   * the format from blockette 1000. The return type is byte[], so the caller
   * must decode the data based on its format.
   * @return the data.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Get the data size.
   * @return the data size.
   */
  public int getDataSize() {
    return data.length;
  }

  /**
   * Get the data header.
   * @return the data header.
   */
  public DataHeader getHeader() {
    return (DataHeader) header;
  }

  /**
   * Get the record size.
   * @return the record size.
   */
  public int getRecordSize() {
    return RECORD_SIZE;
  }

  protected void recheckDataOffset() throws SeedFormatException {
    int size = getHeader().getSize();
    Blockette[] blocks = getBlockettes();
    for (int i = 0; i < blocks.length; i++) {
      size += blocks[i].getSize();
    }
    if (data != null) {
      size += data.length;
    }
    if (size > RECORD_SIZE) {
      throw new SeedFormatException("Can't fit blockettes and data in record "
          + size);
    }
    if (data != null) {
      // shift the data to end of blockette so pad happens between
      // blockettes and data
      getHeader().setDataOffset((short) (RECORD_SIZE - data.length));
    }
  }

  /**
   * Set the data.
   * @param data the data.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public void setData(byte[] data) throws SeedFormatException {
    this.data = data;
    recheckDataOffset();
  }

  /**
   * Set the record size.
   * @param recordSize the record size.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public void setRecordSize(int recordSize) throws SeedFormatException {
    int tmp = RECORD_SIZE;
    RECORD_SIZE = recordSize;
    try {
      recheckDataOffset();
    } catch (SeedFormatException e) {
      RECORD_SIZE = tmp;
      throw e;
    }
  }

  public String toString() {
    String s = "Data " + super.toString();
    s += "\n" + data.length + " bytes of data read.";
    return s;
  }

  /**
   * Write this data record.
   * @param dos the data output stream.
   * @throws IOException if an I/O Exception occurs.
   */
  public void write(DataOutputStream dos) throws IOException {
    Blockette[] blocks = getBlockettes();
    getHeader().setNumBlockettes((byte) blocks.length);
    if (blocks.length != 0) {
      getHeader().setDataBlocketteOffset((byte) 48);
    }
    getHeader().write(dos);
    DataBlockette dataB;
    short blockettesSize = getHeader().getSize();
    for (int i = 0; i < blocks.length; i++) {
      dataB = (DataBlockette) blocks[i];
      blockettesSize += (short) dataB.getSize();
      if (i != blocks.length - 1) {
        dos.write(dataB.toBytes(blockettesSize));
      } else {
        dos.write(dataB.toBytes((short) 0));
      }
    } // end of for ()
    for (int i = blockettesSize; i < getHeader().getDataOffset(); i++) {
      dos.write(ZERO_BYTE);
    }
    dos.write(data);
    int remainBytes = RECORD_SIZE - getHeader().getDataOffset() - data.length;
    for (int i = 0; i < remainBytes; i++) {
      dos.write(ZERO_BYTE);
    } // end of for ()
  }

  /**
   * Writes this data record.
   * @param out the output.
   * @throws IOException if an I/O Exception occurs.
   */
  public void writeASCII(Writer out) throws IOException {
    out.write("DataRecord\n");
    getHeader().writeASCII(out);
    Blockette[] b = getBlockettes();
    for (int i = 0; i < b.length; i++) {
      b[i].writeASCII(out);
    }
    out.write("End DataRecord\n");
  }
} // DataRecord
