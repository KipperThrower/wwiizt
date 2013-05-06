package pl.wwiizt.tagger.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;


@Service
public class TaggerService {
	
	@Autowired
	private CclService cclService;
	
	
	public ChunkList runTagger(String text) {
		
		
		String taggerOutput = "";
		ChunkList chunkList = cclService.parseString(taggerOutput);
		return chunkList;
	}

}
