package com.jay.dataparser;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

public class CreateTableUtils {
    /**
     * 合并指定行的多个列
     *
     * @param table  表格对象
     * @param row    行索引
     * @param fromCol 起始列索引
     * @param toCol  终止列索引
     */
    public static void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
        for (int colIndex = fromCol; colIndex <= toCol; colIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(colIndex);
            if (colIndex == fromCol) {
                // The first cell in the merged set retains the text
                cell.setText(cell.getText());
            } else {
                // Cells in other columns are made empty
                cell.setText("");
            }
            // Merging cells
            if (colIndex > fromCol) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            }
        }
    }
    /**
     * 合并指定列的多个行
     *
     * @param table  表格对象
     * @param col    列索引
     * @param fromRow 起始行索引
     * @param toRow  终止行索引
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                // The first cell in the merged set retains the text
                cell.setText(cell.getText());
            } else {
                // Cells in other rows are made empty
                cell.setText("");
            }
            // Merging cells
            if (rowIndex > fromRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            }
        }
    }

    // 设置单元格文本
    public static void setCellText(XWPFTable table, int row, int col, String text) {
        table.getRow(row).getCell(col).setText(text);
    }
}
