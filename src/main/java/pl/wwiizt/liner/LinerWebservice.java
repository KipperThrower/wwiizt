package pl.wwiizt.liner;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;


@Service
public class LinerWebservice {

	private final static String ANALYZER_URL = "http://156.17.128.80/nlp/morphanalyser/document";
	private final static String TAGGER_URL = "http://156.17.128.80/nlp/tagger/document";
	private final static String NERD_URL = "http://156.17.128.80/nlp/nerws/document";

	//te sa do pobrania, wiec na koncu musi byc '/'
	private final static String DOCUMENT_STATUS_URL = "http://156.17.128.80/nlp/document/status/";
	private final static String GET_DOCUMENT_URL = "http://156.17.128.80/nlp/document/";

	//statusy
	private final static String STATUS_READY = "READY";
	private final static String STATUS_DUMPED = "DUMPED";

	//metody http
	private final static String POST = "POST";
	private final static String GET = "GET";

	private final static String TEXT_FORMAT = "text";

	private ObjectMapper mapper = new ObjectMapper();

	public String parse(String text) {
		String result = text;

		try {
			String analyzed = getDocument(ANALYZER_URL, text, TEXT_FORMAT);
			String tagged = getDocument(TAGGER_URL, analyzed, null);
			result = getDocument(NERD_URL, tagged, null);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return result;
	}

	private String getDocument(String url, String text, String format) throws Exception {
		String token = getToken(url, text, format);

		while (!isDocumentReady(token)) {
		}

		return connect(GET_DOCUMENT_URL + token, null, GET);
	}

	private String getToken(String uri, String input, String format) throws IOException {
		StringBuffer encodedInput = new StringBuffer("content=");
		encodedInput.append(URLEncoder.encode(input, "UTF-8"));

		if (format != null) {
			encodedInput.append("&input_format=");
			encodedInput.append(format);
		}

		String response = connect(uri, encodedInput.toString(), POST);

		Map<String, Object> map = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
		});

		return (String) map.get("data");
	}

	private String connect(String address, String params, String method) throws IOException {
		URL url = new URL(address);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Language", "pl");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		//send
		if (params != null) {
			connection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));

			DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
			writer.writeBytes(params);
			writer.flush();
			writer.close();
		}

		//response
		Scanner scanner = new Scanner(connection.getInputStream());
		StringBuffer response = new StringBuffer();

		while (scanner.hasNextLine()) {
			response.append(scanner.nextLine());
		}

		scanner.close();

		return response.toString();
	}

	private boolean isDocumentReady(String token) throws Exception {
		String response = connect(DOCUMENT_STATUS_URL + token, null, GET);
		Map<String, Object> map = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
		});

		String status = (String) map.get("data");

		if (STATUS_DUMPED.equals(status))
			throw new Exception("Document dumped!");

		return STATUS_READY.equals(status);
	}
}
