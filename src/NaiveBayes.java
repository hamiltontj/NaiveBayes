import java.io.*;
import org.apache.poi.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class NaiveBayes 
{ 
	public static void main(String[] args) 
	{ 
		String fileName = "./data/IrisDataSet.xls";
		String test = "";
		String data[][];
		
		
		System.out.println("Importing file called: " + fileName);
		
		FileInputStream file;
		try {
			file = new FileInputStream(new File(fileName));
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Error file not found");
			return;
		}
		
		try 
		{
			Workbook excelFile = new HSSFWorkbook(file);
			
			Sheet sheet1 = excelFile.getSheetAt(0);
			data = new String[sheet1.getPhysicalNumberOfRows()][sheet1.getRow(0).getPhysicalNumberOfCells()];
			
			for(Row row : sheet1)
			{
				for(Cell cell: row)
				{
					data[cell.getRowIndex()][cell.getColumnIndex()] = cell.toString();
				}
			}
			
			
			excelFile.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	
		System.out.println("Imported");
		System.out.println(test);
	}
}