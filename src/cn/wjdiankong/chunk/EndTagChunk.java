package cn.wjdiankong.chunk;

import cn.wjdiankong.main.ChunkTypeNumber;
import cn.wjdiankong.main.Utils;

public class EndTagChunk implements Chunk{
	
	public byte[] type = new byte[4];
	public byte[] size = new byte[4];
	public byte[] lineNumber = new byte[4];
	public byte[] unknown = new byte[4];
	public byte[] uri = new byte[4];
	public byte[] name = new byte[4];
	
	public int offset;
	public String tagValue;
	
	public EndTagChunk(){
		//初始化以下没有太大用途的字段
		type = Utils.int2Byte(ChunkTypeNumber.CHUNK_ENDTAG);
		size = Utils.int2Byte(24);
		lineNumber = new byte[4];
		unknown = new byte[4];
		uri = Utils.int2Byte(-1);		
	}
	
	public static EndTagChunk createChunk(int name){
		EndTagChunk chunk = new EndTagChunk();
		chunk.name = Utils.int2Byte(name);
		return chunk;
	}
	
	public byte[] getChunkByte(){
		byte[] bytes = new byte[getLen()];
		bytes = Utils.byteConcat(bytes, type, 0);
		bytes = Utils.byteConcat(bytes, size, 4);
		bytes = Utils.byteConcat(bytes, lineNumber, 8);
		bytes = Utils.byteConcat(bytes, unknown, 12);
		bytes = Utils.byteConcat(bytes, uri, 16);
		bytes = Utils.byteConcat(bytes, name, 20);
		return bytes;
	}
	
	public int getLen(){
		return type.length + size.length + lineNumber.length + unknown.length + uri.length + name.length;
	}
	
	public static EndTagChunk createChunk(byte[] byteSrc, int offset){
		
		EndTagChunk chunk = new EndTagChunk();
		
		chunk.offset = offset;
		
		//解析type
		chunk.type = Utils.copyByte(byteSrc, 0, 4);
		
		//解析size
		chunk.size = Utils.copyByte(byteSrc, 4, 4);
		
		//解析行号
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);

		//解析unknown
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);

		//解析Uri
		chunk.uri = Utils.copyByte(byteSrc, 16, 4);

		//解析TagName
		chunk.name = Utils.copyByte(byteSrc, 20, 4);
		
		return chunk;
	}

}
