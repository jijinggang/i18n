//author:jjg
//time:2012-07-27
//properties文件翻译类
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

public class TextTranslator implements ITranslate {
	private LangMapping _langMap = null;
	
	/**
	 * 检查所有待翻译关键字
	 */
	@Override
	public boolean Check(String fileSrc) {
		parseFile(fileSrc, _langMap, false);
		return true;
	}

	/**
	 * 根据翻译文件生成新的资源文件
	 */
	@Override
	public boolean Translate(String fileDest) {
		parseFile(fileDest, _langMap, true);
		return true;
	}

	/**
	 * 检索翻译关键字 bRewrite等于false
	 * 根据翻译文件生成新资源文件 bRewrite等于true
	 * @param fileURI bRewrite等于false时传入为待翻译文件的URL；等于true时传入为目标文件的URL
	 * @param langMap 源文字与翻译文字的键值对
	 * @param bRewrite 检索模式false 翻译模式true
	 */
	private void parseFile(String fileURI, LangMapping langMap, boolean bRewrite) {
		try {
			File file = new File(fileURI);
			Scanner scan = new Scanner(new File(fileURI), "UTF-8");
			
			//记录所有的KEY
			Map<String, String> map = new LinkedHashMap<String, String>();
			
			//将所有KEY再次记录一次 避免丢失
			while(scan.hasNextLine()) {
				String strLine = scan.nextLine();
				//忽略注释行
				if(strLine.startsWith("#"))
					continue;
				String[] items = strLine.split("=", 2);
				if(items.length < 2) 
					continue;
				
				langMap.Add(items[1], fileURI);
				//记录键值
				map.put(items[0], items[1]);
			}
			
			//关闭
			scan.close();
			
			//生成目标文件
			if(bRewrite) {
				//创建数据流并清空文件
				FileOutputStream fileOS = new FileOutputStream(file, false);
				Writer writer = new OutputStreamWriter(fileOS, "UTF-8");
				for(Entry<String, String> entry : map.entrySet()) {
					String strValue = entry.getValue();
					String strTrans = langMap.Find(strValue);
					if(strTrans == null) //未翻译的先用原始语言代替
						strTrans = strValue;
					writer.write(entry.getKey() + "=" + strTrans + "\r\n");
				}
				writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TextTranslator(LangMapping langMap) {
		_langMap = langMap;
	}
}
