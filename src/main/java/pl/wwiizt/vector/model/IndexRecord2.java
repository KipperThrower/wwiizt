package pl.wwiizt.vector.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import pl.wwizt.vector.distances.Distance;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class IndexRecord2 {

	private static final Logger LOGGER = Logger.getLogger(IndexRecord2.class);
	
	private Map<String, Double> tokens = Maps.newHashMap();
	private String docName;

	public void load(String filePath) {
		try {
			String content = Files.toString(new File(filePath), Charsets.UTF_8);
			String[] lines = content.split("\n");
			for (String s : lines) {
				String[] columns = s.split(" ");
				tokens.put(columns[0], Double.parseDouble(columns[1]));
			}
		} catch (IOException e) {
			LOGGER.error(e, e);
		}
	}
	
	public void parse(String content, IndexHeader2 ih, int countDocs) {
		String[] tokens = content.split(" ");
		for(String s : tokens) {
			Double count = this.tokens.get(s);
			if (count == null) {
				count = 1d;
			} else {
				count++;
			}
			this.tokens.put(s, count);
		}
		normalize(tokens.length, ih, countDocs);
	}
	
	private void normalize(int size, IndexHeader2 ih, int countDocs) {
		for(String s : tokens.keySet()) {
			Double d = tokens.get(s);
			d /= size;
			d *= Math.log(countDocs / ih.getIDF(s));
			tokens.put(s, d);
		}
	}
	
	public void writeToFile(String path) {
		try {
			Files.write(toString(), new File(path), Charsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error(e, e);
		}
	}
	
	public double compare(IndexRecord2 that, Distance distance) {
		List<Double> v1 = Lists.newArrayList();
		List<Double> v2 = Lists.newArrayList();
		for (Entry<String, Double> entry : this.tokens.entrySet()) {
			v1.add(entry.getValue());
			Double d = that.tokens.get(entry.getKey());
			if (d == null) {
				v2.add(0d);
			} else {
				v2.add(d);
			}
		}
		
		for (Entry<String, Double> entry : that.tokens.entrySet()) {
			v1.add(entry.getValue());
			Double d = this.tokens.get(entry.getKey());
			if (d == null) {
				v2.add(0d);
			} else {
				v2.add(d);
			}
		}
		return distance.measureDistance(v1, v2);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Double> entry : tokens.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	public Map<String, Double> getTokens() {
		return tokens;
	}

	public void setTokens(Map<String, Double> tokens) {
		this.tokens = tokens;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

}
