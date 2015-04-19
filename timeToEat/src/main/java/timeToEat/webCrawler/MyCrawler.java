package timeToEat.webCrawler;

// main DB method
import timeToEat.domain.*;
import static timeToEat.service.OfyService.*;

// main DB method
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.googlecode.objectify.*;

/**
 * @author TasjaG
 * 
 * A web crawler for gathering information about restaurants
 * from the site and updating the main DB.
 * 
 * Screen scraping is implemented using the JSoup library.
 * The links are taken from the CRAWLER'S DB.
 * 
 * NOTES:
 * Requests to the site need to have pauses between them 
 * so that the crawler is not mistaken for a DDOS attack
 * 
 * DEFAULT PAUSE BETWEEN REQUESTS:  
 * 10 seconds
 * 
 * "robots.txt" on chicken.kiev.ua:
 * 
 * User-agent: *
 * Disallow: /viewzakaz/
 * Disallow: /admin/
 * Disallow: /myadmin/
 * Disallow: /phpmyadmin/
 * Disallow: /maryanlog.php
 * Disallow: /contact.phtml
 * 
 * User-agent: Slurp
 * Disallow: / 
 */
public class MyCrawler {
	
	/** The pause between requests to the site in milliseconds - DO NOT EDIT*/
	protected static int PAUSE = 10000;
	
	/** The database for the crawler, contains links to crawl */
	protected static final CrawlerDB cdb = new CrawlerDB();
	
	/** The page to get links from */
	private static String PAGE;
	
	/** Creates a crawler object and removes old data from the DB */
	public MyCrawler() throws SQLException
	{
		cdb.runSql2("TRUNCATE Record;");
		PAGE = "http://chicken.kiev.ua/listall.phtml";
	}
	
	/** Deletes the old crawl and clears the*/
	public static boolean startCrawl() throws SQLException, IOException {
		// filling the crawler's database with links to crawl
		getLinksAdmin();
		
		//copyLinks();
		//printLinks();			
		
		// scraping every page saved in the crawler's DB
		// for info and dynamically updating the main DB
		startScraping();
		
		//System.out.println("CRAWL COMPLETED");
		return true;
	}
		
	/**
	 *  Removes old records from the main DB before 
	 *  screen scraping each page in the crawler's DB
	 *  and dynamically updating the main DB. 
	 *  
	 *  It takes ~180 minutes to update the database 
	 *	DO NOT ADJUST THE TIME or you WILL get BANNED!
	 */
	protected static void startScraping() {
		try {
			String query = "SELECT * FROM `record`";
	 		Statement st = cdb.conn.createStatement();
	 		ResultSet rs = st.executeQuery(query);
	       
			while (rs.next())
			{
				String link = rs.getString("URL");
				scrapePage(link);
				
				// the pause between requests - DO NOT REMOVE
				try {
				    Thread.sleep(PAUSE);
				} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
				// the pause between requests - DO NOT REMOVE
			}
			st.close();
	    }
	    catch (Exception e)	{ System.err.println(e.getMessage()); }	
	}
	
	/** Gets information from a single web page in the crawler's DB
	 *  and creates a new Element which is then inserted into the main DB */
	protected static void scrapePage(String URL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			System.out.println("Error whilst fetching the URL");
			e.printStackTrace(); }
	
		// getting the restaurant's name
		Elements name = doc.getElementsByClass("name");
		String strName = name.text();
		
		//Unfinished page on chicken.kiev.ua CHECK
		if (name.equals("��") || name.contains("�����"))	
			return;
		
		// getting the table containing the rest of the data
		Elements tables = doc.getElementsByAttributeValue("width", "50%");
		Element table = tables.first(); 		// The one we need is the first one only
	
		String strTable = table.toString(); 
		//System.out.println(strTable);
		//System.out.println();
	
