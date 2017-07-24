package cn.wjdiankong.main;

import java.io.FileInputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import cn.wjdiankong.chunk.AttributeData;
import cn.wjdiankong.chunk.EndTagChunk;
import cn.wjdiankong.chunk.StartTagChunk;
import cn.wjdiankong.chunk.StringChunk;
import cn.wjdiankong.chunk.TagChunk;

public class XmlEditor {
	
	public static int tagStartChunkOffset = 0, tagEndChunkOffset;
	public static int subAppTagChunkOffset = 0;
	public static int subTagChunkOffsets = 0;
	
	public static String[] isNotAppTag = new String[]{
			"uses-permission", "uses-sdk", "compatible-screens", "instrumentation", "library",
			"original-package", "package-verifier", "permission", "permission-group", "permission-tree",
			"protected-broadcast", "resource-overlay", "supports-input", "supports-screens", "upgrade-key-set",
			"uses-configuration", "uses-feature"};
	
	public static String prefixStr = "http://schemas.android.com/apk/res/android";
	
	/**
	 * 删除标签内容
	 * @param tagName
	 * @param name
	 */
	public static void removeTag(String tagName, String name){
		ParserChunkUtils.parserXml();
		for(TagChunk tag : ParserChunkUtils.xmlStruct.tagChunkList){
			int tagNameIndex = Utils.byte2int(tag.startTagChunk.name);
			String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
			if(tagName.equals(tagNameTmp)){
				for(AttributeData attrData : tag.startTagChunk.attrList){
					String attrName = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
					if("name".equals(attrName)){
						String value = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.valueString);
						if(name.equals(value)){
							//找到指定的tag开始删除
							int size = Utils.byte2int(tag.endTagChunk.size);
							int delStart = tag.startTagChunk.offset;
							int delSize = (tag.endTagChunk.offset - tag.startTagChunk.offset) + size;
							ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart, delSize);
							
							modifyFileSize();
							return;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 添加标签内容 
	 */
	public static void addTag(String insertXml){
		ParserChunkUtils.parserXml();
		try {
	        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();  
	        XmlPullParser pullParser = pullParserFactory.newPullParser();  
	        pullParser.setInput(new FileInputStream(insertXml), "UTF-8");  
	        int event = pullParser.getEventType();  
	        // 若为解析到末尾  
	        while (event != XmlPullParser.END_DOCUMENT){ // 文档结束 
	            // 节点名称  
	            switch (event) {  
	            
	                case XmlPullParser.START_DOCUMENT: // 文档开始  
	                    break;  
	                    
	                case XmlPullParser.START_TAG: // 标签开始 
	                	String tagName = pullParser.getName();
	                	int name = getStrIndex(tagName);
	                	int attCount = pullParser.getAttributeCount();
	                	byte[] attribute = new byte[20*attCount];
	                	for(int i=0;i<pullParser.getAttributeCount();i++){
	                		int attruri = getStrIndex(prefixStr);
	                		//这里需要对属性名做分离
	                		String attrName = pullParser.getAttributeName(i);
	                		String[] strAry = attrName.split(":");
	                		int[] type = getAttrType(pullParser.getAttributeValue(i));
	                		int attrname = getStrIndex(strAry[1]);
	                		int attrvalue = getStrIndex(pullParser.getAttributeValue(i));
	                		int attrtype = type[0];
	                		int attrdata = type[1];
	                		AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
	                		attribute = Utils.byteConcat(attribute, data.getByte(), data.getLen()*i);
	                	}
	                	
	                	StartTagChunk startChunk = StartTagChunk.createChunk(name, attCount, -1, attribute);
	                	//构造一个新的chunk之后，开始写入
	                	if(isNotAppTag(tagName)){
	            			ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subTagChunkOffsets, startChunk.getChunkByte());
	            			subTagChunkOffsets += startChunk.getChunkByte().length;
	                	}else{
	                		ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subAppTagChunkOffset, startChunk.getChunkByte());
	            			subAppTagChunkOffset += startChunk.getChunkByte().length;
	                	}
	                    break;  
	                    
	                case XmlPullParser.END_TAG: // 标签结束  
	                	tagName = pullParser.getName();
	                	name = getStrIndex(tagName);
	                	EndTagChunk endChunk = EndTagChunk.createChunk(name);
	                	if(isNotAppTag(tagName)){
	            			ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subTagChunkOffsets, endChunk.getChunkByte());
	            			subTagChunkOffsets += endChunk.getChunkByte().length;
	                	}else{
	                		ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, subAppTagChunkOffset, endChunk.getChunkByte());
	            			subAppTagChunkOffset += endChunk.getChunkByte().length;
	                	}
	                    break;  
	                    
	            }
	            event = pullParser.next(); // 下一个标签  
	        }  
		} catch (XmlPullParserException e) {
			System.out.println("parse xml err:"+e.toString());
		} catch (IOException e){
			System.out.println("parse xml err:"+e.toString());
		}
		
		modifStringChunk();
		
		modifyFileSize();
		
	}
	
