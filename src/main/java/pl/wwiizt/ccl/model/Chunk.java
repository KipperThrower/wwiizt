package pl.wwiizt.ccl.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.CollectionUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Chunk {
	
	@XmlAttribute(name = "id")
	private String id;
	@XmlAttribute(name = "type")
	private String type;
	@XmlElement(name = "sentence")
	private List<Sentence> sentences;

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentenes) {
		this.sentences = sentenes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getPlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(sentences)) {
			for (Sentence sentence : sentences) {
				sb.append(sentence.getPlainText());
			}
		}
		return sb.toString();
	}
	
	public String getBasePlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(sentences)) {
			for (Sentence sentence : sentences) {
				sb.append(sentence.getBasePlainText());
			}
		}
		return sb.toString();
	}

}
