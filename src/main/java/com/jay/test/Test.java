package com.jay.test;

import com.jay.weparser.AnalyzeData;

import java.util.Map;

public class Test {
    public static void main(String[] args) {

        String PATH="/home/jay/IdeaProjects/PrinterV2/src/main/resources/RealTest.doc";
        Map<String,Double> map = AnalyzeData.getKeyValueMap(PATH);
        String deviceName = AnalyzeData.getDeviceName(PATH);
        String deviceType = AnalyzeData.getDeviceType(PATH);
        String deviceNumber = AnalyzeData.getDeviceNumber(PATH);
        System.out.println("设备名称："+deviceName);
        System.out.println("保护型号："+deviceType);
        System.out.println("定值单编号："+deviceNumber);
        System.out.println("数据总数："+map.size());
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            System.out.println("key：" + entry.getKey() + ", value：" + entry.getValue());
        }
    }

}
