package pl.wwiizt.main;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.search.service.SearchEngineService;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private static ApplicationContext appContext;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		appContext = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext.xml" });
		if (args != null && args.length > 1 && "convert".equals(args[0])) {
			CclService service = appContext.getBean(CclService.class);
			service.convertFilesToPlainText(new File(args[1]));
		}
		
		if (args != null && args.length > 1 && "index".equals(args[0])) {
			SearchEngineService service = appContext.getBean(SearchEngineService.class);
			service.index(new File(args[1]));
		}
		
		if (args != null && args.length > 1 && "search".equals(args[0])) {
			SearchEngineService service = appContext.getBean(SearchEngineService.class);
			List<String> hits = service.search(args[1]);
			for (String hit : hits) {
				System.out.println(hit);
			}
		}
	}

}