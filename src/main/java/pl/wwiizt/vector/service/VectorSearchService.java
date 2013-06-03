package pl.wwiizt.vector.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.main.Main;
import pl.wwiizt.vector.model.Hint;
import pl.wwiizt.vector.model.IndexHeader;
import pl.wwiizt.vector.model.IndexHeader2;
import pl.wwiizt.vector.model.IndexRecord;
import pl.wwiizt.vector.model.IndexRecord2;
import pl.wwiizt.wordnet.WordnetJDBC;
import pl.wwizt.vector.distances.CosineDistance;
import pl.wwizt.vector.distances.Distance;
import pl.wwizt.vector.distances.EuclidesDistance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ranges;

@Service
public class VectorSearchService {

	private static final Logger LOGGER = Logger
			.getLogger(VectorSearchService.class);

	@Autowired
	private CclService cclService;

//	@PostConstruct
//	public void testname() throws Exception {
//		List<Hint> hints = search(new File(
//				"D:\\do szkoły\\Wydobywanie wiedzy i informacji z tekstu\\projectWorkspace\\subwiki-with-questions\\index"),
//				"D:\\do szkoły\\Wydobywanie wiedzy i informacji z tekstu\\projectWorkspace\\subwiki-with-questions\\ccl-Testosteron.xml", null, new EuclidesDistance(), false, true);
//		for(Hint hint : hints) {
//			System.out.println(hint);
//		}
//	}

	public void index(File dir, String indexName, Set<String> stopList,
			boolean tfidf) {
		Preconditions.checkNotNull(dir);

		String pathToIndexDir = dir.getAbsolutePath() + File.separator
				+ indexName + File.separator;

		long time = System.currentTimeMillis();

		LOGGER.info("Building header");

		IndexHeader2 header = new IndexHeader2();

		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());

			if (files != null) {

				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {
						String plainText = cl.getBasePlainText();
						plainText = filterStopList(plainText, stopList);

						header.addHeaderFromWholeContent(plainText);
					}
				}
				header.writeToFile(pathToIndexDir + "header.csv");

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Header build. Time = "
							+ (System.currentTimeMillis() - time) + "ms");
			}
		}

		time = System.currentTimeMillis();

		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {

				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {
						IndexRecord2 ir = new IndexRecord2();
						ir.parse(cl.getBasePlainText(), header, files.length);
						ir.writeToFile(pathToIndexDir + file.getName());
					}
				}

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Files indexed. Time = "
							+ (System.currentTimeMillis() - time) + "ms");
			}
		}

	}

	public List<Hint> search(File indexDir, String filePath,
			Set<String> stopList, Distance distance, boolean tfidf,
			boolean useSynonyms) {
		ChunkList cl = cclService.loadFile(filePath);
		IndexHeader2 header = new IndexHeader2();
		header.load(indexDir.getAbsoluteFile() + File.separator + "header.csv");
		IndexRecord2 searchedIR = new IndexRecord2();

		String plainText = cl.getBasePlainText();
		plainText = filterStopList(plainText, stopList);

		if (useSynonyms)
			plainText = addSynonyms(plainText);

		

		List<Hint> hints = Lists.newArrayList();

		if (indexDir.isDirectory()) {
			File[] files = indexDir.listFiles(new XmlFileFilter());
			if (files != null) {
				searchedIR.parse(plainText, header, files.length);
				for (File file : files) {
					IndexRecord2 ir = new IndexRecord2();
					ir.load(file.getAbsolutePath());
					Hint hint = new Hint();
					hint.setPath(file.getName());
					hint.setRank(ir.compare(searchedIR, distance));
					if (Ranges.closed(0d, 2d).contains(hint.getRank())) {
						hints.add(hint);
					}
				}
			}
		}

		Collections.sort(hints, distance.getComparator());
		hints = hints.subList(0, Main.MAX_DOCS);
		return hints;
	}

	public List<Hint> searchWithoutIndex(File dir, String path,
			Distance distance, Set<String> stopList, boolean tfidf,
			boolean useSynonyms) {

		List<Hint> hints = Lists.newArrayList();
		ChunkList clSearched = cclService.loadFile(path);
		String clSearchedString = filterStopList(clSearched.getBasePlainText(),
				stopList);

		if (useSynonyms)
			clSearchedString = addSynonyms(clSearchedString);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				long time = System.currentTimeMillis();

				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {

						String clString = filterStopList(cl.getBasePlainText(),
								stopList);

						IndexHeader header = new IndexHeader();
						header.parse(clSearchedString, clString);

						IndexRecord searchedIR = new IndexRecord();
						searchedIR.parseFromFile(path, clSearchedString,
								header, tfidf);

						IndexRecord ir = new IndexRecord();
						ir.parseFromFile(file.getName(), clString, header,
								tfidf);

						Hint hint = new Hint();
						hint.setPath(ir.getFilePath());
						hint.setRank(ir.compare(searchedIR, distance));
						hints.add(hint);
					}
				}

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Files searched. Time = "
							+ (System.currentTimeMillis() - time) + "ms");
			}
		}

		Collections.sort(hints, distance.getComparator());
		hints = hints.subList(0, Main.MAX_DOCS);
		return hints;
	}

	private String addSynonyms(String input) {
		StringBuilder sb = new StringBuilder();

		for (String s : input.split(" ")) {
			sb.append(s);
			sb.append(" ");

			List<String> list = WordnetJDBC.INSTANCE.getLexFromTheSameSynset(s);

			for (String syn : list) {
				sb.append(syn);
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private String filterStopList(String content, Set<String> stopList) {
		if (stopList == null) {
			return content;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : content.split(" ")) {
			if (!stopList.contains(s.toLowerCase())) {
				sb.append(s);
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private IndexHeader readHeader(File path) {
		IndexHeader ir = new IndexHeader();
		StringBuilder sb = new StringBuilder();

		try (Scanner scanner = new Scanner(path)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
				sb.append("\n");
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("[readHeader]", e);
		}

		ir.parse(sb.toString());

		return ir;
	}

	private class XmlFileFilter implements FileFilter {

		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	}

}
