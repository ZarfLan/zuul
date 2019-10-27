package com.jiuzhe.app.gateway.utils;

import java.security.MessageDigest;

public class MD5Util {
    private static final String HEX_DIGITS[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * @Description:MD5编码
     * @author:张磊
     * @date:2018/4/4
     */
    public static String md5(String codingContent) {
        try {
            byte[] btInput = codingContent.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            String str = "";
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str += HEX_DIGITS[byte0 >>> 4 & 0xf] + HEX_DIGITS[byte0 & 0xf];
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
