package pl.wwiizt.vector.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class IndexHeader {

	private static final String CRLF = "\n";

	private Set<String> headers = Sets.newTreeSet();
	private Map<String, Integer> documentsContainingToken = Maps.newHashMap();
	private List<String> headersList = Lists.newArrayList();
	private boolean convertedToList = false;

	public Set<String> getHeaders() {
		return headers;
	}

	public void addHeader(String header) {
		headers.add(header);
		convertedToList = false;
	}
	
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
		this.headers.addAll(tokensFromDoc);
		
		headersList = getList();
		convertedToList = true;
	}
	
	public int getIDF(String token) {
		Integer idf = documentsContainingToken.get(token);
		return idf == null ? 0 : idf;
	}

	public void parse(String content) {
		Preconditions.checkNotNull(content);

		headers = Sets.newTreeSet();
		String[] array = content.split(CRLF);

		for (String s : array) {
			String[] cols = s.split(" ");
			headers.add(cols[0]);
			documentsContainingToken.put(cols[0], Integer.parseInt(cols[1]));
		}

		headersList = getList();
		convertedToList = true;
	}
	
	public void parse(String content1, String content2) {
		Preconditions.checkNotNull(content1);
		Preconditions.checkNotNull(content2);

		headers = Sets.newTreeSet();
		String[] array = content1.split(" ");

		for (String s : array) {
			headers.add(s);
		}
		
		array = content2.split(" ");

		for (String s : array) {
			headers.add(s);
		}

		headersList = getList();
		convertedToList = true;
	}

	public String getHeaderString(int number) {
		if (!convertedToList) {
			headersList = getList();
			convertedToList = true;
		}

		if (number < headersList.size()) {
			return headersList.get(number);
		}

		return null;
	}

	public int getHeaderNumber(String header) {
		if (!convertedToList) {
			headersList = getList();
			convertedToList = true;
		}

		return headersList.indexOf(header);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String h : headers) {
			sb.append(h);
			sb.append(CRLF);
		}
		return sb.toString();
	}

	private List<String> getList() {
		return new ArrayList<>(headers);
	}

}
