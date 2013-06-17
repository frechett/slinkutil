package com.isti.slinkutil;

import java.util.Set;

/**
 * The Configuration parameters.
 */
public interface IConfigParams {
  /** Default value for max # of msgs held in cache (0 = no limit). */
  public static final int DEF_MAX_CACHE_SIZE = 10000;

  /** Default value for max age of messages held in cache (milliseconds). */
  public static final int DEF_MAX_CACHEAGE = 3600 * 1000;

  /** Float codec. */
  public static final String FLOAT_CODEC = "FLOAT";

  /** Integer codec. */
  public static final String INTEGER_CODEC = "INTEGER";

  /** Steim1 MiniSeed codec. */
  public static final String STEIM1_MINISEED_CODEC = "STEIM1";

  /** Steim2 MiniSeed codec. */
  public static final String STEIM2_MINISEED_CODEC = "STEIM2";

  /** Default value for MiniSeed codec. */
  public static final String DEF_MINISEED_CODEC = STEIM2_MINISEED_CODEC;

  /**
   * Get the codec.
   * @return the codec.
   */
  public String getCodec();

  /**
   * Get the maximum cache age in milliseconds.
   * @return the maximum cache age in milliseconds.
   */
  public int getMaxCacheAge();

  /**
   * Get the maximum cache size.
   * @return the maximum cache size.
   */
  public int getMaxCacheSize();

  /**
   * Get the selected channel names.
   * @return the selected channel names or null if none.
   */
  public Set getSelectedChannelNames();
}
