package me.zhengweibao.utils.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhengweibao
 */
public class Sha1DigestUtil {

	private static final Logger logger = LoggerFactory.getLogger(Sha1DigestUtil.class);

	/**
	 * 所有16进制字符
	 */
	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * UTF-8字符集
	 */
	private static final Charset UTF8_CHARSET = Charset.forName("UTF8");

	private static final MessageDigest shaDigest;

	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot found SHA algorithm of MessageDigest instance!");
			throw new RuntimeException(e);
		}
	}

	public static String getSha1HexDigest(String contentToDigest){
		byte[] dataBytes = shaDigest.digest(contentToDigest.getBytes(UTF8_CHARSET));
		return new String(encodeHex(dataBytes));
	}

	private static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = HEX_CHARS[(0xF0 & data[i]) >>> 4];
			out[j++] = HEX_CHARS[0x0F & data[i]];
		}
		return out;
	}
}
