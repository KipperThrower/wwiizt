package pl.wwiizt.ccl.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.model.ChunkList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Service("cclService")
public class CclService {
	
	private static final Logger LOGGER = Logger.getLogger(CclService.class);

	public ChunkList loadFile(File file) {
		Preconditions.checkNotNull(file);

		ChunkList chunkList = new ChunkList();
		try {
			JAXBContext context = JAXBContext.newInstance(ChunkList.class);
			Unmarshaller um = context.createUnmarshaller();
			chunkList = (ChunkList) um.unmarshal(new FileReader(file));
			chunkList.setFileName(file.getName());
			chunkList.setTitle(file.getName().replace("ccl-", "").replaceAll("\\.xml$", "").replace("32", " "));
		} catch (Exception ex) {
			LOGGER.error(ex, ex);
		}
		return chunkList;
	}
	
	public ChunkList parseString(String string) {
		Preconditions.checkNotNull(string);

		ChunkList chunkList = null;
		try {
			JAXBContext context = JAXBContext.newInstance(ChunkList.class);
			Unmarshaller um = context.createUnmarshaller();
			chunkList = (ChunkList) um.unmarshal(new StringReader(string));
		} catch (Exception ex) {
			LOGGER.error(ex, ex);
		}
		return chunkList;
	}

	public List<ChunkList> loadDirectory(File dir) {
		Preconditions.checkNotNull(dir);
		
		List<ChunkList> chunkList = Lists.newArrayList();
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				for(File file : files) {
					ChunkList cl = loadFile(file);
					chunkList.add(cl);
				}
			}
		}
		
		return chunkList;
	}
	
	public String getPlainTextFromFile(File file) {
		ChunkList cl = loadFile(file);
		return cl != null ? cl.getPlainText() : null;
	}
	
	public String getBasePlainTextFromFile(File file) {
		ChunkList cl = loadFile(file);
		return cl != null ? cl.getBasePlainText() : null;
	}
	
	public ChunkList loadFile(String file) {
		return loadFile(new File(file));
	}
	
	public List<ChunkList> loadDirectory(String dir) {
		return loadDirectory(new File(dir));
	}
	
	public String getPlainTextFromFile(String file) {
		ChunkList cl = loadFile(file);
		return cl != null ? cl.getPlainText() : null;
	}
	
	public String getBasePlainTextFromFile(String file) {
		ChunkList cl = loadFile(file);
		return cl != null ? cl.getBasePlainText() : null;
	}
	
	public void convertFilesToPlainText(File dir) {
		Preconditions.checkNotNull(dir);
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				for(File file : files) {
					ChunkList cl = loadFile(file);
					if (cl != null) {
						String plainTextFilePath = file.getAbsolutePath().replaceAll("\\.xml$", ".txt");
						writeToFile(cl.getPlainText(), plainTextFilePath);
						String basePlainTextFilePath = file.getAbsolutePath().replaceAll("\\.xml$", "_base.txt");
						writeToFile(cl.getBasePlainText(), basePlainTextFilePath);
					}
				}
			}
		}
	}
	
	public void writeToFile(String content, String file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.write(content);
			fw.flush();
		} catch(IOException e) {
			LOGGER.error(e, e);
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				LOGGER.error(e, e);
			}
		}
	}
	
	private class XmlFileFilter implements FileFilter {
		
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	}

}