		String kitchen = "";
		if(strTable.contains("<i>�����</i>: ")) {
			int idx = strTable.lastIndexOf("<i>�����</i>: ");
			String sub = strTable.substring(idx);
			int i;
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ' '))
					break;
			sub = sub.substring(i+1);
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ',') || (sub.charAt(i) == ';') || (sub.charAt(i) == '<'))
					break;
			kitchen = sub.substring(0, i);
		}
		
		int minPrice = 0;
		int maxPrice = 0;
		if(strTable.contains("<i>���������</i>: ")) {
			String price = "";
			String minP = "";
			String maxP = "";
			int idx = strTable.lastIndexOf("<i>���������</i>: ");
			String sub = strTable.substring(idx);
			int i;
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ' '))
					break;
			sub = sub.substring(i+1);
			
			if(sub.contains("�� $") && sub.contains("�� $")) {
				
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == '$')
						break;
				sub = sub.substring(i+1);
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == ' ')
						break;
				minP = sub.substring(0, i);
				minPrice = Integer.parseInt(minP);
				sub = sub.substring(i+1);
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == '$')
						break;
				sub = sub.substring(i+1);
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == ' ')
						break;
				maxP = sub.substring(0, i);
				maxPrice = Integer.parseInt(maxP);
			}
			else if(sub.contains("�� $")) {
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == '$')
						break;
				sub = sub.substring(i+1);
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == ' ')
						break;
				minP = sub.substring(0, i);
				minPrice = Integer.parseInt(minP);
			}
			else if(sub.contains("�� $")) {
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == '$')
						break;
				sub = sub.substring(i+1);
				for(i=0; i<sub.length(); i++)
					if(sub.charAt(i) == ' ')
						break;
				maxP = sub.substring(0, i);
				maxPrice = Integer.parseInt(maxP);
			}
		}
		
		String address = "";
		if(strTable.contains("<i>�����</i>: ")) {
			int idx = strTable.lastIndexOf("<i>�����</i>: ");
			String sub = strTable.substring(idx);
			int i;
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ' '))
					break;
			sub = sub.substring(i+1);
			for(i=0; i<sub.length(); i++)
				if(sub.charAt(i) == '<')
					break;
			address = sub.substring(0, i-2);
		}
		
		// adding to main DB
		timeToEat.service.LoadService.addRestRecord(strName, kitchen, maxPrice, minPrice, address);
		
		/*
		System.out.println(strName);
		System.out.println("Kitchen: " + kitchen);
		System.out.println("Minimum price: " + minPrice);
		System.out.println("Maximum price: " + maxPrice);
		System.out.println("Adress: " + adress);
		System.out.println();
		*/
	}
	
	/** Gathers links of restaurants from a page into crawler's DB */
	private static void getLinksAdmin() throws SQLException, IOException{
		// clears the DB of old links
		cdb.runSql2("TRUNCATE Record;");
			
		try {
		    Thread.sleep(PAUSE);
		} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
			
		Document doc = Jsoup.connect(PAGE).get();		
		Elements questions = doc.select("a[href]");
		
		String URL;
		for(Element link: questions){
			
			if(link.attr("href").contains("restoran.phtml?file=")
					&& !link.attr("href").contains("#lunch")
					&& !link.attr("href").contains("list")
					&& !link.attr("href").contains("rayon")
					&& !link.attr("href").contains("kuh")
					&& !link.attr("href").contains("empty")
					&& !link.attr("href").contains("111") //Unfinished page on chicken.kiev.ua
					) {
				
				URL = link.attr("abs:href");
					
				// checking if the given URL is already in database
				String sql = "select * from Record where URL = '"+URL+"'";
				ResultSet rs = cdb.runSql(sql);
					
				// storing the URL to database to avoid parsing again
				sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";
				PreparedStatement stmt = cdb.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, URL);
				stmt.execute();	
			}
		}
	}
	
	/** Prints the list of links from database on the console */
	protected static void printLinks() {
		try
	    {
			String query = "SELECT * FROM `record`";
	 		Statement st = cdb.conn.createStatement();
	 		ResultSet rs = st.executeQuery(query);
	       
			while (rs.next())
			{
				int id = rs.getInt("RecordID");
				String link = rs.getString("URL");
				System.out.println(id + " " + link);
			}
			st.close();
	    }
	    catch (Exception e) { System.err.println(e.getMessage()); }
	}

	/** Copies the list of links from crawler's DB to DATASTORE */
	public static void copyLinks() {
		try
	    {
			String query = "SELECT * FROM `record`";
	 		Statement st = cdb.conn.createStatement();
	 		ResultSet rs = st.executeQuery(query);
	       
			while (rs.next())
			{
				String URL = rs.getString("URL");
				Key<RestLink> linkKey = factory().allocateId(RestLink.class);
				long linkId = linkKey.getId();
				RestLink link = new RestLink(linkId, URL);
				ofy().save().entity(link).now();
			}
			st.close();
	    }
	    catch (Exception e) { System.err.println(e.getMessage()); }
	}

	//*    ---------------------    TEST METHODS    -----------------------   */
	//*    ----------------------------------------------------------------   */
	//*    ----------------------------------------------------------------   */
	
	/** Tests the updating of the main DB using a small number of pages */
	public static void startCrawlTest() throws SQLException, IOException {
		// filling the crawler's database with links to crawl
		getLinksAdmin();
		
		// printing links to console
		//printLinks();			
				
		// scraping every page saved in the crawler's DB
		// for info and dynamically updating the main DB
		startScrapingTest();
		
		System.out.println("TEST CRAWL COMPLETED");
	}
	
	/** Tests scraping with a small number of pages  */
	protected static void startScrapingTest() {
		try {
			String query = "SELECT * FROM `record`";
			Statement st = cdb.conn.createStatement();
			ResultSet rs = st.executeQuery(query);
       
			//DIFFERENT HERE
			for(int i=0; i<10; i++) {
			//for(int i=0; i<30; i++) {
			//for(int i=0; i<50; i++) {
				rs.next();
			//DIFFERENT HERE
					
				String link = rs.getString("URL");
				//DIFFERENT HERE
				System.out.println(i);
				//DIFFERENT HERE
				scrapePage(link);
			
				// the pause between requests - DO NOT REMOVE
				try {
					Thread.sleep(PAUSE);
				} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
				// the pause between requests - DO NOT REMOVE
			}
			st.close();
    	}
    	catch (Exception e)	{ System.err.println(e.getMessage()); }	
	}

	//*    ----------------------------------------------------------------   */
	//*    ----------------------------------------------------------------   */
	//*    ---------------------   TEST METHODS END -----------------------   */
}
