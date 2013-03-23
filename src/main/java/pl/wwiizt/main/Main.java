package pl.wwiizt.main;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private static ClassPathXmlApplicationContext appContext;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		appContext = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext.xml" });

	}

}