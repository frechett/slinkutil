package com.isti.slinkutil;

import java.util.Set;

/**
 * The Configuration parameters.
 */
public class ConfigParams implements IConfigParams {
  /** Codec to use for MiniSeed. */
  private String codec = DEF_MINISEED_CODEC;

  /** Maximum age for messages held in cache (milliseconds, 0 = no limit). */
  private int maxCacheAge = DEF_MAX_CACHEAGE;

  /** Maximum number of messages held in cache (0 = no limit). */
  private int maxCacheSize = DEF_MAX_CACHE_SIZE;

  /** The selected channel names. */
  private Set selectedChannelNames;

  /**
   * Get the codec.
   * @return the codec.
   */
  public String getCodec() {
    return codec;
  }

  /**
   * Get the maximum cache age in milliseconds.
   * @return the maximum cache age in milliseconds.
   */
  public int getMaxCacheAge() {
    return maxCacheAge;
  }

  /**
   * Get the maximum cache size.
   * @return the maximum cache size.
   */
  public int getMaxCacheSize() {
    return maxCacheSize;
  }

  /**
   * Get the selected channel names.
   * @return the selected channel names or null if none.
   */
  public Set getSelectedChannelNames() {
    return selectedChannelNames;
  }
}
