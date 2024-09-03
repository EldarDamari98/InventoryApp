package com.example.excelnew;

import org.apache.poi.ss.usermodel.*;
import java.io.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class ProductRemover {

    private static final String EXCEL_FILE_PATH1 = "C:\\Users\\Eldar\\Desktop\\ITON\\EXCEL\\חוברת1.xlsx";
    private static final String EXCEL_FILE_PATH2 = "C:\\Users\\Eldar\\Desktop\\ITON\\EXCEL\\חוברת2.xlsx";

    // Keeping the existing implementation of removeProduct
    public void removeProduct(String sku) {
        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH1)) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            Row productRow = null;
            Row lastNonZeroRow = null;
            int lastRowIndex = -1;
            int productRowIndex = -1;

            // Searching for the product and the last row with a quantity greater than 0
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                Cell skuCell = row.getCell(1); // Assuming SKU is in the second column (index 1)
                if (skuCell != null) {
                    String cellValue;

                    if (skuCell.getCellType() == CellType.STRING) {
                        cellValue = skuCell.getStringCellValue();
                    } else if (skuCell.getCellType() == CellType.NUMERIC) {
                        cellValue = String.valueOf((int) skuCell.getNumericCellValue()); // convert to int first to remove decimal
                    } else {
                        continue; // skip other types of cells
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

                    if (available > 0) {
                        lastNonZeroRow = row;
                        lastRowIndex = i;
                        System.out.println("שורה אחרונה עם כמות גדולה מ-0: " + (lastRowIndex + 1));
                    }
                }
            }

            if (productRow != null) {
                Cell availableCell = productRow.getCell(4);
                int available = (int) availableCell.getNumericCellValue();

                if (available == 1 && lastNonZeroRow != null && lastRowIndex != -1 && productRowIndex != -1) {
                    System.out.println("הכמות למוצר עם SKU " + sku + " היא 1 ולכן יש להחליף שורות.");
                    System.out.println("מחליף שורות בין שורה " + (productRowIndex + 1) + " ושורה " + (lastRowIndex + 1));
                    printRow(sheet, productRowIndex);
                    printRow(sheet, lastRowIndex);
                    availableCell.setCellValue(0);

                    // Coloring the row in red
                    colorRowRed(wb, productRow);

                    // Swapping the rows
                    swapRows(sheet, productRowIndex, lastRowIndex);

                    // Printing the rows after the swap
                    printRow(sheet, productRowIndex);
                    printRow(sheet, lastRowIndex);
                } else if (available > 1) {
                    availableCell.setCellValue(available - 1);
                    System.out.println("הורדת כמות למוצר עם SKU " + sku + " לכמות: " + (available - 1));
                }

                // Saving the file after changes
                try (OutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH1)) {
                    wb.write(fileOut);
                    System.out.println("הקובץ נשמר בהצלחה.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // New function to update the sales workbook
    public void updateSalesSheet(String sku, String dateSold, String customerName, String status, double soldPrice, String invoiceNumber) {
        try (InputStream inp1 = new FileInputStream(EXCEL_FILE_PATH1);
             InputStream inp2 = new FileInputStream(EXCEL_FILE_PATH2)) {

            Workbook wb1 = WorkbookFactory.create(inp1);
            Workbook wb2 = WorkbookFactory.create(inp2);
            Sheet sheet1 = wb1.getSheetAt(0);
            Sheet sheet2 = wb2.getSheetAt(0);

            Row productRow = null;

            // Searching for the product in Workbook 1
            for (int i = 1; i <= sheet1.getLastRowNum(); i++) {
                Row row = sheet1.getRow(i);
                Cell skuCell = row.getCell(1); // Assuming SKU is in the second column (index 1)
                if (skuCell != null && skuCell.getStringCellValue().equals(sku)) {
                    productRow = row;
                    break;
                }
            }

            if (productRow != null) {
                int lastRowNum = sheet2.getLastRowNum();
                Row newRow = sheet2.createRow(++lastRowNum);

                // Copying details from Workbook 1 to Workbook 2
                for (int i = 0; i <= productRow.getLastCellNum(); i++) {
                    Cell cell = productRow.getCell(i);
                    if (cell != null) {
                        Cell newCell = newRow.createCell(i);
                        newCell.setCellValue(cell.toString());
                    }
                }

                // Filling in the details provided by the user
                newRow.createCell(4).setCellValue(dateSold); // DateSold column
                newRow.createCell(11).setCellValue(customerName); // CustomerName column
                newRow.createCell(12).setCellValue(status); // Status column
                newRow.createCell(13).setCellValue(soldPrice); // SoldPrice column
                newRow.createCell(14).setCellValue(invoiceNumber); // InvoiceNumber column

                // Saving the changes in Workbook 2
                try (OutputStream fileOut2 = new FileOutputStream(EXCEL_FILE_PATH2)) {
                    wb2.write(fileOut2);
                    System.out.println("הפרטים נשמרו בהצלחה בחוברת המכירות.");
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

        System.out.println("לפני ההחלפה:");
        printRow(sheet, rowIndex1);
        printRow(sheet, rowIndex2);

        // Saving the first row in a temporary variable
        System.out.println("שומר את שורה " + (rowIndex1 + 1) + " במשתנה זמני.");
        Row tempRow = sheet.createRow(sheet.getLastRowNum() + 1);
        copyRow(sheet, tempRow, row1);
        printRow(sheet, tempRow.getRowNum());

        // Copying Row 2 to the place of Row 1
        System.out.println("מעתיק את שורה " + (rowIndex2 + 1) + " לשורה " + (rowIndex1 + 1));
        copyRow(sheet, row1, row2);
        printRow(sheet, rowIndex1);

        // Copying the temporary row (original Row 1) to the place of Row 2
        System.out.println("מעתיק את השורה הזמנית לשורה " + (rowIndex2 + 1));
        copyRow(sheet, row2, tempRow);
        printRow(sheet, rowIndex2);

        // Deleting the temporary row
        System.out.println("מוחק את השורה הזמנית.");
        sheet.removeRow(tempRow);

        System.out.println("אחרי ההחלפה:");
        printRow(sheet, rowIndex1);
        printRow(sheet, rowIndex2);
    }

    private void copyRow(Sheet sheet, Row target, Row source) {
        System.out.println("העתקת תאים משורה " + (source.getRowNum() + 1) + " לשורה " + (target.getRowNum() + 1));
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

    private void colorRowRed(Workbook wb, Row row) {
        // Creating a cell style with a red background color
        XSSFCellStyle redStyle = (XSSFCellStyle) wb.createCellStyle();
        XSSFColor redColor = new XSSFColor(new java.awt.Color(255, 0, 0), null);
        redStyle.setFillForegroundColor(redColor);
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Maximum number of columns in the sheet
        int maxColumns = row.getSheet().getRow(0).getLastCellNum();

        // Applying the style to all cells in the row, including empty cells
        for (int i = 0; i < maxColumns; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                cell = row.createCell(i); // Create a new cell if it is empty
            }
            cell.setCellStyle(redStyle);
        }
    }

    private void printRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            System.out.print("תוכן השורה " + (rowIndex + 1) + ": ");
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    System.out.print(cell.toString() + " | ");
                } else {
                    System.out.print("null | ");
                }
            }
            System.out.println();
        }
    }
}
