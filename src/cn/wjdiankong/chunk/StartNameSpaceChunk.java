package cn.wjdiankong.chunk;

import cn.wjdiankong.main.Utils;

public class StartNameSpaceChunk {

	public byte[] type = new byte[4];
	public byte[] size = new byte[4];
	public byte[] lineNumber = new byte[4];
	public byte[] unknown = new byte[4];
	public byte[] prefix = new byte[4];
	public byte[] uri = new byte[4];

	public static StartNameSpaceChunk createChunk(byte[] byteSrc){

		StartNameSpaceChunk chunk = new StartNameSpaceChunk();

		//解析type
		chunk.type = Utils.copyByte(byteSrc, 0, 4);
		
		//解析size
		chunk.size = Utils.copyByte(byteSrc, 4, 4);

		//解析行号
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);

		//解析unknown
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
		
		//解析prefix(这里需要注意的是行号后面的四个字节为FFFF,过滤)
		chunk.prefix = Utils.copyByte(byteSrc, 16, 4);

		//解析Uri
		chunk.uri = Utils.copyByte(byteSrc, 20, 4);

		return chunk;

	}

}
