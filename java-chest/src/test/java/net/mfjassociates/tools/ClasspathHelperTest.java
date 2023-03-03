package net.mfjassociates.tools;

import static net.mfjassociates.tools.ClasspathHelper.DEFAULT_WORKBOOK_FILE;
import static net.mfjassociates.tools.ClasspathHelper.openWorkbook;
import static net.mfjassociates.tools.ClasspathHelper.storeClasspathToExcel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

public class ClasspathHelperTest {

	private static final String JUNIT_SHEET_NAME = "JUNIT-TEST";
	@Test
	public void testStoreClasspathToExcel() throws IOException {
		storeClasspathToExcel(JUNIT_SHEET_NAME);
		assertTrue( DEFAULT_WORKBOOK_FILE.exists(), DEFAULT_WORKBOOK_FILE.getAbsolutePath()+" workbook does not exist");
		Workbook wb=openWorkbook(DEFAULT_WORKBOOK_FILE);
		assertNotNull(wb.getSheet(JUNIT_SHEET_NAME), "worksheet "+JUNIT_SHEET_NAME+" does not exist");
		assertEquals(wb.getSheet(JUNIT_SHEET_NAME).getRow(0).getCell(0).getStringCellValue(), "Name", "first cell is incorrect");
	}
	
	@Test
	public void testDuplicateSheet() throws IOException {
		storeClasspathToExcel(JUNIT_SHEET_NAME);
		storeClasspathToExcel(JUNIT_SHEET_NAME);
	}

}
