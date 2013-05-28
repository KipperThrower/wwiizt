package pl.wwiizt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.elasticsearch.common.collect.Lists;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.feature.service.FeatureService;
import pl.wwiizt.helpers.MeasuresHelper;
import pl.wwiizt.liner.LinerWebservice;
import pl.wwiizt.search.service.SearchEngineService;
import pl.wwiizt.vector.model.Hint;
import pl.wwiizt.vector.service.VectorSearchService;
import pl.wwizt.vector.distances.CosineDistance;
import pl.wwizt.vector.distances.Distance;
import pl.wwizt.vector.distances.EuclidesDistance;

public class Main {

	private static ApplicationContext appContext;

	private final static String SEARCH = "search";
	private final static String INDEX = "index";
	private final static String CONVERT = "convert";
	private final static String INDEX_NAME = "indexName"; //tez przy modelu wektorowym
	private final static String PRINT_ALL = "printAll";
	private final static String HELP = "help";
	private final static String SELECT_FEATURES = "selectFeatures";
	private final static String STOP_LIST = "stoplist";

	
	//do modelu wektorowego 
	private final static String EUCLIDES_DISTANCE = "euclidesDistance";
	private final static String USE_SYNONYMS = "useSynonyms";
	private final static String TFIDF = "tfidf";


	private final static String INDEX_VECTORS = "indexVectors"; //sciezka do indeksowania dokumentow, do uzycia tez INDEX_NAME
	private final static String SEARCH_VECTOR = "searchVector"; //dokument do szukania
	private final static String DOCUMENTS_DIR = "documentsDir"; //dokumenty nie zaindeksowane, do wyszukania
	private final static String INDEXED_VECTORS_PATH = "indexedVectors"; //dokumenty zaindeksowane, do wyszukiwania

	public final static int MAX_DOCS = 20;

	private static boolean printAll = false;
	private static Map<ChunkList, List<String>> queryAndSupposedResults;

	private static double precisionMean;
	private static double recallMean;
	private static double fmeasureMean;
	private static double mrr;

	private static Set<String> stopList = new HashSet<>();

	private static String indexName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = getOptions();
		CommandLine cmd = parseCommandLine(options, args);

