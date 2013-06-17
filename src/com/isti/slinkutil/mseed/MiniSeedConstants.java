//MiniSeedConstants.java:  Defines the miniSEED constants.
//
//  9/15/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil.mseed;

/**
 * Interface MiniSeedConstants defines the miniSEED constants.
 */
public interface MiniSeedConstants {
  /**
   * Big-endian (Java) word order (used in B1000.)
   */
  public static final byte BIG_ENDIAN_WORD_ORDER = 1;

  /**
   * The preferred begging of data to be used in the blockette 999.
   */
  public static final int PREFERRED_BEGINNING_OF_DATA = 48;

  /**
   * The preferred data length.
   */
  public static final int PREFERRED_DATA_LENGTH = 512;

  /**
   * The preferred data record length expressed as an exponent as a power of 2
   * (2^9=512.)
   */
  public static final byte PREFERRED_DATA_RECORD_LENGTH = 9;

  /**
   * The size of the B1000 and B1001 needed in miniSEED.
   */
  public static final int BLOCKETTE_TOTAL_SIZE = 16;

  /**
   * The preferred maximum byte length.
   */
  public static final int PREFERRED_MAX_BYTE_LENGTH = PREFERRED_DATA_LENGTH
      - PREFERRED_BEGINNING_OF_DATA - BLOCKETTE_TOTAL_SIZE;
}
