/**
 * MultiFileMSeedRead.java
 *
 * @author Created by Omnicore CodeGuide
 */

package com.isti.slinkutil.seisFile.mseed;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The multiple file miniSEED reader.
 */
public class MultiFileMSeedRead extends MiniSeedRead {

  MiniSeedRead current;

  int current_record = 0;

  int currentIndex = 0;

  File[] files;

  int numReadTotal = 0;

  /**
   * Create the multiple file miniSEED reader.
   * @param files the files.
   * @throws IOException if an I/O Exception occurs.
   */
  public MultiFileMSeedRead(File[] files) throws IOException {
    this.files = files;
    initNextFile();
  }

  public void close() throws IOException {
    if (current != null) {
      current.close();
    }
    currentIndex = files.length; // make sure no read after close
  }

  /** gets the next logical record int the seed volume. This may not
   exactly correspond to the logical record structure within the
   volume as "continued" records will be concatinated to avoid
   partial blockettes. */
  public DataRecord getNextRecord() throws SeedFormatException, IOException {
    if (current == null) {
      throw new EOFException("Cannot read past end of file list");
    }
    try {
      DataRecord d = current.getNextRecord();
      numReadTotal++;
      return d;
    } catch (EOFException e) {
      // try next file
      initNextFile();
      DataRecord d = current.getNextRecord();
      numReadTotal++;
      return d;
    }
  }

  public int getNumRecordsRead() {
    return numReadTotal;
  }

  protected void initNextFile() throws IOException {
    if (currentIndex < files.length) {
      if (current != null) {
        current.close();
        current = null;
      }
      current = new MiniSeedRead(new DataInputStream(new BufferedInputStream(
          new FileInputStream(files[currentIndex]))));
      currentIndex++;
    }
  }

}
