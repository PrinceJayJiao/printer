package com.jay.listener;

import purejavacomm.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialPortListener {
    private static final String PORT_NAME = "/dev/ttyS9";
    private static final long TIMEOUT_MS = 10000; // 超时时间（毫秒）

    public static void main(String[] args) {

    }

    public static void listen() {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORT_NAME);
            SerialPort serialPort = (SerialPort) portIdentifier.open("SerialPortListener", 2000);

            // 设置串口参数
            serialPort.setSerialPortParams(
                    9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );

            // 获取串口输入流
            InputStream inputStream = serialPort.getInputStream();

            // 记录上一次接收数据的时间
            long lastReceiveTime = System.currentTimeMillis();

            // 数据缓冲区
            ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

            // 持续监听串口数据
            while (true) {
                // 读取数据，设置超时时间
                serialPort.enableReceiveTimeout((int) TIMEOUT_MS);
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);

                if (bytesRead > 0) {
                    // 有新数据到达
                    lastReceiveTime = System.currentTimeMillis();
                    // 缓存未解码的数据
                    receivedData.write(buffer, 0, bytesRead);
                } else {
                    // 超时，将数据保存到文件
                    long elapsedTime = System.currentTimeMillis() - lastReceiveTime;
                    if (elapsedTime > TIMEOUT_MS && receivedData.size() > 0) {
                        // 保存原始十六进制形式的数据
                        String fileName = saveDataToFileHex(receivedData.toByteArray());
//                        AnalyzeActionFile analyzer = new AnalyzeActionFile();
                        AnalyzeActionFile.main(new String[]{fileName});
                        boolean isFixedValueFile = AnalyzeActionFile.isFixedValueFile;
                        // 保存 GB2312 解码后的数据
                        if(isFixedValueFile){
                            saveDataToFileGB2312(receivedData.toByteArray());
                        }

                        File output = new File(fileName);
                        if (output.exists()) {
                            if (output.delete()) {
                                System.out.println("文件删除成功");
                            } else {
                                System.out.println("文件删除失败");
                            }
                        } else {
                            System.out.println("文件不存在");
                        }
                        receivedData.reset(); // 重置缓冲区
                        lastReceiveTime = System.currentTimeMillis();
                    }
                }
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String saveDataToFileHex(byte[] data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        String serialPortFile =  "/home/linaro/rcv/serialPort/";
        boolean isDirectoryExist = Files.exists(Paths.get(serialPortFile));
        if (!isDirectoryExist) {
            // 如果目录不存在，则创建目录
            try {
                Files.createDirectories(Paths.get(serialPortFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String fileName = serialPortFile + dateFormat.format( new Date()) + "_hex.log";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // 将字节数组转换为十六进制字符串
            String hexData = bytesToHex(data);
            StringBuilder spacedHexData = new StringBuilder(hexData);
            for (int i = 1; i < spacedHexData.length(); i += 2) {
                spacedHexData.insert(i, ' ');
            }

            writer.write(hexData);
            System.out.println("原始数据已保存到文件: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private static void saveDataToFileGB2312(byte[] data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        String fixedValueFile =  "/home/linaro/rcv/fixedValueFile/";
        boolean isDirectoryExist = Files.exists(Paths.get(fixedValueFile));
        if (!isDirectoryExist) {
            // 如果目录不存在，则创建目录
            try {
                Files.createDirectories(Paths.get(fixedValueFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String fileName =fixedValueFile + dateFormat.format(new Date()) + "_gb2312.log";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // 将字节数组解码为 GB2312 字符串
            String decodedData = new String(data, "GB2312");
            writer.write(decodedData);
            System.out.println("GB2312 解码后的数据已保存到文件: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexStringBuilder.append(String.format("%02X", b));
        }
        return hexStringBuilder.toString();
    }

    private static String insertCharacter(String original, int index, char ch) {
        if (index < 0 || index > original.length()) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        // 使用 StringBuilder 构建可变字符串
        StringBuilder stringBuilder = new StringBuilder(original);

        // 在指定位置插入字符
        stringBuilder.insert(index, ch);

        // 返回修改后的字符串
        return stringBuilder.toString();
    }
}
