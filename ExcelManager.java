package com.example.excelnew;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class ExcelManager {
    private static final String EXCEL_FILE_PATH = "C:\\Users\\Eldar\\Desktop\\ITON\\EXCEL\\חוברת1.xlsx";

    // In the ExcelManager.java file
    public List<Product> getAllProductsFromExcel() {
        List<Product> products = new ArrayList<>();
        String defaultImage = "default.png"; // Assuming all products receive a default image

        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH)) {
            Workbook workbook = WorkbookFactory.create(inp);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);

                String sku = row.getCell(1).getStringCellValue();
                String name = row.getCell(0).getStringCellValue();
                String description = row.getCell(3).getStringCellValue();

                double price = 0.0;
                if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                    price = row.getCell(2).getNumericCellValue();
                } else if (row.getCell(2).getCellType() == CellType.STRING) {
                    try {
                        price = Double.parseDouble(row.getCell(2).getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.err.println("Cannot parse price: " + row.getCell(2).getStringCellValue());
                    }
                }

                int quantity = 0;
                if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                    quantity = (int) row.getCell(4).getNumericCellValue();
                } else if (row.getCell(4).getCellType() == CellType.STRING) {
                    try {
                        quantity = Integer.parseInt(row.getCell(4).getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.err.println("Cannot parse quantity: " + row.getCell(4).getStringCellValue());
                    }
                }

                double buyingPrice = 0.0;
                if (row.getCell(5).getCellType() == CellType.NUMERIC) {
                    buyingPrice = row.getCell(5).getNumericCellValue();
                } else if (row.getCell(5).getCellType() == CellType.STRING) {
                    try {
                        buyingPrice = Double.parseDouble(row.getCell(5).getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.err.println("Cannot parse BuyingPrice: " + row.getCell(5).getStringCellValue());
                    }
                }

                double weight = 0.0;
                if (row.getCell(10) != null && row.getCell(10).getCellType() == CellType.NUMERIC) {
                    weight = row.getCell(10).getNumericCellValue();
                }

                String goldKarat = "";
                if (row.getCell(6) != null && row.getCell(6).getCellType() == CellType.STRING) {
                    goldKarat = row.getCell(6).getStringCellValue();
                }

                String centerStoneCT = "";
                if (row.getCell(7) != null && row.getCell(7).getCellType() == CellType.STRING) {
                    centerStoneCT = row.getCell(7).getStringCellValue();
                }

                String sideStonesCT = "";
                if (row.getCell(8) != null && row.getCell(8).getCellType() == CellType.STRING) {
                    sideStonesCT = row.getCell(8).getStringCellValue();
                }

                // Reading the product status from Shopify
                String shopifyStatus = "";
                if (row.getCell(11) != null && row.getCell(11).getCellType() == CellType.STRING) {
                    shopifyStatus = row.getCell(11).getStringCellValue();
                }

                // Creating a product with the added description and status
                products.add(new Product(sku, name, price, quantity, defaultImage, buyingPrice, weight, goldKarat, centerStoneCT, sideStonesCT, description, shopifyStatus, new ArrayList<>()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }


    public void markProductAsUploadedToShopify(String sku) {
        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH)) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getCell(1).getStringCellValue().equals(sku)) {
                    Cell statusCell = row.createCell(11); // Assuming that column 11 (L) is used for the upload status
                    statusCell.setCellValue("Uploaded");
                    break;
                }
            }

            try (OutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
                wb.write(fileOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addProductToExcel(String title, String sku, double price, String description, int available, double BuyingPrice, String goldKarat, String centerStone, String sideStones, double weightInGrams, boolean isUploadedToShopify) {
        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH)) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.createRow(++lastRowNum);
            row.createCell(0).setCellValue(title);
            row.createCell(1).setCellValue(sku);
            row.createCell(2).setCellValue(price);
            row.createCell(3).setCellValue(description);
            row.createCell(4).setCellValue(available);
            row.createCell(5).setCellValue(BuyingPrice);
            row.createCell(6).setCellValue(goldKarat);
            row.createCell(7).setCellValue(centerStone);
            row.createCell(8).setCellValue(sideStones);
            row.createCell(10).setCellValue(weightInGrams);

            // Check if not marked for upload to Shopify
            if (!isUploadedToShopify) {
                row.createCell(11).setCellValue("Not Uploaded");
            }

            try (OutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
                wb.write(fileOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getCollectionIdsFromExcel(String sku) {
        List<String> collectionIds = new ArrayList<>();
        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH)) {
            Workbook workbook = WorkbookFactory.create(inp);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getCell(1).getStringCellValue().equals(sku)) {
                    Cell collectionCell = row.getCell(9);
                    if (collectionCell != null) {
                        String[] collections = collectionCell.getStringCellValue().split(",\\s*");
                        System.out.println("Found collections for SKU " + sku + ": " + Arrays.toString(collections));
                        for (String collection : collections) {
                            switch (collection.trim()) {
                                case "OUTLET":
                                    collectionIds.add("283586232399");
                                    System.out.println("Collection 'OUTLET' mapped to ID: 283586232399");
                                    break;
                                case "OUTLET ARRIVED":
                                    collectionIds.add("283650490447");
                                    System.out.println("Collection 'OUTLET ARRIVED' mapped to ID: 283650490447");
                                    break;
                                case "OUTLET RINGS":
                                    collectionIds.add("283650424911");
                                    System.out.println("Collection 'OUTLET RINGS' mapped to ID: 283650424911");
                                    break;
                                case "OUTLET BRACELETS":
                                    collectionIds.add("283650555983");
                                    System.out.println("Collection 'OUTLET BRACELETS' mapped to ID: 283650555983");
                                    break;
                                case "OUTLET NECKLACES":
                                    collectionIds.add("283650523215");
                                    System.out.println("Collection 'OUTLET NECKLACES' mapped to ID: 283650523215");
                                    break;
                                case "OUTLET EARRINGS":
                                    collectionIds.add("283650392143");
                                    System.out.println("Collection 'OUTLET EARRINGS' mapped to ID: 283650392143");
                                    break;
                                default:
                                    System.out.println("Unknown collection: " + collection);
                                    break;
                            }
                        }
                    } else {
                        System.out.println("No collections found for SKU: " + sku);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collectionIds;
    }



    public void addCollectionToExcel(String sku, List<String> collections) {
        try (InputStream inp = new FileInputStream(EXCEL_FILE_PATH)) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getCell(1).getStringCellValue().equals(sku)) {
                    Cell collectionCell = row.getCell(9);
                    String existingCollections = collectionCell != null ? collectionCell.getStringCellValue() : "";

                    StringBuilder collectionBuilder = new StringBuilder(existingCollections);
                    for (String collection : collections) {
                        if (!existingCollections.contains(collection)) {
                            if (collectionBuilder.length() > 0) {
                                collectionBuilder.append(", ");
                            }
                            collectionBuilder.append(collection);
                        }
                    }

                    row.createCell(9).setCellValue(collectionBuilder.toString()); // Column index for Collection
                    break;
                }
            }

            try (OutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
                wb.write(fileOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