	/**
	 * 删除属性
	 * @param tag
	 * @param tagName
	 * @param attrName
	 */
	public static void removeAttr(String tag, String tagName, String attrName){
		ParserChunkUtils.parserXml();
		for(StartTagChunk chunk : ParserChunkUtils.xmlStruct.startTagChunkList){
			int tagNameIndex = Utils.byte2int(chunk.name);
			String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
			
			if(tag.equals(tagNameTmp)){
				
				//如果是application，manifest标签直接处理就好
				if(tag.equals("application") || tag.equals("manifest")){
					for(AttributeData data : chunk.attrList){
						String attrNameTemp1 = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
						if(attrName.equals(attrNameTemp1)){
							//如果找到对应的标签，发现只有一个属性值，并且删除成功，同时还得把这个标签给删除了
							if(chunk.attrList.size() == 1){
								removeTag(tag, tagName);
								return ;
							}
							//还得修改对应的tag chunk中属性个个数和大小
							int countStart = chunk.offset + 28;
							byte[] modifyByte = Utils.int2Byte(chunk.attrList.size()-1);
							ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
							
							//修改chunk的大小
							int chunkSizeStart = chunk.offset + 4;
							int chunkSize = Utils.byte2int(chunk.size);
							byte[] modifyByteSize = Utils.int2Byte(chunkSize-20);//一个属性块是20个字节
							ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
							
							//删除属性内容
							int delStart = data.offset;
							int delSize = data.getLen();
							ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart, delSize);
							
							modifyFileSize();
							return;
						}
					}
				}
				
				//否则需要通过name找到指定的tag
				for(AttributeData attrData : chunk.attrList){
					String attrNameTemp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
					if("name".equals(attrNameTemp)){//得先找到tag对应的唯一名称
						String value = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.valueString);
						if(tagName.equals(value)){
							for(AttributeData data : chunk.attrList){
								String attrNameTemp1 = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data.name);
								if(attrName.equals(attrNameTemp1)){
									
									//如果找到对应的标签，发现只有一个属性值，并且删除成功，同时还得把这个标签给删除了
									if(chunk.attrList.size() == 1){
										removeTag(tag, tagName);
										return ;
									}
									
									//还得修改对应的tag chunk中属性个个数和大小
									int countStart = chunk.offset + 28;
									byte[] modifyByte = Utils.int2Byte(chunk.attrList.size()-1);
									ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
									
									//修改chunk的大小
									int chunkSizeStart = chunk.offset + 4;
									int chunkSize = Utils.byte2int(chunk.size);
									byte[] modifyByteSize = Utils.int2Byte(chunkSize-20);
									ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
									
									//删除属性内容
									int delStart = data.offset;
									int delSize = data.getLen();
									ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, delStart, delSize);
									
									modifyFileSize();
									return ;
									
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 更改属性值
	 * @param tag
	 * @param tagName
	 * @param attrName
	 * @param attrValue
	 */
	public static void modifyAttr(String tag, String tagName, String attrName, String attrValue){
		ParserChunkUtils.parserXml();
		XmlEditor.removeAttr(tag, tagName, attrName);
		ParserChunkUtils.parserXml();
		XmlEditor.addAttr(tag, tagName, attrName, attrValue);
	}
	
	/**
	 * 添加属性值
	 * @param tag
	 * @param tagName
	 * @param attrName
	 * @param attrValue
	 */
	public static void addAttr(String tag, String tagName, String attrName, String attrValue){
		ParserChunkUtils.parserXml();
		//构造一个属性出来
		int[] type = getAttrType(attrValue);
		int attrname = getStrIndex(attrName);
		int attrvalue = getStrIndex(attrValue);
		int attruri = getStrIndex(prefixStr);;
		int attrtype = type[0];//属性类型
		int attrdata = type[1];//属性值，是int类型
		
		AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
		
		for(StartTagChunk chunk : ParserChunkUtils.xmlStruct.startTagChunkList){
			
			int tagNameIndex = Utils.byte2int(chunk.name);
			String tagNameTmp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(tagNameIndex);
			if(tag.equals(tagNameTmp)){
				
				//如果是application，manifest标签直接处理就好
				if(tag.equals("application") || tag.equals("manifest")){
					//还得修改对应的tag chunk中属性个个数和大小
					int countStart = chunk.offset + 28;
					byte[] modifyByte = Utils.int2Byte(chunk.attrList.size()+1);
					ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
					
					//修改chunk的大小
					int chunkSizeStart = chunk.offset + 4;
					int chunkSize = Utils.byte2int(chunk.size);
					byte[] modifyByteSize = Utils.int2Byte(chunkSize+20);
					ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
					
					//添加属性内容到原来的chunk上
					ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, chunk.offset + chunkSize, data.getByte());
					
					modifStringChunk();
					
					modifyFileSize();
					
					return;
				}
				
				for(AttributeData attrData : chunk.attrList){
					String attrNameTemp = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(attrData.name);
					if("name".equals(attrNameTemp)){//得先找到tag对应的唯一名称
						
						//还得修改对应的tag chunk中属性个个数和大小
						int countStart = chunk.offset + 28;
						byte[] modifyByte = Utils.int2Byte(chunk.attrList.size()+1);
						ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByte, countStart);
						
						//修改chunk的大小
						int chunkSizeStart = chunk.offset + 4;
						int chunkSize = Utils.byte2int(chunk.size);
						byte[] modifyByteSize = Utils.int2Byte(chunkSize+20);
						ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, modifyByteSize, chunkSizeStart);
						
						//添加属性内容到原来的chunk上
						ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, chunk.offset + chunkSize, data.getByte());
						
						modifStringChunk();
						
						modifyFileSize();
						
						return;
					}
				}
			}
		}
		
	}
	
