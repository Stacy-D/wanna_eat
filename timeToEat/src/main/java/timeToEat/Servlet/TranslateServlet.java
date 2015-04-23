package timeToEat.Servlet;
import timeToEat.domain.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static timeToEat.service.OfyService.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.GeoPt;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class TranslateServlet extends HttpServlet {
	private final static Logger LOG = Logger.getLogger(TranslateServlet.class.getName());
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		improveAddress();
	}
	
	private void improveTranslate(){
		List<Restaurant> rests = ofy().load().type(Restaurant.class).list();
		for(Restaurant res: rests){
			List<String> kitchen = res.getCuisine();
			for(String cui: kitchen){
				String temp = cui;
				if(temp.contains("ський")) temp = temp.replace("ський", "ська");
				else if(temp.contains("ської")) temp = temp.replace("ської", "ська");
				else if(temp.contains("олов")) temp ="домашня";
				else if(temp.contains("втора")) temp = "авторська";
				res.removeCuisineTag(cui);
				res.addCuisineTag(temp.toLowerCase());
				
			}
			ofy().save().entity(res).now();
		}
	}
	private void translateData(){
		Translate.setClientId("ID");
	    Translate.setClientSecret("KEY");
	    String cuisine = null;
		List<Restaurant> rests = ofy().load().type(Restaurant.class).list();
		int i = 5;
		for(Restaurant res: rests){
			if(i< 5) continue;
			i++;
			List<String> address = res.getAddress();
			for(String cui: address){
				String temp = cui;
				if(cui.contains("ул")&&!cui.contains("ул.")){
					temp = temp.replace("ул", "ул.");
				}
				else if(cui.contains("пр-кт")&&!cui.contains("пр-кт.")){
					temp = temp.replace("пр-кт", "пр-кт.");
				}
				else if(cui.contains("б-р")){
					temp = temp.replace("б-р", "бул.");
				}
		 try {
			String ukrTranslation = Translate.execute(temp,Language.RUSSIAN, Language.UKRAINIAN);
			res.removeAddress(cui);
			res.addAddress(ukrTranslation);
		} catch (Exception e) {
			LOG.info("Error in translation: "+cui+"   "+e.getMessage());
		}
		 }
			ofy().save().entity(res).now();
		 }
	}

	private void improveAddress(){
		List<Restaurant> rests = ofy().load().type(Restaurant.class).list();
		for(Restaurant res: rests){
			List<String> address = res.getAddress();
			int count = address.size();
			for(String tempAddress: address){
				if(tempAddress.contains("Київ, Україна")) continue;
			res.removeAddress(tempAddress);
			}
			if(count == res.getAddress().size()){ 
				LOG.info(res.getDisplayName()+" skipped");
				continue;}
			List<GeoPt> geopt = res.getLocation();
			LOG.info(res.getDisplayName());
			for(GeoPt loc: geopt){
				try {
					res.addAddress(findAddress(loc));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ofy().save().entity(res).now();
		}
		
	}
	public String findAddress(GeoPt coords) throws IOException {
		LOG.info(coords.toString());
		 final Geocoder geocoder = new Geocoder();
		 
		 GeocoderRequest geoReq = new GeocoderRequestBuilder().setLocation(new LatLng(String.valueOf(coords.getLatitude()),String.valueOf(coords.getLongitude()))).setLanguage("uk").getGeocoderRequest();
		LOG.info(geoReq.toString());
		 GeocodeResponse geocoderResponse = geocoder.geocode(geoReq);
	 	    List<GeocoderResult> someList = geocoderResponse.getResults();
	 	    LOG.info(String.valueOf(someList.size()));
	 	    GeocoderResult data = someList.get(0);
	 	  return  data.getFormattedAddress();
	}
	
}
