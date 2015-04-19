package timeToEat.test;
import java.util.*;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import timeToEat.domain.Profile;
import timeToEat.domain.Restaurant;

/**
 * Tests for Profile POJO.
 */
public class ProfileTest {

    private static final String EMAIL = "example@gmail.com";

    private static final String USER_ID = "123456789";


    private static final String DISPLAY_NAME = "Your Name Here";

    private Profile profile;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        profile = new Profile(USER_ID, DISPLAY_NAME, EMAIL);
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
        assertEquals(EMAIL, profile.getMainEmail());
    }

    @Test
    public void testUpdate() throws Exception {
        String newDisplayName = "New Display Name";
        profile.update(newDisplayName);
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(newDisplayName, profile.getDisplayName());
        assertEquals(EMAIL, profile.getMainEmail());
    }
    @Test
    public void testListValues() throws Exception {
        List<String> restaurantKeys = new ArrayList<>();
        assertEquals(restaurantKeys, profile.getFavouritePlaces());
        Key<Restaurant> restaurantKey = Key.create(Restaurant.class, "465873");
        profile.addToFavouritePlaces(restaurantKey.getString());
        restaurantKeys.add(restaurantKey.getString());
        assertEquals(restaurantKeys, profile.getFavouritePlaces());
    }
}
