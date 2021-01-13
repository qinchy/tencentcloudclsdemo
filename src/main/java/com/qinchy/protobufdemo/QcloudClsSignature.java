package com.qinchy.protobufdemo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class QcloudClsSignature {

    public static final String LINE_SEPARATOR = "\n";
    public static final String Q_SIGN_ALGORITHM_KEY = "q-sign-algorithm";
    public static final String Q_SIGN_ALGORITHM_VALUE = "sha1";
    public static final String Q_AK = "q-ak";
    public static final String Q_SIGN_TIME = "q-sign-time";
    public static final String Q_KEY_TIME = "q-key-time";
    public static final String Q_HEADER_LIST = "q-header-list";
    public static final String Q_URL_PARAM_LIST = "q-url-param-list";
    public static final String Q_SIGNATURE = "q-signature";
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 过滤请求头
     *
     * @param originHeaders 原始请求头
     * @return {@link Map<String, String>}
     **/
    private static Map<String, String> filterHeaders(Map<String, String> originHeaders) {
        Map<String, String> signHeaders = new HashMap<String, String>();
        for (Entry<String, String> headerEntry : originHeaders.entrySet()) {
            String key = headerEntry.getKey();
            if (key.equalsIgnoreCase("content-type")
                    || key.equalsIgnoreCase("content-md5")
                    || key.equalsIgnoreCase("host")
                    || key.startsWith("x")
                    || key.startsWith("X")) {
                String lowerKey = key.toLowerCase();
                String value = headerEntry.getValue();
                signHeaders.put(lowerKey, value);
            }
        }

        return signHeaders;
    }

    /**
     * 获取请求头/请求参数的key列表用;拼接
     *
     * @param signHeaders TODO
     * @return {@link String}
     **/
    private static String buildSignMemberStr(Map<String, String> signHeaders) {
        StringBuilder strBuilder = new StringBuilder();
        // 是否是第一次，涉及到是否拼接;号。
        boolean seenFirst = false;
        for (String key : signHeaders.keySet()) {
            if (!seenFirst) {
                seenFirst = true;
            } else {
                strBuilder.append(";");
            }
            strBuilder.append(key.toLowerCase());
        }
        return strBuilder.toString();
    }

    /**
     * 将map转换成key1=value1&key2=value2的字符串
     *
     * @param kVMap 传入参数map
     * @return {@link String}
     **/
    private static String formatMapToStr(Map<String, String> kVMap) throws UnsupportedEncodingException {
        StringBuilder strBuilder = new StringBuilder();
        boolean seeOne = false;
        for (Entry<String, String> entry : kVMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String lowerKey = key.toLowerCase();
            String encodeKey = URLEncoder.encode(lowerKey, DEFAULT_ENCODING).replace("*", "%2A");
            String encodedValue = "";
            if (value != null) {
                encodedValue = URLEncoder.encode(value, DEFAULT_ENCODING).replace("*", "%2A");
            }
            if (!seeOne) {
                seeOne = true;
            } else {
                strBuilder.append("&");
            }
            strBuilder.append(encodeKey).append("=").append(encodedValue);
        }
        return strBuilder.toString();
    }

    /**
     * 返回签名有效起止时间
     *
     * @param expireMillsecond 签名的有效期
     * @return {@link String}
     **/
    private static String buildTimeStr(long expireMillsecond) {
        StringBuilder strBuilder = new StringBuilder();
        long startTime = System.currentTimeMillis() / 1000 - 60;
        long endTime = (System.currentTimeMillis() + expireMillsecond) / 1000;
        strBuilder.append(startTime).append(";").append(endTime);
        //return "1510109254;1510109314";
        return strBuilder.toString();
    }

    /**
     * 构造签名
     *
     * @param secretId secretId
     * @param secretKey secretKey
     * @param method 请求方式
     * @param path 请求路径
     * @param paramMap 请求参数map
     * @param headerMap 请求头map
     * @param expireMillsecond 超时时间
     * @return {@link String}
     **/
    public static String buildSignature(String secretId, String secretKey, String method, String path,
                                        Map<String, String> paramMap, Map<String, String> headerMap,
                                        long expireMillsecond)
            throws UnsupportedEncodingException {

        Map<String, String> filteredHeaders = filterHeaders(headerMap);
        // 根据字典顺序排序后的请求头
        TreeMap<String, String> sortedSignHeaders = new TreeMap<String, String>();
        // 根据字典顺序排序后的请求参数
        TreeMap<String, String> sortedParams = new TreeMap<String, String>();

        sortedSignHeaders.putAll(filteredHeaders);
        sortedParams.putAll(paramMap);

        // 获取请求头参数用;号拼接的字符串
        String qHeaderListStr = buildSignMemberStr(sortedSignHeaders);
        // 获取请求参数用;号拼接的字符串
        String qUrlParamListStr = buildSignMemberStr(sortedParams);

        String qKeyTimeStr, qSignTimeStr;
        qKeyTimeStr = qSignTimeStr = buildTimeStr(expireMillsecond);

        // 第一步：使用secretKey对签名有效起止时间进行HmacSHA1加密，加密结果作为第三步的密钥
        String signKey = HmacUtils.hmacSha1Hex(secretKey, qKeyTimeStr);

        // 请求方式小写
        String formatMethod = method.toLowerCase();
        String formatUri = path;
        // 对请求参数按key字典顺序拼接成"key1=value1&key2=value2"
        String formatParameters = formatMapToStr(sortedParams);
        String formatHeaders = formatMapToStr(sortedSignHeaders);

        // 拼接带换行的字符串
        String formatStr = new StringBuilder()
                .append(formatMethod).append(LINE_SEPARATOR)
                .append(formatUri).append(LINE_SEPARATOR)
                .append(formatParameters).append(LINE_SEPARATOR)
                .append(formatHeaders).append(LINE_SEPARATOR)
                .toString();

        // 第二步：对字符串进行SHA1编码，编码结果作为第三步的stringToSign的其中一个输入
        // 开始对请求方式、请求uri、请求参数、请求头的如下格式进行SHA1编码
        //        get
        //        /logset
        //        logset_id=c6d2f9d4-b15b-4f05-ae28-516eae4bdd22
        //        host=ap-shanghai.cls.tencentcs.com
        String hashFormatStr = DigestUtils.sha1Hex(formatStr);

        // 第三步：使用第一步的结果作为密钥，对stringToSign进行HmacSHA1加密
        //        其中stringToSign包含了第二步的编码结果hashFormatStr
        String stringToSign = new StringBuilder()
                .append(Q_SIGN_ALGORITHM_VALUE).append(LINE_SEPARATOR)
                .append(qSignTimeStr).append(LINE_SEPARATOR)
                .append(hashFormatStr).append(LINE_SEPARATOR)
                .toString();
        String signature = HmacUtils.hmacSha1Hex(signKey, stringToSign);

        String authoriationStr = new StringBuilder()
                .append(Q_SIGN_ALGORITHM_KEY).append("=").append(Q_SIGN_ALGORITHM_VALUE).append("&")
                .append(Q_AK).append("=").append(secretId).append("&")
                .append(Q_SIGN_TIME).append("=").append(qSignTimeStr).append("&")
                .append(Q_KEY_TIME).append("=").append(qKeyTimeStr).append("&")
                .append(Q_HEADER_LIST).append("=").append(qHeaderListStr).append("&")
                .append(Q_URL_PARAM_LIST).append("=").append(qUrlParamListStr).append("&")
                .append(Q_SIGNATURE).append("=").append(signature)
                .toString();
        return authoriationStr;
    }

    public static void main(String args[]) {
        Map<String, String> paramMap = new HashMap();
        paramMap.put("logset_id", "c6d2f9d4-b15b-4f05-ae28-516eae4bdd22");

        Map<String, String> headerMap = new HashMap();
        headerMap.put("Host", "ap-shanghai.cls.tencentcs.com");
        headerMap.put("User-Agent", "AuthSDK");
        try {
            System.out.println(
                    QcloudClsSignature.buildSignature(
                            "",
                            "",
                            "GET", "/logset",
                            paramMap, headerMap, 300000));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        paramMap.clear();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Content-MD5", "f9c7fc33c7eab68dfa8a52508d1f4659");
        try {
            System.out.println(
                    QcloudClsSignature.buildSignature(
                            "",
                            "",
                            "PUT", "/logset",
                            paramMap, headerMap, 300000));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
