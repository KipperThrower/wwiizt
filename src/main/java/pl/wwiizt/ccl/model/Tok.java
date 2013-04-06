package pl.wwiizt.ccl.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.CollectionUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Tok {
	
	@XmlElement(name = "orth")
	private String orth;
	@XmlElement(name = "lex")
	private List<Lex> lexes;
	@XmlElement(name = "ann")
	private List<Ann> anns;
	@XmlElement(name = "prop")
	private List<Prop> props;
	
	public String getOrth() {
		return orth;
	}
	
	public void setOrth(String orth) {
		this.orth = orth;
	}
	
	public List<Lex> getLexes() {
		return lexes;
	}
	
	public void setLexes(List<Lex> lexes) {
		this.lexes = lexes;
	}
	
	public List<Ann> getAnns() {
		return anns;
	}
	
	public void setAnns(List<Ann> anns) {
		this.anns = anns;
	}

	public List<Prop> getProps() {
		return props;
	}

	public void setProps(List<Prop> props) {
		this.props = props;
	}
	
	public String getFirstBase() {
		if (!CollectionUtils.isEmpty(lexes)) {
			return lexes.get(0).getBase();
		}
		return "";
	}
	
}
