package com.isti.slinkutil.seisFile.mseed;

/**
 * MiniSeedRead.java
 * 
 * 
 * Created: Thu Apr 8 12:10:52 1999
 * 
 * @author Philip Crotwell
 * @version
 */
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The miniSEED reader.
 */
public class MiniSeedRead {

  /**
   * Test the miniSEED reader.
   * @param args the arguments.
   */
  public static void main(String[] args) {
    DataInputStream ls = null;
    try {
      System.out.println("open socket");
      if (args.length == 0) {
        Socket lissConnect = new Socket("anmo.iu.liss.org", 4000);
        ls = new DataInputStream(new BufferedInputStream(
            lissConnect.getInputStream(), 1024));
      } else {
        ls = new DataInputStream(new BufferedInputStream(new FileInputStream(
            args[0]), 4096));
      }
      MiniSeedRead rf = new MiniSeedRead(ls);
      for (int i = 0; i < 10; i++) {
        SeedRecord sr;
        try {
          sr = rf.getNextRecord();
        } catch (MissingBlockette1000 e) {
          System.out
              .println("Missing Blockette1000, trying with record size of 4096");
          // try with 4096 as default
          sr = rf.getNextRecord(4096);
        }
        System.out.println(sr);
        if (sr instanceof DataRecord) {
          DataRecord dr = (DataRecord) sr;
          dr.getData();
        }
      }
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    } finally {
      try {
        if (ls != null)
          ls.close();
      } catch (Exception ee) {
      }
    }
  }

  protected DataInput inStream;

  protected int numRead = 0;

  protected boolean readData;

  protected int recordSize;

  protected MiniSeedRead() {
  }

  /**
   * Reads the miniSEED from the specified input.
   * @param inStream the input.
   * @throws IOException if an I/O Exception occurs.
   */
  public MiniSeedRead(DataInput inStream) throws IOException {
    this.inStream = inStream;
  }

  /**
   * Close the miniSEED.
   * @throws IOException if an I/O Exception occurs.
   */
  public void close() throws IOException {
    inStream = null;
  }

  /**
   * Gets the next logical record int the seed volume. This may not exactly
   * correspond to the logical record structure within the volume as
   * "continued" records will be concatinated to avoid partial blockettes.
   * @return the data record.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O Exception occurs.
   */
  public DataRecord getNextRecord() throws SeedFormatException, IOException {
    return getNextRecord(0);
  }

  /**
   * Gets the next logical record int the seed volume. This may not exactly
   * correspond to the logical record structure within the volume as
   * "continued" records will be concatinated to avoid partial blockettes.
   * @param defaultRecordSize the default record size.
   * @return the data record.
   * @throws SeedFormatException if a SEED format exception occurs.
   * @throws IOException if an I/O Exception occurs.
   */
  public DataRecord getNextRecord(int defaultRecordSize)
      throws SeedFormatException, IOException {
    return DataRecord.read(inStream, defaultRecordSize);
  }

  /**
   * Get the number of records read.
   * @return the number of records read.
   */
  public int getNumRecordsRead() {
    return numRead;
  }
} // MiniSeedRead
