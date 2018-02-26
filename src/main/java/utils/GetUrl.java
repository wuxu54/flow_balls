package utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by SpongeBob on 2017/9/26.
 *
 * 公司路径访问，签名工具类
 */

public class GetUrl {
    public static String URL_SAVE = "";
    public static String PAYURL_SAVE = "";

    /**
     * 获取签名后的请求路径
     *
     * @param map 参数
     * @return
     */
    public static String getSignUrl(Map<String, Object> map, String secret) {
        String url = getUrl(map);
        try {
            String sign = getSigns(url + secret);
            url = url + "&" + "sign=" + sign;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getSigns(String waitSign) throws IOException {

        // 使用MD5对待签名串求签
        byte[] bytes = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            bytes = md5.digest(waitSign.getBytes("UTF-8"));
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex);
        }

        // 将MD5输出的二进制结果转换为小写的十六进制
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();

    }

    public static String getUrl(Map<String, Object> map) {
        StringBuffer stringBuffer = new StringBuffer();
        Map<String, Object> treeMap = new TreeMap<>(map);
        //遍历map集合
        Iterator<Map.Entry<String, Object>> iterator = treeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            stringBuffer.append(entry.getKey() + "=" + entry.getValue());
            stringBuffer.append("&");
        }
        String url = stringBuffer.toString();
        if (url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
