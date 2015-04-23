package timeToEat.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.appengine.api.datastore.GeoPt;

import timeToEat.Servlet.TranslateServlet;

public class TranslateServletTest {

	@Test
	public void test() throws IOException {
		TranslateServlet e = new TranslateServlet();
		System.out.println(e.findAddress(new GeoPt(50.47156524658203f,30.47572135925293f)));
	}

}
