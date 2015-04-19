package timeToEat.test;

import static org.junit.Assert.*;

import org.junit.*;

import timeToEat.domain.Profile;
import timeToEat.form.ProfileForm;
import timeToEat.spi.Time2EatApi;
import static timeToEat.service.OfyService.ofy;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;

public class TimeToEatApiTest {

    private static final String EMAIL = "example@gmail.com";

    private static final String USER_ID = "123456789";


    private static final String DISPLAY_NAME = "Your Name Here";

    private static final String NAME = "GCP Live";

    private User user;

    private Time2EatApi timeToEatApi;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        user = new User(EMAIL, "gmail.com", USER_ID);
        timeToEatApi = new Time2EatApi();
    }

    @After
    public void tearDown() throws Exception {
        ofy().clear();
        helper.tearDown();
    }
    @Test(expected = UnauthorizedException.class)
    public void testGetProfileWithoutUser() throws Exception {
    	timeToEatApi.getProfile(null);
    }
    @Test
    public void testSaveProfile() throws Exception {
        Profile profile = timeToEatApi.saveProfile(
                user, new ProfileForm(DISPLAY_NAME));
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
        profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
    }
    @Test
    public void testSaveProfileWithNull() throws Exception {
        // Save the profile for the first time with null values.
        Profile profile = timeToEatApi.saveProfile(user, new ProfileForm(null));
        String displayName = EMAIL.substring(0, EMAIL.indexOf("@"));
        // Check the return value first.
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(displayName, profile.getDisplayName());
        // Fetch the Profile via Objectify.
        profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(displayName, profile.getDisplayName());
    }

    @Test
    public void testGetProfile() throws Exception {
    	timeToEatApi.saveProfile(user, new ProfileForm(DISPLAY_NAME));
        // Fetch the Profile via the API.
        Profile profile = timeToEatApi.getProfile(user);
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
    }

    @Test
    public void testUpdateProfile() throws Exception {
        // Save for the first time.
    	timeToEatApi.saveProfile(user, new ProfileForm(DISPLAY_NAME));
        Profile profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
        // Then try to update it.
        String newDisplayName = "New Name";
        timeToEatApi.saveProfile(user, new ProfileForm(newDisplayName));
        profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(newDisplayName, profile.getDisplayName());
    }

    @Test
    public void testUpdateProfileWithNulls() throws Exception {
    	timeToEatApi.saveProfile(user, new ProfileForm(DISPLAY_NAME));
        Profile profile = timeToEatApi.saveProfile(user, new ProfileForm(null));
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
        profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        assertEquals(USER_ID, profile.getUserId());
        assertEquals(EMAIL, profile.getMainEmail());
        assertEquals(DISPLAY_NAME, profile.getDisplayName());
    }
/*
    @Test
    public void testDistance(){
    	GeoPt one = new GeoPt(40.76f, -73.984f);
    	GeoPt two = new GeoPt(41.89f, 12.492f);
    	assert((6887.9044-timeToEatApi.haversineDistance(one, two))<0.0000000000000000000000001);
    }

*/

}
