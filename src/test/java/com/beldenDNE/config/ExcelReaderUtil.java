package com.beldenDNE.config;


import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExcelReaderUtil {

    public static List<String> readProductNames(String filePath, String sheetName) throws IOException {
        List<String> productNames = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);

        for (Row row : sheet) {
            // Asumiendo que los nombres de los productos están en la primera columna (índice 0)
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                productNames.add(cell.getStringCellValue());
            }

           }
        workbook.close();
        fis.close();
        return productNames;
    }
}