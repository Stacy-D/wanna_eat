package timeToEat.test;
import static timeToEat.service.OfyService.ofy;
import static org.junit.Assert.*;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.*;

import timeToEat.domain.Profile;
import timeToEat.domain.Restaurant;
import timeToEat.form.RestaurantForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests for Conference POJO.
 */
public class RestaurantTest {
	private static final Long RES_ID =7357394759347L;
	private static final String RES_NAME = "Restaurant name";
	private static final double MAX_PRICE = 90.0;
	private static final double MIN_PRICE = 45.0;
	private static final boolean NETWORK = false;
	private static final List<GeoPt> LOCATION = new ArrayList<GeoPt>();
	private List<String> cuisine;
	private Restaurant restaurant;
	private RestaurantForm restaurantForm;
	   private final LocalServiceTestHelper helper =
	            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
	                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

	    @Before
	    public void setUp() throws Exception {
	        helper.setUp();
	        cuisine = new ArrayList<String>();
	        cuisine.add("Українська");
	        cuisine.add("Російська");
	        LOCATION.add(new GeoPt(45f,45f));
	        restaurant = new Restaurant(RES_ID, RES_NAME, MAX_PRICE,MIN_PRICE,"vsdahldk");
	    }

	    @After
	    public void tearDown() throws Exception {
	        helper.tearDown();
	    }
	    @Test
	    public void testRestaurant(){
	    	Restaurant restaurant = new Restaurant(RES_ID, RES_NAME, MAX_PRICE,MIN_PRICE,"sdvas");
	    	assertEquals(RES_ID, restaurant.getRestaurantId());
	    	assertEquals(RES_NAME,restaurant.getDisplayName());
	    	assert(MAX_PRICE == restaurant.getMaxPrice());
	    	assert(MIN_PRICE == restaurant.getMinPrice());
	    	assertEquals(NETWORK,restaurant.getNetwork());
	    	restaurant.addLocation(LOCATION.get(0));
	    	assertEquals(LOCATION,restaurant.getLocation());
	    }


}