		if (args.length <= 1 || cmd.hasOption(HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("wwiizt", options);
			System.exit(1);
		}
		appContext = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });

		if (cmd.hasOption(SELECT_FEATURES)) {
			FeatureService service = appContext.getBean(FeatureService.class);
			service.extractBigramsAndSelectFeatures(cmd.getOptionValue(SELECT_FEATURES));
		}

		if (cmd.hasOption(CONVERT)) {
			CclService service = appContext.getBean(CclService.class);
			service.convertFilesToPlainText(new File(cmd.getOptionValue(CONVERT)));
		}

		indexName = cmd.getOptionValue(INDEX_NAME, SearchEngineService.INDEX_NAME);

		SearchEngineService service = appContext.getBean(SearchEngineService.class);

		if (cmd.hasOption(INDEX)) {
			service.index(new File(cmd.getOptionValue(INDEX)), indexName);
		}

		if (cmd.hasOption(STOP_LIST)) {
			initStopList(cmd.getOptionValue(STOP_LIST));
		}

		printAll = cmd.hasOption(PRINT_ALL);

		if (cmd.hasOption(SEARCH)) {
			parseQueryAndSupposedResults(new File(cmd.getOptionValue(SEARCH)));
			search();
		}

		if (cmd.hasOption(INDEX_VECTORS))
			handleVectorIndex(cmd);

		if (cmd.hasOption(SEARCH_VECTOR))
			handleVectorSearch(cmd);

		service.closeNode();
	}

	private static void handleVectorIndex(CommandLine cmd) {
		String indexName = cmd.hasOption(INDEX_NAME) ? cmd.getOptionValue(INDEX_NAME) : "index";
		String inputPath = cmd.getOptionValue(INDEX_VECTORS);

		VectorSearchService service = appContext.getBean(VectorSearchService.class);
		service.index(new File(inputPath), indexName);
	}

	private static void handleVectorSearch(CommandLine cmd) {
		Distance distance = cmd.hasOption(EUCLIDES_DISTANCE) ? new EuclidesDistance() : new CosineDistance();
		boolean tfidf = cmd.hasOption(TFIDF);
		boolean useSynonyms = cmd.hasOption(USE_SYNONYMS);

		VectorSearchService service = appContext.getBean(VectorSearchService.class);
		List<Hint> hints = Lists.newArrayList();
		
		if (cmd.hasOption(INDEXED_VECTORS_PATH)) {
			hints = service.search(new File(cmd.getOptionValue(INDEXED_VECTORS_PATH)), cmd.getOptionValue(SEARCH_VECTOR), distance, tfidf, useSynonyms);
		} else if (cmd.hasOption(DOCUMENTS_DIR)) {
			hints = service.searchWithoutIndex(new File(cmd.getOptionValue(DOCUMENTS_DIR)), cmd.getOptionValue(SEARCH_VECTOR), distance, stopList, tfidf, useSynonyms);
		}
		
		for(Hint hint : hints) {
			System.out.println(hint);
		}
		
	}

	private static Options getOptions() {
		Options options = new Options();

		options.addOption(SEARCH, "s", true, "search query");
		options.addOption(INDEX, "i", true, "index path");
		options.addOption(CONVERT, "i", true, "convert path");
		options.addOption(PRINT_ALL, "p", false, "print all results");
		options.addOption(INDEX_NAME, true, "index name");
		options.addOption(STOP_LIST, true, "stop list path");

		options.addOption(EUCLIDES_DISTANCE, false, "use Euclides distance (cosine distance otherwise)");
		options.addOption(USE_SYNONYMS, false, "use synonyms");
		options.addOption(TFIDF, false, "use tf/idf");

		options.addOption(INDEX_VECTORS, true, "folder path of documents to index");
		options.addOption(SEARCH_VECTOR, "v", true, "ccl to search similar documents");
		options.addOption(DOCUMENTS_DIR, true, "directory of unindexed documents to find similar");
		options.addOption(INDEXED_VECTORS_PATH, true, "directory of indexed documents to find similar");

		options.addOption(HELP, "h", false, "help");

		return options;
	}

	private static CommandLine parseCommandLine(Options options, String[] args) {
		CommandLineParser cmdParser = new PosixParser();
		CommandLine result = null;
		try {
			result = cmdParser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

		return result;
	}

	private static void parseQueryAndSupposedResults(File file) {
		CclService cclService = appContext.getBean(CclService.class);
		LinerWebservice linerWebservice = appContext.getBean(LinerWebservice.class);

		Scanner scan = null;
		queryAndSupposedResults = new HashMap<ChunkList, List<String>>();
		int i = 0;
		try {
			scan = new Scanner(file);

			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				String[] splittedLine = line.split(";");

				if (splittedLine.length < 2)
					continue;

				String query = splittedLine[0];
				String[] supposedResults = splittedLine[1].split(" ");

				File queryCcl = new File(file.getPath() + "Dir" + File.separator + i + ".xml");
				if (!queryCcl.exists()) {
					String xml = linerWebservice.parse(query);
					cclService.writeToFile(xml, queryCcl.getPath());
				}
				ChunkList cl = cclService.loadFile(queryCcl);

				queryAndSupposedResults.put(cl, Arrays.asList(supposedResults));
				i++;
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + file.getAbsolutePath());
			System.exit(1);
		}

		scan.close();
	}

	private static void search() {
		SearchEngineService service = appContext.getBean(SearchEngineService.class);

		for (Entry<ChunkList, List<String>> e : queryAndSupposedResults.entrySet()) {
			List<String> hits = service.search(e.getKey(), indexName, stopList);

			if (hits.size() > MAX_DOCS)
				hits = hits.subList(0, MAX_DOCS);

			MeasuresHelper measures = new MeasuresHelper(e.getValue(), hits, hits.size());

			saveMeasures(measures);

			if (printAll)
				printResultsAndMeasures(e.getKey(), e.getValue(), hits, measures);
		}

		printFinalMeasures();
	}

	private static void printResultsAndMeasures(ChunkList cl, List<String> supposedResults, List<String> searchResults, MeasuresHelper measures) {
		System.out.println("\n=========================== Query: ");
		System.out.println(cl.getPlainText());
		System.out.println("\n=========================== Supposed results: ");
		printList(supposedResults);
		System.out.println("\n=========================== Search results: ");
		printList(searchResults);

		if (!searchResults.isEmpty()) {
			System.out.println("\n=========================== Measures");
			printMeasures(measures);
		}
	}

	private static void printList(List<String> list) {
		if (list.isEmpty())
			System.out.println("Nothing found");
		else {

			for (String s : list)
				System.out.println(s);
		}
	}

	private static void printMeasures(MeasuresHelper measures) {
		System.out.println("Precision: " + measures.getPrecision());
		System.out.println("Recall: " + measures.getRecall());
		System.out.println("F-score: " + measures.getFmeasure());
		//		System.out.println("Recall_rank: " + measures.getRecallRank());
		//		System.out.println("Precision_log: " + measures.getLogarithmicPrecision());
	}

	private static void saveMeasures(MeasuresHelper measures) {
		precisionMean += measures.getPrecision();
		recallMean += measures.getRecall();
		fmeasureMean += measures.getFmeasure();
		mrr += measures.getRank() == 0 ? 0 : 1.0 / (double) measures.getRank();
	}

	/** 
	 *	sum = 0
	 *	for question in collection:
	 *	sum += 1/rank
	 *
	 *	return sum / len(collection)
	 *				
	 * http://en.wikipedia.org/wiki/Mean_reciprocal_rank
	 */
	private static void printFinalMeasures() {
		int size = queryAndSupposedResults.size();
		System.out.println("\n\nPrecision mean: " + precisionMean / (double) size);
		System.out.println("\n\nRecall mean: " + recallMean / (double) size);
		System.out.println("\n\nF-measure mean: " + fmeasureMean / (double) size);
		System.out.println("\n\nMRR: " + mrr / (double) size);
	}

	private static void initStopList(String path) {
		try (Scanner scanner = new Scanner(new File(path))) {
			while (scanner.hasNext()) {
				stopList.add(scanner.next().trim());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}