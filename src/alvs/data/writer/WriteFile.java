/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package alvs.data.writer;

import com.csvreader.CsvWriter;
import alvs.data.BugDataset;
import alvs.data.impl.peaklists.SimplePeakListRowOther;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Writes the data sets into a file.
 *
 * @author SCSANDRA
 */
public class WriteFile {

       
        /**
         * Writes Comma Separated file for basic data set.
         *
         * @param dataset basic data set
         * @param path Path where the new file will be created
         */
        public void WriteCommaSeparatedBasicDataset(BugDataset dataset, String path) {
                try {
                        CsvWriter w = new CsvWriter(path);
                        String[] data = new String[dataset.getNumberCols()];
                        int c = 0;
                        for (String experimentName : dataset.getAllColumnNames()) {
                                data[c++] = experimentName;
                        }
                        w.writeRecord(data);
                        for (int i = 0; i < dataset.getNumberRows(); i++) {
                                SimplePeakListRowOther lipid = (SimplePeakListRowOther) dataset.getRow(i);
                                c = 0;
                                for (String experimentName : dataset.getAllColumnNames()) {
                                        if (lipid.getPeak(experimentName) == null) {
                                                data[c++] = "";
                                        } else {
                                                data[c++] = String.valueOf(lipid.getPeak(experimentName));
                                        }
                                }
                                w.writeRecord(data);
                        }
                        w.endRecord();
                        w.close();
                } catch (Exception exception) {
                        exception.printStackTrace();
                }
        }

       /**
         * Writes the basic data set into an excel file.
         *
         * @param dataset basic data set
         * @param path Path where the new file will be created
         */
        public void WriteXLSFileBasicDataset(BugDataset dataset, String path) {
                FileOutputStream fileOut = null;
                try {
                        HSSFWorkbook wb;
                        HSSFSheet sheet;
                        try {
                                FileInputStream fileIn = new FileInputStream(path);
                                POIFSFileSystem fs = new POIFSFileSystem(fileIn);
                                wb = new HSSFWorkbook(fs);
                                int NumberOfSheets = wb.getNumberOfSheets();
                                sheet = wb.createSheet(String.valueOf(NumberOfSheets));
                        } catch (Exception exception) {
                                wb = new HSSFWorkbook();
                                sheet = wb.createSheet("Mass Lynx");
                        }
                        HSSFRow row = sheet.getRow(0);
                        if (row == null) {
                                row = sheet.createRow(0);
                        }
                        int cont = 0;
                        for (String experimentName : dataset.getAllColumnNames()) {
                                this.setCell(row, cont++, experimentName);

                        }
                        for (int i = 0; i < dataset.getNumberRows(); i++) {
                                SimplePeakListRowOther lipid = (SimplePeakListRowOther) dataset.getRow(i);

                                row = sheet.getRow(i + 1);
                                if (row == null) {
                                        row = sheet.createRow(i + 1);
                                }
                                int c = 0;
                                for (String experimentName : dataset.getAllColumnNames()) {
                                        if (lipid.getPeak(experimentName) == null) {
                                                this.setCell(row, c++, "");
                                        } else {
                                                this.setCell(row, c++, lipid.getPeak(experimentName));
                                        }
                                }
                        }
                        //Write the output to a file
                        fileOut = new FileOutputStream(path);
                        wb.write(fileOut);
                        fileOut.close();
                } catch (Exception exception) {
                        exception.printStackTrace();
                }
        }

       
        /**
         * Write data in a cell of the excel file.
         *
         * @param row Cell row
         * @param Index Cell column
         * @param data data to be writen into the cell
         */
        private void setCell(HSSFRow row, int Index, Object data) {
                if (data.getClass().toString().contains("String")) {
                        HSSFCell cell = row.getCell((short) Index);
                        if (cell == null) {
                                cell = row.createCell((short) Index);
                        }
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        cell.setCellValue((String) data);
                } else if (data.getClass().toString().contains("Double")) {
                        HSSFCell cell = row.getCell((short) Index);
                        if (cell == null) {
                                cell = row.createCell((short) Index);
                        }
                        cell.setCellValue((Double) data);
                } else if (data.getClass().toString().contains("Integer")) {
                        HSSFCell cell = row.getCell((short) Index);
                        if (cell == null) {
                                cell = row.createCell((short) Index);
                        }
                        cell.setCellValue((Integer) data);
                }
        }

      
}
