package net.mfjassociates.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ModuleRef;

public class ClasspathHelper {

	
	public static final File DEFAULT_WORKBOOK_FILE;
	private static final String DEFAULT_WORKBOOK_NAME = "java_classpath.xlsx";
	
	static {
		String parent = System.getProperty("user.home");
		if (parent==null) parent = System.getenv("TMP");
		File parentDir;
		if (parent==null) parentDir=new File("C:\temp");
		else parentDir = new File(parent);
		DEFAULT_WORKBOOK_FILE = new File(parentDir, DEFAULT_WORKBOOK_NAME);
	}

	public static void main(String[] args) throws IOException {
		storeClasspathToExcel("Classpath2");
		
		Runtime rt=Runtime.getRuntime();
		Process p=rt.exec(new String[]{"cmd.exe","/c", "start excel.exe "+DEFAULT_WORKBOOK_FILE.getAbsolutePath()});
		System.out.println(p.isAlive());

	}
	
	public static void silentStoreClasspathToExcel(String sheetName) {
		try {
			storeClasspathToExcel(sheetName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void storeClasspathToExcel(String sheetName) throws IOException {
		if (sheetName==null || sheetName.length()==0) throw new IllegalArgumentException("missing or invalid sheet name");
		Workbook wb=openWorkbook(DEFAULT_WORKBOOK_FILE);
		Sheet sheet=replaceSheet(wb, sheetName);
		fillSheet(sheet);
		FileOutputStream fos=new FileOutputStream(DEFAULT_WORKBOOK_FILE);
		wb.write(fos);
		fos.close();
	}
	private static Sheet replaceSheet(Workbook wb, String sheetName) {
		Sheet sheet = wb.getSheet(sheetName);
		if (sheet!=null) wb.removeSheetAt(wb.getSheetIndex(sheet)); // zap existing sheet
		sheet=wb.createSheet(sheetName);
		return sheet;
	}
	
	public static Workbook openWorkbook(File wbFile) throws IOException {
		Workbook wb=new XSSFWorkbook();
		if (wbFile.exists()) {
			FileInputStream fis=new FileInputStream(wbFile);
			wb=new XSSFWorkbook(fis);
			fis.close();
		}
		return wb;
	}

	private static void fillSheet(Sheet sheet) {
		int rowno=0;
		createRow(sheet, rowno++, "Name", "Path", "isFile");
		List<File> cp=new ClassGraph().scan().getClasspathFiles();
		for (File file : cp) {
			createRow(sheet, rowno++, file.getName(), file.getAbsolutePath(), file.isFile());
		}
	}

	private static void createRow(Sheet sheet, int rowno, Object... cells) {
		Row r=sheet.createRow(rowno);
		int cellno=0;
		for (Object cell : cells) {
			Cell c = r.createCell(cellno++);
			if (cell instanceof String) c.setCellValue(((String)cell).toString());
			else if (cell instanceof Boolean) c.setCellValue(((Boolean)cell).booleanValue());
		}
	}

}
