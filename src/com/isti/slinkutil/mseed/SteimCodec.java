//SteimCodec:  Defines an abstract Steim Codec.
//
//  9/22/2010 -- [KF]  Initial version.
//

package com.isti.slinkutil.mseed;

import java.io.IOException;

import com.isti.slinkutil.seedcodec.SteimFrameBlock;

/**
 * Class SteimCodec defines a Steim Codec.
 */
public abstract class SteimCodec extends AbstractDataEncoder {
  /**
   * Defines the Steim data.
   */
  protected static class SteimData implements EncodedData {
    private final byte[] encodedData;

    private final boolean fullFlag;

    private final int numSamples;

    private final byte steimEncodingFormat;

    /**
     * Creates the Steim data.
     * @param steimEncodingFormat the Steim encoding format.
     * @param steimFrameBlock the Steim frame block.
     * @throws IOException if error.
     */
    public SteimData(byte steimEncodingFormat, SteimFrameBlock steimFrameBlock)
        throws IOException {
      this.steimEncodingFormat = steimEncodingFormat;
      encodedData = steimFrameBlock.getEncodedData();
      numSamples = steimFrameBlock.getNumSamples();
      fullFlag = steimFrameBlock.isFull();
    }

    /**
     * Return the compressed byte representation of the data for inclusion in a
     * data record.
     * @return byte array containing the encoded, compressed data.
     */
    public byte[] getEncodedData() {
      return encodedData;
    }

    /**
     * Returns the encoding format.
     * @return the encoding format.
     */
    public byte getEncodingFormat() {
      return steimEncodingFormat;
    }

    /**
     * Return the number of data samples.
     * @return the number of samples.
     */
    public int getNumSamples() {
      return numSamples;
    }

    /**
     * Determines if the data is full.
     * @return true if the data is full, false otherwise.
     */
    public boolean isFull() {
      return fullFlag;
    }
  }

  /**
   * The Steim frame length.
   */
  public static final int STEIM_FRAME_LENGTH = 64;

  /**
   * The Steim maximum number of frames.
   */
  public static final int STEIM_MAX_FRAMES = 63;

  /**
   * The Steim minimum number of frames.
   */
  public static final int STEIM_MIN_FRAMES = 1;

  /**
   * Returns the number of frames needed for the size.
   * @param size the size.
   * @return the number of frames needed.
   */
  public static int getFrames(int size) {
    return size / STEIM_FRAME_LENGTH;
  }

  /** The number of frames. */
  private int frames;

  /**
   * Creates a Steim Codec.
   * @param frames the number of frames to use in the encoding.
   */
  public SteimCodec(int frames) {
    setFrames(frames);
  }

  /**
   * Get the number of frames.
   * @return the number of frames.
   */
  public int getFrames() {
    return frames;
  }

  /**
   * Set the frames.
   * @param frames the number of frames to use in the encoding.
   */
  public final void setFrames(int frames) {
    if (frames < STEIM_MIN_FRAMES) {
      frames = STEIM_MIN_FRAMES;
    } else if (frames > STEIM_MAX_FRAMES) {
      frames = STEIM_MAX_FRAMES;
    }
    this.frames = frames;
  }
}
