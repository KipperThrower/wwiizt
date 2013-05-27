package pl.wwiizt.vector.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class IndexHeader {

	private static final String CRLF = "\n";

	private Set<String> headers = Sets.newTreeSet();
	private List<String> headersList = Lists.newArrayList();
	private boolean convertedToList = false;

	public Set<String> getHeaders() {
		return headers;
	}

	public void addHeader(String header) {
		headers.add(header);
		convertedToList = false;
	}

	public void parse(String content) {
		Preconditions.checkNotNull(content);

		headers = Sets.newTreeSet();
		String[] array = content.split(CRLF);
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
