package com.jay.pdfparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TextParse {
    public static Map<String, Double> keyValueMap = new HashMap<>();
    public static String device_name,device_type,device_number;

    public static void main(String[] args){
        //test
//        System.out.println(isNumber("11.0"));
//        System.out.println(isSerialNumber("12.31"));
//        System.out.println(isNextSerialNumber("1.8","1.9"));
//        System.out.println(isNextSerialNumber("1.8","1.11"));
//        System.out.println(isNextSerialNumber("1.9","2.1"));
//        System.out.println(isNextSerialNumber("1.13","1.14"));
    }
    public static void parse(ArrayList< ArrayList<String> > textsString,int numIndex, int keyIndex, int valueIndex) {
        getMap(textsString, numIndex, keyIndex, valueIndex);
        getDeviceInfo(textsString);
    }

    private static void getMap(ArrayList< ArrayList<String> > textsString,int numIndex, int keyIndex, int valueIndex) {
//        System.out.println(numIndex+" "+keyIndex+" "+valueIndex);
        int flag;
        if(numIndex == -1 || keyIndex == -1 || valueIndex == -1){
            flag = 0;
        }else{
            flag = 1;
        }
//        flag = 0;

        String currentSerialNumber = null;

        for (ArrayList<String> texts : textsString) {
//            System.out.println("textPasrse.java:41:  "+texts);
//            System.out.println(texts);

            String serialNumber = null;
            String extractedStringKey = null;
            String extractedStringValue = null;

            switch (flag) {
                case 0:
                    boolean foundNumber = false;
                    int count = 1;

                    for (String item : texts) {
                        if (!foundNumber) {
                            // 寻找第一个包含数字的元素
                            if (item != null && !item.isEmpty() && isSerialNumber(item)) {
                                foundNumber = true;
                                serialNumber = item;
                            }
                        } else {
                            // 将之后的非空元素存储起来
                            if (item != null && !item.isEmpty()) {
                                if (count == 1) {
                                    extractedStringKey = item;
                                    count++;
                                } else if (count == 2 && isNumber(item)) {
                                    extractedStringValue = item;
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case 1:
                    serialNumber = texts.get(numIndex);
                    extractedStringKey = texts.get(keyIndex);
                    extractedStringValue = texts.get(valueIndex);
                    break;
            }

            if (serialNumber == null || extractedStringKey == null || extractedStringValue == null) continue;

//            System.out.println("key " + extractedStringKey + "value: " + extractedStringValue);
            if(isSerialNumber(serialNumber)) {
                if (currentSerialNumber != null) {
                    if (isNextSerialNumber(currentSerialNumber, serialNumber)) {
                        if(isNumber(extractedStringValue)) {
                            keyValueMap.put(extractedStringKey, Double.parseDouble(extractedStringValue));
                        }
                        currentSerialNumber = serialNumber;
                    }
                } else {
                    if(isNumber(extractedStringValue)) {
                        keyValueMap.put(extractedStringKey, Double.parseDouble(extractedStringValue));
                    }
                    currentSerialNumber = serialNumber;
                }
            }
        }
    }

    //判断字符串是否为数字
    private static boolean isNumber(String str){
        if (str.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        } else {
            return false;
        }
    }

    //判断字符串是否为序号
    private static boolean isSerialNumber(String str){
        if (str.matches("\\d+\\.\\d+")) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isNextSerialNumber(String currentSerialNumber, String nextSerialNumber) {
        String[] currentParts = currentSerialNumber.split("\\.");
        String[] nextParts = nextSerialNumber.split("\\.");

        int currentMajor = Integer.parseInt(currentParts[0]);
        int currentMinor = Integer.parseInt(currentParts[1]);
        int nextMajor = Integer.parseInt(nextParts[0]);
        int nextMinor = Integer.parseInt(nextParts[1]);

        if (nextMajor - currentMajor == 1 && nextMinor == 1) {
            return true;
        } else if (nextMajor == currentMajor && nextMinor - currentMinor == 1) {
            return true;
        }

        return false;
    }

    private static void getDeviceInfo(ArrayList< ArrayList<String> > textsString) {
        // 查找 "设备名称" 的位置
        for (ArrayList<String> texts : textsString) {
//            System.out.println("size:"+texts.size());
            for (int i = 1; i < texts.size(); i++) {
                if (Objects.equals(texts.get(i - 1), "设备名称")||Objects.equals(texts.get(i - 1), "保护型号")||Objects.equals(texts.get(i - 1), "定值单编号")) {
                    if(Objects.equals(texts.get(i - 1), "设备名称")){
                        device_name = texts.get(i);
                    }
                    if(Objects.equals(texts.get(i - 1), "保护型号")){
                        device_type = texts.get(i);
                    }
                    if(Objects.equals(texts.get(i - 1), "定制单编号")){
                        device_number = texts.get(i);
                        System.out.println("the device number is " + device_number);
                    }
                    return;
                }
            }
        }
        System.out.println("未找到设备名称。");
    }

    public static Map<String, Double> getKeyValueMap(){
        return keyValueMap;
    }

    public static String getDeviceName(){
        return device_name;
    }
    public static String getDeviceType(){
        return device_type;
    }
    public static String getDeviceNumber(){
        return device_number;
    }
}
