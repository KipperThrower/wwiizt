package pl.wwiizt.feature.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import pl.wwiizt.ccl.service.CclService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Service
public class FeatureService {

	public static final int MAX_FEATURES = 100;

	private static final String SPACE = " ";
	private static final String CSV_SEPARATOR = ",";
	private static final double STANDARD_PROBABILITY = 0.0;

	private static final Logger LOGGER = Logger.getLogger(FeatureService.class);

	@Autowired
	private CclService cclService;

	private Set<String> stopList = new HashSet<>();

	public Map<String, Double> getBigramsMap(String text) {
		Preconditions.checkNotNull(text);

		Map<String, Double> result = Maps.newHashMap();
		String[] tokens = text.split(SPACE);

		for (int i = 0; i < tokens.length; i++) {
			String bigram = tokens[i];// + SPACE + tokens[i + 1];

			if (true) {//!stopList.contains(bigram.toLowerCase())) {
				Double count = result.get(bigram);

				if (count == null) {
					count = 0.;
				}

				count++;
				result.put(bigram, count);
			}
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

	public List<String> getCsvWithFeatures(String[][] paths, String pathToFilesList) throws IOException {
		List<Map<String, Double>> bigrams = Lists.newArrayList();

		for (String[] path : paths) {
			bigrams.add(getBigramsMap(readFile(path[1] + "plain.txt")));
		}

		List<String> bigramsList = sortBigrams(getBigramsSet(bigrams));
		FileWriter fw = new FileWriter(pathToFilesList + ".data");
		FileWriter fw1 = new FileWriter(pathToFilesList + ".arff");

		int j = 1;

		for (String s : bigramsList) {
			fw1.write("% bigram" + j++ + " = " + s + "\n");
		}

		fw1.write("@RELATION texts\n");
		fw1.write("\n");
		fw1.write("@ATTRIBUTE class 	{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}\n");

		for (int i = 1; i <= bigramsList.size(); i++) {
			fw1.write("@ATTRIBUTE bigram" + i + "	REAL\n");
		}

		fw1.write("@DATA\n");

		for (int i = 0; i < paths.length; i++) {
			fw.write(paths[i][0] + CSV_SEPARATOR + getCsvRow(bigrams.get(i), bigramsList));
			fw.write("\n");
			fw.flush();

			fw1.write(paths[i][0] + CSV_SEPARATOR + getCsvRow(bigrams.get(i), bigramsList));
			fw1.write("\n");
			fw1.flush();
		}

		fw.close();
		fw1.close();
		return bigramsList;
	}

	public Set<Integer> selectFeatures(String path) {
		Integer[] f = new Integer[] { 2, 9, 10, 13, 14, 15, 49, 591, 1225, 1226, 1230, 1383, 2019, 4279, 5223, 5415, 5647, 8240, 8483, 8802, 9393, 9406, 9437,
				9673, 10078, 10100, 11573, 11717, 11801, 11851, 13556, 13712, 13802, 13848, 13909, 14023, 14228, 14547, 14717, 15201, 15320, 16038, 16245,
				16472, 16581, 16732, 17575, 17836, 18361, 18466, 18976, 19160, 19366, 19565, 19939, 20363, 20794, 21229, 21253, 21783, 22160, 22279, 22669,
				23138, 23185, 23192, 23403, 24181, 24691, 25047, 25503, 26213, 26227, 26939, 27087, 27088, 27091, 27092 };
		//2,35,577,839,1362,1972,3425,4083,4257,4463,5389,6625,7432,8595,8699,9106,9378,9612,9973,9975,10014,10035,10821,10842,12337,13453,13608,13698,13744,13918,14116,14120,14910,15076,15476,15477,15884,15913,16596,17437,17695,17857,18217,18275,18322,19011,19217,19416,19509,19754,19790,20214,20305,20570,20602,21099,21306,21329,21628,22966,22982,23013,23020,23292,23505,24510,25112,25606,25971,26041,26893};

		Set<Integer> result = Sets.newTreeSet();
		for (Integer i : f) {
			result.add(i - 1);
		}
		return result;
		//		try {
		//			Dataset data;
		//			data = FileHandler.loadDataset(new File(path), 0, ",");
		//			GreedyForwardSelection ga = new GreedyForwardSelection(MAX_FEATURES, new PearsonCorrelationCoefficient());
		//			ga.build(data);
		//			return ga.selectedAttributes();
		//		} catch (IOException e) {
		//			LOGGER.error(e, e);
		//			return null;
		//		}

	}

	public void rewriteSelectedFeatures(String path, List<String> bigrams) {
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path.replace(".data", "_selected.data"))))) {
				try (FileWriter fw1 = new FileWriter(path + "_selected.arff");) {

					Set<Integer> selectedFeatures = selectFeatures(path);

					int j = 1;

					for (Integer sel : selectedFeatures) {
						fw1.write("% bigram" + j++ + " = " + bigrams.get(sel - 1) + "\n");
					}

					selectedFeatures.add(0);
					fw1.write("@RELATION texts\n");
					fw1.write("\n");
					fw1.write("@ATTRIBUTE class 	{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}\n");

					for (int i = 1; i <= selectedFeatures.size() - 1; i++) {
						fw1.write("@ATTRIBUTE bigram" + i + "	REAL\n");
					}

					fw1.write("@DATA\n");

					String line = reader.readLine();

					while (line != null) {
						String[] cols = line.split(CSV_SEPARATOR);
						for (Integer i : selectedFeatures) {
							if (i.intValue() > 0) {
								writer.write(CSV_SEPARATOR);
								fw1.write(CSV_SEPARATOR);
							}
							writer.write(cols[i]);
							fw1.write(cols[i]);
						}
						writer.write("\n");
						fw1.write("\n");
						line = reader.readLine();
					}
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("[rewriteSelectedFeatures]", e);
		} catch (IOException e) {
			LOGGER.error("[rewriteSelectedFeatures]", e);
		}
	}

	public List<String> convertToWekaFormat(String pathToFilesList) {
		String[][] paths = readFileList(pathToFilesList);

		for (int i = 0; i < paths.length; i++) {
			writeToFile(cclService.getBasePlainTextFromFile(paths[i][1]), paths[i][1] + "plain.txt");
		}

		try {
			return getCsvWithFeatures(paths, pathToFilesList);
		} catch (IOException e) {
			LOGGER.error("[convertToWekaFormat]", e);
		}

		return Lists.newArrayList();
	}

	public void extractBigramsAndSelectFeatures(String pathToFlieList) {
		LOGGER.info("Extract");
		List<String> bigrams = convertToWekaFormat(pathToFlieList);
		LOGGER.info("select");
		rewriteSelectedFeatures(pathToFlieList + ".data", bigrams);
		LOGGER.info("select done");
	}

	public String[][] readFileList(String path) {
		StringBuilder sb = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
			String line = reader.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e, e);
		} catch (IOException e) {
			LOGGER.error(e, e);
		}

		String content = sb.toString();
		String[] lines = content.split("\n");
		String[][] cells = new String[lines.length][2];
		for (int i = 0; i < cells.length; i++) {
			
			cells[i] = lines[i].split(";");
		}
		
		return cells;
	}

	public void writeToFile(String content, String file) {
		try (PrintWriter pw = new PrintWriter(file)) {
			pw.write(content);
			pw.flush();
		} catch (IOException e) {
			LOGGER.error("[writeToFile]", e);
		}
	}

	public String readFile(String file) {
		try {
			return FileCopyUtils.copyToString(new FileReader(file));
		} catch (FileNotFoundException e) {
			LOGGER.error("[readFile]", e);
		} catch (IOException e) {
			LOGGER.error("[readFile]", e);
		}
		return "";
	}

	private void initStopList(String path) {
		try (Scanner scanner = new Scanner(new File(path))) {
			while (scanner.hasNext()) {
				stopList.add(scanner.next().trim());
			}

		} catch (FileNotFoundException e) {
			LOGGER.error("[initStopList]", e);
		}
	}

	//	@PostConstruct
	public void test() throws IOException {
		initStopList("stopList");
		final String PATH = "D:\\list.txt";
		extractBigramsAndSelectFeatures(PATH);
		//		printArffHeader(26905);
	}

}
