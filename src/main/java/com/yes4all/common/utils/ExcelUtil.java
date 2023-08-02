package com.yes4all.common.utils;

import com.yes4all.common.errors.BusinessException;

import com.yes4all.domain.*;

import com.yes4all.domain.BookingPackingList;
import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.Vendor;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yes4all.constants.GlobalConstant.*;

public class ExcelUtil {
    static DecimalFormat decimalFormat2 = new DecimalFormat("#,###.00");
    static DecimalFormat decimalFormat3 = new DecimalFormat("#,###.000");
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    // private constructor
    private ExcelUtil() {
    }

    public static byte[] generateExcelFile(String sheetName, int startRow, List<String> headers, Map<Long, List<Object>> sortedExcelDTOs, BookingPackingList bookingPackingList, Vendor vendor) {
        // Create workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Generate excel file
        generateExcelFileWorkbookFromBOL(workbook, sheetName, startRow, headers, sortedExcelDTOs, bookingPackingList, vendor);

        // Convert workbook to byte array
        return convertWorkbookToByte(workbook);
    }


    public static byte[] generateExcelFileWH(String sheetName, int startRow, List<String> headers, Map<Long, List<Object>> sortedExcelDTOs ,CommercialInvoiceWH commercialInvoiceWH, Vendor vendor) {
        // Create workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Generate excel file
        generateExcelFileWorkbookWH(workbook, sheetName, startRow, headers, sortedExcelDTOs, commercialInvoiceWH, vendor);

        // Convert workbook to byte array
        return convertWorkbookToByte(workbook);
    }

    public static byte[] generateExcelFilePI(String sheetName, int startRow, List<String> headers, Map<Long, List<Object>> sortedExcelDTOs, ProformaInvoice proformaInvoice, Vendor vendor) {
        // Create workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Generate excel file
        generateExcelFileWorkbookFromPI(workbook, sheetName, startRow, headers, sortedExcelDTOs, proformaInvoice, vendor);

        // Convert workbook to byte array
        return convertWorkbookToByte(workbook);
    }

    public static XSSFWorkbook generateExcelFileWorkbook(XSSFWorkbook workbook, String sheetName, int startRow,
                                                         List<String> headers, Map<Long, List<Object>> sortedExcelDTOs) {
        log.info("START Generate excel file");

        try {
            // Create sheet
            XSSFSheet sheet = CommonDataUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);

            // Creating header
            createHeaders(workbook, sheet, startRow, headers);

            // Creating data rows for each item
            addExcelCell(workbook, sheet, ++startRow, sortedExcelDTOs);

            // Resize the columns to fit the content
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            log.info("END generate excel file");
            return workbook;
        } catch (Exception e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
    }

    public static XSSFWorkbook generateExcelFileWorkbookFromBOL(XSSFWorkbook workbook, String sheetName, int startRow,
                                                                List<String> headers, Map<Long, List<Object>> sortedExcelDTOs, BookingPackingList bookingPackingList, Vendor vendor) {
        log.info("START Generate excel file");

        try {
            // Create sheet
            XSSFSheet sheet = CommonDataUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);
            // Creating header
            createHeadersFromBOL(workbook, sheet, startRow, headers, bookingPackingList, vendor);

            // Creating data rows for each item
            addExcelCellFromBOL(workbook, sheet, ++startRow, sortedExcelDTOs, vendor.getVendorName());

//            // Resize the columns to fit the content
//            for (int i = 0; i < headers.size(); i++) {
//                sheet.autoSizeColumn(i);
//            }

            log.info("END generate excel file");
            return workbook;
        } catch (Exception e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
    }
    public static XSSFWorkbook generateExcelFileWorkbookWH(XSSFWorkbook workbook, String sheetName, int startRow,
                                                                List<String> headers, Map<Long, List<Object>> sortedExcelDTOs,CommercialInvoiceWH commercialInvoiceWH, Vendor vendor) {
        log.info("START Generate excel file");

        try {
            // Create sheet
            XSSFSheet sheet = CommonDataUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);
            // Creating header
            createHeadersFromWH(workbook, sheet, startRow, headers, commercialInvoiceWH, vendor);

            // Creating data rows for each item
            addExcelCellFromWH(workbook, sheet, ++startRow, sortedExcelDTOs, vendor.getVendorName());

//            // Resize the columns to fit the content
//            for (int i = 0; i < headers.size(); i++) {
//                sheet.autoSizeColumn(i);
//            }

            log.info("END generate excel file");
            return workbook;
        } catch (Exception e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
    }



