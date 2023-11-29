package com.jay.dataparser;

public class Main {
    public static void main(String[] args) throws Exception {
        // 读取docx文件并获取数据的示例
//        String PATH="RealTest.docx";
//        AnalyzeData analyzeData = new AnalyzeData();
//        Map<String,Double> map = analyzeData.getKeyValueMap(PATH);
//        String deviceName = analyzeData.getDeviceName(PATH);
//        System.out.println("设备名称："+deviceName);
//        System.out.println("数据总数："+map.size());
//        for (Map.Entry<String, Double> entry : map.entrySet()) {
//            System.out.println("key：" + entry.getKey() + ", value：" + entry.getValue());
//        }

        String inputfilePath="data1.log";                               //读取的输入文件路径
        String outputfilePath="processdata1.log";                       //输出的预处理文件的路径
        String writeWordPath="output.docx";                             //生成的结果文件的路径
        WriteToWord write = new WriteToWord();
        write.writeToWord(inputfilePath,outputfilePath,writeWordPath);
    }
}