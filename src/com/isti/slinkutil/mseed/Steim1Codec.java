//Steim1MiniSeed.java:  Defines Steim1 Codec.
//
//  9/18/2009 -- [KF]  Initial version.
//  9/22/2010 -- [KF]  Changed to extend new SteimCodec class.
//

package com.isti.slinkutil.mseed;

import com.isti.slinkutil.LogMgr;
import com.isti.slinkutil.seedcodec.Steim1;

/**
 * Class Steim1Encoder defines Steim1 Codec.
 */
public class Steim1Codec extends SteimCodec {
  /**
   * Steim1 encoding format (used in B1000.)
   */
  public static final byte STEIM1_ENCODING_FORMAT = 10;

  /**
   * Creates a Steim1 Codec.
   * @param frames the number of frames to use in the encoding.
   */
  public Steim1Codec(int frames) {
    super(frames);
  }

  /**
   * Encode the array of integer values into a compressed byte frame block.
   * @param samples the data points represented as signed integers.
   * @param samplesLength the samples length.
   * @return the encoded data or null if error.
   */
  public EncodedData encode(int[] samples, int samplesLength) {
    try {
      return new SteimData(STEIM1_ENCODING_FORMAT, Steim1.encode(samples,
          getFrames(), getBias(), samplesLength));
    } catch (Exception ex) {
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
    }
    return null;
  }
}
