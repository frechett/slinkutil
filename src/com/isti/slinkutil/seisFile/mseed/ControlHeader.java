package com.isti.slinkutil.seisFile.mseed;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import com.isti.slinkutil.SLinkUtilFns;

/**
 * The control header.
 */
public class ControlHeader {

  /**
   * Test the control header.
   * @param args the arguments.
   */
  public static void main(String[] args) {
    ControlHeader.tester(args[0]);

  }

  /**
   * Read the control header.
   * @param in the data input.
   * @return the control header.
   * @throws IOException if an I/O Exception occurs.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public static ControlHeader read(DataInput in) throws IOException,
      SeedFormatException {
    byte[] seqBytes = new byte[6];
    in.readFully(seqBytes);
    String seqNumString = new String(seqBytes);

    int sequenceNum = 0;
    try {
      sequenceNum = Integer.valueOf(seqNumString).intValue();

    } catch (NumberFormatException e) {
      System.err.println("seq num unreadable, setting to 0 " + e.toString());
    } // end of try-catch

    byte typeCode = in.readByte();

    int b = in.readByte();
    boolean continuationCode;
    if (b == 32) {
      // a space, so no continuation
      continuationCode = false;
    } else if (b == 42) {
      // an asterisk, so is a continuation
      continuationCode = true;
    } else {
      throw new SeedFormatException(
          "ControlHeader, expected space or *, but got" + b);
    }

    if (typeCode == (byte) 'D' || typeCode == (byte) 'R'
        || typeCode == (byte) 'Q') {
      // Data Header
      return DataHeader
          .read(in, sequenceNum, (char) typeCode, continuationCode);
    } else {
      // Control header
      return new ControlHeader(sequenceNum, typeCode, continuationCode);
    }
  }

  /**
   * Test the control header.
   * @param fileName the file name.
   */
  public static void tester(String fileName) {

    DataOutputStream dos = null;
    try {
      dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
          fileName)));
      ControlHeader controlHeaderObject = new ControlHeader(23, (byte) 'D',
          true);
      controlHeaderObject.write(dos);
      dos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected boolean continuationCode;

  protected int sequenceNum;

  protected byte typeCode;

  /**
   * Create the control header.
   * @param sequenceNum the sequence number.
   * @param typeCode the type code.
   * @param continuationCode true if continuation, false otherwise.
   */
  public ControlHeader(int sequenceNum, byte typeCode, boolean continuationCode) {
    this.sequenceNum = sequenceNum;
    this.typeCode = (byte) typeCode;
    this.continuationCode = continuationCode;
  }

  /**
   * Create the control header.
   * @param sequenceNum the sequence number.
   * @param typeCode the type code.
   * @param continuationCode true if continuation, false otherwise.
   */
  public ControlHeader(int sequenceNum, char typeCode, boolean continuationCode) {
    this(sequenceNum, (byte) typeCode, continuationCode);
  }

  /**
   * Get the sequence number.
   * @return the sequence number.
   */
  public int getSequenceNum() {
    return sequenceNum;
  }

  /**
   * Get the size.
   * @return the size.
   */
  public short getSize() {
    return 8;
  }

  /**
   * Get the type code.
   * @return the type code.
   */
  public char getTypeCode() {
    return (char) typeCode;
  }

  /**
   * Get the continuation.
   * @return true if continuation, false otherwise.
   */
  public boolean isContinuation() {
    return continuationCode;
  }

  public String toString() {
    return getTypeCode() + "  " + getSequenceNum();
  }

  /**
   * This method writes Control Header into the output stream
   * While writing, it will conform to the format of MiniSeed
   * @param dos the data output stream.
   * @throws IOException if an I/O Exception occurs.
    */
  protected void write(DataOutput dos) throws IOException {
    DecimalFormat sequenceNumFormat = new DecimalFormat("000000");
    String sequenceNumString = sequenceNumFormat.format(sequenceNum);
    byte[] sequenceNumByteArray = SLinkUtilFns.getBytes(sequenceNumString);
    byte continuationCodeByte;
    if (continuationCode == true) {
      // if it is continuation,it is represented as asterix '*'
      continuationCodeByte = (byte) 42;
    } else {
      // if it continuationCode is false...it is represented as space ' '
      continuationCodeByte = (byte) 32;
    }
    try {
      dos.write(sequenceNumByteArray);
      dos.write((byte) typeCode);
      dos.write(continuationCodeByte);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
