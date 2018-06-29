import java.util.ArrayList;
import java.util.List;

//主控程序
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if(args.length < 2){
				System.out.println("使用方式 java -jar i18n.jar SourcePath DestPath [-q] [-onlyconfig]");
				return;
			}
			//String strPath =  "F:\\10.12.2.60\\labs\\jijinggang\\i18n\\test\\";
			//transDir(strPath + "CHS", strPath + "ENG");
			System.out.println(args[0] + " --> " + args[1]);
			List<String> argOpts = new ArrayList<String>();
			for(int i = 2; i < args.length; i++){
				argOpts.add(args[i]);
			}
			boolean quiet = false;
			if(argOpts.contains("-q")) //quiet
				quiet = true;
			boolean onlyConfig = false;
			if(argOpts.contains("-onlyconfig")) //only copy config files, ignore other files, don't copy to dest folder
				onlyConfig = true;
			transDir(args[0],args[1],quiet, onlyConfig);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void transDir(String dirSrc, String dirDest,boolean quiet, boolean onlyConfig) throws Exception {
		LangMapping langMap = new LangMapping();
		String mapFile = dirDest + "\\i18n.xls";
		List<String> files = Util.GetDirFiles(dirSrc);

		// 遍历源文件
		for (String file : files) {
			System.out.println("Check: " + file);
			checkOrTransFile(file, langMap, false);
		}
		langMap.ReadFromExcel(mapFile);
		langMap.SaveToExcel(mapFile);// 生成新的i18n

		if (!quiet && !langMap.IsAllTranslated()) {
			System.out.println("没有翻译完全，翻译表格文件为:\n" + mapFile + "\n" +
					"\n是否强行翻译配置文件(y/n)？");
			int c = System.in.read();
			if (c != 'y') {
				return;
			}
		}

		// 生成翻译文件
		for (String fileOrg : files) {
			String fileName = fileOrg.substring(dirSrc.length());
			String file = dirDest + fileName;
			if(onlyConfig){
				String strExt = Util.GetFileExt(file);
				if (!strExt.equals("xls") && !strExt.equals("xlsx") && !strExt.equals("properties") && !strExt.equals("xml")
						&& !file.toLowerCase().endsWith(".properties.txt"))
					continue;
			}
			Util.copyFile(fileOrg, file);
			System.out.println("Generate: " + file);
			checkOrTransFile(file, langMap, true);
		}
		System.out.println("处理完毕！");
	}
	private static void checkOrTransFile(String file, LangMapping langMap, boolean bTrans) {
		try {
			String strExt = Util.GetFileExt(file);
			ITranslate trans = null;
			
			//这个是英豪客户端的文件 特殊处理一下 虽然是properties文件 但是当成文本处理
			if(file.contains("pwyh_CN") && strExt.equals("properties"))
				trans = new TextTranslator(langMap);
			else if (strExt.equals("xls") || strExt.equals("xlsx"))
				trans = new ExcelTranslator(langMap);
			else if (strExt.equals("properties"))
				trans = new PropsTranslator(langMap);
			else if (strExt.equals("xml"))
				trans = new XmlTranslator(langMap);
			else if (file.toLowerCase().endsWith(".properties.txt")){
				trans = new TextTranslator(langMap);
			}
			else
				return;
			if (bTrans)
				trans.Translate(file);
			else
				trans.Check(file);
		} catch (Exception e) {
			throw new RuntimeException("处理文件发生错误：" + file, e);
		}
	}
}
