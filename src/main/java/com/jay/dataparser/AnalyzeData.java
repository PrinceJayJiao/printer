package com.jay.dataparser;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import org.apache.poi.hwpf.HWPFDocument;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeData {

    public  Map<String,Double> getKeyValueMap(String filePath){
        Map<String,Double> map=new HashMap<>();    //读取表格内容放在map上
        String fileExtension=filePath.substring(filePath.lastIndexOf(".") + 1);
        if (fileExtension.equalsIgnoreCase("docx")) {
            map= analyzeDocx(filePath);
        } else if (fileExtension.equalsIgnoreCase("doc")) {
            map=analyzeDoc(filePath);
        } else {
            //System.out.println("不支持的文件类型: " + fileExtension);
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
            //else抛出异常后不会执行后面的return，除非return 放在finally里
        }
        return map;
    }
    public  String getDeviceName(String filePath){
        String deviceName="Not Found Device Name ! ";
        String fileExtension=filePath.substring(filePath.lastIndexOf(".") + 1);
        if (fileExtension.equalsIgnoreCase("docx")) {
            deviceName= getDeviceNameDocx(filePath);
        } else if (fileExtension.equalsIgnoreCase("doc")) {
            deviceName=getDeviceNameDoc(filePath);
        } else {
            //System.out.println("不支持的文件类型: " + fileExtension);
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
            //else抛出异常后不会执行后面的return，除非return 放在finally里
        }
        return deviceName;
    }
    private  String getDeviceNameDocx(String filePath){
        String deviceName="Not Found Device Name ! ";
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XWPFDocument document = new XWPFDocument(fis);

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for(XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText.trim().equals("设备名称")) {
                            // 找到包含"设备名称"的单元格后，读取其右边一个单元格的内容
                            int cellIndex = row.getTableCells().indexOf(cell);
                            if (cellIndex < row.getTableCells().size() - 1) {
                                XWPFTableCell rightCell = row.getTableCells().get(cellIndex + 1);
                                deviceName = rightCell.getText();
                                System.out.println("设备名称: " + deviceName);
                                return deviceName;
                            }
                        }
                    }
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deviceName;
    }
    private String getDeviceNameDoc(String filePath) {
        //必须保证xwpf和hwpf在maven导入包的版本号相同
        String deviceName="Not Found Device Name ! ";
        try {
            FileInputStream fis = new FileInputStream(filePath);
            HWPFDocument document = new HWPFDocument(fis);
            //获取文件内容对象
            Range r = document.getRange();
            //获取文件中所有的表格
            TableIterator tableIterator = new TableIterator(r);

            while (tableIterator.hasNext()) {   //遍历每个表格
                Table table = tableIterator.next();
                for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
                    TableRow row = table.getRow(rowIndex);
                        for (int colIndex = 0; colIndex < row.numCells()-1; colIndex++) {
                            String cellText = row.getCell(colIndex).text();
                            if (cellText.trim().equals("设备名称")) {
                                // 找到包含"设备名称"的单元格后，读取其右边一个单元格的内容
                                deviceName = row.getCell(colIndex+1).text();
                                deviceName = deviceName.substring(0,deviceName.length()-1);//多一个特殊字符要去掉
                                System.out.println("设备名称: " + deviceName);
                                    return deviceName;
                            }
                        }
                }
            }
        fis.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return deviceName;
}
    private Map<String,Double> analyzeDocx(String filePath){

        Map<String,Double> map=new HashMap<>();    //读取表格内容放在map上
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            XWPFDocument document = new XWPFDocument(fileInputStream);

            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {            //遍历每个表格
                int order=0,keyCol=0,valueCol=0,rowData=0;      //记录序号、键与值的列 和 从第几行开始读数据
                int dataStart=3;                        //开始设为3，只有序号、名称和新定值在同一行才会变成0,0代表开始读取
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : rows) {         //遍历每一行
                    List<XWPFTableCell> cells = row.getTableCells();
                    if(dataStart!=0) {              //寻找读取数据的起始行和列
                        dataStart=3;
                        for (XWPFTableCell cell : cells) {  //遍历每一列
                            String cellText = cell.getText();
                            if (cellText.trim().equals("序号")) {     //获得序号的列
                                order=cells.indexOf(cell);
                                dataStart--;
                            }
                            else if (cellText.trim().equals("名称")) {    //获得键的列
                                keyCol = cells.indexOf(cell);
                                dataStart--;
                            }
                            else if (cellText.trim().equals("新定值")) {   //获得值的列
                                valueCol = cells.indexOf(cell);
                                dataStart--;
                            }
                            if(dataStart==0){                             //序号、名称和新定值在同一行
                                rowData = rows.indexOf(row)+1;            //得到开始读取数据的行索引
                                System.out.println("rowStart：" + rowData);
                                System.out.println("keyCol：" + keyCol);
                                System.out.println("valueCol：" + valueCol);
                                break;
                            }
                        }
                    }
                    else{      //    dataStart==0代表此行要读取;
                        String serial = cells.get(order).getText();
                        try {       //下一行可能出现异常，比如读完数据后下一行为备注，因此进行捕获
                            double serialNumber = Double.parseDouble(serial);   //得到序号值
                            double serialInt = Math.floor(serialNumber);        //得到序号的整数部分
                            if (serialNumber - serialInt != 0) {                //如果序号是1.x，2.x这种说明是要读的数据
                                String key = cells.get(keyCol).getText();
                                String valueStr = cells.get(valueCol).getText();
                                try {
                                    double value = Double.parseDouble(valueStr); // 尝试将值转换为浮点数`6
                                    String serial_key=serial+"_"+key;                //设置key为序号+key的方式
                                    map.put(serial_key, value);
                                } catch (NumberFormatException e) {
                                    // 处理值无法转换为浮点数的情况
                                    System.out.println("无法将值转换为浮点数：" + valueStr);
                                }
                            } else {                            //比如如果是 1 设备参数 这种目录先不做处理
                                //TODO                          //还有一点，1.1 1.2这种值现场整定没有处理
                            }
                        }catch (NumberFormatException e) {
                            //此时已经读完这一段的数据，但可能这一个表格下面还有数据，应该继续遍历
                            dataStart=3;
                            System.out.println("这段数据已经读完，这一行是： " + e.getMessage());
                        }
                    }

                }
                System.out.println("当前页表格内容读取完毕...");
            }
          //  for (Map.Entry<String, Double> entry : map.entrySet()) {
         //       System.out.println("键：" + entry.getKey() + ", 值：" + entry.getValue());
         //   }
            fileInputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
            //不管try有没有发生异常都会返回map，因为catch没抛出异常
        }
        return map;
    }

    private Map<String,Double> analyzeDoc(String filePath) {
        //必须保证xwpf和hwpf在maven导入包的版本号相同
        Map<String, Double> map = new HashMap<>();    //读取表格内容放在map上
        try {
            FileInputStream fis = new FileInputStream(filePath);
            HWPFDocument document = new HWPFDocument(fis);
            //获取文件内容对象
            Range r = document.getRange();
            //获取文件中所有的表格
            TableIterator tableIterator = new TableIterator(r);

            while (tableIterator.hasNext()) {   //遍历每个表格
                Table table = tableIterator.next();
                int order=0,keyCol=0,valueCol=0,rowData=0;      //记录序号、键与值的列 和 从第几行开始读数据
                int dataStart=3;
                for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
                    TableRow row = table.getRow(rowIndex);

                    if(dataStart!=0) {              //寻找读取数据的起始行和列
                        dataStart = 3;
                        for (int colIndex = 0; colIndex < row.numCells(); colIndex++) {
                            String cellText = row.getCell(colIndex).text();
                            if (cellText.trim().equals("序号")) {     //获得序号的列
                                order = colIndex;
                                dataStart--;
                            } else if (cellText.trim().equals("名称")) {    //获得键的列
                                keyCol = colIndex;
                                dataStart--;
                            } else if (cellText.trim().equals("新定值")) {   //获得值的列
                                valueCol = colIndex;
                                dataStart--;
                            }
                            if (dataStart == 0) {                             //序号、名称和新定值在同一行
                                rowData = rowIndex + 1;            //得到开始读取数据的行索引
                                System.out.println("rowStart：" + rowData);
                                System.out.println("keyCol：" + keyCol);
                                System.out.println("valueCol：" + valueCol);
                                break;
                            }
                        }
                    }
                    else{      //    dataStart==0代表此行要读取;
                        String serial = row.getCell(order).text();
                        //这个读取有点问题，会多一个无法识别的字符，好像是Ascii编码7的字符，去除
                        serial=serial.substring(0,(serial.length()-1));
                        try {       //下一行可能出现异常，比如读完数据后下一行为备注，因此进行捕获
                            double serialNumber = Double.parseDouble(serial);   //得到序号值
                            double serialInt = Math.floor(serialNumber);        //得到序号的整数部分
                            if (serialNumber - serialInt != 0) {                //如果序号是1.x，2.x这种说明是要读的数据
                                String key = row.getCell(keyCol).text();
                                key=key.substring(0,(key.length()-1));            //同理多一个无法识别的字符
                                String valueStr = row.getCell(valueCol).text();
                                valueStr=valueStr.substring(0,(valueStr.length()-1)); //同理多一个无法识别的字符
                                try {
                                    double value = Double.parseDouble(valueStr); // 尝试将值转换为整数
                                    String serial_key=serial+"_"+key;                //设置key为序号+key的方式
                                    map.put(serial_key, value);
                                } catch (NumberFormatException e) {
                                    // 处理值无法转换为浮点数的情况
                                    System.out.println("无法将值转换为浮点数：" + valueStr);
                                }
                            } else {                            //比如如果是 1 设备参数 这种目录先不做处理
                                //TODO                          //还有一点，1.1 1.2这种值现场整定没有处理
                            }
                        }catch (NumberFormatException e) {
                            //此时已经读完这一段的数据，但可能这一个表格下面还有数据，应该继续遍历
                            dataStart=3;
                            System.out.println("这段数据已经读完，这一行是： " + e.getMessage());
                        }
                    }
                }
                System.out.println("当前表格内容读取完毕...");
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

}
