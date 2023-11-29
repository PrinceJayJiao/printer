package com.jay.dataparser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFilter {
    public static String filterString(String input) {
        // 匹配汉字、英文字母、换行符、空格、数字、
//        String regex = "[\u4e00-\u9fa5a-zA-Z\\n\\s\\d]+";
//        String regex = "[\\u4e00-\\u9fa5a-zA-Z\\n\\s\\d.]+";
//        String regex = "[\\u4e00-\\u9fa5a-zA-Z\\n\\s\\d.│┼┬─]+";  // 包括竖线，十字之类的
        String regex = "[\\u4e00-\\u9fa5a-zA-Z\\n\\s\\d.,，()（）│]+";  // 只有竖线
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(matcher.group());
        }

        return result.toString(); // 把连续多个空格的地方都变成一个空格
    }
}
