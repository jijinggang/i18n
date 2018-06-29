//author:jjg
//time:2012-07-27
//单个Excel文件的翻译
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelTranslator implements ITranslate {

	public ExcelTranslator(LangMapping langMap) {
		_langMap = langMap;
	}

	@Override
	public boolean Check(String fileSrc) {
		parseFile(fileSrc, _langMap, false);
		return true;
	}

	@Override
	public boolean Translate(String fileDest) {
		parseFile(fileDest, _langMap, true);
		return true;
	}

	// private variables
	private LangMapping _langMap = null;
	private String _fileName = null;
	// private function
	private void parseFile(String file, LangMapping langMap, boolean bRewrite) {
		_fileName = file;
		Workbook wb = Util.OpenWorkbook(file);
		// 获取工作单数量
		int sheetCount = wb.getNumberOfSheets();
		int currRow = 0;
		try{
			for (int i = 0; i < sheetCount; i++) {
				currRow = i;
				Sheet sheet = wb.getSheetAt(i);
				parseSheet(sheet, langMap, bRewrite);
			}
		}catch (Exception e) {
			throw  new RuntimeException("ERR Read "+file+": row="+ currRow, e);
		}
		if(bRewrite)
			Util.SaveExcel(wb, file);

	}
    private final int MAXLEN = 10;
	private void parseSheet(Sheet sheet, LangMapping langMap, boolean bRewrite) {
		int rowNum = sheet.getLastRowNum();
		if (rowNum < 1)
			return;
		//检查前几行的excel
		int checkLen = Math.min(rowNum, MAXLEN);
		Row row = sheet.getRow(0);
		if(row == null) return;
		int cellNum = row.getLastCellNum();
		// 此处存在默认规则，即第一行中注释为I18N的列为需要国际化的列
		for (int j = row.getFirstCellNum(); j < cellNum; j++) {
			
			for (int i = 0; i < checkLen; i++)
			{
				Row Ri = sheet.getRow(i);
				Cell cell = Ri.getCell(j);
				if(cell == null) 
					continue;
				Comment comment = cell.getCellComment();
				if (comment != null) {
					String str = comment.getString().getString();
					if (str.contains("{I18N}")) {
						parseColumn(sheet, j, i+1, langMap, bRewrite);
						break; 
					}
				}
			}
		}
	}

	private void parseColumn(Sheet sheet, int j,int r, LangMapping langMap, boolean bRewrite) {
		int rowNum = sheet.getLastRowNum();
		for (int i = r; i <= rowNum; i++) {
			Row row = sheet.getRow(i);
			if(null == row)
				continue;
			Cell cell = row.getCell(j);
			if(null == cell || (cell.getCellType() != Cell.CELL_TYPE_STRING) )
				continue;
			String strSrc = cell.getStringCellValue();
			if (strSrc != null)
				strSrc = strSrc.trim();
			if (!strSrc.isEmpty()) {
				if (bRewrite) {
					String strDest = langMap.Find(strSrc);
					if (strDest != null && !strDest.isEmpty())
						cell.setCellValue(strDest);
				} 
				langMap.Add(strSrc, _fileName);
			}
		}
	}
}
