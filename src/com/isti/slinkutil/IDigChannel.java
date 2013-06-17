package com.isti.slinkutil;

/**
 * The information for a digitizer channel.
 */
public interface IDigChannel {
  /**
   * Returns the SCNL object for the digitizer channel.
   * @return The 'StaChaNetLoc' object.
   */
  public IStaChaNetLoc getStaChaNetLocObj();

  /**
   * Determines if this channel is selected.
   * @return true if selected, false otherwise.
   */
  public boolean isSelected();
}
