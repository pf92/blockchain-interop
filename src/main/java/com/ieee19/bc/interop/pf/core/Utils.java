package com.ieee19.bc.interop.pf.core;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Utils {

    public static ZonedDateTime convertUnixEpochTime(long unixEpochTimeInSeconds) {
        Instant instant = Instant.ofEpochSecond(unixEpochTimeInSeconds);
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static String decodeHex(String hex) throws DecoderException, UnsupportedEncodingException {
        String hexWithOutPrefix = hex.replace("0x", "");
        byte[] bytes = Hex.decodeHex(hexWithOutPrefix);
        return new String(bytes, "UTF-8");
    }

    public static String encodeHex(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static String encodeHexPrefix(byte[] data) {
        return "0x" + Hex.encodeHexString(data);
    }

}
