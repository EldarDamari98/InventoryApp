package com.example.excelnew;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class QuantityAdder {

    public void addQuantity(String sku, int amountToAdd) {
        try (InputStream inp = new FileInputStream("C:\\Users\\Eldar\\Desktop\\ITON\\EXCEL\\חוברת1.xlsx")) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            Row productRow = null;
            Row firstZeroRow = null;
            int firstZeroRowIndex = -1;
            int productRowIndex = -1;

            // Searching for the product and the first row with quantity 0
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                Cell skuCell = row.getCell(1); // Assuming SKU is in the second column (index 1)
                if (skuCell != null) {
                    String cellValue;

                    if (skuCell.getCellType() == CellType.STRING) {
                        cellValue = skuCell.getStringCellValue();
                    } else if (skuCell.getCellType() == CellType.NUMERIC) {
                        cellValue = String.valueOf((int) skuCell.getNumericCellValue());
                    } else {
                        continue;
                    }

                    Cell availableCell = row.getCell(4); // Assuming Available is in the fifth column (index 4)
                    if (availableCell == null || availableCell.getCellType() != CellType.NUMERIC) {
                        continue; // Skip rows with invalid cells
                    }

                    int available = (int) availableCell.getNumericCellValue();

                    if (cellValue.equals(sku)) {
                        productRow = row;
                        productRowIndex = i;
                        System.out.println("נמצא מוצר עם SKU " + sku + " בשורה: " + (productRowIndex + 1));
                    }

                    if (available == 0 && firstZeroRowIndex == -1) {
                        firstZeroRow = row;
                        firstZeroRowIndex = i;
                        System.out.println("נמצאה שורה ראשונה עם כמות 0 בשורה: " + (firstZeroRowIndex + 1));
                    }
                }
            }

            if (productRow != null) {
                Cell availableCell = productRow.getCell(4);
                int available = (int) availableCell.getNumericCellValue();
                int newAvailable = available + amountToAdd;

                availableCell.setCellValue(newAvailable);
                System.out.println("הוספת כמות למוצר עם SKU " + sku + " לכמות: " + newAvailable);

                if (available == 0 && firstZeroRow != null && firstZeroRowIndex != -1 && productRowIndex != -1) {
                    System.out.println("כמות המוצר עם SKU " + sku + " עלתה מ-0 ולכן יש להחליף שורות.");
                    swapRows(sheet, productRowIndex, firstZeroRowIndex);
                    removeRowColor(firstZeroRow, wb);
                }

                // Saving the file after changes
                try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\Eldar\\Desktop\\ITON\\EXCEL\\חוברת1.xlsx")) {
                    wb.write(fileOut);
                    System.out.println("הקובץ נשמר בהצלחה.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void swapRows(Sheet sheet, int rowIndex1, int rowIndex2) {
        if (rowIndex1 == rowIndex2) {
            System.out.println("שורות זהות - אין צורך בהחלפה.");
            return;
        }

        Row row1 = sheet.getRow(rowIndex1);
        Row row2 = sheet.getRow(rowIndex2);

        System.out.println("מחליף שורות בין שורה " + (rowIndex1 + 1) + " ושורה " + (rowIndex2 + 1));

        Row tempRow = sheet.createRow(sheet.getLastRowNum() + 1);
        copyRow(sheet, tempRow, row1);
        copyRow(sheet, row1, row2);
        copyRow(sheet, row2, tempRow);

        sheet.removeRow(tempRow);
    }

    private void copyRow(Sheet sheet, Row target, Row source) {
        for (int i = 0; i < source.getLastCellNum(); i++) {
            Cell oldCell = source.getCell(i);
            Cell newCell = target.createCell(i);

            if (oldCell != null) {
                switch (oldCell.getCellType()) {
                    case STRING:
                        newCell.setCellValue(oldCell.getStringCellValue());
                        break;
                    case NUMERIC:
                        newCell.setCellValue(oldCell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        newCell.setCellValue(oldCell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        newCell.setCellFormula(oldCell.getCellFormula());
                        break;
                    default:
                        newCell.setCellValue(oldCell.toString());
                }
                newCell.setCellStyle(oldCell.getCellStyle());
            }
        }
    }

    private void removeRowColor(Row row, Workbook wb) {
        XSSFCellStyle defaultStyle = (XSSFCellStyle) wb.createCellStyle();
        int maxColumns = row.getSheet().getRow(0).getLastCellNum();

        for (int i = 0; i < maxColumns; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                cell = row.createCell(i);
            }
            cell.setCellStyle(defaultStyle);
        }
    }
}
