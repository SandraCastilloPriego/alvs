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

package alvs.data.parser.impl;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 * @author scsandra
 */
public class ParserXLS {
    public HSSFWorkbook openExcel(String file_name)
	throws IOException
	{
		FileInputStream fileIn = null;
		try
		{	
			HSSFWorkbook wb;
			POIFSFileSystem fs;
			fileIn = new FileInputStream(file_name);
			fs = new POIFSFileSystem(fileIn);
			wb = new HSSFWorkbook(fs);
			return wb;
		}
		finally
		{
			if (fileIn != null)
         	  fileIn.close();
		}		
	}
	
	//get the number of rows to read in the excel file
	public int getNumberRows(int init, HSSFSheet sheet){		
		Iterator rowIt = sheet.rowIterator();
		int num = 0;
						
		while(rowIt.hasNext()) {   
			HSSFRow row   = (HSSFRow) rowIt.next();
			HSSFCell cell;
			cell = row.getCell((short) 0 );
			if((cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) && row.getRowNum()>init)
				break;
			num = row.getRowNum();			
		}		
		return num-init;
	}
	
	/*
	 *return 0 if the cell is blank, boolean or its color is red
	 *return 2 if the cell is string  
	 *return 1 if the cell is numeric or formula
	 */
	public int v_type(HSSFWorkbook wb, HSSFRow row, HSSFCell cell){		
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_BLANK :
			System.out.println( " Error - Row: " + row.getRowNum()+ " Cell: " + cell.getCellNum() + "  - Cell type Blank  - " + cell.toString());
			return 0;			
		case HSSFCell.CELL_TYPE_BOOLEAN :
			System.out.println( " Error - Row: " + row.getRowNum()+ " Cell: " + cell.getCellNum() + "  - Cell type boolean  - "+ cell.toString());
			return 0;
		case HSSFCell.CELL_TYPE_FORMULA :
			System.out.println( " Error - Row: " + row.getRowNum()+ " Cell: " + cell.getCellNum() + "  - Cell type formula  - "+ cell.toString());
			return 1;
		case HSSFCell.CELL_TYPE_NUMERIC :
			HSSFCellStyle style = cell.getCellStyle();
	        HSSFFont font = wb.getFontAt(style.getFontIndex());
			if(font.getColor() == (new HSSFColor.RED().getIndex())){				
				return 0;
			}
			return 1;
		case HSSFCell.CELL_TYPE_STRING :
			style = cell.getCellStyle();
	        font = wb.getFontAt(style.getFontIndex());
			if(font.getColor() == (new HSSFColor.RED().getIndex())){					
				return 0;
			}
			return 2;					
		default :
			return 0;
		}
	}	
	
	//get the name experiment from the name of the excel file
	public String getDatasetName(String file_name){
		Pattern pat = Pattern.compile("[\\\\/]");
		Matcher matcher = pat.matcher(file_name);   
		int index = 0;
		while(matcher.find()){
			index = matcher.start();
		}		
		String n = file_name.substring(index+1, file_name.length()-4);
		return n;
	}	

	 //replace all patterns in str string to string replace
	public String replace(String str, String pattern, String replace) {		
		 Pattern pat = Pattern.compile(pattern);
		 Matcher matcher = pat.matcher(str);   
		 if(matcher.find()){			 		 
			 str = matcher.replaceAll(replace);
		 }		
		 return str;
	 }  

}
