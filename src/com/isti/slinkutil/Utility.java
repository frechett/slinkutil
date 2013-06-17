package com.isti.slinkutil;

/**
 * Utility.java
 * 
 * 
 * Created: Fri Apr 2 14:28:55 1999
 * 
 * @author Philip Crotwell
 * @version
 */
public class Utility {

  /**
   * Convert the byte to an integer.
   * @param a the byte.
   * @return the integer.
   */
  public static int bytesToInt(byte a) {
    return (int) a;
  }

  /**
   * Convert the bytes to an integer.
   * @param a the first byte.
   * @param b the second byte.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the integer.
   */
  public static int bytesToInt(byte a, byte b, boolean swapBytes) {
    if (swapBytes) {
      return (a & 0xff) + ((int) b << 8);
    } else {
      return ((int) a << 8) + (b & 0xff);
    }
  }

  /**
   * Convert the bytes to an integer.
   * @param a the first byte.
   * @param b the second byte.
   * @param c the third byte.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the integer.
   */
  public static int bytesToInt(byte a, byte b, byte c, boolean swapBytes) {
    if (swapBytes) {
      return (a & 0xff) + ((b & 0xff) << 8) + ((int) c << 16);
    } else {
      return ((int) a << 16) + ((b & 0xff) << 8) + (c & 0xff);
    }
  }

  /**
   * Convert the bytes to an integer.
   * @param a the first byte.
   * @param b the second byte.
   * @param c the third byte.
   * @param d the fourth byte.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the integer.
   */
  public static int bytesToInt(byte a, byte b, byte c, byte d, boolean swapBytes) {
    if (swapBytes) {
      return ((a & 0xff)) + ((b & 0xff) << 8) + ((c & 0xff) << 16)
          + ((d & 0xff) << 24);
    } else {
      return ((a & 0xff) << 24) + ((b & 0xff) << 16) + ((c & 0xff) << 8)
          + ((d & 0xff));
    }
  }

  /**
   * Convert the bytes to an integer.
   * @param info the bytes.
   * @param i the index.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the integer.
   */
  public static int bytesToInt(byte[] info, int i, boolean swapBytes) {
    return bytesToInt(info[i], info[i + 1], info[i + 2], info[i + 3], swapBytes);
  }

  /**
   * Convert the bytes to a short.
   * @param hi the high byte.
   * @param low the low byte.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the short.
   */
  public static short bytesToShort(byte hi, byte low, boolean swapBytes) {
    if (swapBytes) {
      return (short) ((hi & 0xff) + (low & 0xff) << 8);
    } else {
      return (short) (((hi & 0xff) << 8) + (low & 0xff));
    }
  }

  /**
   * Convert the float to a byte array.
   * @param a the float.
   * @return the byte array.
   */
  public static byte[] floatToByteArray(float a) {
    return intToByteArray(floatToIntBits(a));
  }

  /**
   * Convert the float to bits that represent the floating-point number.
   * @param a the float.
   * @return the bits that represent the floating-point number.
   */
  public static int floatToIntBits(float a) {
    return Float.floatToIntBits(a);
  }

  /**
   * Format the bytes.
   * @param source the array of bytes.
   * @param start the starting index.
   * @param end the ending index.
   * @return the formatted bytes.
   */
  public static byte[] format(byte[] source, int start, int end) {
    byte[] returnByteArray = new byte[start - end + 1];
    int j = 0;
    for (int i = start; i < end; i++, j++) {
      returnByteArray[j] = source[i];
    }
    return returnByteArray;
  }

  /**
   * Inserts float into dest at index pos 
   * @param value the float value.
   * @param dest the destination array.
   * @param pos the starting position in the destination array.
   */
  public static void insertFloat(float value, byte[] dest, int pos) {
    int bits = floatToIntBits(value);
    byte[] b = Utility.intToByteArray(bits);
    System.arraycopy(b, 0, dest, pos, 4);
  }

