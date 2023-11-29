package com.jay.dataparser;
import java.util.ArrayList;
import java.util.List;

public class TableParser {
    private static int verticalBarNum;
    private static List<Integer> verticalBarPos = new ArrayList<>();
    public static void main(String[] args) {
//        String input = "序号 描述 ： 实际值 单位\n" +
//                "01 电变化量启动流 ： 0.08 A\n" +
//                "02 零序启动电流定值 ： 0.08 A\n" +
//                "03 差动动作电流定值 ： 0.10 A \n" +
//                "43 零序电压定值 ： 6.00 V\n" +
//                "44  负序电压定值 ： 6.00 V\n" +
//                "45 低电流定值 ： 0.06 A";
        String input = "序号 报警时间 报警元件 实际值 序号\n" +
                "1493 2022-06-17-10：46:11:556 对时异常 1->0\n" +
                "1494 2022-06-17-10:46:12:439 保护PT断线 0->1\n" +
                "1495 2022-06-17-10:46:12:440 装置报警 0->1";
        String[] lines = input.split("\n"); // 分割成一行一行

        int count = 0;
        int index = 0;
        String currentLine = lines[0];

        // 使用indexOf循环查找当前行中含有几个“序号”
        while ((index = currentLine.indexOf("序号", index)) != -1) {
            // 找到一个匹配，增加计数器，并将搜索的起始位置更新为上一次匹配的结束位置的下一个位置
            count++;
            index = index + 2;
        }
        System.out.println("当前行有"+count+"个序号");
//        input = StringFilter.filterString(input);
//
//        List<TableRow> tableRows = parseTable(input);
//
//        for (TableRow row : tableRows) {
//            System.out.println(row);
//        }
    }

    public static Table parseTable(String tableTitle, String[] lines) { // 进入到这一步说明当前行含有“序号”两个字
        int count = 0;
        String currentLine = lines[FileParser.currentLineNum];
        verticalBarNum = countVerticalBarNum(currentLine); // 含有序号的这一行作为“有几个竖线”的标准
        String[] items = currentLine.split("│");  // 按竖线分割，看有几个序号
        for (int i = 0; i < items.length; i++) {
            items[i] = items[i].replace(" ", ""); // 把空格去掉，防止序号两个字中间很多空格
            if(items[i].contains("序号"))
                count++;
        }
//        // 使用indexOf循环查找当前行中含有几个“序号”
//        while ((index = currentLine.indexOf("序号", index)) != -1) {
//            // 找到一个匹配，增加计数器，并将搜索的起始位置更新为上一次匹配的结束位置的下一个位置
//            count++;
//            index = index + 2;
//        }

        if(count == 1){ //如果这行只有一个"序号"
            return parseTableWithOneXuhao(tableTitle, lines);
        }
        else { //如果这行有两   个"序号"
            return parseTableWithTwoXuhao(tableTitle, lines);
        }
    }

    public static Table parseTableWithOneXuhao(String tableTitle, String[] lines) {
        List<TableRow> tableRows = new ArrayList<>();
        boolean parsingTable = false; //用于标记是否正在解析表格部分，默认为 false。
        String[] headers = null;  //用于存储表头的字符串数组，默认为 null。

        while (FileParser.currentLineNum < FileParser.maxLineNum) { // 行数不到整个文件最后就行，后面判断是否还在表格内
            String line = lines[FileParser.currentLineNum]; // 逐行分析
            line = removeFirstAndLastPipe(line);
//            String[] items = line.split("\\s+");  //每行文本按空格分割
            String[] items = line.split("│");  //每行文本按|分割
            items = clearSpace(items);
            if (!parsingTable) { //  不在parsetable，即尚未开始解析表格
                // 检查这一行是否含有“序号”
                for (String item : items) {
                    if (item.contains("序号")) {
                        parsingTable = true;
                        headers = items;
                        FileParser.currentLineNum++;
                        break;
                    }
                }
            } else {
                // 有“序号”的行是标题
                if (headers == null) {//headers == null，说明当前行是表格的第一行，将当前行的 items 赋给 headers。
                    headers = items;
                    FileParser.currentLineNum++;
                } else{
                    boolean isTableContent = false; // 标记是否是表格的一行，即含有"01 02"这样的
                    for(String item : items){
                        if (isNumeric(item) && isInRange(item, 1, 9999)){
                            isTableContent = true;
                            break;
                        }
                    }
                    if(isTableContent) {  // 如果含有一个序号，说明是表格内容，就处理这一行
                        line = lines[FileParser.currentLineNum]; // 需要获取原始数据，因为前面把开头结尾的竖线去掉了
                        int currentVerBarNum = countVerticalBarNum(line); //看看这行有几个竖线
                        if(currentVerBarNum == verticalBarNum)
                            recordVerticalBarPos(line);  // 如果竖线个数没错的话，就记录这一行的竖线位置
                        else{ // 如果错了，就把这一行的竖线全部去除，然后用上一行竖线的位置来分割
                            line = line.replace("│"," ");
                            for(int i=0; i<verticalBarNum; i++){
                                line = insertCharAtPos(line,'│',verticalBarPos.get(i));
                            }
//                            System.out.println("缺少竖线的行：");
//                            System.out.println(line);
//                            System.out.println("上一行的竖线位置：");
//                            System.out.println(verticalBarPos);
                        }
                        // 到这里把缺少的竖线加上了
                        line = removeFirstAndLastPipe(line);  // 然后重新去掉一次开头结尾的竖线
                        items = line.split("│");  //每行文本按|分割
                        items = clearSpace(items);

                        TableRow row = new TableRow(headers, items);
                        tableRows.add(row);
                        FileParser.currentLineNum++;
                    }
                    if(!isTableContent) { // 如果这行不是表格内容，看看下一行是不是，如果两行都不是，认为表格结束
                        FileParser.currentLineNum++;
                        line = removeFirstAndLastPipe(lines[FileParser.currentLineNum]);  // 获取下一行内容
                        items = clearSpace(line.split("\\s+"));  // 用竖线分割并除去空格
                        for(String item : items){
                            if(isNumeric(item) && isInRange(item,1,9999))
                                isTableContent = true;
                        }
                        if(!isTableContent){
                            // 如果下一行是表格内容，那么继续解析表格，这一行作废,currentlinenum不用-1
                            // 如果下一行还不是表格内容，那说明这一整个表格都结束了，退出大循环
                            FileParser.currentLineNum-=2;

                            return new Table(tableTitle, tableRows);
                        }
                    }
                }
            }
        }
        return new Table(tableTitle, tableRows);
    }

