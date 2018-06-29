//author:jjg
//time:2012-07-27
//翻译接口
public interface ITranslate {
	//遍历源语言文件
	boolean Check(String fileSrc);
	//生成目标语言文件
	boolean Translate(String fileDest);
}
