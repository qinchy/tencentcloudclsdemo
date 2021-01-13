package com.qinchy.protobufdemo;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * protoc序列化
 *
 * @author Administrator
 * @date 2021/1/12 11:45
 */
public class QcloudClsDataBuilder {

    /**
     * 通过json构造序列化字节数组
     *
     * @param jsonObject json对象
     * @return {@link byte[]}
     **/
    public static byte[] buildData(JSONObject jsonObject) {
        TencentClsData.LogGroupList logGroupList = getLogGroupList(jsonObject);

        byte[] byteArray = logGroupList.toByteArray();
        return byteArray;
    }

    /**
     * 批量添加log
     *
     * @param logGroupBuilder logGroupBuilder
     * @param logs            log
     **/
    private void addLog(TencentClsData.LogGroup.Builder logGroupBuilder, List<TencentClsData.Log> logs) {
        for (TencentClsData.Log log : logs) {
            addLog(logGroupBuilder, log);
        }
    }

    /**
     * 添加Log,对应与腾讯云cls中protobuf结构的Log
     *
     * @param logGroupBuilder logGroupBuilder
     * @param log             log
     **/
    private static void addLog(TencentClsData.LogGroup.Builder logGroupBuilder, TencentClsData.Log log) {
        logGroupBuilder.addLogs(log);
    }

    /**
     * 添加LogTag,对应与腾讯云cls中protobuf结构的LogTag
     *
     * @param logGroupBuilder logGroupBuilder
     * @param logTag          logTag
     **/
    private static void addLogTag(TencentClsData.LogGroup.Builder logGroupBuilder, TencentClsData.LogTag logTag) {
        logGroupBuilder.addLogTags(logTag);
    }

    /**
     * 添加LogTag组,对应与腾讯云cls中protobuf结构的LogTag
     *
     * @param logGroupBuilder logGroupBuilder
     * @param logTags         logTag组
     **/
    private static void addLogTag(TencentClsData.LogGroup.Builder logGroupBuilder,
                                  List<TencentClsData.LogTag> logTags) {
        for (TencentClsData.LogTag logTag : logTags) {
            addLogTag(logGroupBuilder, logTag);
        }
    }

    /**
     * 添加LogGroupList,对应与腾讯云cls中protobuf结构的LogGroupList
     *
     * @param logGroupListBuilder logGroupListBuilder
     * @param logGroup            logGroup
     **/
    private static void addLogGroupList(TencentClsData.LogGroupList.Builder logGroupListBuilder,
                                        TencentClsData.LogGroup logGroup) {
        logGroupListBuilder.addLogGroupList(logGroup);
    }

    /**
     * 添加LogGroupList,对应与腾讯云cls中protobuf结构的LogGroupList
     *
     * @param logGroupListBuilder logGroupListBuilder
     * @param logGroups           logGroup组
     **/
    private static void addLogGroupList(TencentClsData.LogGroupList.Builder logGroupListBuilder,
                                        List<TencentClsData.LogGroup> logGroups) {
        for (TencentClsData.LogGroup logGroup : logGroups) {
            addLogGroupList(logGroupListBuilder, logGroup);
        }
    }

    /**
     * 解析json构造log
     *
     * @param jsonObject json对象
     * @return {@link TencentClsData.Log.Builder}
     **/
    private static TencentClsData.Log getLog(JSONObject jsonObject) {
        TencentClsData.Log.Builder logBuilder = TencentClsData.Log.newBuilder();

        logBuilder.setTime(System.currentTimeMillis());
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            // 将数据遍历构成content
            TencentClsData.Log.Content.Builder contentBuilder = TencentClsData.Log.Content.newBuilder();
            contentBuilder.setKey(entry.getKey());
            contentBuilder.setValue(entry.getValue().toString());

            TencentClsData.Log.Content content = contentBuilder.build();
            logBuilder.addContents(content);
        }

        return logBuilder.build();
    }

    /**
     * 获取LogTag
     *
     * @param jsonObject json对象
     * @return {@link List < TencentClsData.LogTag > }
     **/
    private static List<TencentClsData.LogTag> getLogTag(JSONObject jsonObject) {
        List<TencentClsData.LogTag> logTags = new ArrayList<TencentClsData.LogTag>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            TencentClsData.LogTag.Builder logTagBuilder = TencentClsData.LogTag.newBuilder();
            logTagBuilder.setKey(entry.getKey());
            logTagBuilder.setValue(entry.getValue().toString());
            logTags.add(logTagBuilder.build());
        }
        return logTags;
    }

    /**
     * 获取LogGroup.Builder
     *
     * @param jsonObject json对象
     * @return {@link TencentClsData.LogGroup.Builder}
     **/
    private static TencentClsData.LogGroup getLogGroup(JSONObject jsonObject) {
        TencentClsData.LogGroup.Builder logGroupBuilder = TencentClsData.LogGroup.newBuilder();
//        logGroupBuilder.setContextFlow("test.contextflow");
//        logGroupBuilder.setFilename("test.file");
//        logGroupBuilder.setSource("127.0.0.1");

        TencentClsData.Log log = getLog(jsonObject);
        addLog(logGroupBuilder, log);

//        List<TencentClsData.LogTag> logTags = getLogTag(jsonObject);
//        addLogTag(logGroupBuilder, logTags);

        return logGroupBuilder.build();
    }

    /**
     * 获取LogGroupList
     *
     * @param jsonObject json对象
     * @return {@link TencentClsData.LogGroupList}
     **/
    public static TencentClsData.LogGroupList getLogGroupList(JSONObject jsonObject) {
        TencentClsData.LogGroupList.Builder logGroupListBuilder = TencentClsData.LogGroupList.newBuilder();

        TencentClsData.LogGroup logGroup = getLogGroup(jsonObject);
        addLogGroupList(logGroupListBuilder, logGroup);

        return logGroupListBuilder.build();
    }

}
