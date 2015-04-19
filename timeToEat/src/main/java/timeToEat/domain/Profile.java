package timeToEat.domain;
import java.util.*;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;


@Entity
public class Profile {
	/**
     *  Use userId as the datastore key.
     */
	@Id
	private String userId;
	  /**
     * Any string user wants us to display him/her on this system.
     */
	private String displayName;
    /**
     * User's main e-mail address.
     */
	private String mainEmail;
	
	private List<String> favouritePlaces = new ArrayList<>(0);
	
	private Profile(){}
	public Profile(String userId,String displayName,String mainEmail){
		this.userId = userId;	
		this.displayName = displayName;
		this.mainEmail = mainEmail;
	}
    /**
     * Getter for favouritePlaces.
     * @return an immutable copy of favouritePlaces.
     */
    public List<String> getFavouritePlaces() {
        return ImmutableList.copyOf(favouritePlaces);
    }
    /**
     * Adds a RestaurantId to favouritePlaces.
     * @param restaurantKey a websafe String representation of the Restaurant Key.
     */
    public void addToFavouritePlaces(String restaurantKey) {
        favouritePlaces.add(restaurantKey);
    }
    /**
     * Getter for userId.
     * @return userId.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Getter for displayName.
     * @return displayName.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Getter for mainEmail.
     * @return mainEmail.
     */
    public String getMainEmail() {
        return mainEmail;
    }
	public void update(String nameToChange) {
		if (nameToChange!= null)
	this.displayName = nameToChange;		
	}
    /**
     * Remove the restaurantId from favouritePlaces.
     *
     * @param restaurantKey a websafe String representation of the Restaurant Key.
     */
    public void removePlaceFromFavourite(String restaurantKey) {
        if (favouritePlaces.contains(restaurantKey)) {
            favouritePlaces.remove(restaurantKey);
        } else {
            throw new IllegalArgumentException("Invalid restaurantKey: " + restaurantKey);
        }
    }
}
