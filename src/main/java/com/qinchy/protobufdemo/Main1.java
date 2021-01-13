package com.qinchy.protobufdemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author Administrator
 * @date 2021/1/12 17:09
 */
public class Main1 {
    public static void main(String[] args) throws IOException {
//        getSendData();
//        get();
        post();
        return;
    }

    private static byte[] getSendData() throws IOException {
        String json = "{\n" +
                "\t\"ownerId\": \"wxid_0vtf86zimd522\",\n" +
                "\t\"syncSeq\": \"1609509362325\",\n" +
                "\t\"serverTime\": 1609509366268,\n" +
                "\t\"subgroupId\": \"1078740\",\n" +
                "\t\"status\": 4,\n" +
                "\t\"localId\": 454,\n" +
//                "\t\"_id\": \"wxid_0vtf86zimd522-1592883400247-454\",\n" +
                "\t\"serverId\": \"5020795556531757393\",\n" +
                "\t\"type\": 285212721,\n" +
                "\t\"createTime\": 1609495467000,\n" +
                "\t\"talker\": \"gh_09e5395b86db\",\n" +
                "\t\"talkerType\": 0,\n" +
                "\t\"content\": \"\",\n" +
                "\t\"receive\": true,\n" +
                "\t\"bizChatId\": -1\n" +
                "}";

        // 将字段都放到一个Log里面
        JSONObject jsonObject = JSON.parseObject(json);
        return QcloudClsDataBuilder.buildData(jsonObject);
    }

    private static String getAuthorization(String method, String uri, Map<String, String> paramMap,
                                           Map<String, String> headerMap) throws UnsupportedEncodingException {
        return QcloudClsSignature.buildSignature(
                "",
                "",
                method, uri,
                paramMap, headerMap, 300000);
    }

    /**
     * get请求
     **/
    private static void get() throws UnsupportedEncodingException {
        String url = "http://ap-shanghai.cls.tencentcs.com/logset?logset_id=";
        String uri = "/logset";
        OkHttpClient okHttpClient = new OkHttpClient();

        Map<String, String> paramMap = new HashMap();
        //paramMap.put("logset_id", "c6d2f9d4-b15b-4f05-ae28-516eae4bdd22");

        Map<String, String> headerMap = new HashMap();
        headerMap.put("Host", "ap-shanghai.cls.tencentcs.com");
        headerMap.put("User-Agent", "AuthSDK");

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Host", "ap-shanghai.cls.tencentcs.com")
                .addHeader("Authorization", getAuthorization("get", uri, paramMap, headerMap))
                .build();
        final Call call = okHttpClient.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void post() throws UnsupportedEncodingException {
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/x-protobuf");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                byte[] bytes = getSendData();
                sink.write(bytes);
            }
        };

        Map<String, String> httpClientHeader = new HashMap();
        httpClientHeader.put("Host", "ap-shanghai.cls.tencentcs.com");
        httpClientHeader.put("Content-Type", "application/x-protobuf");

        Map<String, String> paramMap = new HashMap();
//        paramMap.put("topic_id", "c6d2f9d4-b15b-4f05-ae28-516eae4bdd22");

        Map<String, String> headerMap = new HashMap();
        headerMap.put("Host", "ap-shanghai.cls.tencentcs.com");
        headerMap.put("User-Agent", "AuthSDK");

        String url = "http://ap-shanghai.cls.tencentcs.com/structuredlog?topic_id=";
        String uri = "/structuredlog";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(requestBody);

        for (Map.Entry<String, String> entry : httpClientHeader.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        builder.addHeader("Authorization", getAuthorization("post", uri, paramMap, headerMap));

        final Request request = builder.build();
        final Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.code());
            System.out.println(response.message());
            System.out.println(response.body().string() + "ddddddd");

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
