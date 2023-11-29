package com.jay.dataparser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PreprocessDataLog {
    //预处理文件，删除控制字符
    public void processLogFile(String inputFilePath, String outputFilePath) throws IOException {

        Map<String, String> map = new HashMap<>();
        try(// 创建 FileReader 对象，用于按 GB2312 编码格式读取文件
            FileReader fileReader = new FileReader(inputFilePath, Charset.forName("GB2312"));
            // 创建 FileWriter 对象，用于按 GB2312 编码格式写入文件
            FileWriter fileWriter = new FileWriter(outputFilePath, Charset.forName("GB2312"));
            // 创建 BufferedReader 对象，用于逐行读取文件内容
            BufferedReader reader = new BufferedReader(fileReader);
            //先进行预处理删除控制字符
            BufferedWriter writer = new BufferedWriter(fileWriter);) {

            //第一行的控制字符以ack结尾，其他行的控制字符都以soh结尾
            String line;
            while ((line = reader.readLine()) != null) {
                // Find the last occurrence of SOH character
                line=line.replace('ぉ', '─');  //乱码的问题先替换
                line=line.replace('喋','─');
                line=line.replace('扩','│');
                line=line.replace('丞','─');

                int lastAckIndex = line.lastIndexOf('\u0006');
                if (lastAckIndex >= 0) {
                    // Keep only the content after the last SOH
                    line = line.substring(lastAckIndex + 1);
                }

                int lastSohIndex = line.lastIndexOf('\u0001');
                if (lastSohIndex >= 0) {
                    // Keep only the content after the last SOH
                    line = line.substring(lastSohIndex + 1);
                }

                    writer.write(line);
                    writer.newLine(); // Add newline for each line
            }
        }
        catch (Exception e) {
            // Handle any exceptions that may occur during line processing
            e.printStackTrace();
        }
    }
}
