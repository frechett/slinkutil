//MiniSeedMsgHldr.java:  Holds a miniSEED message and its envelope
//                       information.
//
//  9/25/2009 -- [ET]  Initial version.
//   2/1/2010 -- [ET]  Added 'getStartTimeObj()' method; modified to extend
//                     the 'Number' class to facilitate using this class
//                     with a comparator.
//

package com.isti.slinkutil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class MiniSeedMsgHldr holds a miniSEED message and its envelope information.
 */
public class MiniSeedMsgHldr extends Number {
	/**
   * 
   */
	private static final long serialVersionUID = 1L;
	private final byte[] messageDataArray;
	private final IMessageNumber messageNumber;
	private final int numSamples;
	private final IStaChaNetLoc staChaNetLoc;
	private final SeedTime startTime;

	/**
	 * Creates a miniSEED holder object.
	 * 
	 * @param staChaNetLoc
	 *            station, channel, network, location.
	 * @param startTime
	 *            message start time.
	 * @param messageDataArray
	 *            byte array containing message data.
	 * @param numSamples
	 *            the number of samples.
	 * @param messageNumber
	 *            the SeedLink message number.
	 */
	public MiniSeedMsgHldr(IStaChaNetLoc staChaNetLoc, SeedTime startTime,
			byte[] messageDataArray, int numSamples,
			IMessageNumber messageNumber) {
		this.staChaNetLoc = staChaNetLoc;
		this.startTime = startTime;
		this.messageDataArray = messageDataArray;
		this.numSamples = numSamples;
		this.messageNumber = messageNumber;
	}

	/**
	 * Creates a miniSEED holder object.
	 * 
	 * @param stationIdStr
	 *            station identifier string.
	 * @param channelIdStr
	 *            channel identifier string.
	 * @param networkCodeStr
	 *            network code string.
	 * @param locationIdStr
	 *            location identifier string.
	 * @param startTime
	 *            message start time.
	 * @param messageDataArray
	 *            byte array containing message data.
	 * @param numSamples
	 *            the number of samples.
	 * @param messageNumber
	 *            the SeedLink message number.
	 */
	public MiniSeedMsgHldr(String stationIdStr, String channelIdStr,
			String networkCodeStr, String locationIdStr, SeedTime startTime,
			byte[] messageDataArray, int numSamples,
			IMessageNumber messageNumber) {
		this(new StaChaNetLoc(stationIdStr, channelIdStr, networkCodeStr,
				locationIdStr), startTime, messageDataArray, numSamples,
				messageNumber);
	}

	/**
	 * Returns the message start time, in milliseconds since 1/1/1970. This
	 * method is needed to extend the 'Number' class, which facilitates using
	 * this class with a comparator.
	 * 
	 * @return The message start time.
	 */
	public double doubleValue() {
		return (double) (startTime.getTime());
	}

	/**
	 * Returns the message start time, in milliseconds since 1/1/1970. This
	 * method is needed to extend the 'Number' class, which facilitates using
	 * this class with a comparator.
	 * 
	 * @return The message start time.
	 */
	public float floatValue() {
		return (float) (startTime.getTime());
	}

	/**
	 * Returns the channel identifier string for the message.
	 * 
	 * @return The channel identifier string.
	 */
	public String getChannelIdStr() {
		return staChaNetLoc.getChannelCode();
	}

	/**
	 * Get the file for this miniSEED data.
	 * 
	 * @param outputDir
	 *            the output directory.
	 * @return the file.
	 */
	public File getFile(File outputDir) {
		return new File(outputDir, staChaNetLoc.getUniqueId() + "."
				+ getStartTimeMsVal() + ".mseed");
	}

	/**
	 * Returns the location identifier string for the message.
	 * 
	 * @return The location identifier string.
	 */
	public String getLocationIdStr() {
		return staChaNetLoc.getLocationCode();
	}

	/**
	 * Returns the byte array containing message data.
	 * 
	 * @return The byte array containing message data
	 */
	public byte[] getMessageDataArray() {
		return messageDataArray;
	}

	/**
	 * Returns the index number for message.
	 * 
	 * @return The index number for message.
	 */
	public int getMessageNumber() {
		return messageNumber.getMessageNumber();
	}

	/**
	 * Returns the network code string for the message.
	 * 
	 * @return The network code string.
	 */
	public String getNetworkCodeStr() {
		return staChaNetLoc.getNetworkCode();
	}

	/**
	 * Returns the number of samples.
	 * 
	 * @return the number of samples.
	 */
	public int getNumSamples() {
		return numSamples;
	}

	/**
	 * Return the SeedLink message number.
	 * 
	 * @return the SeedLink message number.
	 */
	public IMessageNumber getSLinkMessageNumber() {
		return messageNumber;
	}

	/**
	 * Returns the SCNL object for the message.
	 * 
	 * @return The 'StaChaNetLoc' object for the message.
	 */
	public IStaChaNetLoc getStaChaNetLocObj() {
		return staChaNetLoc;
	}

	/**
	 * Returns the message start time, in milliseconds since 1/1/1970.
	 * 
	 * @return The message start time.
	 */
	public long getStartTimeMsVal() {
		return startTime.getTime();
	}

	/**
	 * Returns the message start time object.
	 * 
	 * @return The message start time object.
	 */
	public SeedTime getStartTimeObj() {
		return startTime;
	}

	/**
	 * Returns the station identifier string for the message.
	 * 
	 * @return The station identifier string.
	 */
	public String getStationIdStr() {
		return staChaNetLoc.getStationCode();
	}

	/**
	 * Returns the time the message was created.
	 * 
	 * @return the time the message was created.
	 */
	public long getTimeCreated() {
		return messageNumber.getTimeCreated();
	}

	/**
	 * Returns the message start time, in milliseconds since 1/1/1970. This
	 * method is needed to extend the 'Number' class, which facilitates using
	 * this class with a comparator.
	 * 
	 * @return The message start time.
	 */
	public int intValue() {
		return (int) (startTime.getTime());
	}

	/**
	 * Returns the message start time, in milliseconds since 1/1/1970. This
	 * method is needed to extend the 'Number' class, which facilitates using
	 * this class with a comparator.
	 * 
	 * @return The message start time.
	 */
	public long longValue() {
		return startTime.getTime();
	}

	/**
	 * Returns a string representation of the miniSEED message.
	 * 
	 * @return a string representation of the miniSEED message.
	 */
	public String toString() {
		return staChaNetLoc + ", msgNum=" + messageNumber + ", start="
				+ startTime + ", samples=" + numSamples;
	}

	/**
	 * Write the miniSEED data to a file.
	 * 
	 * @param file
	 *            the file.
	 * @throws IOException
	 *             if an I/O Exception occurs.
	 */
	public void write(File file) throws IOException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			write(bos);
		} finally {
			SLinkUtilFns.close(fos);
			SLinkUtilFns.close(bos);
		}
	}

	/**
	 * Write the miniSEED data to the output stream.
	 * 
	 * @param os
	 *            the output stream.
	 * @throws IOException
	 *             if an I/O Exception occurs.
	 */
	public void write(OutputStream os) throws IOException {
		os.write(messageDataArray);
		os.flush();
	}
}
