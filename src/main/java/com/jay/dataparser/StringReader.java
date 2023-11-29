package com.jay.dataparser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;

public class StringReader {
    public static String readlines() {  // 从控制台输入多行数据，用exit作为结束
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入多行数据 (输入 'exit' 作为结束):");
        StringBuilder inputLines = new StringBuilder();
        String line;

        // 读取多行输入，直到用户输入 "exit"
        while (!(line = scanner.nextLine()).equals("exit")) {
            inputLines.append(line).append("\n");
        }
        scanner.close();
        return inputLines.toString();
    }

    public static String readFileToString(String filePath) { // 从文件读入数据，存在一个string里返回
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), Charset.forName("GB2312")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}
