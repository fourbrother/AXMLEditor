package cn.wjdiankong.chunk;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import cn.wjdiankong.main.ParserChunkUtils;
import cn.wjdiankong.main.Utils;

public class StringChunk {

	public byte[] type;
	public byte[] size;
	public byte[] strCount;
	public byte[] styleCount;
	public byte[] unknown;
	public byte[] strPoolOffset;
	public byte[] stylePoolOffset;
	public byte[] strOffsets;
	public byte[] styleOffsets;
	public byte[] strPool;
	public byte[] stylePool;

	public ArrayList<String> stringContentList;
	
	public byte[] getByte(ArrayList<String> strList){
		
		byte[] strB = getStrListByte(strList);
		
		byte[] src = new byte[0];
		
		src = Utils.addByte(src, type);
		src = Utils.addByte(src, size);
		src = Utils.addByte(src, Utils.int2Byte(strList.size()));//�ַ�����
		src = Utils.addByte(src, styleCount);
		src = Utils.addByte(src, unknown);
		src = Utils.addByte(src, strPoolOffset);
		src = Utils.addByte(src, stylePoolOffset);
		
		byte[] strOffsets = new byte[0];

		int len = 0;
		for(int i=0;i<strList.size();i++){
			strOffsets = Utils.addByte(strOffsets, Utils.int2Byte(len));
			len += (strList.get(i).length() * 2 + 4);//�����4�����ַ���ͷ�����ַ�������2���ֽڣ����ַ�����β��2���ֽ�
		}
		
		src = Utils.addByte(src, strOffsets);//д��string offsetsֵ
		
		int newStyleOffsets = src.length;//д��strOffsets֮�����styleOffsets��ֵ
		
		src = Utils.addByte(src, styleOffsets);//д��style offsetsֵ
		
		int newStringPools = src.length;
		
		src = Utils.addByte(src, strB);//д��string pools
		
		src = Utils.addByte(src, stylePool); //д��style pools
		
		//��ΪstrOffsets��С�ĸı䣬�����styleOffsetsҲ��Ҫ�䶯
		if(styleOffsets != null && styleOffsets.length > 0){
			//ֻ��style��Ч����д��
			src = Utils.replaceBytes(src, Utils.int2Byte(newStyleOffsets), 28+strList.size()*4);
		}
		
		//��ΪstrOffsets��С�ı䣬�����strPoolOffsets��stylePoolOffsetҲҪ�䶯
		src = Utils.replaceBytes(src, Utils.int2Byte(newStringPools), 20);//�޸�strPoolOffsetsƫ��ֵ
		
		//����String Chunk�Ĵ�С������4�ı�����������ǲ��룬��ΪChunkһ����2�ı���������ֻ��Ҫ����2���ֽڼ���
		if(src.length %4 != 0){
			src = Utils.addByte(src, new byte[]{0,0});
		}
		
		//�޸�chunk���յĴ�С
		src = Utils.replaceBytes(src, Utils.int2Byte(src.length), 4);
		
		return src;
	}
	
	public int getLen(){
		return type.length+size.length+strCount.length+styleCount.length+
				unknown.length+strPoolOffset.length+stylePoolOffset.length+
				strOffsets.length+styleOffsets.length+strPool.length+stylePool.length;
	}
	
