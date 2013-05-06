package pl.wwiizt.ccl.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.util.CollectionUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChunkList {
	
	@XmlElement(name = "chunk")
	private List<Chunk> chunkList;
	@XmlTransient
	private String title;
	@XmlTransient
	private String fileName;

	public List<Chunk> getChunkList() {
		return chunkList;
	}

	public void setChunkList(List<Chunk> chunkList) {
		this.chunkList = chunkList;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
	public String getPlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(chunkList)) {
			for (Chunk chunk : chunkList) {
				sb.append(chunk.getPlainText());
			}
		}
		return sb.toString();
	}
	
	public String getBasePlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(chunkList)) {
			for (Chunk chunk : chunkList) {
				sb.append(chunk.getBasePlainText());
			}
		}
		return sb.toString();
	}
	
	public String getFirstSentencePlainText() {
		if (!CollectionUtils.isEmpty(chunkList)) {
			return chunkList.get(1).getFirstSentencePlainText();
		}
		return "";
	}
	
	public String getFirstSentenceBasePlainText() {
		if (!CollectionUtils.isEmpty(chunkList)) {
			return chunkList.get(1).getFirstSentenceBasePlainText();
		}
		return "";
	}

}
