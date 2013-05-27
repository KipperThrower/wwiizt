package pl.wwiizt.vector.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.main.Main;
import pl.wwiizt.vector.model.Hint;
import pl.wwiizt.vector.model.IndexHeader;
import pl.wwiizt.vector.model.IndexRecord;
import pl.wwizt.vector.distances.Distance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Service
public class VectorSearchService {

	private static final Logger LOGGER = Logger
			.getLogger(VectorSearchService.class);

	@Autowired
	private CclService cclService;

	public void index(File dir) throws IOException {
		Preconditions.checkNotNull(dir);

		String pathToIndexDir = dir.getAbsolutePath() + File.separator
				+ "index" + File.separator;

		long time = System.currentTimeMillis();

		LOGGER.info("Building header");

		IndexHeader header = new IndexHeader();
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			
			if (files != null) {
				
				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {
						String[] tokens = cl.getBasePlainText().split(" ");
						for (String token : tokens) {
							header.addHeader(token);
						}
					}
				}
				FileWriter fw = new FileWriter(new File(pathToIndexDir + "header.csv"));
				fw.write(header.toString());
				fw.close();

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Header build. Time = " + (System.currentTimeMillis() - time) + "ms");
			}
		}

		time = System.currentTimeMillis();
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				FileWriter fw = new FileWriter(new File(pathToIndexDir + "index.csv"));
				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {
						IndexRecord ir = new IndexRecord();
						ir.parseFromFile(file.getAbsolutePath(),
								cl.getBasePlainText(), header);
						fw.write(ir.toString());
						fw.write("\n");
					}
				}
				fw.close();

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Files indexed. Time = " + (System.currentTimeMillis() - time) + "ms");
			}
		}

	}

	public List<Hint> search(File indexDir, String file, Distance distance)  {
		ChunkList cl = cclService.loadFile(file);
		IndexHeader header = readHeader(new File(indexDir.getAbsoluteFile()
				+ File.separator + "header.csv"));
		IndexRecord searchedIR = new IndexRecord();
		searchedIR.parseFromFile(file, cl.getBasePlainText(), header);

		List<Hint> hints = Lists.newArrayList();
		
		
		try(Scanner scan = new Scanner(new File(indexDir.getAbsoluteFile() + File.separator + "index.csv"))) {
			while(scan.hasNextLine()) {
				IndexRecord ir = new IndexRecord();
				ir.parseFromCSV(scan.nextLine());
				Hint hint = new Hint();
				hint.setPath(ir.getFilePath());
				hint.setRank(ir.compare(searchedIR, distance));
				hints.add(hint);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("[search]", e);
		}
		
		Collections.sort(hints);

		return hints;
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
