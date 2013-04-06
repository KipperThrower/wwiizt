package pl.wwiizt.search.service;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import pl.wwiizt.ccl.model.ChunkList;
import pl.wwiizt.ccl.service.CclService;
import pl.wwiizt.json.service.JsonService;

import static org.elasticsearch.node.NodeBuilder.*;



@Service
public class SearchEngineService {
	
	public static final String INDEX_NAME = "wiki";
	public static final String TYPE_NAME = "wiki";
	
	public static final String FIELD_FIRST_SENTENCE_BASE_PLAIN_TEXT = "firstSentenceBasePlainText";
	public static final String FIELD_FIRST_SENTENCE_PLAIN_TEXT = "firstSentencePlainText";
	public static final String FIELD_BASE_PLAIN_TEXT = "basePLainText";
	public static final String FIELD_PLAIN_TEXT = "plainText";
	
	public static final float BOOST_FIELD_FIRST_SENTENCE_BASE_PLAIN_TEXT = 2.5f;
	public static final float BOOST_FIELD_FIRST_SENTENCE_PLAIN_TEXT = 2;
	public static final float BOOST_FIELD_BASE_PLAIN_TEXT = 1.5f;
	public static final float BOOST_FIELD_PLAIN_TEXT = 1;
	
	private static final Logger LOGGER = Logger.getLogger(SearchEngineService.class);
	
	@Autowired
	private JsonService jsonService;
	@Autowired
	private CclService cclService;	
	
	public void index(ChunkList chunkList) {
		Preconditions.checkNotNull(chunkList);
		
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		IndexResponse response = client.prepareIndex(INDEX_NAME, TYPE_NAME, chunkList.getFileName())
			.setSource(jsonService.getJson(chunkList))
			.execute()
			.actionGet();

		node.close();
	}
	
	public void index(File dir) {
		Preconditions.checkNotNull(dir);
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new XmlFileFilter());
			if (files != null) {
				for(File file : files) {
					ChunkList cl = cclService.loadFile(file);
					if (cl != null) {
						IndexResponse response = client.prepareIndex(INDEX_NAME, TYPE_NAME, cl.getFileName())
								.setSource(jsonService.getJson(cl))
								.execute()
								.actionGet();
						LOGGER.info("File indexed: " + cl.getFileName());
					}
				}
			}
		}
		node.close();
	}
	
	public List<String> search(String query) {
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		QueryBuilder builder = QueryBuilders.boolQuery()
//				.should(QueryBuilders.matchPhraseQuery(FIELD_FIRST_SENTENCE_BASE_PLAIN_TEXT, query).boost(BOOST_FIELD_FIRST_SENTENCE_BASE_PLAIN_TEXT))
				.should(QueryBuilders.matchPhraseQuery(FIELD_FIRST_SENTENCE_PLAIN_TEXT, query).boost(BOOST_FIELD_FIRST_SENTENCE_PLAIN_TEXT))
//				.should(QueryBuilders.matchPhraseQuery(FIELD_BASE_PLAIN_TEXT, query).boost(BOOST_FIELD_BASE_PLAIN_TEXT))
				.should(QueryBuilders.matchPhraseQuery(FIELD_PLAIN_TEXT, query).boost(BOOST_FIELD_PLAIN_TEXT));
				
		
		SearchResponse response = client.prepareSearch(INDEX_NAME)
		        .setTypes(TYPE_NAME)
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(builder)
		        .setFrom(0)
		        .setSize(10)
		        .setExplain(true)
		        .execute()
		        .actionGet();

		node.close();
		List<String> result = Lists.newArrayList();
		for (SearchHit sh: response.getHits()) {
			result.add(sh.getId());
		}
		return result;
	}
	
	private class XmlFileFilter implements FileFilter {
		
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	}

}