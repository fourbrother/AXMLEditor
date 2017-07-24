package cn.wjdiankong.chunk;

import java.util.ArrayList;

import cn.wjdiankong.main.ChunkTypeNumber;
import cn.wjdiankong.main.Utils;

public class StartTagChunk implements Chunk{

	public byte[] type;
	public byte[] size;
	public byte[] lineNumber;
	public byte[] unknown;
	public byte[] uri;
	public byte[] name;
	public byte[] flag;
	public byte[] attCount;
	public byte[] classAttr;
	public byte[] attribute;
	public ArrayList<AttributeData> attrList;
	
	public int offset;
	
	public StartTagChunk(){
		//初始化以下没有太大用途的字段
		type = Utils.int2Byte(ChunkTypeNumber.CHUNK_STARTTAG);
		lineNumber = new byte[4];
		unknown = new byte[4];
		flag = new byte[4];
		classAttr = new byte[4];
		
		//flag必须为0x00140014
		int flatInt = 0;
		flatInt = flatInt | 0x00140014;
		flag = Utils.int2Byte(flatInt);
	}
	
	public byte[] getChunkByte(){
		byte[] bytes = new byte[getLen()];
		bytes = Utils.byteConcat(bytes, type, 0);
		bytes = Utils.byteConcat(bytes, size, 4);
		bytes = Utils.byteConcat(bytes, lineNumber, 8);
		bytes = Utils.byteConcat(bytes, unknown, 12);
		bytes = Utils.byteConcat(bytes, uri, 16);
		bytes = Utils.byteConcat(bytes, name, 20);
		bytes = Utils.byteConcat(bytes, flag, 24);
		bytes = Utils.byteConcat(bytes, attCount, 28);
		bytes = Utils.byteConcat(bytes, classAttr, 32);
		bytes = Utils.byteConcat(bytes, attribute, 36);
		return bytes;
	}
	
	public int getLen(){
		return type.length+size.length+lineNumber.length+unknown.length+uri.length+name.length+flag.length+attCount.length+classAttr.length+attribute.length;
	}
	
	public static StartTagChunk createChunk(int name, int attCount, int uri, byte[] attribute){
		StartTagChunk chunk = new StartTagChunk();
		chunk.size = new byte[4];
		chunk.name = Utils.int2Byte(name);
		chunk.uri = Utils.int2Byte(uri);
		chunk.attCount = Utils.int2Byte(attCount);
		chunk.attribute = attribute;
		chunk.size = Utils.int2Byte(chunk.getLen());
		return chunk;
	}
	
	public static StartTagChunk createChunk(byte[] byteSrc, int offset){
		StartTagChunk chunk = new StartTagChunk();
		
		chunk.offset = offset;
		
		//解析ChunkTag
		chunk.type = Utils.copyByte(byteSrc, 0, 4);

		//解析ChunkSize
		chunk.size = Utils.copyByte(byteSrc, 4, 4);

		//解析行号
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);

		//解析unknown
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);

		//解析Uri
		chunk.uri = Utils.copyByte(byteSrc, 16, 4);

		//解析TagName
		chunk.name = Utils.copyByte(byteSrc, 20, 4);
		
		//解析flag
		chunk.flag = Utils.copyByte(byteSrc, 24, 4);

		//解析属性个数(这里需要过滤四个字节:14001400)
		chunk.attCount = Utils.copyByte(byteSrc, 28, 4);
		int attrCount = Utils.byte2int(chunk.attCount);
		
		chunk.classAttr = Utils.copyByte(byteSrc, 32, 4);
		chunk.attribute = Utils.copyByte(byteSrc, 36, attrCount*20);
		
		chunk.attrList = new ArrayList<AttributeData>(attrCount);
		
		//解析属性
		//这里需要注意的是每个属性单元都是由五个元素组成，每个元素占用四个字节：namespaceuri, name, valuestring, type, data
		//在获取到type值的时候需要右移24位
		for(int i=0;i<attrCount;i++){
			Integer[] values = new Integer[5];
			AttributeData attrData = new AttributeData();
			for(int j=0;j<5;j++){
				int value = Utils.byte2int(Utils.copyByte(byteSrc, 36+i*20+j*4, 4));
				attrData.offset = offset + 36 + i*20;
				switch(j){
				case 0:
					attrData.nameSpaceUri = value;
					break;
				case 1:
					attrData.name = value;
					break;
				case 2:
					attrData.valueString = value;
					break;
				case 3:
					value = (value >> 24);
					attrData.type = value;
					break;
				case 4:
					attrData.data = value;
					break;
				}
				values[j] = value;
			}
			chunk.attrList.add(attrData);
		}
		
		return chunk;
	}

}
