package cn.wjdiankong.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
	
	private final static String CMD_TXT = "[usage java -jar AXMLEditor.jar [-tag|-attr] [-i|-r|-m] [标签名|标签唯一ID|属性名|属性值] [输入文件|输出文件]";

	public static void main(String[] args){

		/**
		 * 命令格式：
		 * -i 添加动作
		 * -r 删除动作
		 * -m 更新动作
		 * -attr 属性
		 * -tag 标签
		 * 属性操作直接输入参数即可，标签操作需要输入信息
		 */
		
		if(args.length < 3){
			System.out.println("参数有误...");
			System.out.println(CMD_TXT);
			return;
		}
		
		String inputfile = args[args.length-2];
		String outputfile = args[args.length-1];
		File inputFile = new File(inputfile);
		File outputFile = new File(outputfile);
		if(!inputFile.exists()){
			System.out.println("输入文件不存在...");
			return;
		}
		
		//读文件
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream(inputFile);
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len=fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
			ParserChunkUtils.xmlStruct.byteSrc = bos.toByteArray();
		}catch(Exception e){
			System.out.println("parse xml error:"+e.toString());
		}finally{
			try{
				fis.close();
				bos.close();
			}catch(Exception e){
			}
		}
		
		doCommand(args);
		
		//写文件
		if(!outputFile.exists()){
			outputFile.delete();
		}
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(outputFile);
			fos.write(ParserChunkUtils.xmlStruct.byteSrc);
			fos.close();
		}catch(Exception e){
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void testDemo(){
		//删除一个tag，删除tag时必须指定tag名称和name值，这样才能唯一确定一个tag信息
		//XmlEditor.removeTag("uses-permission", "android.permission.INTERNET");
		//XmlEditor.removeTag("activity", ".MainActivity");

		//删除属性，必须要指定属性对应的tag名称和name值，然后就是属性名称
		//XmlEditor.removeAttr("activity", ".MainActivity", "name");
		//XmlEditor.removeAttr("uses-permission", "android.permission.INTERNET", "name");

		//添加标签，直接在xml中配置即可，需要注意的是配置信息：manifest下面的标签必须在application标签的后面
		//XmlEditor.addTag();

		//添加属性，必须指定标签内容
		//XmlEditor.addAttr("activity", ".MainActivity", "jiangwei", "fourbrother");

		//更改属性，这里直接采用先删除，再添加策略完成
		//XmlEditor.modifyAttr("application", "package", "debuggable", "true");
	}

	public static void doCommand(String[] args){
		if("-tag".equals(args[0])){
			if(args.length < 2){
				System.out.println("缺少参数...");
				System.out.println(CMD_TXT);
				return;
			}
			//标签
			if("-i".equals(args[1])){
				if(args.length < 3){
					System.out.println("缺少参数...");
					System.out.println(CMD_TXT);
					return;
				}
				//插入操作
				String insertXml = args[2];
				File file = new File(insertXml);
				if(!file.exists()){
					System.out.println("插入标签xml文件不存在...");
					return;
				}
				XmlEditor.addTag(insertXml);
				System.out.println("插入标签完成...");
				return;
			}else if("-r".equals(args[1])){
				if(args.length < 4){
					System.out.println("缺少参数...");
					System.out.println(CMD_TXT);
					return;
				}
				//删除操作
				String tag = args[2];
				String tagName = args[3];
				XmlEditor.removeTag(tag, tagName);
				System.out.println("删除标签完成...");
				return;
			}else{
				System.out.println("操作标签参数有误...");
				System.out.println(CMD_TXT);
				return;
			}
		}else if("-attr".equals(args[0])){
			if(args.length < 2){
				System.out.println("缺少参数...");
				System.out.println(CMD_TXT);
				return;
			}
			//属性
			if("-i".equals(args[1])){
				if(args.length < 6){
					System.out.println("缺少参数...");
					System.out.println(CMD_TXT);
					return;
				}
				//插入属性
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				String value = args[5];
				XmlEditor.addAttr(tag, tagName, attr, value);
				System.out.println("插入属性完成...");
				return;
			}else if("-r".equals(args[1])){
				if(args.length < 5){
					System.out.println("缺少参数...");
					System.out.println(CMD_TXT);
					return;
				}
				//删除属性
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				XmlEditor.removeAttr(tag, tagName, attr);
				System.out.println("删除属性完成...");
				return;
			}else if("-m".equals(args[1])){
				if(args.length < 6){
					System.out.println("缺少参数...");
					System.out.println(CMD_TXT);
					return;
				}
				//修改属性
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				String value = args[5];
				XmlEditor.modifyAttr(tag, tagName, attr, value);
				System.out.println("修改属性完成...");
			}else{
				System.out.println("操作属性参数有误...");
				System.out.println(CMD_TXT);
				return;
			}
		}
	}
	
}
