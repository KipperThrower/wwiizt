package pl.wwiizt.vector.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.vector.model.Hint;
import pl.wwiizt.vector.model.IndexHeader;
import pl.wwiizt.vector.model.IndexRecord;

@Service
public class VectorSearchService {

	private static final Logger LOGGER = Logger.getLogger(VectorSearchService.class);

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
				FileWriter fw = new FileWriter(new File(pathToIndexDir
						+ "header.csv"));
				fw.write(header.toString());
				fw.close();

				LOGGER.info("Header build. Time = "
						+ (System.currentTimeMillis() - time) + "ms");
			}
		}

		time = System.currentTimeMillis();
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				FileWriter fw = new FileWriter(new File(pathToIndexDir
						+ "index.csv"));
				for (File file : files) {
					ChunkList cl = cclService.loadFile(file);

					if (cl != null) {
						IndexRecord ir = new IndexRecord();
						ir.parseFromFile(file.getAbsolutePath(), cl.getBasePlainText(), header);
						fw.write(ir.toString());
						fw.write("\n");
					}
				}
				fw.close();
				LOGGER.info("Files indexed. Time = "
						+ (System.currentTimeMillis() - time) + "ms");
			}
		}

	}

	public List<Hint> search(File indexDir, String file) throws IOException {
		ChunkList cl = cclService.loadFile(file);
		IndexHeader header = readHeader(new File(indexDir.getAbsoluteFile() + File.separator + "header.csv"));
		IndexRecord searchedIR = new IndexRecord();
		searchedIR.parseFromFile(file, cl.getBasePlainText(), header);
		
		List<Hint> hints = Lists.newArrayList();
		BufferedReader br = new BufferedReader(new FileReader(indexDir.getAbsoluteFile() + File.separator + "index.csv"));
		String line = null;
		while ((line = br.readLine()) != null) {
			IndexRecord ir = new IndexRecord();
			ir.parseFromCSV(line);
			Hint hint = new Hint();
			hint.setPath(ir.getFilePath());
			hint.setRank(ir.compare(searchedIR));
			hints.add(hint);
		}
		br.close();
		Collections.sort(hints);
		
		return hints;
	}
	
	public IndexHeader readHeader(File path) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		IndexHeader ir = new IndexHeader();
		ir.parse(sb.toString());
		return ir;
	}

	private class XmlFileFilter implements FileFilter {

		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	}

}