	public static StringChunk createChunk(byte[] byteSrc, int stringChunkOffset){

		StringChunk chunk = new StringChunk();

		//String Chunk�ı�ʾ
		chunk.type = Utils.copyByte(byteSrc, 0+stringChunkOffset, 4);

		//String Size
		chunk.size = Utils.copyByte(byteSrc, 4+stringChunkOffset, 4);
		int chunkSize = Utils.byte2int(chunk.size);

		//String Count
		chunk.strCount = Utils.copyByte(byteSrc, 8+stringChunkOffset, 4);
		int chunkStringCount = Utils.byte2int(chunk.strCount);

		chunk.stringContentList = new ArrayList<String>(chunkStringCount);

		//Style Count
		chunk.styleCount = Utils.copyByte(byteSrc, 12+stringChunkOffset, 4);
		int chunkStyleCount = Utils.byte2int(chunk.styleCount);

		//unknown
		chunk.unknown = Utils.copyByte(byteSrc, 16+stringChunkOffset, 4);

		//������Ҫע����ǣ�������ĸ��ֽ���Style�����ݣ�Ȼ������ŵ��ĸ��ֽ�ʼ����0������������Ҫֱ�ӹ�����8���ֽ�
		//String Offset �����String Chunk����ʼλ��0x00000008
		chunk.strPoolOffset = Utils.copyByte(byteSrc, 20+stringChunkOffset, 4);

		//Style Offset
		chunk.stylePoolOffset = Utils.copyByte(byteSrc, 24+stringChunkOffset, 4);

		//String Offsets
		chunk.strOffsets = Utils.copyByte(byteSrc, 28+stringChunkOffset, 4*chunkStringCount);

		//Style Offsets
		chunk.styleOffsets = Utils.copyByte(byteSrc, 28+stringChunkOffset+4*chunkStringCount, 4*chunkStyleCount);
		
		int stringContentStart = 8 + Utils.byte2int(chunk.strPoolOffset);

		//String Content
		byte[] chunkStringContentByte = Utils.copyByte(byteSrc, stringContentStart, chunkSize);

		/**
		 * �ڽ����ַ�����ʱ���и����⣬���Ǳ��룺UTF-8��UTF-16,�����UTF-8�Ļ�����00��β�ģ������UTF-16�Ļ���00 00��β��
		 */
		//����ĸ�ʽ�ǣ�ƫ��ֵ��ʼ�������ֽ����ַ����ĳ��ȣ��������ַ��������ݣ�������������ַ����Ľ�����00
		byte[] firstStringSizeByte = Utils.copyByte(chunkStringContentByte, 0, 2);
		//һ���ַ���Ӧ�����ֽ�
		int firstStringSize = Utils.byte2Short(firstStringSizeByte)*2;
		byte[] firstStringContentByte = Utils.copyByte(chunkStringContentByte, 2, firstStringSize+2);
		
		String firstStringContent = new String(firstStringContentByte);
		
		chunk.stringContentList.add(Utils.filterStringNull(firstStringContent));
		//���ַ������ŵ�ArrayList��
		int endStringIndex = 2+firstStringSize+2;
		while(chunk.stringContentList.size() < chunkStringCount){
			//һ���ַ���Ӧ�����ֽڣ�����Ҫ����2
			int stringSize = Utils.byte2Short(Utils.copyByte(chunkStringContentByte, endStringIndex, 2))*2;
			String str;
			if (stringSize == 0) {
				// ���ַ���
				str = "";
			}else{
				byte[] temp = Utils.copyByte(chunkStringContentByte, endStringIndex + 2, stringSize);
				str = new String(temp, StandardCharsets.UTF_16LE);
			}
			chunk.stringContentList.add(Utils.filterStringNull(str));
			endStringIndex += (2+stringSize+2);
		}
		// �����������λ00
		endStringIndex += 2;
		
		int len = 0;
		for(String str : chunk.stringContentList){
			len += 2;
			len += str.length()*2;
			len += 2;
		}
		chunk.strPool = Utils.copyByte(byteSrc, stringContentStart, endStringIndex);
		int stylePool = stringContentStart + endStringIndex;
		
		chunk.stylePool = Utils.copyByte(byteSrc, stylePool, chunkSize-(stylePool));
		
		return chunk;
	}
	
	private byte[] getStrListByte(ArrayList<String> strList){
		byte[] src = new byte[0];
		for(int i=0;i<strList.size();i++){
			String str = strList.get(i);
			byte[] tempAry = new byte[0];
			short len = (short) str.length();
			byte[] lenAry = Utils.shortToByte(len);
			tempAry = Utils.addByte(tempAry, lenAry);
			tempAry = Utils.addByte(tempAry, str.getBytes(StandardCharsets.UTF_16LE));
			tempAry = Utils.addByte(tempAry, new byte[]{0, 0});
			src = Utils.addByte(src, tempAry);
		}
		src = Utils.addByte(src, new byte[]{0, 0});
		return src;
	}
	
	private ArrayList<String> convertStrList(ArrayList<String> stringContentList){
		ArrayList<String> destList = new ArrayList<String>(stringContentList.size());
		for(String str : stringContentList){
			byte[] temp = str.getBytes();
			byte[] src = new byte[temp.length*2];
			for(int i=0;i<temp.length;i++){
				src[i*2] = temp[i];
				src[i*2+1] = 0;
			}
			destList.add(new String(src));
		}
		return destList;
	}
	
}