	/**
	 * 重新插入String Chunk内容块
	 */
	private static void modifStringChunk(){
		//写入StartTagChunk chunk之前，因为有字符串信息增加，所以得修改字符串内容
		StringChunk strChunk = ParserChunkUtils.xmlStruct.stringChunk;
		byte[] newStrChunkB = strChunk.getByte(ParserChunkUtils.xmlStruct.stringChunk.stringContentList);
		//删除原始String Chunk
		ParserChunkUtils.xmlStruct.byteSrc = Utils.removeByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, Utils.byte2int(strChunk.size));
		//插入新的String Chunk
		ParserChunkUtils.xmlStruct.byteSrc = Utils.insertByte(ParserChunkUtils.xmlStruct.byteSrc, ParserChunkUtils.stringChunkOffset, newStrChunkB);
	}
	
	/**
	 * 修改文件最终的大小
	 */
	public static void modifyFileSize(){
		byte[] newFileSize = Utils.int2Byte(ParserChunkUtils.xmlStruct.byteSrc.length);
		ParserChunkUtils.xmlStruct.byteSrc = Utils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, newFileSize, 4);
	}
	
	/**
	 * 获取字符串的索引值，如果字符串存在直接返回，不存在就放到末尾返回对应的索引值
	 * @param str
	 * @return
	 */
	public static int getStrIndex(String str){
		if(str == null || str.length() == 0){
			return -1;
		}
		for(int i=0; i<ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size(); i++){
			if(ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(i).equals(str)){
				return i;
			}
		}
		ParserChunkUtils.xmlStruct.stringChunk.stringContentList.add(str);
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size()-1;
	}
	
	/**
	 * 判断是否是application外部的标签，application的内部和外部标签需要区分对待
	 * @param tagName
	 * @return
	 */
	public static boolean isNotAppTag(String tagName){
		for(String str : isNotAppTag){
			if(str.equals(tagName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取属性对应类型值
	 * @param tagValue
	 * @return
	 */
	public static int[] getAttrType(String tagValue){
		
		int[] result = new int[2];
		
		if(tagValue.equals("true") || tagValue.equals("false")){//boolean
			result[0] = result[0] | AttributeType.ATTR_BOOLEAN;
			if(tagValue.equals("true")){
				result[1] = 1;
			}else{
				result[1] = 0;
			}
		}else if(tagValue.equals("singleTask") || tagValue.equals("standard") 
				|| tagValue.equals("singleTop") || tagValue.equals("singleInstance")){//启动模式int类型
			result[0] = result[0] | AttributeType.ATTR_FIRSTINT;
			if(tagValue.equals("standard")){
				result[1] = 0;
			}else if(tagValue.equals("singleTop")){
				result[1] = 1;
			}else if(tagValue.equals("singleTask")){
				result[1] = 2;
			}else{
				result[1] = 3;
			}
		}else if(tagValue.equals("minSdkVersion") || tagValue.equals("versionCode")){
			result[0] = result[0] | AttributeType.ATTR_FIRSTINT;
			result[1] = Integer.valueOf(tagValue);
		}else if(tagValue.startsWith("@")){//引用
			result[0] = result[0] | AttributeType.ATTR_REFERENCE;
			result[1] = 0x7F000000;
		}else if(tagValue.startsWith("#")){//色值
			result[0] = result[0] | AttributeType.ATTR_ARGB4;
			result[1] = 0xFFFFFFFF;
		}else{//字符串
			result[0] = result[0] | AttributeType.ATTR_STRING;
			result[1] = getStrIndex(tagValue);
		}
		
		result[0] = result[0] | 0x08000000;
		result[0] = Utils.byte2int(Utils.reverseBytes(Utils.int2Byte(result[0])));//字节需要翻转一次
		
		return result;
	}
	
}
