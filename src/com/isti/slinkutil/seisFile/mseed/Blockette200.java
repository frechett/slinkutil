package com.isti.slinkutil.seisFile.mseed;

import java.io.IOException;
import java.io.Writer;

import com.isti.slinkutil.SLinkUtilFns;
import com.isti.slinkutil.Utility;

/**
 * The Generic Event Detection Blockette.
 */
public class Blockette200 extends DataBlockette {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**  Full size of blockette 200 */
  private static final int B200_SIZE = 52;

  /** The background position. */
  private static final int BACKGROUND = 12;

  /** The event detector position. */
  private static final int EVENT_DETECTOR = 28;

  /** The event detector length. */
  private static final int EVENT_DETECTOR_LENGTH = 24;

  /** The period position. */
  private static final int PERIOD = 8;

  /** The signal position. */
  private static final int SIGNAL = 4;

  /** The signal onset position. */
  private static final int SIGNAL_ONSET = 18;

  /**
   * Create the Generic Event Detection Blockette.
   * @param info the bytes.
   * @param swapBytes true to swap bytes, false otherwise.
   */
  public Blockette200(byte[] info, boolean swapBytes) {
    super(info, swapBytes);
    trimToSize(getSize());
  }

  /**
   * Create the Generic Event Detection Blockette.
   * @param signal the signal.
   * @param period the period.
   * @param background
   * @param signalOnset
   * @param eventDetector
   */
  public Blockette200(float signal, float period, float background,
      Btime signalOnset, String eventDetector) {
    super(B200_SIZE);
    Utility.insertFloat(signal, info, SIGNAL);
    Utility.insertFloat(period, info, PERIOD);
    Utility.insertFloat(background, info, BACKGROUND);
    byte[] onsetBytes = signalOnset.getAsBytes();
    System.arraycopy(onsetBytes, 0, info, SIGNAL_ONSET, onsetBytes.length);
    if (eventDetector.length() > EVENT_DETECTOR_LENGTH) {
      throw new IllegalArgumentException(
          "The event detector can only be up to " + EVENT_DETECTOR_LENGTH
              + " characters in length");
    }
    byte[] detectorBytes = SLinkUtilFns.getBytes(eventDetector);
    if (detectorBytes.length != eventDetector.length()) {
      throw new IllegalArgumentException(
          "The characters in event detector must be in the ASCII character set i.e. from 0-127");
    }
    detectorBytes = Utility.pad(detectorBytes, EVENT_DETECTOR_LENGTH,
        (byte) ' ');
    System.arraycopy(detectorBytes, 0, info, EVENT_DETECTOR,
        detectorBytes.length);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Blockette200) {
      byte[] oinfo = ((Blockette200) o).info;
      if (info.length != oinfo.length) {
        return false;
      }
      for (int i = 0; i < oinfo.length; i++) {
        if (info[i] != oinfo[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * @return - the background estimate field
   */
  public float getBackground() {
    return Float
        .intBitsToFloat(Utility.bytesToInt(info, BACKGROUND, swapBytes));
  }

  /**
   * Get the event detector.
   * @return the event detector.
   */
  public String getEventDetector() {
    return new String(info, EVENT_DETECTOR, EVENT_DETECTOR_LENGTH);
  }

  public String getName() {
    return "Generic Event Detection Blockette";
  }

  /**
   * @return - the signal period field
   */
  public float getPeriod() {
    return Float.intBitsToFloat(Utility.bytesToInt(info, PERIOD, swapBytes));
  }

  /**
   * @return - the signal amplitude field
   */
  public float getSignal() {
    return Float.intBitsToFloat(Utility.bytesToInt(info, SIGNAL, swapBytes));
  }

  /**
   * @return - the signal onset time field
   */
  public Btime getSignalOnset() {
    return new Btime(info, SIGNAL_ONSET);
  }

  public int getSize() {
    return B200_SIZE;
  }

  public int getType() {
    return 200;
  }

  public void writeASCII(Writer out) throws IOException {
    out.write("Blockette200");
  }
}
