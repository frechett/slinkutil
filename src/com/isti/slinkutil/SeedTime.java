//SeedTime.java:  Defines the SEED time.
//
//  9/17/2009 -- [KF]  Initial version.
//  1/29/2010 -- [ET]  Minor improvements.
//

package com.isti.slinkutil;

import com.isti.slinkutil.seisFile.mseed.Btime;

/**
 * Class SeedTime defines SEED time.
 */
public class SeedTime {
	/** SLinkCalendar object for converting time values. */
	private static SLinkCalendar _cal = null;

	/** The calendar sync object. */
	private static final Object calendarSyncObject = new Object();

	/** The number of days for 1/1/1970 GMT. */
	private static final long epochDays = 719162; // same as getDays(1970)

	/**
	 * Creates a calendar.
	 * 
	 * @return the calendar.
	 */
	protected static SLinkCalendar createCalendar() {
		return new SLinkCalendar();
	}

	/**
	 * Returns a copy of the 'Btime' value.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 * @return the new 'Btime' value.
	 */
	public static Btime getBtime(Btime btime) {
		final Btime newBtime = new Btime();
		newBtime.year = btime.year;
		newBtime.jday = btime.jday;
		newBtime.hour = btime.hour;
		newBtime.min = btime.min;
		newBtime.sec = btime.sec;
		newBtime.tenthMilli = btime.tenthMilli;
		return newBtime;
	}

	/**
	 * Returns the 'Btime' value.
	 * 
	 * @param time
	 *            the milliseconds since 1/1/1970 GMT.
	 * @return the 'Btime' value.
	 */
	public static Btime getBtime(long time) {
		return getBtime(time, time < 0);
	}

	/**
	 * Returns the 'Btime' value.
	 * 
	 * @param time
	 *            the milliseconds since 1/1/1970 GMT.
	 * @param useCalFlag
	 *            true to use a calendar, false otherwise.
	 * @return the 'Btime' value.
	 */
	protected static Btime getBtime(long time, boolean useCalFlag) {
		Btime btime = new Btime();
		if (useCalFlag) {
			synchronized (calendarSyncObject) {
				final SLinkCalendar cal = getCalendar();
				cal.setTimeInMillis(time);
				btime.year = cal.get(SLinkCalendar.YEAR);
				btime.jday = cal.get(SLinkCalendar.DAY_OF_YEAR);
				btime.hour = cal.get(SLinkCalendar.HOUR_OF_DAY);
				btime.min = cal.get(SLinkCalendar.MINUTE);
				btime.sec = cal.get(SLinkCalendar.SECOND);
				btime.tenthMilli = cal.get(SLinkCalendar.MILLISECOND) * 10;
			}
			return btime;
		}
		// get the tenths of ms
		btime.tenthMilli = (int) ((time % 1000)) * 10;
		// convert ms to seconds
		time /= 1000;
		// get the seconds
		btime.sec = (int) (time % 60);
		// convert seconds to minutes
		time /= 60;
		// get the minutes
		btime.min = (int) (time % 60);
		// convert minutes to hours
		time /= 60;
		// get the hours
		btime.hour = (int) (time % 24);
		// convert hours to days
		time /= 24;
		// add the epoch days
		time += epochDays;
		// get the year
		btime.year = getYear(time);
		// get the Julian day
		time -= getDays(btime.year);
		btime.jday = (int) time + 1;
		return btime;
	}

	/**
	 * Get the calendar.
	 * 
	 * @return the calendar.
	 */
	private static SLinkCalendar getCalendar() {
		synchronized (calendarSyncObject) {
			if (_cal == null) {
				_cal = createCalendar();
			}
			return _cal;
		}
	}

	/**
	 * Return the number of days for the specified year.
	 * 
	 * @param year
	 *            the year.
	 * @return the number of days for the specified year.
	 */
	private static long getDays(int year) {
		// This logic is from "sun.util.calendar.BaseCalendar"
		int prevyear = year - 1;
		long days = (365 * prevyear) + (prevyear / 4) - (prevyear / 100)
				+ (prevyear / 400);
		return days;
	}

