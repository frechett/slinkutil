package com.isti.slinkutil;

/**
 * The message number.
 */
public interface IMessageNumber {
  /**
   * Get a byte array containing the message number.
   * @return A byte array containing the message number.
   */
  public byte[] getBytes();

  /**
   * Get the message number.
   * @return the message number.
   */
  public int getMessageNumber();

  /**
   * Get the time the message was created.
   * @return the time the message was created.
   */
  public long getTimeCreated();
}
