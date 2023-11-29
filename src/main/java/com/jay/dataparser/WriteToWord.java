package com.jay.dataparser;

import org.apache.poi.xwpf.usermodel.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.jay.dataparser.CreateTableUtils.mergeCellsVertically;

public class WriteToWord {
    public void writeToWord(String inputfilePath,String outputfilePath,String writeWordPath){

        //进行预处理
        try {
            PreprocessDataLog preprocessDataLog = new PreprocessDataLog();
            preprocessDataLog.processLogFile(inputfilePath,outputfilePath);
            System.out.println("Processing completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //读数据
        //ReadData readData = new ReadData();
        //List<TableParser.TableRow> dataList= readData.readDatalog(outputfilePath);
        List<TableParser.Table> dataTables = FileParser.getTables();//之前模块的所有表格数据
        TableParser.Table dataTable;
        List<TableParser.TableRow> tableContent=null;    //代表某一个表格的数据
        try {
            // 创建 FileReader 对象，用于按 GB2312 编码格式读取文件
            FileReader fileReader = new FileReader(outputfilePath, Charset.forName("GB2312"));
            // 创建 BufferedReader 对象，用于逐行读取文件内容
            BufferedReader reader = new BufferedReader(fileReader);


            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();

            String line;
            // 去除当前行中的所有空格
            //    String lineWithoutSpaces = line.replaceAll("\\s", ""); // \s是匹配空白字符,\\s是先转义\
            //    String[] parts = lineWithoutSpaces.split("\\│");

            XWPFTable table = null;
            boolean endTable=false; //表示表格的结束
            //int startRow=-1,endRow=-1;//合并某些列
            int count=0;   //代表第几个表格
            // 逐行读取文件内容并输出符合条件的行
            while ((line = reader.readLine()) != null) {

                // 检测表格开始
                if (line.contains("┌")) {
                    if(table==null) {
                        dataTable=dataTables.get(count); //获取之前模块的表格数据
                        tableContent = dataTable.tableContent;

                        TableParser.TableRow rowData = tableContent.get(0);
                        table = document.createTable(); //创建table会创建一个1行1列的表格
                        XWPFTableRow row = table.getRow(0) ;    //第一行
                        for (int j = 0; j < rowData.items.length; j++) {
                            XWPFTableCell cell;
                            if(j==0)                        //第一列直接得到
                                cell = row.getCell(0);
                            else
                                cell=row.createCell();      //不是第一列要创建
                            cell.setText(rowData.headers[j]);
                        }
                    }
                } else if(line.contains("├")){      //检测行中间的线
                    //pass
                }else if (table != null && line.contains("│")) {    //检测表格内容
                    if(endTable==false) { //说明是表格的行，输出表格的内容，但是只输出一次
                        System.out.println("表格的行数: "+tableContent.size());
                        System.out.println("表格的项数: "+tableContent.get(count).items.length);
                        for (int i = 0; i < tableContent.size(); i++) {
                            XWPFTableRow row =  table.createRow();
                            TableParser.TableRow rowData = tableContent.get(i);
                            for (int j = 0; j < rowData.items.length; j++) {
                                XWPFTableCell cell = row.getCell(j);
                                //System.out.println(data);
                                cell.setText(rowData.items[j]);
                            }
                        }
                        endTable=true;
                    }
                } else if (line.contains("└")){ //表格结束
                    count++;
                    table=null;
                    endTable=false;      //可能还有下个表格
                    //startRow=-1;
                    //endRow=-1;
                }else {
                    // 非表格内容作为普通段落处理
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(line);
                }
            }

            // 保存文档到文件
            FileOutputStream out = new FileOutputStream(writeWordPath);
            document.write(out);
            out.close();

            System.out.println("Word document created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
