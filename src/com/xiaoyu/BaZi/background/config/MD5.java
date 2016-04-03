package com.xiaoyu.BaZi.background.config;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z' };

	private static final char[] UPPER_CASE_DIGITS = { '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z' };

	public static String toMd5(String url) {
		try {
			if (!TextUtils.isEmpty(url)) {
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				byte[] md5bytes = messageDigest.digest(url.getBytes());
				return bytesToHexString(md5bytes, false);
			}
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
		return "";
	}

	private static String bytesToHexString(byte[] bytes, boolean upperCase) {
		char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
		char[] buf = new char[bytes.length * 2];
		int c = 0;
		for (byte b : bytes) {
			buf[c++] = digits[(b >> 4) & 0xf];
			buf[c++] = digits[b & 0xf];
		}
		return new String(buf);
	}

}
