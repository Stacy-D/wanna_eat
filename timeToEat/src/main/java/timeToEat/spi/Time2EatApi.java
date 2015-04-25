package timeToEat.spi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import timeToEat.Constants;
import timeToEat.domain.AppEngineUser;
import timeToEat.domain.Profile;
import timeToEat.domain.Restaurant;
import timeToEat.form.ProfileForm;
import timeToEat.form.RestaurantQueryForm;
import timeToEat.webCrawler.DatastoreCrawler;
import static timeToEat.service.OfyService.ofy;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;
import com.googlecode.objectify.*;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

/**
 * Defines conference APIs.
 */
@Api(name = "timeToEat", version = "v1", 
scopes = { Constants.EMAIL_SCOPE }, 
clientIds = { Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID },
description = "API for the #timeToEat Backend application.")
public class Time2EatApi {

	private static final Logger LOG = Logger.getLogger(Time2EatApi.class.getName());
    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    public Profile saveProfile(final User user, ProfileForm profileForm) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String mainEmail = user.getEmail();
        String displayName = profileForm.getDisplayName();
        Profile profile = ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
        if (profile == null) {
            if (displayName == null) {
                displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
            }
            profile = new Profile(getUserId(user), displayName, mainEmail);
        } else {
            profile.update(displayName);
        }
        ofy().save().entity(profile).now();
        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        return ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
    }
    /**
     * Returns list of Restaurants, which are closer than 2 km from current user location
     * @param myLocation
     * @return list of restaurants
     */
    @ApiMethod(name ="getRestaurants", path = "restaurant/getrestaurants", httpMethod = HttpMethod.POST)
    public List<Restaurant> getRestaurants(GeoPt myLocation){
    List<Restaurant> query = ofy().load().type(Restaurant.class).list();
    for(Restaurant rest:query){
    	if(closestLocation(myLocation,rest.getLocation())>2){
    		query.remove(rest);
    	}
    }
    return query;
    }
    /**
     * Calculation of distance using Haversine formula
     * @param myLocation
     * @param restaurantLoc
     * @return distance between two points 
     */
    private double haversineDistance(GeoPt myLocation, GeoPt restaurantLoc){
    	double earth = 6371; //Earth`s radius in km
    	double dlat = Math.toRadians(restaurantLoc.getLatitude()-myLocation.getLatitude());
    	double dlon = Math.toRadians(restaurantLoc.getLongitude() - myLocation.getLongitude());
    	double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(Math.toRadians(myLocation.getLatitude())) *
    			Math.cos(Math.toRadians(restaurantLoc.getLatitude())) * Math.sin(dlon/2) * Math.sin(dlon/2);
    	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    	return earth*c;
    }
    @ApiMethod(
            name = "addToFavourite",
            path = "restaurant/{websafeRestaurantKey}/like",
            httpMethod = HttpMethod.POST
    )
    public void addToFavourite(final User user, @Named("websafeRestaurantKey") final String websafeRestaurantKey) 
    		throws UnauthorizedException{
    	  if (user == null) {
              throw new UnauthorizedException("Authorization required");
          }
    	  final String userId = getUserId(user);
    	  Key<Restaurant> restaurantKey = Key.create(websafeRestaurantKey);
    	   Restaurant restaurant = ofy().load().key(restaurantKey).now();
           // 404 when there is no Restaurant with the given restaurantId.
           if (restaurant == null) {
               throw new NotFoundException();
           }
           Profile profile = getProfileFromUser(user, userId);
           
           if (profile.getFavouritePlaces().contains(websafeRestaurantKey)) return;
           else {
               profile.addToFavouritePlaces(websafeRestaurantKey);;
               ofy().save().entities(profile, restaurant).now();
    }
    }
           @ApiMethod(
        		   name = "removeFromFavourite",
                   path = "restaurant/{websafeRestaurantKey}/removelike",
                   httpMethod = HttpMethod.DELETE)
           public void removeFromFavourite(final User user, @Named("websafeRestaurantKey") final String websafeRestaurantKey) throws UnauthorizedException{
        	   if (user == null) {
                   throw new UnauthorizedException("Authorization required");
               }   
        	   final String userId = getUserId(user);
        	   Key<Restaurant> restaurantKey = Key.create(websafeRestaurantKey);
        	   Restaurant restaurant = ofy().load().key(restaurantKey).now();
        	   if (restaurant == null) {
                   throw new NotFoundException();
               }
        	   Profile profile = getProfileFromUser(user, userId);
               
               if (profile.getFavouritePlaces().contains(websafeRestaurantKey)) {
            	   profile.removePlaceFromFavourite(websafeRestaurantKey);
            	   ofy().save().entities(profile, restaurant).now();
               }

           }
    
    private static Profile getProfileFromUser(User user, String userId) {
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        if (profile == null) {
            String email = user.getEmail();
            profile = new Profile(userId, extractDefaultDisplayNameFromEmail(email), email);
        }
        return profile;
    }
    
    private static String getUserId(User user) {
        String userId = user.getUserId();
        if (userId == null) {
            LOG.info("userId is null, so trying to obtain it from the datastore.");
            AppEngineUser appEngineUser = new AppEngineUser(user);
            ofy().save().entity(appEngineUser).now();
            // Begin new session for not using session cache.
            Objectify objectify = ofy().factory().begin();
            AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
            userId = savedUser.getUser().getUserId();
            LOG.info("Obtained the userId: " + userId);
        }
        return userId;
    }
    /**
     * Returns a Restaurant object with the given restaurantId.
     *
     * @param websafeRestaurantKey The String representation of the Restaurant Key.
     * @return a Restaurant object with the given restaurantId.
     * @throws NotFoundException when there is no Restaurant with the given restaurantId.
     */
    @ApiMethod(
            name = "getRestaurant",
            path = "restaurant/{websafeRestaurantKey}",
            httpMethod = HttpMethod.GET
    )
    public Restaurant getRestaurant(@Named("websafeRestaurantKey") final String websafeRestaurantKey)
            throws NotFoundException {
        Key<Restaurant> restaurantKey = Key.create(websafeRestaurantKey);
        Restaurant restaurant = ofy().load().key(restaurantKey).now();
        if (restaurant == null) {
            throw new NotFoundException();
        }
        return restaurant;
    }
    private double closestLocation(GeoPt myLocation, List<GeoPt> restaurantLoc){
    	if(restaurantLoc.size() == 1) return haversineDistance(myLocation, restaurantLoc.get(0));
    	GeoPt answer = restaurantLoc.iterator().next();
    	double distance = haversineDistance(myLocation, answer);
    	double temp = 0;
    	while(restaurantLoc.iterator().hasNext()){
    		temp = haversineDistance(myLocation, restaurantLoc.iterator().next());
    		if(distance > temp){ distance = temp;
    		}
    	}
    	return distance;
    }
    /**
     * Method for query Restaurants
     * @param resForm
     * @return
     */
    @ApiMethod(
    		name = "queryRestaurants",
    		path = "queryRestaurants",
    		httpMethod = HttpMethod.POST)
    public List<Restaurant> queryRestaurants(RestaurantQueryForm resForm){
    	 Iterable<Restaurant> restaurantIterable = resForm.getQuery();
         List<Restaurant> result = new ArrayList<>(0);
         for(Restaurant res: restaurantIterable )
         {
        	result.add(res); 
         }
         return result;
    }
    /**
     * This method may return HashMap like <Restaurant,GeoPt> in future
     * @param user
     * @param myLocation
     * @return
     * @throws UnauthorizedException
     */
    @ApiMethod(
    		name = "getClosestFavorite",
    		path = "getClosestFavorite",
    		httpMethod = HttpMethod.GET)
    public List<Restaurant> getClosestFavorite(final User user,GeoPt myLocation) throws UnauthorizedException{
    	if(user == null) throw new UnauthorizedException("Authorization required");
    	Profile profile = getProfile(user);
    	List<String> places = profile.getFavouritePlaces();
    	if(places.size() == 0) return new ArrayList<Restaurant>(0);
    	List<Restaurant> answer = new ArrayList<Restaurant>();
    	for(String webKey: places){
    		Key<Restaurant> restaurantKey = Key.create(webKey);
    		Restaurant temp = ofy().load().key(restaurantKey).now();
    		if(closestLocation(myLocation,temp.getLocation())<2)
    		{
    			answer.add(temp);
    		}
    	}
    	return answer;
    }
    @ApiMethod(
    		name ="checkNetwork",
    		path = "checkNetwork",
    		httpMethod = HttpMethod.GET)
    public void checkNetwork() throws IOException{
    	List<Restaurant> query = ofy().load().type(Restaurant.class).list();
    	for(Restaurant res: query){
    		res.networkAvailable();
    		ofy().save().entity(res).now();
    	}
    }
}

