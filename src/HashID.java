// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {

	public static byte [] computeHashID(String line) throws Exception {
		if (line.endsWith("\n")) {
			// What this does and how it works is covered in a later lecture
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(line.getBytes(StandardCharsets.UTF_8));
			return md.digest();

		} else {
			// 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
			throw new Exception("No new line at the end of input to HashID");
		}
	}
	//this method will calculate the distance between two hashIDs by counting the number of differing bits
	public static int calculateDistanceBetween(byte[] hashID1, byte[] hashID2) {
		int distance = 0;
		for (int i = 0; i < hashID1.length; i++) {
			distance += Integer.bitCount(hashID1[i] ^ hashID2[i]);
		}
		return 256 - distance;
	}
	// New method to count leading zeros in a byte array. It counts the number of leading zeros in a hashID
	public static int countAmountOfZeros(byte[] hashID) {
		int leadingZeros = 0;
		for (byte b : hashID) {
			if (b == 0) {
				leadingZeros += 8; // Each byte contains 8 bits
			} else {
				leadingZeros += Integer.numberOfLeadingZeros(Byte.toUnsignedInt(b)) - 24;
				break;
			}
		}
		return leadingZeros;
	}
	// New method to perform a bitwise XOR operation between two hashIDs, returning the result as a new byte array
	public static byte[] xor(byte[] hashID1, byte[] hashID2) {
		if (hashID1.length != hashID2.length) {
			throw new IllegalArgumentException("Byte arrays must have the same length for XOR operation");
		}

		byte[] result = new byte[hashID1.length];
		for (int i = 0; i < hashID1.length; i++) {
			result[i] = (byte) (hashID1[i] ^ hashID2[i]);
		}
		return result;
	}
}

