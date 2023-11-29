package com.jay.dataparser;
import java.util.ArrayList;
import java.util.List;

public class FileParser {
    public static int currentLineNum;  // 用一个static变量控制当前读到的行，可以在不同的文件中控制这个变量
    public static int maxLineNum; // 用来表示文件总行数

    public static List<TableParser.Table> getTables(){
        List<TableParser.Table> tables = new ArrayList<>(); // 包含所有表格
        String filePath = "data1.log";
        String input = StringReader.readFileToString(filePath); // 从文件读取内容
        input = removeEmptyLines(input);
//        String input = readlines(); // 这是从面板输入
        input = StringFilter.filterString(input); // 除了汉字、英文字母、换行符、空格、数字之外的全删掉
        String[] lines = input.split("\n"); // 分割成一行一行
        maxLineNum = lines.length;
        currentLineNum=0;
        while(currentLineNum < maxLineNum) { // 一行一行分析，直到最后一行
            String currentLine = lines[currentLineNum];
            if(containsKeyword(currentLine)){ // 如果这行含有表格标题，就认为是表格的开始
                String tableTitle = getTableTitle(currentLine); // 含有这些关键字的行就是这个表格的标题，可能有乱码，所以需要提取一下
                currentLineNum++; // 含有这些关键字的这行跳过，从下面有“序号”两个字的行开始
                currentLine = lines[currentLineNum];
                if(!currentLine.contains("│")){  // 因为可能标题行后面还有一行乱码
                    currentLineNum++;
                    currentLine = lines[currentLineNum];
                }
                if(currentLine.contains("序号") || currentLine.contains("序 号") || currentLine.contains("序  号")){
                    System.out.println("当前输出的表格是： "+tableTitle+":");
                    TableParser.Table table = TableParser.parseTable(tableTitle, lines);
                    tables.add(table);
                    for (TableParser.TableRow row : table.tableContent) {
                        System.out.println(row);
                    }
                }
            }
            else currentLineNum++;
        }

        return tables;
    }

    public static boolean containsKeyword(String currentLine){
        if(currentLine.contains("设备参数定值") || currentLine.contains("保护定值") ||
                currentLine.contains("功能软压板") || currentLine.contains("装置参数") ||
                currentLine.contains("通信参数") || currentLine.contains("保护控制字") ||
                currentLine.contains("开入量状态") || currentLine.contains("压板状态") ||
                currentLine.contains("母线保护定值") || currentLine.contains("母线保护控制字"))
            return true;
        return false;
    }

    public static String getTableTitle(String currentLine){
        if(currentLine.contains("设备参数定值"))
            return "设备参数定值";
        else if(currentLine.contains("母线保护定值"))
            return "母线保护定值";
        else if(currentLine.contains("保护定值"))
            return "保护定值";
        else if(currentLine.contains("功能软压板"))
            return "功能软压板";
        else if(currentLine.contains("装置参数"))
            return "装置参数";
        else if(currentLine.contains("通信参数"))
            return "通信参数";
        else if(currentLine.contains("母线保护控制字"))
            return "母线保护控制字";
        else if(currentLine.contains("保护控制字"))
            return "保护控制字";
        else if(currentLine.contains("开入量状态"))
            return "开入量状态";
        else if(currentLine.contains("压板状态"))
            return "压板状态";

//        else if(currentLine.contains(""))
//            return "";
        else
            return "无标题";
    }

    public static String removeEmptyLines(String input) {
        // 使用正则表达式匹配空白行，并替换为空字符串
        return input.replaceAll("(?m)^\\s*\\n", "");
    }

    //    public static List<TableParser.TableRow> getTableByOne(){
//        List<TableParser.TableRow> tableRows = new ArrayList<>();
//        String filePath = "data1.log";
//        String input = StringReader.readFileToString(filePath); // 从文件读取内容
//        input = Main.removeEmptyLines(input);
////        String input = readlines(); // 这是从面板输入
//        input = StringFilter.filterString(input); // 除了汉字、英文字母、换行符、空格、数字之外的全删掉
//        String[] lines = input.split("\n"); // 分割成一行一行
//        Main.maxLineNum = lines.length;
//        Main.currentLineNum=0;
//        while(Main.currentLineNum < Main.maxLineNum) { // 一行一行分析，直到最后一行
//            String currentLine = lines[Main.currentLineNum];
//            if(Main.containsKeyword(currentLine)){ // 如果这行含有表格标题，就认为是表格的开始
//                String tableTitle = Main.getTableTitle(currentLine); // 含有这些关键字的行就是这个表格的标题，可能有乱码，所以需要提取一下
//                Main.currentLineNum++; // 含有这些关键字的这行跳过，从下面有“序号”两个字的行开始
//                currentLine = lines[Main.currentLineNum];
//                if(!currentLine.contains("│")){  // 因为可能标题行后面还有一行乱码
//                    Main.currentLineNum++;
//                    currentLine = lines[Main.currentLineNum];
//                }
//                if(currentLine.contains("序号") || currentLine.contains("序 号") || currentLine.contains("序  号")){
//                    System.out.println("当前输出的表格是： "+tableTitle+":");
//                    return(TableParser.parseTable(lines));
//                }
//            }
//            else Main.currentLineNum++;
//        }
//        return tableRows;
//    }
}
