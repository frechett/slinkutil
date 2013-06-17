//SLinkTime.java:  Defines the SEED Link time.
//
//  10/6/2009 -- [KF]  Initial version.
//

package com.isti.slinkutil;

import java.util.StringTokenizer;

/**
 * Class SLinkTime defines the SEED Link time.
 */
public class SLinkTime {
  /** The delimiter. */
  private static final String delimiter = ",";

  /** The calendar sync object. */
  private static final Object calendarSyncObject = new Object();

  /** SLinkCalendar object for converting time values. */
  private static SLinkCalendar _cal = null;

  /** The milliseconds since 1/1/1970 GMT. */
  private final long time;

  /**
   * Creates a SeedLink Time with the current time.
   */
  public SLinkTime() {
    this(System.currentTimeMillis());
  }

  /**
   * Creates a SeedLink Time with the specified time.
   * @param time the milliseconds since 1/1/1970 GMT.
   */
  public SLinkTime(long time) {
    this.time = time;
  }

  /**
   * Creates a SeedLink Time with the time specified by the text.
   * @param s the time text (year,month,day,hour,minute,second, e.g.
   *          Õ2009,10,02,13,08,00Õ).
   * @throws IllegalArgumentException if there is an invalid value or an invalid
   *           number of values.
   * @throws NumberFormatException if the string does not contain parsable
   *           integers.
   */
  public SLinkTime(String s) {
    this(getTime(s));
  }

  /**
   * Compares two times for equality.
   * @return true if the objects are the same; false otherwise.
   */
  public boolean equals(Object obj) {
    if (obj instanceof SLinkTime) {
      return time == ((SLinkTime) obj).time;
    }
    return false;
  }

  /**
   * Return the milliseconds since 1/1/1970 GMT.
   * @return the milliseconds since 1/1/1970 GMT.
   */
  public long getTime() {
    return time;
  }

  /**
   * Return a string representation of the time.
   * @return a string representation of the time.
   */
  public String toString() {
    synchronized (calendarSyncObject) {
      return getText(getCalendar(time));
    }
  }

  /**
   * Creates a calendar.
   * @return the calendar.
   */
  protected static SLinkCalendar createCalendar() {
    return new SLinkCalendar();
  }

  /**
   * Get the calendar. Access to this method should be synchronized.
   * @return the calendar.
   * @see #calendarSyncObject
   */
  protected static SLinkCalendar getCalendar() {
    if (_cal == null) {
      _cal = createCalendar();
    }
    return _cal;
  }

  /**
   * Get the calendar. Access to this method should be synchronized.
   * @param time the milliseconds since 1/1/1970 GMT.
   * @return the calendar.
   * @see #calendarSyncObject
   */
  protected static SLinkCalendar getCalendar(long time) {
    SLinkCalendar cal = getCalendar();
    cal.setTimeInMillis(time);
    return cal;
  }

  /**
   * Returns the text.
   * @param year the year.
   * @param month the month.
   * @param day the day of the month.
   * @param hour the hour.
   * @param min the minute.
   * @param sec the seconds.
   * @return the text.
   */
  public static String getText(int year, int month, int day, int hour, int min,
      int sec) {
    // convert month from 0-11 to 1-12
    return year + delimiter + (month + 1) + delimiter + day + delimiter + hour
        + delimiter + min + delimiter + sec;
  }

  /**
   * Return the text.
   * @param cal the calendar.
   * @return the text.
   */
  protected static String getText(SLinkCalendar cal) {
    return getText(cal.get(SLinkCalendar.YEAR), cal.get(SLinkCalendar.MONTH),
        cal.get(SLinkCalendar.DAY_OF_MONTH),
        cal.get(SLinkCalendar.HOUR_OF_DAY), cal.get(SLinkCalendar.MINUTE), cal
            .get(SLinkCalendar.MILLISECOND));
  }

  /**
   * Return the time.
   * @param year the year.
   * @param month the month.
   * @param day the day of the month.
   * @param hour the hour.
   * @param min the minute.
   * @param sec the seconds.
   * @return the time.
   */
  public static long getTime(int year, int month, int day, int hour, int min,
      int sec) {
    synchronized (calendarSyncObject) {
      final SLinkCalendar cal = getCalendar();
      return setTime(cal, year, month, day, hour, min, sec);
    }
  }

  /**
   * Return the time for the specified text.
   * @param s the time text (year,month,day,hour,minute,second, e.g.
   *          Õ2009,10,02,13,08,00Õ).
   * @return the time.
   * @throws IllegalArgumentException if there is an invalid value.
   */
  public static long getTime(String s) {
    final StringTokenizer st = new StringTokenizer(s, delimiter);
    if (st.countTokens() != 6) {
      throw new IllegalArgumentException("Invalid number of values \"" + s
          + "\": " + st.countTokens());
    }
    final int year = Integer.parseInt(st.nextToken());
    // convert month from 1-12 to 0-11
    final int month = Integer.parseInt(st.nextToken()) - 1;
    final int day = Integer.parseInt(st.nextToken());
    final int hour = Integer.parseInt(st.nextToken());
    final int minute = Integer.parseInt(st.nextToken());
    final int second = Integer.parseInt(st.nextToken());
    return getTime(year, month, day, hour, minute, second);
  }

  /**
   * Set the time.
   * @param cal the calendar.
   * @param year the year.
   * @param month the month.
   * @param day the day of the month.
   * @param hour the hour.
   * @param min the minute.
   * @param sec the seconds.
   * @return the time.
   * @throws IllegalArgumentException if there is an invalid value.
   */
  protected static long setTime(SLinkCalendar cal, int year, int month,
      int day, int hour, int min, int sec) {
    cal.clear();
    cal.set(SLinkCalendar.YEAR, year);
    cal.set(SLinkCalendar.MONTH, month);
    cal.set(SLinkCalendar.DAY_OF_MONTH, day);
    cal.set(SLinkCalendar.HOUR_OF_DAY, hour);
    cal.set(SLinkCalendar.MINUTE, min);
    cal.set(SLinkCalendar.SECOND, sec);
    return cal.getTimeInMillis();
  }
}
