package pl.wwiizt.search.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.service.CclService;

@Service
public class SearchEngineTest {

	@Autowired
	private CclService cclService;
	@Autowired
	private SearchEngineService searchEngineService;
	
//	@PostConstruct
	public void test() {
		final String PATH = "D:\\do szkoły\\Wydobywanie wiedzy i informacji z tekstu\\subwiki-with-questions";
		final String TEXT_QUERY = "stany zjednoczone";
		
		searchEngineService.index(new File(PATH), "test");
		
		List<String> hits = searchEngineService.search(TEXT_QUERY, "test");
		for (String hit : hits) {
			System.out.println(hit);
		}
	}
	
}
