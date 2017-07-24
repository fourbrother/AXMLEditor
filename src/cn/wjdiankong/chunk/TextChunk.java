package cn.wjdiankong.chunk;

import cn.wjdiankong.main.Utils;

public class TextChunk {
	
	public byte[] type = new byte[4];
	public byte[] size = new byte[4];
	public byte[] lineNumber = new byte[4];
	public byte[] unknown = new byte[4];
	public byte[] name = new byte[4];
	public byte[] unknown1 = new byte[4];
	public byte[] unknown2 = new byte[4];
	
	public static TextChunk createChunk(byte[] byteSrc){
		
		TextChunk chunk = new TextChunk();
		
		chunk.type = Utils.copyByte(byteSrc, 0, 4);
		chunk.size = Utils.copyByte(byteSrc, 4, 4);
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
		chunk.name = Utils.copyByte(byteSrc, 16, 4);
		chunk.unknown1 = Utils.copyByte(byteSrc, 20, 4);
		chunk.unknown2 = Utils.copyByte(byteSrc, 24, 4);
		
		return chunk;
		
	}

}
