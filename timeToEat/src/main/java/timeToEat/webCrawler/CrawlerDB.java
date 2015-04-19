package timeToEat.webCrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author TasjaG
 * 
 * A class for handling actions related
 * to the database for the crawler.
 * 
 * DB URL: jdbc:mysql://localhost:3306/crawler
 * DB LOGIN: root
 * DB PASSWORD: d3fKd2TZCyqRweNu
 */
public class CrawlerDB {

	/** The DB URL*/
	private static final String URL = "jdbc:mysql://localhost:3306/crawler";
	
	/** Connection object - visible inside the package */
	Connection conn = null;
	 
	/** Creates a DB object and makes a connection with the bot's database */
	public CrawlerDB() {		
		try {
		
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(URL, "root", "d3fKd2TZCyqRweNu");
			System.out.println("conn built");
		
		} 
		catch (SQLException e)           {	e.printStackTrace();	} 
		catch (ClassNotFoundException e) {	e.printStackTrace();	}	
	}
 
	/** Executes a query to the DB */
	public ResultSet runSql(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.executeQuery(sql);
	}
	
	/** Sends a statement to the DB */
	public boolean runSql2(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.execute(sql);
	}
 
	@Override
	/** Closes the connection with the DB */
	protected void finalize() throws Throwable {
		if (conn != null || !conn.isClosed())
			conn.close();
	}
	
}
