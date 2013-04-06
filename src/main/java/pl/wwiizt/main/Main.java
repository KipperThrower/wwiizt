package pl.wwiizt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.search.service.SearchEngineService;

public class Main {

	private static ApplicationContext appContext;

	private final static String SEARCH = "search";
	private final static String INDEX = "index";
	private final static String CONVERT = "convert";
	private final static String HELP = "help";

	private final static int MAX_DOCS = 20;
	
	private static String query;
	private static List<String> supposedResults;
	private static List<String> hits;
	
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

		if (cmd.hasOption(CONVERT)) {
			CclService service = appContext.getBean(CclService.class);
			service.convertFilesToPlainText(new File(cmd.getOptionValue(CONVERT)));
		}

		if (cmd.hasOption(INDEX)) {
			SearchEngineService service = appContext.getBean(SearchEngineService.class);
			service.index(new File(cmd.getOptionValue(INDEX)));
		}

		if (cmd.hasOption(SEARCH)) {
			parseQueryAndSupposedResults(new File(cmd.getOptionValue(SEARCH)));
			search();
			printResultsAndMeasures();
		}
	}

	private static Options getOptions() {
		Options options = new Options();

		options.addOption(SEARCH, "s", true, "search query");
		options.addOption(INDEX, "i", true, "index path");
		options.addOption(CONVERT, "i", true, "convert path");
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
		Scanner scan = null;
		supposedResults = new ArrayList<String>();

		try {
			scan = new Scanner(file);

			query = scan.nextLine();

			while (scan.hasNextLine() && supposedResults.size() <  MAX_DOCS) {
				String nextLine = scan.nextLine();
				
				if (!"".equals(nextLine))
					supposedResults.add(nextLine);
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + file.getAbsolutePath());
			System.exit(1);
		}

		scan.close();
	}
	
	private static void search() {
		SearchEngineService service = appContext.getBean(SearchEngineService.class);
		hits = service.search(query);
		
		if (hits.size() > MAX_DOCS)
			hits = hits.subList(0, MAX_DOCS);
		
	}

	private static void printResultsAndMeasures() {
		System.out.println("\n\n=========================== Query: ");
		System.out.println(query);
		System.out.println("\n\n=========================== Supposed results: ");
		printList(supposedResults);
		System.out.println("\n\n=========================== Search results: ");
		printList(hits);
		System.out.println("\n\n=========================== Measures");
		printMeasures();
	}

	private static void printList(List<String> list) {
		if (list.isEmpty())
			System.out.println("Nothing found");
		else {
			
			for (String s: list)
				System.out.println(s);
		}
		
	}
	
	private static void printMeasures() {
		// TODO Auto-generated method stub
	}
	
}