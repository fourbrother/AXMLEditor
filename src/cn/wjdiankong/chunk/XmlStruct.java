package cn.wjdiankong.chunk;

import java.util.ArrayList;

public class XmlStruct {
	
	public byte[] byteSrc;
	
	public byte[] magicNumber;
	
	public byte[] fileSize;
	
	public StringChunk stringChunk;
	
	public ResourceChunk resChunk;
	
	public StartNameSpaceChunk startNamespaceChunk;
	
	public EndNameSpaceChunk endNamespaceChunk;
	
	public ArrayList<StartTagChunk> startTagChunkList = new ArrayList<StartTagChunk>();
	
	public ArrayList<EndTagChunk> endTagChunkList = new ArrayList<EndTagChunk>();
	
	public ArrayList<TextChunk> textChunkList = new ArrayList<TextChunk>();
	
	public ArrayList<TagChunk> tagChunkList = new ArrayList<TagChunk>();
	
	public void clear(){
		magicNumber = null;
		fileSize = null;
		stringChunk = null;
		resChunk = null;
		startNamespaceChunk = null;
		endNamespaceChunk = null;
		startTagChunkList.clear();
		endTagChunkList.clear();
		textChunkList.clear();
		tagChunkList.clear();
		
	}

}