    public static Table parseTableWithTwoXuhao(String tableTitle, String[] lines){
        List<TableRow> tableRows = new ArrayList<>();
        Table table = new Table(tableTitle, tableRows);
        return table;
    }

    static class TableRow { // 内部类 TableRow 用于表示表格的一行，包含一个 headers 数组和一个 items 数组。
        String[] headers;
        String[] items;

        public TableRow(String[] headers, String[] items) {
            this.headers = headers;
            this.items = items;
        }

        @Override
//        public String toString() {
//            StringBuilder result = new StringBuilder();
//            for (int i = 0; i < headers.length; i++) {
//                result.append(headers[i]).append(": ").append(items[i]).append(" ");
//            }
//            return result.toString();
//        }
        public String toString() {
            StringBuilder result = new StringBuilder();
            int minLength = Math.min(headers.length, items.length);
            for (int i = 0; i < headers.length; i++) {
                result.append(headers[i]).append(": ");
                // Check if the current index is within the bounds of the items array
                if (i < items.length) {
                    result.append(items[i] != null ? items[i] : "");
                }
                result.append(" ");
            }
            return result.toString();
        }
    }

    public static class Table{
        String tableTitle;
        List<TableRow> tableContent;

        public Table(String tableTitle, List<TableRow> tableContent) {
            this.tableTitle = tableTitle;
            this.tableContent = tableContent;
        }
    }

    // 判断字符串是否是数字形式
    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    // 判断字符串是否在指定范围内
    private static boolean isInRange(String str, int min, int max) {
        int num = Integer.parseInt(str);
        return num >= min && num <= max;
    }

    // 把每一项中的空格都去掉
    public static String[] clearSpace(String[] items){
        for (int i = 0; i < items.length; i++) {
            items[i] = items[i].replace(" ", ""); // 把空格去掉，防止序号两个字中间很多空格
        }
        return items;
    }

    // 把一行字符串的第一个|及其左侧删掉，最后一个|删掉
    public static String removeFirstAndLastPipe(String input) {
        int n = input.length();
        int count = 0;
        for(int i=0;i<n;i++){
            if(input.charAt(i) == '│')
                count++;
        }
        if(count == 1)
            return input;

        int firstPipeIndex = input.indexOf("│");
        int lastPipeIndex = input.lastIndexOf("│");

        if (firstPipeIndex != -1 && lastPipeIndex != -1) {
            return input.substring(firstPipeIndex + 1, lastPipeIndex);
        } else {
            // 如果没有找到 |，则返回原始字符串
            return input;
        }
    }

    public static int countVerticalBarNum(String input){
        int length = input.length();
        int count = 0;
        for(int i=0; i<length; i++)
            if(input.charAt(i) == '│')
                count++;
        return count;
    }

    public static void recordVerticalBarPos(String input){
        verticalBarPos.clear();  // 先把位置清空
        int length = input.length();
        for(int i=0; i<length; i++)
            if(input.charAt(i) == '│')
                verticalBarPos.add(i);
    }

    public static String insertCharAtPos(String originalString, char charToInsert, int positionToInsert){
        // 将字符串转换为StringBuilder
        StringBuilder stringBuilder = new StringBuilder(originalString);
        while(positionToInsert<originalString.length()-1 && originalString.charAt(positionToInsert)!=' ')
            positionToInsert++;

        if (positionToInsert >= 0 && positionToInsert <= originalString.length()){
            // 在指定位置插入字符
            stringBuilder.insert(positionToInsert, charToInsert);
            return stringBuilder.toString();
        }
        else
            return originalString;
    }
}