package pl.wwiizt.wordnet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum WordnetJDBC {

	INSTANCE("connection.properties");

	private Connection connection;
	private Statement statement;

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
			statement = connection.createStatement();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getSynonyms(String lemma) {
		List<String> result = new ArrayList<>();

		ResultSet resultSet;
		try {
			StringBuilder sb = new StringBuilder("select lemma from lexicalunit where id  in (");
			sb.append("select parent_id from lexicalrelation where rel_id not in (");
			sb.append("select id from relationtype where name like 'antonimia%') and child_id in (");
			sb.append("select id from lexicalunit where lemma = '");
			sb.append(lemma);
			sb.append("'));");

			resultSet = statement.executeQuery(sb.toString());

			while (resultSet.next()) {
				result.add(resultSet.getString("lemma"));
			}

			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
}
