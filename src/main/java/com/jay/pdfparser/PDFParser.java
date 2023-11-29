package com.jay.pdfparser;

import java.io.IOException;
import java.util.Map;

public class PDFParser {

    public static String getDeviceName() {
        return deviceName;
    }

    public static Map<String, Double> getKeyValueMap() {
        return keyValueMap;
    }

    static String deviceName;
    static Map<String, Double> keyValueMap;
    private static String type;
    private static String number;

    public static String getType() {
        return type;
    }

    public static String getNumber() {
        return number;
    }

    public static void parse(String pdfFilePath) throws IOException{
        if (pdfFilePath == null){
            pdfFilePath = "src/main/resources/test.pdf";
        }


        // 提取并解析文本
        try {
            PDFTableExtractor.main(new String[]{pdfFilePath});

            // 获取键值对和设备名称
            keyValueMap = TextParse.keyValueMap;
            deviceName = TextParse.device_name;
            type = TextParse.getDeviceType();
            number = TextParse.getDeviceNumber();
            System.out.println("the number is "+ number);
            System.out.println("the type is "+ type);


//            // 输出键值对
//            for (Map.Entry<String, Double> entry : keyValueMap.entrySet()) {
//                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
//            }
//            System.out.println();

            // 输出设备名称
//            System.out.println("device_name: " + device_name);

        } catch (CustomLogicException e) {
            System.err.println("pdf文件解析捕获到异常：" + e.getMessage());
        }
    }
}