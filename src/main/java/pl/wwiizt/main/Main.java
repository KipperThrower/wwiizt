package pl.wwiizt.main;

import java.io.File;
import java.util.List;

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
			SearchEngineService service = appContext.getBean(SearchEngineService.class);
			List<String> hits = service.search(cmd.getOptionValue(SEARCH));

			System.out.println("\n=========================== Search results: ");
			
			if (hits.isEmpty())
				System.out.println("No results found");
			else {
				for (String hit : hits)
					System.out.println(hit);
			}
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
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

}