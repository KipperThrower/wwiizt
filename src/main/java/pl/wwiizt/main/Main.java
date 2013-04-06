package pl.wwiizt.main;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.wwiizt.ccl.service.CclService;

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
	}

}