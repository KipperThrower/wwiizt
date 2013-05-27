package pl.wwiizt.vector.model;

import java.util.List;

import pl.wwizt.vector.distances.Distance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class IndexRecord {
	
	private static final String CSV_SEPARATOR = ";";
	private static final String SPACE = " ";

	private String filePath;
	private List<Double> fields;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public List<Double> getFields() {
		return fields;
	}

	public void setFields(List<Double> fields) {
		this.fields = fields;
	}
	
	public void parseFromCSV(String line) {
		Preconditions.checkNotNull(line);
		
		fields = Lists.newArrayList();
		String[] array = line.split(CSV_SEPARATOR);
		filePath = array[0];
		for (int i = 1; i < array.length; i++) {
			fields.add(Double.parseDouble(array[i]));
		}
	}
	
	public void parseFromFile(String path, String content, IndexHeader header) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(content);
		Preconditions.checkNotNull(header);
		
		initFields(header.getHeaders().size());
		filePath = path;
		String[] tokens = content.split(SPACE);
		for (int i = 0; i < tokens.length; i++) {
			int number = header.getHeaderNumber(tokens[i]);
			if (number != -1) {
				Double count = fields.get(number);
				count++;
				fields.set(i, count);
			}
		}
		normalize();
		
	}
	
	public double compare(IndexRecord that, Distance distance) {
		return distance.measureDistance(this.fields, that.fields);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(filePath);
		for (Double d : fields) {
			if (d == null) {
				d = 0.0;
			}
			sb.append(CSV_SEPARATOR);
			sb.append(d);
		}
		return sb.toString();
	}
	
	private void normalize() {
		int size = fields.size();
		for (int i = 0; i < size; i++) {
			double d = fields.get(i);
			fields.set(i, d / size);
		}
	}
	
	private void initFields(int size) {
		fields = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			fields.add(0.0);
		}
	}

}
