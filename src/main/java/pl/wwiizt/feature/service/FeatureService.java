package pl.wwiizt.feature.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;
import net.sf.javaml.featureselection.subset.GreedyForwardSelection;
import net.sf.javaml.tools.data.FileHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.service.CclService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


@Service
public class FeatureService {
	
	public static final int MAX_FEATURES = 20;
	
	private static final String SPACE = " ";
	private static final String CSV_SEPARATOR = ",";
	private static final double STANDARD_PROBABILITY = 0.0001;
	
	private static final Logger LOGGER = Logger.getLogger(FeatureService.class);
	
	@Autowired
	private CclService cclService;
	
	public Map<String, Double> getBigramsMap(String text) {
		Preconditions.checkNotNull(text);
		
		Map<String, Double> result = Maps.newHashMap();
		String[] tokens = text.split(SPACE);
		for(int i = 0; i < tokens.length - 1; i++) {
			String bigram = tokens[i] + SPACE + tokens[i + 1];
			Double count = result.get(bigram);
			if (count == null) {
				count = 0.;
			}
			count++;
			result.put(bigram, count);
		}
		normalize(result);
		return result;
	}
	
	public Set<String> getBigramsSet(List<Map<String, Double>> bigrams) {
		Set<String> result = Sets.newHashSet();
		for (Map<String, Double> b : bigrams) {
			result.addAll(b.keySet());
		}
		return result;
	}
	
	
	public void normalize(Map<String, Double> bigrams) {
		Preconditions.checkNotNull(bigrams);
		
		int size = bigrams.keySet().size();
		for (String key : bigrams.keySet()) {
			Double value = bigrams.get(key);
			value /= size;
			bigrams.put(key, value);
		}
	}
	
	public List<String> sortBigrams(Set<String> bigrams) {
		Preconditions.checkNotNull(bigrams);
		
		List<String> result = Lists.newArrayList();
		result.addAll(bigrams);
		Collections.sort(result);
		return result;
	}
	
	public String getCsvRow(Map<String, Double> bigrams, List<String> features) {
		StringBuilder result = new StringBuilder();
		for (String feature : features) {
			Double value = bigrams.get(feature);
			if (value != null) {
				result.append(value);
			} else {
				result.append(STANDARD_PROBABILITY);
			}
			result.append(CSV_SEPARATOR);
		}
		return result.toString();
	}
	
	public String[] getCsvWithFeatures(String[] texts) {
		List<Map<String, Double>> bigrams = Lists.newArrayList();
		for (String text : texts) {
			bigrams.add(getBigramsMap(text));
		}
		List<String> bigramsList = sortBigrams(getBigramsSet(bigrams));
		
		String[] result = new String[bigrams.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = getCsvRow(bigrams.get(i), bigramsList);
		}
		
		return result;
	}
	
	public Set<Integer> selectFeatures(String path) {
		try {
			Dataset data;
			data = FileHandler.loadDataset(new File(path), 0, ",");
			GreedyForwardSelection ga = new GreedyForwardSelection(MAX_FEATURES, new PearsonCorrelationCoefficient());
			ga.build(data);
			return ga.selectedAttributes();
		} catch (IOException e) {
			LOGGER.error(e, e);
			return null;
		}
		
	}
	
	public void rewriteSelectedFeatures(String path) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(new File(path)));
			writer = new BufferedWriter(new FileWriter(new File(path.replace(".data", "_selected.data"))));
			Set<Integer> selectedFeatures = selectFeatures(path);
			selectedFeatures.add(0);
			String line = reader.readLine();
			while(line != null) {
				String[] cols = line.split(CSV_SEPARATOR);
				for (Integer i : selectedFeatures) {
					if (i > 0) {
						writer.write(CSV_SEPARATOR);
					}
					writer.write(cols[i]);
				}
				writer.write("\n");
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e, e);
		} catch (IOException e) {
			LOGGER.error(e, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error(e, e);
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					LOGGER.error(e, e);
				}
			}
		}
		
		
	}
	
	public void convertToWekaFormat(String pathToFilesList) {
		String[][] paths = readFileList(pathToFilesList);
		String[] contents = new String[paths.length];
		for (int i = 0; i < paths.length; i++) {
			contents[i] = cclService.getBasePlainTextFromFile(paths[i][1]);
		}
		String[] csvs = getCsvWithFeatures(contents);
		for (int i = 0; i < csvs.length; i++) {
			csvs[i] = paths[i][0] + CSV_SEPARATOR + csvs[i];
		}
		writeToFile(csvs, pathToFilesList + ".data");
	}
	
	public void extractBigramsAndSelectFeatures(String pathToFlieList) {
		convertToWekaFormat(pathToFlieList);
		rewriteSelectedFeatures(pathToFlieList + ".data");
	}
	
	public String[][] readFileList(String path) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(path)));
			String line = reader.readLine();
			while(line != null) {
				sb.append(line);
				sb.append("\n");
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e, e);
		} catch (IOException e) {
			LOGGER.error(e, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error(e, e);
				}
			}
		}
		String content = sb.toString();
		String[] lines = content.split("\n");
		String[][] cells = new String[lines.length][2];
		for (int i = 0; i < cells.length; i++) {
			cells[i] = lines[i].split(";");
		}
		return cells;
	}
	
	private void writeToFile(String[] content, String file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			for (String s : content) {
				fw.write(s);
				fw.write("\n");
			}
			fw.flush();
		} catch(IOException e) {
			LOGGER.error(e, e);
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				LOGGER.error(e, e);
			}
		}
	}
	
//	@PostConstruct
	public void test() {
		final String PATH = "D:\\list.txt";
		extractBigramsAndSelectFeatures(PATH);
	}

}