    public static XSSFWorkbook generateExcelFileWorkbookFromPI(XSSFWorkbook workbook, String sheetName, int startRow,
                                                               List<String> headers, Map<Long, List<Object>> sortedExcelDTOs, ProformaInvoice proformaInvoice, Vendor vendor) {
        log.info("START Generate excel file");

        try {
            // Create sheet
            XSSFSheet sheet = CommonDataUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);
            // Creating header
            createHeadersFromPI(workbook, sheet, startRow, headers, proformaInvoice, vendor);

            // Creating data rows for each item
            addExcelCellFromPI(workbook, sheet, ++startRow, sortedExcelDTOs, proformaInvoice);

//            // Resize the columns to fit the content
//            for (int i = 0; i < headers.size(); i++) {
//                sheet.autoSizeColumn(i);
//            }

            log.info("END generate excel file");
            return workbook;
        } catch (Exception e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
    }

    public static XSSFWorkbook generateExcelVerticalFileWorkbook(XSSFWorkbook workbook, String sheetName, int startRow,
                                                                 Map<Long, List<Object>> sortedExcelDTOs) {
        log.info("START Generate excel file");

        try {
            // Create sheet
            XSSFSheet sheet = CommonDataUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);

            // Creating data rows for each item
            addExcelCellVertical(workbook, sheet, startRow, sortedExcelDTOs);

            // Resize column
            for (Map.Entry<Long, List<Object>> entry : sortedExcelDTOs.entrySet()) {
                sheet.autoSizeColumn(entry.getKey().intValue() - 1);
            }

            log.info("END generate excel file");
            return workbook;
        } catch (Exception e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
    }

    public static byte[] convertWorkbookToByte(XSSFWorkbook workbook) {
        log.info("START convert Workbook to excel file");
        byte[] resource;

        try {
            // Write byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            // return byte array
            resource = byteArrayOutputStream.toByteArray();

            // Close stream
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

            log.info("END convert Workbook to excel file");
        } catch (Exception e) {
            log.error("Convert workbook to byte[] error");
            throw new BusinessException("Convert workbook to byte[] error");
        }
        return resource;
    }