	/**
	 * Return the time.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 * @return the time.
	 */
	public static long getTime(final Btime btime) {
		return getTime(btime, btime.year < 1970);
	}

	/**
	 * Return the time.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 * @param useCalFlag
	 *            true to use a calendar, false otherwise.
	 * @return the time.
	 */
	protected static long getTime(final Btime btime, boolean useCalFlag) {
		final long ms = Math.round(btime.tenthMilli / 10.0);
		if (useCalFlag) {
			synchronized (calendarSyncObject) {
				final SLinkCalendar cal = getCalendar();
				cal.clear();
				cal.set(SLinkCalendar.YEAR, btime.year);
				cal.set(SLinkCalendar.DAY_OF_YEAR, btime.jday);
				cal.set(SLinkCalendar.HOUR_OF_DAY, btime.hour);
				cal.set(SLinkCalendar.MINUTE, btime.min);
				cal.set(SLinkCalendar.SECOND, btime.sec);
				cal.set(SLinkCalendar.MILLISECOND, (int) ms);
				return cal.getTimeInMillis();
			}
		}
		// convert year to days since the epoch
		long time = getDays(btime.year) - epochDays;
		// add the days
		time += btime.jday - 1;
		// convert days to hours
		time *= 24;
		// add the hours
		time += btime.hour;
		// convert hours to minutes
		time *= 60;
		// add the minutes
		time += btime.min;
		// convert minutes to seconds
		time *= 60;
		// add the seconds
		time += btime.sec;
		// convert seconds to ms
		time *= 1000;
		// add the ms
		time += ms;
		return time;
	}

	/**
	 * Return the year for the specified number of days.
	 * 
	 * @param days
	 *            the number of days.
	 * @return the year.
	 */
	protected static int getYear(long days) {
		// This logic is from "sun.util.calendar.BaseCalendar"
		long d0 = days;
		int n400 = (int) (d0 / 146097);
		int d1 = (int) (d0 % 146097);
		int n100 = d1 / 36524;
		int d2 = d1 % 36524;
		int n4 = d2 / 1461;
		int d3 = d2 % 1461;
		int n1 = d3 / 365;
		long year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
		if (!(n100 == 4 || n1 == 4)) {
			++year;
		}
		return (int) year;
	}

	/**
	 * Project the time for the specified number of samples and sample rate.
	 * Note this is not the time of the last sample, but rather the predicted
	 * begin time of the next record. For the last sample time subtract one from
	 * the number of samples. This code was lifted from the
	 * 'kmi.smarts.module.rtslsup.seisFile.mseed.DataHeader.projectTime' method.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 * @param numSamples
	 *            the number of samples.
	 * @param sampleRate
	 *            the sample rate.
	 */
	public static void projectTime(Btime btime, int numSamples,
			double sampleRate) {
		// get the number of ten thousandths of seconds of data
		double tenThousandths = (((double) numSamples / sampleRate) * 10000.0);
		int offset = 0; // leap year offset
		// check to see if this is a leap year we are starting on
		boolean is_leap = ((btime.year % 4) == 0 && (btime.year % 100) != 0)
				|| (btime.year % 400) == 0;
		if (is_leap)
			offset = 1;
		// convert btime to tenths of seconds in the current year, then
		// add that value to the incremental time value tenThousandths
		tenThousandths += ttConvert(btime);
		// now increment year if it crosses the year boundary
		if ((tenThousandths) >= (366 + offset) * 864000000.0) {
			btime.year++;
			tenThousandths -= (365 + offset) * 864000000.0;
		}
		// increment day
		btime.jday = (int) (tenThousandths / 864000000.0);
		tenThousandths -= (double) btime.jday * 864000000.0;
		// increment hour
		btime.hour = (int) (tenThousandths / 36000000.0);
		tenThousandths -= (double) btime.hour * 36000000.0;
		// increment minutes
		btime.min = (int) (tenThousandths / 600000.0);
		tenThousandths -= (double) btime.min * 600000.0;
		// increment seconds
		btime.sec = (int) (tenThousandths / 10000.0);
		tenThousandths -= (double) btime.sec * 10000.0;
		// set tenth seconds
		btime.tenthMilli = (int) tenThousandths;
	}

