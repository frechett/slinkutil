//SLinkCalendar.java:  Defines the SEED Link calendar.
//
//  10/6/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * The SEED Link calendar.
 */
public class SLinkCalendar extends GregorianCalendar {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a SeedLink calendar based on the current time.
   */
  public SLinkCalendar() {
    super(TimeZone.getTimeZone("GMT"));
    setLenient(false);
  }

  /**
   * Returns this Calendar's time value in milliseconds.
   * @return the current time as UTC milliseconds from the epoch.
   * @see #getTime()
   * @see #setTimeInMillis(long)
   */
  public long getTimeInMillis() {
    return super.getTimeInMillis();
  }

  /**
   * Sets this Calendar's current time from the given long value.
   * @param millis the new time in UTC milliseconds from the epoch.
   * @see #setTime(Date)
   * @see #getTimeInMillis()
   */
  public void setTimeInMillis(long millis) {
    super.setTimeInMillis(millis);
  }
}
