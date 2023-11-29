package com.jay.dataparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Comparer {
    public static ComparisonResultMap compareWithStandardValue(Map<String, Double> standardMap, Map<String, Double> inputMap) {
        List<ComparisonResult> comparisonResults = new ArrayList<>();

        for (Map.Entry<String, Double> entry : standardMap.entrySet()) {
            String standardKey = entry.getKey();
            double standardValue = entry.getValue();

            ComparisonResult result = new ComparisonResult();
            result.standardKey = standardKey;
            result.standardValue = standardValue;

            if (inputMap.containsKey(standardKey)) {
                result.inputKey = standardKey;
                result.inputValue = inputMap.get(standardKey);

                result.isExist = true;
                result.isEqual = Double.compare(standardValue, result.inputValue) == 0;
            } else {
                result.inputKey = null;
                result.inputValue = 0.0; // You may adjust this default value

                result.isExist = false;
                result.isEqual = false;
            }

            comparisonResults.add(result);
        }

        return new ComparisonResultMap(comparisonResults);
    }

    public static class ComparisonResult {
        public String standardKey;
        public String inputKey;
        public double standardValue;
        public double inputValue;
        public boolean isEqual;
        public boolean isExist;

        public String getStandardKey() {
            return standardKey;
        }

        public void setStandardKey(String standardKey) {
            this.standardKey = standardKey;
        }

        public String getInputKey() {
            return inputKey;
        }

        public void setInputKey(String inputKey) {
            this.inputKey = inputKey;
        }

        public double getStandardValue() {
            return standardValue;
        }

        public void setStandardValue(double standardValue) {
            this.standardValue = standardValue;
        }

        public double getInputValue() {
            return inputValue;
        }

        public void setInputValue(double inputValue) {
            this.inputValue = inputValue;
        }

        public boolean getIsEqual() {
            return isEqual;
        }

        public void setEqual(boolean equal) {
            isEqual = equal;
        }

        public boolean getIsExist() {
            return isExist;
        }

        public void setExist(boolean exist) {
            isExist = exist;
        }

        @Override
        public String toString() {
            return "ComparisonResult{" +
                    "standardKey='" + standardKey + '\'' +
                    ", inputKey='" + inputKey + '\'' +
                    ", standardValue=" + standardValue +
                    ", inputValue=" + inputValue +
                    ", isEqual=" + isEqual +
                    ", isExist=" + isExist +
                    '}';
        }
    }
    public static class ComparisonResultMap {
        List<ComparisonResult> comparisonResults;
        double comparisonResultNum; // 定值总数
        double accuracy; // 正确率

        public List<ComparisonResult> getComparisonResults() {
            return comparisonResults;
        }

        public void setComparisonResults(List<ComparisonResult> comparisonResults) {
            this.comparisonResults = comparisonResults;
        }

        public double getComparisonResultNum() {
            return comparisonResultNum;
        }

        public void setComparisonResultNum(double comparisonResultNum) {
            this.comparisonResultNum = comparisonResultNum;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        public int getMisMatchedNum() {
            return misMatchedNum;
        }

        public void setMisMatchedNum(int misMatchedNum) {
            this.misMatchedNum = misMatchedNum;
        }

        int misMatchedNum; // 不一致定值数

        public ComparisonResultMap(List<ComparisonResult> comparisonResults){
            this.comparisonResults = comparisonResults;
            comparisonResultNum = comparisonResults.size();
            int accurateNum = 0;
            misMatchedNum = 0;
            for(ComparisonResult comparisonResult : comparisonResults){
                if(comparisonResult.isEqual)
                    accurateNum++;
                else
                    misMatchedNum++;
            }
            accuracy = accurateNum/comparisonResultNum;
        }

    }
}
