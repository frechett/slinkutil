//Steim2MiniSeed.java:  Defines Steim2 Codec.
//
//  9/02/2010 -- [DN]  Initial version.
//  9/22/2010 -- [KF]  Changed to extend new SteimCodec class.
//

package com.isti.slinkutil.mseed;

import com.isti.slinkutil.LogMgr;
import com.isti.slinkutil.seedcodec.Steim2;

/**
 * Class Steim2Encoder defines Steim2 Codec.
 */
public class Steim2Codec extends SteimCodec {
  /**
   * Steim2 encoding format (used in B1000.)
   */
  public static final byte STEIM2_ENCODING_FORMAT = 11;

  /**
   * Creates a Steim2 Codec.
   * @param frames the number of frames to use in the encoding.
   */
  public Steim2Codec(int frames) {
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
      return new SteimData(STEIM2_ENCODING_FORMAT, Steim2.encode(samples,
          getFrames(), getBias(), samplesLength));
    } catch (Exception ex) {
      LogMgr.usrMsgWarning(LogMgr.getStackTraceString(ex));
    }
    return null;
  }
}