  /**
   * Convert the integer to a byte array.
   * @param a the integer.
   * @return the byte array.
   */
  public static byte[] intToByteArray(int a) {
    byte[] returnByteArray = new byte[4];// int is 4 bytes
    returnByteArray[0] = (byte) ((a & 0xff000000) >> 24);
    returnByteArray[1] = (byte) ((a & 0x00ff0000) >> 16);
    returnByteArray[2] = (byte) ((a & 0x0000ff00) >> 8);
    returnByteArray[3] = (byte) ((a & 0x000000ff));
    return returnByteArray;
  }

  /**
   * Convert an integer to an array of 4 bytes, high byte first.
   * @param v the integer value.
   * @return the array of bytes.
   */
  public static byte[] intToBytes(int v) {
    byte[] bytes = new byte[4];
    bytes[0] = (byte) (v >>> 24);
    bytes[1] = (byte) (v >>> 16);
    bytes[2] = (byte) (v >>> 8);
    bytes[3] = (byte) (v >>> 0);
    return bytes;
  }

  /**
   * Convert an integer to an array of 4 bytes, high byte first.
   * @param v the integer value.
   * @param bytes the byte array.
   * @param index the starting index.
   */
  public static void intToBytes(int v, byte[] bytes, int index) {
    bytes[index++] = (byte) (v >>> 24);
    bytes[index++] = (byte) (v >>> 16);
    bytes[index++] = (byte) (v >>> 8);
    bytes[index++] = (byte) (v >>> 0);
  }

  /**
   * Test the utility methods.
   * @param args the arguments.
   */
  public static void main(String[] args) {
    int a = 256;
    byte a1 = (byte) ((a & 0xff000000) >> 24);
    byte a2 = (byte) ((a & 0x00ff0000) >> 16);
    byte a3 = (byte) ((a & 0x0000ff00) >> 8);
    byte a4 = (byte) ((a & 0x000000ff));
    System.out.println("first byte is " + a1);
    System.out.println("2 byte is " + a2);
    System.out.println("3 byte is " + a3);
    System.out.println("4  byte is " + a4);
    byte[] source = new byte[5];
    for (int i = 0; i < 5; i++)
      source[i] = (byte) 10;
    byte[] output = Utility.pad(source, 5, (byte) 32);
    // for(int j = 0; j< output.length; j++)
    // {
    // System.out.println("byte"+j+" " + output[j]);
    // }
    for (int k = output.length - 1; k > -1; k--) {
      System.out.println("byte" + k + " " + output[k]);
    }
  }

  /**
   * Pad the bytes in the specified array of bytes.
   * @param source the array of bytes.
   * @param requiredBytes the number of bytes required.
   * @param paddingByte the padding byte value.
   * @return the padded array of bytes or the original source if no padding was
   * required.
   */
  public static byte[] pad(byte[] source, int requiredBytes, byte paddingByte) {
    if (source.length == requiredBytes) {
      return source;
    } else {
      int length = source.length;
      if (length > requiredBytes) {
        length = requiredBytes;
      }
      byte[] returnByteArray = new byte[requiredBytes];
      System.arraycopy(source, 0, returnByteArray, 0, length);
      for (int i = source.length; i < requiredBytes; i++) {
        returnByteArray[i] = paddingByte;
      }
      return returnByteArray;
    }
  }

  /**
   * Convert the unsigned byte to an integer.
   * @param a the unsigned byte.
   * @return the integer.
   */
  public static int uBytesToInt(byte a) {
    // we and with 0xff in order to get the sign correct (pos)
    return a & 0xff;
  }

  /**
   * Convert the unsigned bytes to an integer.
   * @param a the first byte.
   * @param b the second byte.
   * @param swapBytes true to swap bytes, false otherwise.
   * @return the integer.
   */
  public static int uBytesToInt(byte a, byte b, boolean swapBytes) {
    // we "and" with 0xff to get the sign correct (pos)
    if (swapBytes) {
      return (a & 0xff) + ((b & 0xff) << 8);
    } else {
      return ((a & 0xff) << 8) + (b & 0xff);
    }
  }
} // Utility
