package pl.wwiizt.wordnet;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum WordnetJDBC {

	INSTANCE("connection.properties");

	private Connection connection;

	WordnetJDBC(String propertiesFileName) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Properties properties = new Properties();

			try {
				properties.load(WordnetJDBC.class.getClassLoader().getResourceAsStream(propertiesFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}

			String url = properties.getProperty("url", "jdbc:mysql://localhost:3306/wordnet");
			String user = properties.getProperty("user", "root");
			String password = properties.getProperty("password");
			StringBuffer connectionURL = new StringBuffer(url);
			connectionURL.append("?user=");
			connectionURL.append(user);

			if (password != null && !password.isEmpty()) {
				connectionURL.append("&password=");
				connectionURL.append(password);
			}

			connection = DriverManager.getConnection(connectionURL.toString().trim());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//to wyrzuca lematy, ktore sa polaczone z tym jakas relacja, tylko nie antonimem
	public List<String> getAllLexInRelationsExceptAntonims(String lemma) {
		List<String> result = new ArrayList<>();
		String query = "select distinct lemma from lexicalunit where id  in (select parent_id from lexicalrelation where rel_id not in (select id from relationtype where name like 'antonimia%') and child_id in (select id from lexicalunit where lemma = ? ));";

		try (CallableStatement call = connection.prepareCall(query)) {
			call.setString(1, lemma);

			ResultSet resultSet = call.executeQuery();

			while (resultSet.next())
				result.add(resultSet.getString("lemma"));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	//to wyrzuca lematy, ktore sa w tym samym synsecie
	public List<String> getLexFromTheSameSynset(String lemma) {
		List<Long> synsetsWithThisLemma = getSynsetsWithThisLemma(lemma);
		List<String> result = new ArrayList<>();

		if (synsetsWithThisLemma.isEmpty())
			return result;
					
		//jdbc nie ma chyba jakiegos IN normalnego
		StringBuilder query = new StringBuilder("select distinct lemma from lexicalunit where id in (select lex_id from unitandsynset where syn_id in (");

		for (int i = 0; i < synsetsWithThisLemma.size() - 1; i++) {
			query.append(synsetsWithThisLemma.get(i));
			query.append(", ");
		}

		query.append(synsetsWithThisLemma.get(synsetsWithThisLemma.size() - 1));
		query.append("))");

		try (PreparedStatement lexes = connection.prepareCall(query.toString())) {
			ResultSet resultSet = lexes.executeQuery();

			while (resultSet.next())
				result.add(resultSet.getString("lemma"));

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	//synsety, ktore maja w sobie lemma
	private List<Long> getSynsetsWithThisLemma(String lemma) {
		List<Long> synsetsWithLemma = new ArrayList<>();

		try (PreparedStatement synId = connection.prepareCall("select syn_id from unitandsynset where lex_id in (select id from lexicalunit where lemma = ?)")) {
			synId.setString(1, lemma);
			ResultSet rs = synId.executeQuery();

			while (rs.next())
				synsetsWithLemma.add(rs.getLong("syn_id"));

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return synsetsWithLemma;
	}

}