    public static void addExcelCell(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, Map<Long, List<Object>> contents) {
        List<Object> excelCellDTOs;
        XSSFCellStyle cellStyle = createStyleDetail(workbook);

        for (Map.Entry<Long, List<Object>> entry : contents.entrySet()) {
            XSSFRow dataRow = sheet.createRow(startRow++);
            excelCellDTOs = entry.getValue();
            try {
                for (int columnIndex = 0; columnIndex < excelCellDTOs.size(); columnIndex++) {
                    createCell(dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                }

            } catch (NullPointerException ne) {
                log.error("Excel row for element {} is not valid", entry.getKey());
            }
        }
    }

    public static void addExcelCellFromBOL(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, Map<Long, List<Object>> contents, String vendorName) {
        List<Object> excelCellDTOs;
        XSSFCellStyle cellStyle = createStyleDetail(workbook);
        startRow = 10;
        XSSFCellStyle styleFieldNumber2 = workbook.createCellStyle();
        styleFieldNumber2.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        double totalQuantity = 0;
        double totalAmount = 0;

        for (Map.Entry<Long, List<Object>> entry : contents.entrySet()) {
            XSSFRow dataRow = sheet.createRow(startRow++);
            excelCellDTOs = entry.getValue();
            try {
                for (int columnIndex = 0; columnIndex < excelCellDTOs.size(); columnIndex++) {
                    createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    if (columnIndex == 5 || columnIndex == 6) {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber2);
                    } else {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    }
                    if (columnIndex == 4) {
                        totalQuantity += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                    if (columnIndex == 6) {
                        totalAmount += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                    if (columnIndex == 5 || columnIndex == 6) {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber2);
                    } else {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    }
                    if (columnIndex == 4) {
                        totalQuantity += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                    if (columnIndex == 6) {
                        totalAmount += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                }
            } catch (NullPointerException ne) {
                log.error("Excel row for element {} is not valid", entry.getKey());
            }
        }
        XSSFRow dataRow = sheet.createRow(startRow++);
        for (int columnIndex =0; columnIndex < 7 ; columnIndex++) {
            if (columnIndex == 4) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalQuantity, cellStyle);
            } else if (columnIndex == 6) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalAmount, styleFieldNumber2);
            } else if (columnIndex == 0) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, "Total:", cellStyle);
            } else {
                createCellNoAutoSize(sheet, dataRow, columnIndex, "", cellStyle);
            }

        }
        XSSFRow dataRowFinal = sheet.createRow(startRow + 1);
        createCellNoBorder(sheet, dataRowFinal, 3, vendorName, cellStyle);
    }

    public static void addExcelCellFromWH(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, Map<Long, List<Object>> contents, String vendorName) {
        List<Object> excelCellDTOs;
        XSSFCellStyle cellStyle = createStyleDetail(workbook);
        startRow = 10;
        XSSFCellStyle styleFieldNumber2 = workbook.createCellStyle();
        styleFieldNumber2.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        double totalQuantity = 0;
        double totalAmount = 0;
        for (Map.Entry<Long, List<Object>> entry : contents.entrySet()) {
            XSSFRow dataRow = sheet.createRow(startRow++);
            excelCellDTOs = entry.getValue();
            try {
                for (int columnIndex = 0; columnIndex < excelCellDTOs.size(); columnIndex++) {
                    createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    if (columnIndex == 3 || columnIndex == 5) {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber2);
                    } else {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    }
                    if (columnIndex == 3) {
                        totalQuantity += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                    if (columnIndex == 5) {
                        totalAmount += Double.parseDouble(excelCellDTOs.get(columnIndex).toString());
                    }
                }
            } catch (NullPointerException ne) {
                log.error("Excel row for element {} is not valid", entry.getKey());
            }
        }
        XSSFRow dataRow = sheet.createRow(startRow++);
        for (int columnIndex =0; columnIndex < 6 ; columnIndex++) {
            if (columnIndex == 3) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalQuantity, cellStyle);
            } else if (columnIndex == 5) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalAmount, styleFieldNumber2);
            } else if (columnIndex == 0) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, "Total:", cellStyle);
            } else {
                createCellNoAutoSize(sheet, dataRow, columnIndex, "", cellStyle);
            }

        }
        XSSFRow dataRowFinal = sheet.createRow(startRow + 1);
        createCellNoBorder(sheet, dataRowFinal, 3, vendorName, cellStyle);
    }


    public static void addExcelCellFromPI(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, Map<Long, List<Object>> contents, ProformaInvoice proformaInvoice) {
        List<Object> excelCellDTOs;
        XSSFCellStyle styleField = workbook.createCellStyle();
        XSSFFont fontField = workbook.createFont();
        fontField.setFontHeight(11);
        fontField.setBold(true);
        styleField.setFont(fontField);
        startRow = 13;
        int totalQuantity = 0;
        double totalAmount = 0;
        double totalCTN = 0;
        double totalCBM = 0;
        double totalNW = 0;
        double totalGW = 0;
        XSSFCellStyle styleFieldNumber2 = workbook.createCellStyle();
        styleFieldNumber2.setFont(fontField);
        styleFieldNumber2.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        XSSFCellStyle styleFieldNumber3 = workbook.createCellStyle();
        styleFieldNumber3.setFont(fontField);
        styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        for (Map.Entry<Long, List<Object>> entry : contents.entrySet()) {
            XSSFRow dataRow = sheet.createRow(startRow++);
            excelCellDTOs = entry.getValue();
            try {
                for (int columnIndex = 0; columnIndex < excelCellDTOs.size(); columnIndex++) {
                    if (columnIndex == 4) {
                        totalQuantity += (int) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleField);
                    } else if (columnIndex == 5) {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber2);
                    } else if (columnIndex == 6) {
                        totalAmount += (double) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber2);
                    } else if (columnIndex == 7) {
                        totalCTN += (double) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleField);
                    } else if (columnIndex == 9) {
                        totalCBM += (double) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber3);
                    } else if (columnIndex == 10) {
                        totalNW += (double) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber3);
                    } else if (columnIndex == 11) {
                        totalGW += (double) excelCellDTOs.get(columnIndex);
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleFieldNumber3);
                    } else {
                        createCellNoAutoSize(sheet, dataRow, columnIndex, excelCellDTOs.get(columnIndex), styleField);
                    }
                }
            } catch (NullPointerException ne) {
                log.error("Excel row for element {} is not valid", entry.getKey());
            }
        }
        XSSFRow dataRow = sheet.createRow(startRow++);
        XSSFFont fontFieldTotal = workbook.createFont();
        fontFieldTotal.setFontHeight(11);
        fontFieldTotal.setBold(true);

        for (int columnIndex = 0; columnIndex < 13; columnIndex++) {
            String value = "";
            if (columnIndex == 4) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalQuantity, styleFieldNumber2);
            } else if (columnIndex == 6) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalAmount, styleFieldNumber2);
            } else if (columnIndex == 7) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalCTN, styleField);
            } else if (columnIndex == 9) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalCBM, styleFieldNumber3);
            } else if (columnIndex == 10) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalNW, styleFieldNumber3);
            } else if (columnIndex == 11) {
                createCellNoAutoSize(sheet, dataRow, columnIndex, totalGW, styleFieldNumber3);
            } else {
                createCellNoAutoSize(sheet, dataRow, columnIndex, value, styleField);
            }

        }
        int countRow = startRow;
        XSSFRow dataRowSignature = sheet.createRow(countRow);
        XSSFCellStyle styleFieldBank = workbook.createCellStyle();
        XSSFFont fontFieldBank = workbook.createFont();
        fontFieldBank.setFontHeight(11);
        fontFieldBank.setBold(true);
        styleFieldBank.setFont(fontFieldBank);
        styleFieldBank.setBorderBottom(BorderStyle.NONE);
        styleFieldBank.setBorderTop(BorderStyle.NONE);
        styleFieldBank.setBorderRight(BorderStyle.NONE);
        styleFieldBank.setBorderLeft(BorderStyle.NONE);
        createCellNoBorder(sheet, dataRowSignature, 3, "BUYER’S CONFIRMATION ", styleFieldBank);
        createCellNoBorder(sheet, dataRowSignature, 6, "SELLER’S CONFIRMATION", styleFieldBank);
        countRow++;
        countRow++;
        countRow++;
        XSSFRow dataRowBank = sheet.createRow(countRow);
        createCellNoBorder(sheet, dataRowBank, 3, "COMPANY NAME: " + proformaInvoice.getCompanyName(), styleFieldBank);
        countRow++;
        dataRowBank = sheet.createRow(countRow);
        createCellNoBorder(sheet, dataRowBank, 3, "A/C NUMBER: " + proformaInvoice.getAcNumber(), styleFieldBank);
        countRow++;
        dataRowBank = sheet.createRow(countRow);
        createCellNoBorder(sheet, dataRowBank, 3, "BENEFICARY’S BANK: " + proformaInvoice.getBeneficiaryBank(), styleFieldBank);
        countRow++;
        dataRowBank = sheet.createRow(countRow);
        createCellNoBorder(sheet, dataRowBank, 3, "SWIFT CODE: " + proformaInvoice.getSwiftCode(), styleFieldBank);

    }

    public static void addExcelCellVertical(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, Map<Long, List<Object>> contents) {
        List<Object> excelCellDTOs;
        XSSFCellStyle cellStyle = createStyleDetail(workbook);
        XSSFCellStyle cellStyleHeader = createStyleHeader(workbook);

        for (Map.Entry<Long, List<Object>> entry : contents.entrySet()) {
            XSSFRow dataRow = sheet.createRow(startRow++);
            excelCellDTOs = entry.getValue();
            try {
                for (int columnIndex = 0; columnIndex < excelCellDTOs.size(); columnIndex++) {
                    // First column is a header
                    if (columnIndex == 0) {
                        createCell(dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyleHeader);
                    } else {
                        createCell(dataRow, columnIndex, excelCellDTOs.get(columnIndex), cellStyle);
                    }
                }
            } catch (NullPointerException ne) {
                log.error("Excel row for element {} is not valid", entry.getKey());
            }

        }
    }

    public static void createHeaders(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, List<String> headers) {
        // Create style
        XSSFCellStyle style = createStyleHeader(workbook);

        XSSFRow row = sheet.createRow(startRow);
        // Add header value
        for (int i = 0; i < headers.size(); i++) {
            createCell(row, i, headers.get(i), style);
        }
    }

    public static void createHeadersFromBOL(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, List<String> headers, BookingPackingList bookingPackingList, Vendor vendor) {
        Row row = sheet.createRow(0);
        int rowCount = 0;
        XSSFCellStyle styleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setFontHeight(14);
        fontTitle.setBold(true);
        styleTitle.setFont(fontTitle);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        createCellNoBorder(sheet, row, 0, vendor.getVendorName(), styleTitle);
        ExcelUtil.mergeCell(sheet, 0, 0, 0, 11);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, vendor.getFactoryAddress(), styleTitle);
        ExcelUtil.mergeCell(sheet, 1, 1, 0, 11);
        rowCount++;
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "COMMERCIAL INVOICE", styleTitle);
        ExcelUtil.mergeCell(sheet, 3, 3, 0, 11);
        rowCount++;

        XSSFCellStyle styleField = workbook.createCellStyle();
        XSSFFont fontField = workbook.createFont();
        fontField.setFontHeight(11);
        fontField.setBold(true);
        styleField.setFont(fontField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "SOLD TO: ", styleField);
        createCellNoBorder(sheet, row, 1, SOLD_TO_COMPANY, styleField);
        createCellNoBorder(sheet, row, 6, "INV NO: ", styleField);
        createCellNoBorder(sheet, row, 7, bookingPackingList.getCommercialInvoice().getInvoiceNo(), styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, "Address: " + SOLD_TO_ADDRESS, styleField);
        createCellNoBorder(sheet, row, 6, "DATE: ", styleField);
        LocalDate localDate = bookingPackingList.getCommercialInvoice().getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = localDate.format(formatter);
        createCellNoBorder(sheet, row, 7, formattedDate, styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, "Phone: " + SOLD_TO_TELEPHONE, styleField);
        createCellNoBorder(sheet, row, 6, "P.O No.: ", styleField);


        createCellNoBorder(sheet, row, 7, bookingPackingList.getCommercialInvoice().getCommercialInvoiceDetail().stream().map(CommercialInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(",")), styleField);

        rowCount++;
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        XSSFColor myColor = new XSSFColor(rgb);
        //style.setFillForegroundColor(myColor);
        // style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        row = sheet.createRow(rowCount + 1);
        // Add header value
        for (int i = 0; i < headers.size(); i++) {
            createCellNoAutoSize(sheet, row, i, headers.get(i), style);
        }
    }
    public static void createHeadersFromWH(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, List<String> headers, CommercialInvoiceWH commercialInvoiceWH, Vendor vendor) {
        Row row = sheet.createRow(0);
        int rowCount = 0;
        XSSFCellStyle styleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setFontHeight(14);
        fontTitle.setBold(true);
        styleTitle.setFont(fontTitle);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        createCellNoBorder(sheet, row, 0, vendor.getVendorName(), styleTitle);
        ExcelUtil.mergeCell(sheet, 0, 0, 0, 11);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, vendor.getFactoryAddress(), styleTitle);
        ExcelUtil.mergeCell(sheet, 1, 1, 0, 11);
        rowCount++;
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "COMMERCIAL INVOICE", styleTitle);
        ExcelUtil.mergeCell(sheet, 3, 3, 0, 11);
        rowCount++;

        XSSFCellStyle styleField = workbook.createCellStyle();
        XSSFFont fontField = workbook.createFont();
        fontField.setFontHeight(11);
        fontField.setBold(true);
        styleField.setFont(fontField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "SOLD TO: ", styleField);
        createCellNoBorder(sheet, row, 1, SOLD_TO_COMPANY, styleField);
        createCellNoBorder(sheet, row, 6, "INV NO: ", styleField);
        createCellNoBorder(sheet, row, 7, commercialInvoiceWH.getInvoiceNo(), styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, "Address: " + SOLD_TO_ADDRESS, styleField);
        createCellNoBorder(sheet, row, 6, "DATE: ", styleField);
        LocalDate localDate = commercialInvoiceWH.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = localDate.format(formatter);
        createCellNoBorder(sheet, row, 7, formattedDate, styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, "Phone: " + SOLD_TO_TELEPHONE, styleField);
        createCellNoBorder(sheet, row, 6, "P.O No.: ", styleField);
        createCellNoBorder(sheet, row, 7,"", styleField);
        rowCount++;
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        XSSFColor myColor = new XSSFColor(rgb);
        //style.setFillForegroundColor(myColor);
        // style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        row = sheet.createRow(rowCount + 1);
        // Add header value
        for (int i = 0; i < headers.size(); i++) {
            createCellNoAutoSize(sheet, row, i, headers.get(i), style);
        }
    }


    public static void createHeadersFromPI(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, List<String> headers, ProformaInvoice proformaInvoice, Vendor vendor) {
        Row row = sheet.createRow(0);
        int rowCount = 1;
        XSSFCellStyle styleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setFontHeight(14);
        fontTitle.setBold(true);
        styleTitle.setFont(fontTitle);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        row = sheet.createRow(rowCount);
        rowCount++;
        createCellNoBorder(sheet, row, 0, "PROFORMA INVOICE", styleTitle);
        ExcelUtil.mergeCell(sheet, 1, 1, 0, 11);
        XSSFCellStyle styleField = workbook.createCellStyle();
        XSSFFont fontField = workbook.createFont();
        fontField.setFontHeight(11);
        fontField.setBold(true);
        styleField.setFont(fontField);
        styleField.setBorderBottom(BorderStyle.NONE);
        styleField.setBorderTop(BorderStyle.NONE);
        styleField.setBorderRight(BorderStyle.NONE);
        styleField.setBorderLeft(BorderStyle.NONE);

        XSSFCellStyle styleFieldGeneral = workbook.createCellStyle();
        XSSFFont fontFieldGeneral = workbook.createFont();
        fontFieldGeneral.setFontHeight(11);
        fontFieldGeneral.setBold(true);
        styleFieldGeneral.setFont(fontFieldGeneral);
        styleFieldGeneral.setBorderBottom(BorderStyle.NONE);
        styleFieldGeneral.setBorderTop(BorderStyle.NONE);
        styleFieldGeneral.setBorderRight(BorderStyle.NONE);
        styleFieldGeneral.setBorderLeft(BorderStyle.NONE);
        XSSFCellStyle styleFieldNoBold = workbook.createCellStyle();
        XSSFFont fontFieldNoBold = workbook.createFont();
        fontFieldNoBold.setFontHeight(11);
        styleFieldNoBold.setFont(fontFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 8, "ORDER NO: " + proformaInvoice.getPurchaseOrders().getPoNumber(), styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "SELLER: ", styleFieldGeneral);
        createCellNoBorder(sheet, row, 1, vendor.getVendorName(), styleFieldNoBold);
        LocalDate localDate = proformaInvoice.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String formattedDate = localDate.format(formatter);
        createCellNoBorder(sheet, row, 8, "DATE: " + formattedDate, styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, vendor.getFactoryAddress(), styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "BUYER: ", styleFieldGeneral);
        createCellNoBorder(sheet, row, 1, "TO: " + SOLD_TO_COMPANY, styleFieldNoBold);
        createCellNoBorder(sheet, row, 8, "INV NO: " + proformaInvoice.getOrderNo(), styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 1, "Address: " + SOLD_TO_ADDRESS + "\n" + "Phone: " + SOLD_TO_TELEPHONE, styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 8, "TO: " + proformaInvoice.getFulfillmentCenter(), styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "Payment: ", styleFieldNoBold);
        String shipDate = proformaInvoice.getShipDate().format(formatter);
        createCellNoBorder(sheet, row, 8, "Shipdate: " + shipDate, styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "Port of loading: ", styleFieldNoBold);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(sheet, row, 0, "INCOTERM: ", styleFieldNoBold);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        row = sheet.createRow(rowCount + 1);
        // Add header value
        for (int i = 0; i < headers.size(); i++) {
            createCellNoAutoSize(sheet, row, i, headers.get(i), styleField);
        }
    }

    private static void createCellNoAutoSize(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        cell.setCellStyle(style);

    }

    private static void createCellNoBorder(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, XSSFCellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);


    }

    public static XSSFCellStyle createStyleHeader(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        font.setBold(true);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        XSSFColor myColor = new XSSFColor(rgb);
        style.setFillForegroundColor(myColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public static XSSFCellStyle createStyleDetail(XSSFWorkbook workbook) {
        return workbook.createCellStyle();
    }

    public static void createCell(XSSFRow row, int columnCount, Object valueOfCell, CellStyle style) {
        XSSFCell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public static void mergeCell(XSSFSheet sheet, int startRow, int endRow, int startCol, int lastCol) {
        CellRangeAddress cellRangeAddress = new CellRangeAddress(startRow, endRow, startCol, lastCol);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, sheet);
    }
}
