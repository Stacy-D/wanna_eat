package timeToEat.service;
import static timeToEat.service.OfyService.ofy;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static timeToEat.service.OfyService.factory;
import timeToEat.domain.Restaurant;
import timeToEat.webCrawler.DatastoreCrawler;

import com.google.appengine.api.datastore.GeoPt;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.googlecode.objectify.Key;
public class LoadService{
	private static Logger LOG = Logger.getLogger(LoadService.class.getName());
	/**
	 * Inserts new restaurant record into database or updates old one if it exists
	 * @param name
	 * @param kitchen
	 * @param maxPrice
	 * @param minPrice
	 * @param address
	 */
	public static void addRestRecord(String name, String kitchen, int maxPrice, int minPrice, String address){
		name = name.substring(name.indexOf('«')+1,name.indexOf('»'));
		if(name.contains("(")){
			name = name.substring(0, name.indexOf('('));
		}
		Restaurant temp = ofy().load().type(Restaurant.class).filter("name", name).first().now();
		if(temp == null){
			Key<Restaurant> restKey = factory().allocateId(Restaurant.class);
				temp = new Restaurant(restKey.getId(),name,maxPrice,minPrice,address);
				String[] cuisine = kitchen.split("\\s*,\\s*");
				for(int i = 0; i<cuisine.length;i++){
					if(!temp.checkCuisineTag(cuisine[i]))
						temp.addCuisineTag(cuisine[i]);
				}
		}		
		else{
			temp.addAddress(address);
			temp.networkAvailable();
		}
		try {
			GeoPt location = searchForLocation(address);
			if(!temp.checkLocation(location)){
				temp.addLocation(location);
			}
		} catch (IOException e) {
			LOG.info(e.getMessage());
		}
		ofy().save().entity(temp).now();
		
	}
	/**
	 * Geocoding address into lan, lng
	 * @param name
	 * @param address
	 * @return list of GeoPt
	 * @throws IOException
	 */
	public static GeoPt searchForLocation(String address) throws IOException{
		 final Geocoder geocoder = new Geocoder();
 	    GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(address).setLanguage("rus").getGeocoderRequest();
 	    GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
 	    List<GeocoderResult> someList = geocoderResponse.getResults();
 	    GeocoderResult data = someList.get(0);
 	    GeocoderGeometry data_2 = data.getGeometry();
		return new GeoPt(data_2.getLocation().getLat().floatValue(),data_2.getLocation().getLng().floatValue());
	}
}