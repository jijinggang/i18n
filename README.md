i18n工具,支持多种类型文件,把待翻译的文本提取成excel,翻译后导出到原始文件

## 用法

	java -jar i18n.jar SourceLang DestLang [-q] [-onlyconfig]

- SouceLang: 源语言包所在的目录，可以指定绝对路径或相对路径
- DestLang: 翻译后生成的目标语言目录，可以指定绝对路径或相对路径
- -q:此选项可选，如果加-q，则不论是否翻译完全，均生成所有新配置。此选项主要是为了与其他脚本集成
- -onlyconfig:此选项可选，如果加-onlyconfig，则源路径里面的所有非配置文件均不会复制到目标路径

例如

    java -jar i18n.jar CHS ENG
将把中文翻译为英文

    java -jar i18n.jar CHS ENG -q
将把中文翻译为英文，过程中无提示

## 支持的文件类型

- Excel(.xls/.xlsx)：需要包含需要翻译的列头（即列的第1行）注释中加入`{I18N}`，表示该列需要被翻译
- Java属性文件(.properties)
- 伪properties文件(.properties.txt)：标准的properties是不能包含中文的，类似
	`user.common.tail=\u4ed4\u7ec6\u5730\u6253\u91cf\u7740\u4f60`
   
	如果要在`key = value`中包含中文，则需要把后缀名改为.properties.txt
 
- XML(.xml):目前的处理方式时扫描整个xml，把其中包含中文字符的项提取出来。因此XML文件只支持中文到其他语言的翻
译
- 其他自定义类型：需要自己修改代码加入新的处理类型，实现`ITranslate`接口

##翻译步骤（以简体中文翻译英文为例）

### 1. 准备源语言文件
将某一种语言类型（一般为中文）的配置文件整理到`CHS`目录下

### . 生成待翻译表格
执行 `java -jar i18n.jar CHS ENG` ，会在`ENG`目录下生成`i18n.xls`文件

更新版本时，`ENG`目录已经存在`i18n.xls`，翻译工具会读取已有的翻译信息，填充到新生成的`i18n.xls`中，并把本次的差异标出

### 3. 翻译人员进行翻译
翻译人员翻译`i18n.xls`中的空白项或修改翻译内容

### 4. 生成目标语言
把翻译好的`i18n.xls`放到ENG目录下

再次执行 `java -jar i18n.jar CHS ENG`  
