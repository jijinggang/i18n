//author:jjg
//time:2012-07-27
//工具类
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Util {
	
	/**
	 * 打开工作薄
	 * 
	 * @param xls
	 * @return
	 */
	public static Workbook OpenWorkbook(String xls) {
		try {
			Workbook wb = null;
			String strExt = GetFileExt(xls);
			if(strExt.equals("xlsx"))
				wb = new XSSFWorkbook(new FileInputStream(new File(xls)));// (new POIFSFileSystem(new FileInputStream(xls)));
			else
				wb = new HSSFWorkbook(new FileInputStream(new File(xls)));
			// 获取工作薄
			//HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(xls)));
			return wb;
		} catch (Exception ex) {
			// 抛出异常
			return null;
			// throw new RuntimeException(ex);
		}
	}

	/**
	 * 获取文件的扩展名
	 * @param file
	 * @return
	 */
	public static String GetFileExt(String file){
			int iFind = file.lastIndexOf(".");
			if (iFind <= 0)
				return "";
			String strExt = file.substring(iFind + 1).toLowerCase();
			return strExt;
		}
 
	/**
	 * 保存工作薄
	 * 
	 * @param wb
	 * @param xls
	 * 
	 */
	public static void SaveExcel(Workbook wb, String xls) {
		try {
		
			OutputStream os = new FileOutputStream(xls);
			wb.write(os);
			// 刷新并关闭输出流
			os.flush();
			os.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	//根据文件返回路径
	public static String GetPath(String file){
		int pos = file.lastIndexOf("\\");
		if(pos < 0)
			return "";
		return file.substring(0, pos);
	}
	//复制文件
	public static void copyFile(String fileFromPath, String fileToPath) {
		InputStream in = null;
		OutputStream out = null;
		try{
			try {
				File file = new File(GetPath(fileToPath));
				if(!file.exists())
					file.mkdirs();
				in = new FileInputStream(fileFromPath);
				out = new FileOutputStream(fileToPath);
				byte[] temp = new byte[1024];
				while(true){
					int iRead = in.read(temp);
					if(iRead <= 0)
						break;
					out.write(temp, 0, iRead);
				}
			}
			finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
	public static List<String> GetDirFiles(String dir){
		List<String> files = new ArrayList<String>();
		traveDir(dir, files);
		return files;
		
	}
	private static void traveDir(String dir, List<String> files){
		File file = new File(dir);
		if(file.isHidden())
			return;
		if(file.isFile())
			files.add(dir);
		else if(file.isDirectory()){
			String[] childs = file.list();
			for(String strFile : childs){
				traveDir(dir+"\\"+strFile, files);
			}
		}
	}
}
