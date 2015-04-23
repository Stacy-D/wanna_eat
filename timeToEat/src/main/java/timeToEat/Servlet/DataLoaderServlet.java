package timeToEat.Servlet;

import timeToEat.webCrawler.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class for cron jobs
 * @author Stacy
 *
 */
public class DataLoaderServlet extends HttpServlet {
	
	  @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
		  call(Integer.valueOf(request.getParameter("number")));
		  
	  }
	  private void call(int k){
      DatastoreCrawler.startScraping(k);
	  }
	  

}
