package com.jay.dataparser;

import com.jay.dataparser.Comparer;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class CompareToWord {
    public static void compareToWord(Comparer.ComparisonResultMap comparisonResultMap, String type, String number, String outputPath){
        List<Comparer.ComparisonResult> comparisonResults = comparisonResultMap.getComparisonResults();
        int rows=comparisonResults.size();
        int cols=6; //comparisonResult的属性值
//        for(Comparer.comparisonResult comparisonResult : comparisonResults){
//            System.out.println(comparisonResult);
//        }
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建一个空白文档

            //创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("继电保护定值核对结果报告");
            titleRun.setBold(true);
            titleRun.setFontSize(16); // 设置字体大小
            // 根据 headingLevel 设置标题级别
            titleParagraph.setStyle("Heading" + 1);

            // 创建表格1
            XWPFTable table1 = document.createTable(3, 2);
            table1.getRow(0).getCell(0).setText("保护类型：");
            table1.getRow(0).getCell(1).setText(type);
            table1.getRow(1).getCell(0).setText("定值单编号：");
            table1.getRow(1).getCell(1).setText(number);
            table1.getRow(2).getCell(0).setText("核对时间：");
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            table1.getRow(2).getCell(1).setText(formattedTime);

            // 添加文字
            XWPFParagraph paragraph1 = document.createParagraph();
            XWPFRun run1 = paragraph1.createRun();
            run1.setText("一、定值核对结果统计：");

            // 创建表格2
            XWPFTable table2 = document.createTable(3, 2);
            table2.getRow(0).getCell(0).setText("定值总数：");
            table2.getRow(0).getCell(1).setText(String.valueOf(comparisonResultMap.getComparisonResultNum()));
            table2.getRow(1).getCell(0).setText("不一致定值数：");
            table2.getRow(1).getCell(1).setText(String.valueOf(comparisonResultMap.getMisMatchedNum()));
            table2.getRow(2).getCell(0).setText("正确率：");
            table2.getRow(2).getCell(1).setText(String.valueOf(comparisonResultMap.getAccuracy()));

            // 添加文字
            XWPFParagraph paragraph2 = document.createParagraph();
            XWPFRun run2 = paragraph2.createRun();
            run2.setText("二、定值核对结果：");
            // 创建表格3
            XWPFTable table3 = document.createTable(rows+1, cols);
            //填写表格标题
            XWPFTableRow tableRow = table3.getRow(0);
            tableRow.getCell(0).setText("下发定值名称");
            tableRow.getCell(1).setText("下发定值");
            tableRow.getCell(2).setText("保护装置定值名称");
            tableRow.getCell(3).setText("保护装置定值");
            tableRow.getCell(4).setText("保护装置定值项是否存在");
            tableRow.getCell(5).setText("是否一致");

            // 填充表格内容
            for (int row = 0; row < rows ;row++) {
                tableRow = table3.getRow(row+1);
                Comparer.ComparisonResult comparisonResult= comparisonResults.get(row);
                tableRow.getCell(0).setText(comparisonResult.standardKey);
                tableRow.getCell(1).setText(String.valueOf(comparisonResult.standardValue));
                tableRow.getCell(2).setText(comparisonResult.inputKey);
                tableRow.getCell(3).setText(String.valueOf(comparisonResult.inputValue));
                if(comparisonResult.isExist)
                    tableRow.getCell(4).setText("是");
                else
                    tableRow.getCell(4).setText("否");
                if(comparisonResult.isEqual)
                    tableRow.getCell(5).setText("是");
                else
                    tableRow.getCell(5).setText("否");
            }

            // 保存文档
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
                System.out.println("Table created successfully.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
