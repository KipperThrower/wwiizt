package pl.wwiizt.json.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.search.service.SearchEngineService;

@Service
public class JsonService {
	
	private static final Logger LOGGER = Logger.getLogger(JsonService.class);
	
	private ObjectMapper mapper;
	
	@PostConstruct
	public void init() {
		mapper = new ObjectMapper();
	}
	
	public String getJson(ChunkList chunkList) {
		Preconditions.checkNotNull(chunkList);
		
		Map<String, Object> json = Maps.newHashMap();
		json.put(SearchEngineService.FIELD_FIRST_SENTENCE_PLAIN_TEXT, chunkList.getFirstSentencePlainText());
		json.put(SearchEngineService.FIELD_FIRST_SENTENCE_BASE_PLAIN_TEXT, chunkList.getFirstSentenceBasePlainText());
		json.put(SearchEngineService.FIELD_PLAIN_TEXT, chunkList.getPlainText());
		json.put(SearchEngineService.FIELD_BASE_PLAIN_TEXT, chunkList.getBasePlainText());
		
		try {
			return mapper.writeValueAsString(json);
		} catch (JsonGenerationException e) {
			LOGGER.error(e, e);
		} catch (JsonMappingException e) {
			LOGGER.error(e, e);
		} catch (IOException e) {
			LOGGER.error(e, e);
		}
		return null;
	}

}
