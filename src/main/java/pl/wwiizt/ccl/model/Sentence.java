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
public class Sentence {
	
	private static final String SPACE = " ";
	
	@XmlAttribute(name  = "id")
	private String id;
	@XmlElement(name = "tok")
	private List<Tok> tokens;

	public List<Tok> getTokens() {
		return tokens;
	}

	public void setTokens(List<Tok> tokens) {
		this.tokens = tokens;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getPlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(tokens)) {
			for (Tok tok : tokens) {
				sb.append(tok.getOrth());
				sb.append(SPACE);
			}
		}
		return sb.toString();
	}
	
	public String getBasePlainText() {
		StringBuilder sb = new StringBuilder();
		if (!CollectionUtils.isEmpty(tokens)) {
			for (Tok tok : tokens) {
				sb.append(tok.getFirstBase());
				sb.append(SPACE);
			}
		}
		return sb.toString();
	}

}
