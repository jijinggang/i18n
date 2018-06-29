//author:jjg
//time:2012-07-27
//xml翻译管理类，注意目前的处理方式是遍历xml中存在的中文字符，把他作为key列出来，因此源语言只能为中文
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlTranslator implements ITranslate {
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
	//判断字符串是否包含中文
	private boolean isChinese(String str){
		if(str==null || str.isEmpty())
			return false;
		for(int i = 0; i < str.length(); i++){
			String c = str.substring(i, i+1);
			if(java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", c))
				return true;
		}
		return false;
	}
	private String checkReplaceStr(String str,LangMapping langMap){
		if(!isChinese(str))
			return null;
		langMap.Add(str, _fileName);
		String strDest = langMap.Find(str);
		if (strDest != null && !strDest.isEmpty())
			return strDest;
		else
			return null;
	}
	
	private void parseElement(Element elem, LangMapping langMap, boolean bRewrite){
		List<Attribute> attrs = elem.getAttributes();
		String text = elem.getText();
		String textNew = checkReplaceStr(text,langMap);
		if(bRewrite && null != textNew){
			elem.setText(textNew);
		}
		
		for(Attribute attr : attrs){
			String strValue = attr.getValue();
			String strNew = checkReplaceStr(strValue,langMap);
			if(bRewrite && null != strNew){
				attr.setValue(strNew);
			}
		}
		List<Element> childs = elem.getChildren();
		for(Element child : childs){
			parseElement(child, langMap, bRewrite);
		}
	}
	
	
	private void parseFile(String file, LangMapping langMap, boolean bRewrite) {
		try {
			_fileName = file;
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(file));
			Element elem = doc.getRootElement();
			parseElement(elem, langMap, bRewrite);
			if(bRewrite){
				XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
				out.output(doc, new FileOutputStream(file));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public XmlTranslator(LangMapping langMap) {
		_langMap = langMap;
	}

}
