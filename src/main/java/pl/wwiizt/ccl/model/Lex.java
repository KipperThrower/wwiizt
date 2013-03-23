package pl.wwiizt.ccl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Lex {

	@XmlElement(name = "base")
	private String base;
	@XmlElement(name = "ctag")
	private String ctag;
	@XmlAttribute(name  = "disamb")
	private String disamb;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getCtag() {
		return ctag;
	}

	public void setCtag(String ctag) {
		this.ctag = ctag;
	}

	public String getDisamb() {
		return disamb;
	}

	public void setDisamb(String disamb) {
		this.disamb = disamb;
	}

}
