package com.jay.weparser;

import java.util.Map;

public class WEParser {



//    public static void main(String[] args) throws Exception {
//        String PATH="src/main/resources/RealTest.docx";
//        AnalyzeData analyzeData = new AnalyzeData();
//        Map<String,Double> map = analyzeData.getKeyValueMap(PATH);
//        String deviceName = analyzeData.getDeviceName(PATH);
//        System.out.println("设备名称："+deviceName);
//        System.out.println("数据总数："+map.size());
//        for (Map.Entry<String, Double> entry : map.entrySet()) {
//            System.out.println("key：" + entry.getKey() + ", value：" + entry.getValue());
//        }
//    }

    private static AnalyzeData analyzeData = new AnalyzeData();
    private static String deviceName;
    private static Map<String,Double> map;
    private static String type;
    private static String number;

    public static String getType() {
        return type;
    }

    public static String getNumber() {
        return number;
    }

    public static String getDeviceName(){
        return deviceName;
    }

    public static Map<String,Double> getKeyValueMap(){
        return map;
    }

    public static void parse(String filePath){
        if(filePath == null) filePath = "src/main/resources/RealTest.docx";;
        deviceName = AnalyzeData.getDeviceName(filePath);
        map = AnalyzeData.getKeyValueMap(filePath);
        type = AnalyzeData.getDeviceType(filePath);
//        System.out.println(type);
//        System.out.println(filePath);
        number = AnalyzeData.getDeviceNumber(filePath);


    }

}
