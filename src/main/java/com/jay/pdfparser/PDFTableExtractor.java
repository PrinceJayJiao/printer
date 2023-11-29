package com.jay.pdfparser;

import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.ExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class PDFTableExtractor{
    public static ArrayList< ArrayList<String> > textsString = new ArrayList<>();
    public static int numIndex = -1, keyIndex = -1, valueIndex = -1;
    static String numTitle = "序号", keyTitle = "名称", valueTitle = "新定值";

    public static void main(String[] args) throws CustomLogicException {
        if (args.length == 0) {
            throw new CustomLogicException("没有给出pdf文件路径");
//            System.out.println("Please provide PdfFilePath");
        }

        //args[0]:要解析文件的路径
        String FILENAME = args[0];
        if(!FILENAME.toLowerCase().endsWith(".pdf")){
            throw new CustomLogicException(FILENAME+" 该文件不是pdf文件");
//            System.out.println("The file is not a PDF file");
        }

        try{
            //解析提取后的文本，以map形式存储
            pdfTableExtract(FILENAME);
            if(!textsString.isEmpty()) {
                TextParse.parse(textsString, numIndex, keyIndex, valueIndex);
            }else{
                throw new CustomLogicException("对pdf文件提取的文本为空，无法生成map");
            }
        } catch (IOException e) {
            System.err.println("捕获到异常：" + e.getMessage());
            throw new CustomLogicException("pdf文件解析失败:" + e.getMessage());
        }
    }

    public static void pdfTableExtract(String FILENAME) throws IOException {
        PDDocument pd = PDDocument.load(new File(FILENAME));

        int totalPages = pd.getNumberOfPages();
        System.out.println("Total Pages in Document: "+totalPages);

        ObjectExtractor oe = new ObjectExtractor(pd);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

        for(int pageNum = 1; pageNum <= totalPages; pageNum++) {
            Page page = oe.extract(pageNum);
//            System.out.println("Page: " + pageNum);

            // extract text from the table after detecting
            List<Table> table = sea.extract(page);
            for (Table tables : table) {
                List<List<RectangularTextContainer>> rows = tables.getRows();
//                System.out.println("rows.size: " + rows.size());

                for (List<RectangularTextContainer> cells : rows) {

                    ArrayList<String> texts = new ArrayList<>();
                    for (int i = 0; i < cells.size(); i++) {
                        String text = cells.get(i).getText();
                        texts.add(text);

                        if (text.equals(numTitle)) {
                            numIndex = i;
                        }
                        if (text.equals(keyTitle)) {
                            keyIndex = i;
                        }
                        if (text.equals(valueTitle)) {
                            valueIndex = i;
                        }

                    }
                    textsString.add(texts);
                }

                if (numIndex == -1 || keyIndex == -1 || valueIndex == -1) {
                    numIndex = -1;
                    keyIndex = -1;
                    valueIndex = -1;
                }
            }
        }
    }
}