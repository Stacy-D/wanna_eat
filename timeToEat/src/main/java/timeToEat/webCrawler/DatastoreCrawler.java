package timeToEat.webCrawler;

import static timeToEat.service.OfyService.factory;
import static timeToEat.service.OfyService.ofy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import timeToEat.domain.RestLink;

import com.googlecode.objectify.Key;

/**
 * @author TasjaG
 * 
 * A web crawler for gathering information about restaurants
 * from the site and updating the main DB.
 * 
 * Screen scraping is implemented using the JSoup library.
 * The links are taken from the DATASTORE DB.
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
public class DatastoreCrawler {
	/** The pause between requests to the site in milliseconds - DO NOT EDIT*/
	protected static int PAUSE = 10000;
	
	/** The page to get links from */
	private static final String PAGE = "http://chicken.kiev.ua/listall.phtml";
	private static Logger LOG = Logger.getLogger(DatastoreCrawler.class.getName());
	
	/** Creates a crawler object */
	public DatastoreCrawler() throws SQLException
	{
	//	PAGE = "http://chicken.kiev.ua/listall.phtml";
	}
	
	/** Deletes the old crawl and clears the*/
	public static boolean startCrawl() throws SQLException, IOException {
		// filling the crawler's database with links to crawl
		//getLinksDatastore();
		
		// scraping every page saved in the crawler's DB
		// for info and dynamically updating the main DB
		//startScraping();

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
	public static void startScraping(int k) {
		try {
			List<RestLink> mainQuery = timeToEat.service.OfyService.ofy().load().type(RestLink.class).list();
			List<RestLink> query = new ArrayList<RestLink>(50);
			for(int i = k; i<k+50;i++){
			query.add(mainQuery.get(i));
			}
			for(RestLink link: query){
				scrapePage(link.getLink());
				// the pause between requests - DO NOT REMOVE
				try {
				    Thread.sleep(PAUSE);
				} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
				// the pause between requests - DO NOT REMOVE
			}
	    }
	    catch (Exception e)	{
	    	System.err.println(e.getMessage());
	    	}	
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

		String kitchen = "";
		if(strTable.contains("<i>Кухня</i>: ")) {
			int idx = strTable.lastIndexOf("<i>Кухня</i>: ");
			String sub = strTable.substring(idx);
			int i;
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ' '))
					break;
			sub = sub.substring(i+1);
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ';') || (sub.charAt(i) == '<'))
					break;
			kitchen = sub.substring(0, i);
		}
		
		int minPrice = 0;
		int maxPrice = 0;
		if(strTable.contains("<i>Стоимость</i>: ")) {
			String price = "";
			String minP = "";
			String maxP = "";
			int idx = strTable.lastIndexOf("<i>Стоимость</i>: ");
			String sub = strTable.substring(idx);
			int i;
			for(i=0; i<sub.length(); i++)
				if((sub.charAt(i) == ' '))
					break;
			sub = sub.substring(i+1);
			
			if(sub.contains("от $") && sub.contains("до $")) {
				
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
			else if(sub.contains("от $")) {
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
			else if(sub.contains("до $")) {
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
		if(strTable.contains("<i>Адрес</i>: ")) {
			int idx = strTable.lastIndexOf("<i>Адрес</i>: ");
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
		LOG.info("I can read you!!!!!!!!! "+ strName+"address : "+kitchen);
		if(!address.contains("Киев,")) return;
		
		timeToEat.service.LoadService.addRestRecord(strName, kitchen, maxPrice, minPrice, address);
	}
	
	/** Gathers links of restaurants from a page into Datastore */
	private static void getLinksDatastore() throws SQLException, IOException{
		
		// clears the DB of old links
		List<Key<RestLink>> keys = ofy().load().type(RestLink.class).keys().list();
		ofy().delete().keys(keys).now();
			
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
				
				//Restaurant temp = ofy().load().type(Restaurant.class).filter("name", name).first().now();
				
				// storing the URL to database
				Key<RestLink> linkKey = factory().allocateId(RestLink.class);
				long linkId = linkKey.getId();
				RestLink rLink = new RestLink(linkId, URL);
				ofy().save().entity(rLink).now();
			}
		}
	}

	//*    ---------------------    TEST METHODS    -----------------------   */
	//*    ----------------------------------------------------------------   */
	//*    ----------------------------------------------------------------   */
	
	/** Tests the updating of the main DB using a small number of pages */
	public static void startCrawlTest() throws SQLException, IOException {
		// filling the crawler's database with links to crawl
		//getLinksDatastore();	
		
		// scraping every page saved in the crawler's DB
		// for info and dynamically updating the main DB
		startScrapingTest();
		
		System.out.println("TEST CRAWL COMPLETED");
	}
	public static void startTestLink() throws SQLException, IOException{
		getLinksDatastore();	
	}
	
	/** Tests scraping with a small number of pages  */
	protected static void startScrapingTest() {		
		try {
			List<RestLink> query = timeToEat.service.OfyService.ofy().load().type(RestLink.class).list();
			for(RestLink k: query){
				LOG.info(String.valueOf(k.getLinkID()));
			}
			int t = 3;
			for(RestLink li:query){
				if(t-- <0) break;

					String link = li.getLink();
					LOG.info(link);

					scrapePage(link);

					try {
						Thread.sleep(PAUSE);
					} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
				}
			}
	    catch (Exception e)	{ System.err.println(e.getMessage()); }	
	}
	public static void testLoadService(){
		String one = "http://chicken.kiev.ua/restoran.phtml?file=murakami%2C__pr-t_pobedy";
		scrapePage(one);
		
		// the pause between requests - DO NOT REMOVE
		try {
			Thread.sleep(PAUSE);
		} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		one = "http://chicken.kiev.ua/restoran.phtml?file=murakami_hmelnickogo";
scrapePage(one);
		
		// the pause between requests - DO NOT REMOVE
		try {
			Thread.sleep(PAUSE);
		} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		one = "http://chicken.kiev.ua/restoran.phtml?file=murakami_krasnoarmejskaya";
		scrapePage(one);
	}


}
