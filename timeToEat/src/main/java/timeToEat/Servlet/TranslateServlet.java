package timeToEat.Servlet;
import timeToEat.domain.*;

import java.util.*;
import java.util.logging.Logger;

import static timeToEat.service.OfyService.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class TranslateServlet extends HttpServlet {
	private final static Logger LOG = Logger.getLogger(TranslateServlet.class.getName());
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		Translate.setClientId("Client id");
	    Translate.setClientSecret("Secret key");
	    String cuisine = null;
		List<Restaurant> rests = ofy().load().type(Restaurant.class).list();
		for(Restaurant res: rests){
			List<String> kitchen = res.getCuisine();
			for(String cui: kitchen){
		 try {
			String ukrTranslation = Translate.execute(cuisine, Language.UKRAINIAN);
			res.removeCuisineTag(cui);
			res.addCuisineTag(ukrTranslation);
		} catch (Exception e) {
			LOG.info("Error in translation: "+cui+"   "+e.getMessage());
		}
		 }
			ofy().save().entity(res).now();
		 }
	}

}
