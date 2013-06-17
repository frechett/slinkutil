package com.isti.slinkutil.seisFile.mseed;

/**
 * SeedRecord.java
 * 
 * 
 * Created: Thu Apr 8 11:54:07 1999
 * 
 * @author Philip Crotwell
 * @version
 */
import java.util.Vector;

/**
 * The SEED record.
 */
public abstract class SeedRecord {

  protected Vector blockettes = new Vector();

  protected ControlHeader header;

  /**
   * Create the SEED record.
   * @param header the header.
   */
  public SeedRecord(ControlHeader header) {
    this.header = header;
  }

  /**
   * Add a blockette.
   * @param b the blockette.
   * @throws SeedFormatException if a SEED format exception occurs.
   */
  public void addBlockette(Blockette b) throws SeedFormatException {
    blockettes.addElement(b);
  }

  /**
   * Get the blockettes.
   * @return the blockettes.
   */
  public Blockette[] getBlockettes() {
    Blockette[] allB = new Blockette[blockettes.size()];
    blockettes.copyInto(allB);
    return allB;
  }

  /**
   * Get the blockettes of the specified type.
   * @param type the blockette type.
   * @return the blockettes.
   */
  public Blockette[] getBlockettes(int type) {
    Vector v = new Vector();
    for (int i = 0; i < blockettes.size(); i++) {
      if (((Blockette) blockettes.elementAt(i)).getType() == type) {
        v.addElement(blockettes.elementAt(i));
      }
    }
    Blockette[] allB = new Blockette[v.size()];
    v.copyInto(allB);
    return allB;
  }

  /**
   * Get the number of blockettes of the specified type.
   * @param type the blockette type.
   * @return the number of blockettes.
   */
  public int getNumBlockettes(int type) {
    int out = 0;
    for (int i = 0; i < blockettes.size(); i++) {
      if (((Blockette) blockettes.elementAt(i)).getType() == type) {
        out++;
      }
    }
    return out;
  }

  /**
   * Get a unique blockette for the specified type.
   * @param type the blockette type.
   * @return the blockette.
   * @throws SeedFormatException if there are no or multiple blockettes.
   */
  public Blockette getUniqueBlockette(int type) throws SeedFormatException {
    Blockette[] b = getBlockettes(type);
    if (b.length == 1) {
      return b[0];
    } else if (b.length == 0) {
      if (type == 1000) {
        // special case as b1000 is required in mseed
        throw new MissingBlockette1000();
      }
      throw new SeedFormatException("No blockettes of type " + type);
    } else {
      throw new SeedFormatException("Multiple blockettes of type " + type);
    }
  }

  public String toString() {
    String s = "Record for " + header + "\n";
    s += "Blockettes:\n";
    for (int i = 0; i < blockettes.size(); i++) {
      s += blockettes.elementAt(i) + "\n";
    }
    return s;
  }
} // SeedRecord
