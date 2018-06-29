//author:jjg
//time:2012-07-27
//母语言到目标语言的映射管理
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class LangMapping {
	// 根据原配置表增加一个新字符串Key
	public void Add(String source,String remark) {
		if(source == null || source.trim().isEmpty())
			return;
		
		//转义
		//source = transStr(source);
		if(containsSpecial(source))
			remark += "; WARNNING!!! PLEASE USE \\r\\n\\t";
		
		if (!_map.containsKey(source)) {
			_map.put(source, null);
			_listUsed.add(new StrStr(source,remark));
		}
	}

	public String Find(String source) {
		source = source.trim();
		boolean bFind = _map.containsKey(source);
		if (bFind) {
			return _map.get(source);
		}
		return null;
	}
	
	//从配置文件读取
	private void reset(String source, String dest) {
		if(!_map.containsKey(source)){
			_listUnused.add(new StrStr(source,""));
		}
		_map.put(source,  dest);
	}
	// 从excel读取已有的翻译内容
	public void ReadFromExcel(String file) {
		Workbook wb = Util.OpenWorkbook(file);
		if (wb == null)
			return;
		// 获取工作单数量
		Sheet sheet = wb.getSheetAt(0);
		if (sheet == null)
			return;
		int rowNum = sheet.getLastRowNum();
		int currRow = 0;
		try{
			for (int i = 1; i <= rowNum; i++) {
				currRow = i;
				Row row = sheet.getRow(i);
				Cell cell = row.getCell(0);
				if(null == cell)
					continue;
				String strSrc = cell.getStringCellValue();
				if(null == strSrc)
					continue;
				strSrc = strSrc.trim();
				cell = row.getCell(1);
				if(cell == null)
					continue;
				
				int cellType = cell.getCellType();
				String strDest = null;
				if(cellType == 0) {	//数值型
					Double numDest = cell.getNumericCellValue();
					if(numDest != null) {
						strDest = numDest.intValue() + "";
					}
				} else {
					strDest = cell.getStringCellValue();
				}
				
				if(strDest != null )
					strDest = strDest.trim();
				if (!strSrc.isEmpty()  && strDest != null && !strDest.isEmpty())
					reset(strSrc, strDest);
			}
		}catch (Exception e) {
			throw  new RuntimeException("ERR Read i18n.xls: row="+ currRow, e);
		}
	}

	private static final short COLOR_NO_CHANGE = -1; 
	
	// 把新的映射信息写入到excel
	public void SaveToExcel(String xls) {
		String strExt = Util.GetFileExt(xls);
		Workbook book = null;
		if(strExt.equals("xlsx"))
			book = new XSSFWorkbook();
		else
			book = new HSSFWorkbook();
		Sheet sheet = book.createSheet("i18n");
		
		//设置宽度
		sheet.setColumnWidth(0, 60 * 256);
		sheet.setColumnWidth(1, 60 * 256);
		sheet.setColumnWidth(2, 60 * 256);
		
		//未翻译行
		List<StrStr> noList = new ArrayList<StrStr>();
		for(StrStr key : _listUsed) {
			String value = Find(key.first);
			if(value == null || value.isEmpty()) {
				noList.add(key);
			}
		}
		//已翻译行
		List<StrStr> finishList = new ArrayList<StrStr>();
		for(StrStr key : _listUsed) {
			String value = Find(key.first);
			if(value != null && !value.isEmpty()) {
				finishList.add(key);
			}
		}
		
		writeRow(book, sheet, 0, new StrStr("Source","Remark"), "Destination",HSSFColor.ROYAL_BLUE.index); //写入表头
		writeList(book, sheet, noList, 1, HSSFColor.SEA_GREEN.index);
		writeList(book, sheet, finishList, noList.size() + 1, COLOR_NO_CHANGE);
		writeList(book, sheet, _listUnused, noList.size() + finishList.size() + 1, HSSFColor.GREY_50_PERCENT.index);

		Util.SaveExcel(book, xls);
	}
	private void writeList(Workbook wb, Sheet sheet, List<StrStr> list, int startRowIndex, short color){
		int index = startRowIndex;
		for(StrStr key : list) {
			String value = Find(key.first);
			
			writeRow(wb, sheet, index, key, value, color);
			index++;
		}
	}
	private void writeRow(Workbook wb, Sheet sheet, int rowIndex, StrStr key, String value,short color){
		Row row = sheet.createRow(rowIndex);
		
		Cell cell = row.createCell(0);
		cell.setCellValue(key.first);
		if(color != COLOR_NO_CHANGE)
			setCellColor(cell, wb, color);

		cell = row.createCell(1);
		cell.setCellValue(value);
		if(color != COLOR_NO_CHANGE)
			setCellColor(cell, wb, color);
		
		cell = row.createCell(2);
		cell.setCellValue(key.second);
		if(color != COLOR_NO_CHANGE)
			setCellColor(cell, wb, color);
		
	}
	/*private void setCellColor(Cell cell,Workbook wb, short color){
		CellStyle style = wb.createCellStyle();
		style.setFillForegroundColor(color);
		short BORDER = 1;
		style.setBorderLeft(BORDER);
		style.setBorderRight(BORDER);
		style.setBorderTop(BORDER);
		style.setBorderBottom(BORDER);
		style.setLocked(true);

		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cell.setCellStyle(style);
	}*/
	private Map<Short, CellStyle> _mapStyle = new HashMap<Short, CellStyle>();
	private void setCellColor(Cell cell,Workbook wb, short color){
		CellStyle style = _mapStyle.get(color);
		if(null == style){
			style = wb.createCellStyle();
			style.setFillForegroundColor(color);
			short BORDER = 1;
			style.setBorderLeft(BORDER);
			style.setBorderRight(BORDER);
			style.setBorderTop(BORDER);
			style.setBorderBottom(BORDER);
			style.setLocked(true);
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			_mapStyle.put(color, style);
		}
		cell.setCellStyle(style);
	}

	// 判断是否全被翻译
	public boolean IsAllTranslated() {
		for (Entry<String, String> entry : _map.entrySet()) {
			String value = entry.getValue();
			if(null == value || value.isEmpty())
				return false;
		}		
		return true;
	}
	
	/**
	 * 对特殊字符进行转义
	 * @param src
	 * @return
	 */
	private String transStr(String src) {
		if(src == null) return null;
		
		return src.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}

	private static boolean containsSpecial(String str){
		if(str.contains("\n"))
			return true;
		if(str.contains("\r"))
			return true;
		if(str.contains("\t"))
			return true;
		return false;
	}
	// private
	public Map<String, String> _map = new HashMap<String, String>();
	private List<StrStr> _listUsed = new ArrayList<StrStr>();//保存字符串出现顺序，并记录哪些key目前有用
	private List<StrStr> _listUnused = new ArrayList<StrStr>();//记录哪些key目前已废弃

}
class StrStr{
	String first = null;
	String second = null;
	public StrStr(String first,String second){
		this.first = first;
		this.second = second;
	}
}
