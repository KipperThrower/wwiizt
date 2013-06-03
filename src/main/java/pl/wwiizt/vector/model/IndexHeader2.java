package pl.wwiizt.vector.model;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class IndexHeader2 {
	
	private static final Logger LOGGER = Logger.getLogger(IndexHeader2.class);
	
	private Map<String, Integer> documentsContainingToken = Maps.newHashMap();
	
	public void addHeaderFromWholeContent(String header) {
		Set<String> tokensFromDoc = Sets.newHashSet();
		String[] headers = header.split(" ");
		for(String s : headers) {
			tokensFromDoc.add(s);
		}
		for(String s : tokensFromDoc) {
			Integer count = documentsContainingToken.get(s);
			if (count == null) {
				count = 1;
			} else {
				count++;
			}
			documentsContainingToken.put(s, count);
		}
		
	}
	
	public int getIDF(String token) {
		Integer idf = documentsContainingToken.get(token);
		return idf == null ? 1 : idf;
	}
	
	public void parse(String content) {
		Preconditions.checkNotNull(content);

		String[] array = content.split("\n");

		for (String s : array) {
			String[] cols = s.split(" ");
			documentsContainingToken.put(cols[0], Integer.parseInt(cols[1]));
		}
	}
	
	public void load(String path) {
		try {
			String content = Files.toString(new File(path), Charsets.UTF_8);
			parse(content);
		} catch (IOException e) {
			LOGGER.error(e, e);
		}
	}
	
	public void writeToFile(String path) {
		try {
			Files.write(toString(), new File(path), Charsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error(e, e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : documentsContainingToken.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

}