	/**
	 * Convert contents of Btime structure to the number of ten thousandths of
	 * seconds it represents within that year. This code was lifted from the
	 * 'kmi.smarts.module.rtslsup.seisFile.mseed.DataHeader.ttConvert' method.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 * @return the number of ten thousandths of seconds since within the year.
	 */
	public static double ttConvert(Btime btime) {
		double tenThousandths = btime.jday * 864000000.0;
		tenThousandths += btime.hour * 36000000.0;
		tenThousandths += btime.min * 600000.0;
		tenThousandths += btime.sec * 10000.0;
		tenThousandths += btime.tenthMilli;
		return tenThousandths;
	}

	/** The 'Btime' value. */
	private final Btime btime;

	/** The milliseconds since 1/1/1970 GMT. */
	private final Long time;

	/**
	 * Creates the SEED time for the current time.
	 */
	public SeedTime() {
		this(System.currentTimeMillis());
	}

	/**
	 * Creates the SEED time.
	 * 
	 * @param btime
	 *            the 'Btime' value.
	 */
	public SeedTime(Btime btime) {
		this(getTime(btime), btime);
	}

	/**
	 * Creates the SEED time.
	 * 
	 * @param time
	 *            the milliseconds since 1/1/1970 GMT.
	 */
	public SeedTime(long time) {
		this(time, getBtime(time));
	}

	/**
	 * Creates the SEED time.
	 * 
	 * @param time
	 *            the milliseconds since 1/1/1970 GMT.
	 * @param btime
	 *            the 'Btime' value.
	 */
	protected SeedTime(long time, Btime btime) {
		this.time = Long.valueOf(time);
		this.btime = btime;
	}

	/**
	 * Compares two times for equality.
	 * 
	 * @param obj
	 *            object to compare.
	 * @return true if the objects are the same; false otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof SeedTime) {
			return time.equals(((SeedTime) obj).time);
		}
		return btime.equals(obj);
	}

	/**
	 * Return the 'Btime' value.
	 * 
	 * @return the 'Btime' value.
	 */
	public Btime getBtime() {
		return btime;
	}

	/**
	 * Return the milliseconds since 1/1/1970 GMT.
	 * 
	 * @return the milliseconds since 1/1/1970 GMT.
	 */
	public long getTime() {
		return time.longValue();
	}

	/**
	 * Returns a hash code value for this object.
	 * 
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return time.hashCode();
	}

	/**
	 * Project the time for the specified number of samples and sample rate.
	 * Note this is not the time of the last sample, but rather the predicted
	 * begin time of the next record. For the last sample time subtract one from
	 * the number of samples.
	 * 
	 * @param numSamples
	 *            the number of samples.
	 * @param sampleRate
	 *            the sample rate.
	 * @return the SeedLink time.
	 */
	public SeedTime projectTime(int numSamples, double sampleRate) {
		if (numSamples == 0) {
			return this;
		}
		final Btime newBtime = getBtime(btime);
		projectTime(newBtime, numSamples, sampleRate);
		return new SeedTime(newBtime);
	}

	/**
	 * Return a string representation of the time.
	 * 
	 * @return a string representation of the time.
	 */
	public String toString() {
		return btime.year + "." + btime.jday + " " + btime.hour + ":"
				+ btime.min + ":" + btime.sec + "." + btime.tenthMilli;
	}
}
