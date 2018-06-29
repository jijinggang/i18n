//author:jjg
//time:2012-07-27
//properties文件翻译类
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.Properties;

public class PropsTranslator implements ITranslate {
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
/*	 public static String isoToUTF8(String value){
        String utf8=null;
        
        //String encode = System.getProperty("file.encoding");
		try {
			utf8 = new String(value.getBytes("ISO-8859-1"), "UTF-8");//
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return utf8;
     }
 */    
	// private function
	private void parseFile(String file, LangMapping langMap, boolean bRewrite) {
		try {
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();

			for (Entry<Object, Object> entry : prop.entrySet()) {
				String key = (String.valueOf(entry.getKey()));
				String value = (String.valueOf(entry.getValue()));
				langMap.Add(value,file);
				if (bRewrite) {
					String strDest = langMap.Find(value);
					if (strDest != null && !strDest.isEmpty())
						prop.setProperty(key, strDest);
				}
			}
			if (bRewrite)
				prop.store(new FileOutputStream(file), null);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public PropsTranslator(LangMapping langMap) {
		_langMap = langMap;
	}

}
