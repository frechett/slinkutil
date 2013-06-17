package com.isti.slinkutil;

/**
 * Class BasicMessageManager manages the caching and delivery of 'miniSEED'
 * messages.
 */
public class BasicMessageManager extends AbstractMessageManager {
	/** The message number is not needed for just miniSEED. */
	private static final IMessageNumber messageNumber = new SLinkMessageNumber(
			0);

	/**
	 * Get the current message number and increment the value. This method
	 * should be overridden if the message number is being used for SeedLink.
	 * 
	 * @return the SeedLink message number.
	 */
	public IMessageNumber getMessageNumber() {
		return messageNumber;
	}

	/**
	 * Processes the miniSEED message. This method should be overridden to
	 * process messages.
	 * 
	 * @param miniSeedMsg
	 *            the miniSEED message.
	 */
	public void processMiniSeedMessage(MiniSeedMsgHldr miniSeedMsg) {
		System.out.println(miniSeedMsg + " (length="
				+ miniSeedMsg.getMessageDataArray().length + " bytes)");
	}
}
