package com.yes4all.common.utils;

import com.yes4all.common.enums.EnumNoteExcel;
import com.yes4all.common.errors.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExcelHelper {
    public static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static Workbook readUploadExcel(InputStream inputStream) {
        try {
            return new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            throw new BusinessException("Fail to parse Excel file: " + e.getMessage());
        }
    }

    public static void createCell(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public static void createCellNOTE(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style, XSSFWorkbook workbook) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        EnumNoteExcel[] enumNoteExcels = EnumNoteExcel.class.getEnumConstants();
        Drawing drawing = cell.getSheet().createDrawingPatriarch();
        CreationHelper factory = workbook.getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(columnCount);
        anchor.setCol2(columnCount + 5);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2((int) (row.getRowNum() + Arrays.stream(enumNoteExcels).count()) + 2);
        Comment oComment = drawing.createCellComment(anchor);
        String comment = Arrays.stream(enumNoteExcels).map(i -> i.getId() + ": " + i.getName()).collect(Collectors.joining(" \n"));
        oComment.setString(factory.createRichTextString("- If change Quantity<>0 then typing text. \n" +
            "- If change Quantity=0 then choose options: \n" + comment));
        cell.setCellComment(oComment);
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }
}
