package com.qinchy.protobufdemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
public class Main2 {
    public static void main(String[] args) throws IOException {
//        getSendData();
//        get();
        post();
        return;
    }

    private static byte[] getSendData() throws IOException {
        String json = "{" +
                "\"ownerId\": \"wxid_0vtf86zimd522\"," +
                "\"syncSeq\": \"1609509362325\"," +
                "\"serverTime\": 1609509366268," +
                "\"subgroupId\": \"1078740\"," +
                "\"status\": 3," +
                "\"localId\": 454," +
//                "\"_id\": \"wxid_0vtf86zimd522-1592883400247-454\"," +
                "\"serverId\": \"5020795556531757393\"," +
                "\"type\": 285212721," +
                "\"createTime\": 1609495467000," +
                "\"talker\": \"gh_09e5395b86db\"," +
                "\"talkerType\": 0," +
                "\"content\": \"\"," +
                "\"receive\": true," +
                "\"bizChatId\": -1" +
                "}";
//        String json= "{\"key2\": \"value2\"}";

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
        String url = "http://ap-shanghai.cls.tencentcs.com/logset?logset_id=c6d2f9d4-b15b-4f05-ae28-516eae4bdd22";
        String uri = "/logset";
        OkHttpClient okHttpClient = new OkHttpClient();

        Map<String, String> paramMap = new HashMap();
//        paramMap.put("logset_id", "c6d2f9d4-b15b-4f05-ae28-516eae4bdd22");

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

    private static void post() throws IOException {
        LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

        // 创建 HttpClient 客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String url = "http://ap-shanghai.cls.tencentcs.com/structuredlog?topic_id=";
        HttpPost httpPost = new HttpPost(url);
        String host="ap-shanghai.cls.tencentcs.com";
        httpPost.setHeader("HOST", host);
        httpPost.setHeader("x-cls-compress-type", "lz4");
        httpPost.setHeader("Content-Type", "application/x-protobuf");

        Map<String, String> paramMap = new HashMap();
//        paramMap.put("topic_id", "c6d2f9d4-b15b-4f05-ae28-516eae4bdd22");

        Map<String, String> headerMap = new HashMap();
        headerMap.put("Host", "ap-shanghai.cls.tencentcs.com");
        headerMap.put("User-Agent", "AuthSDK");

        String uri="/structuredlog";
        String authorization =getAuthorization("post",uri,paramMap,headerMap);
        httpPost.setHeader("Authorization", authorization);

        // 创建 HttpPost 参数
        CloseableHttpResponse httpResponse = null;
        try {
            httpPost.setEntity(new ByteArrayEntity(compressor.compress(getSendData())));
            // 请求并获得响应结果
            httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            System.out.println("响应码：" + statusLine.getStatusCode());
            System.out.println("响应信息：" + statusLine.getReasonPhrase());
//            HttpEntity httpEntity = httpResponse.getEntity();
//            // 输出请求结果
//            String response = EntityUtils.toString(httpEntity);
//            System.out.println(response.length() == 0 ? "上传成功" : response);
        } catch (Exception e){
            e.printStackTrace();
        }  finally {
            // 无论如何必须关闭连接
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试
     *
     * @return {@link byte[]}
     **/
    private static byte[] buildSendData(){
        TencentClsData.LogGroup.Builder logGroupBuild = TencentClsData.LogGroup.newBuilder();
        for (int i = 20; i < 25; i++) {
            TencentClsData.Log log = TencentClsData.Log.newBuilder()
                    .addContents(TencentClsData.Log.Content.newBuilder().setKey("key" + i).setValue("value" + i).build())
                    .setTime(System.currentTimeMillis())
                    .build();
            logGroupBuild.addLogs(log);
        }
        TencentClsData.LogGroup logGroup = logGroupBuild.build();
        TencentClsData.LogGroupList logGroupList = TencentClsData.LogGroupList.newBuilder().addLogGroupList(logGroup).build();
        return logGroupList.toByteArray();
    }
}
