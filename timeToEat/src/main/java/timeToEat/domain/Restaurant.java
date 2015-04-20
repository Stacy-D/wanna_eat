package timeToEat.domain;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
/**
 * Restaurant entity
 * @author Stacy
 *
 */
@Entity
public class Restaurant {
	private static final List<String> DEFAULT_CUISINE = ImmutableList.of("Не визначено");
	/**
     *  Use restaurantId as the datastore key.
     */
	@Id
	private Long restaurantId;
	/**
	 * Cuisine of the restaurant
	 */
	@Index
	private ArrayList<String> cuisine;
	private List<String> address;
	@Index
	private String name;
	/**
	 * location of the restaurant
	 */
	@Index
	private ArrayList<GeoPt> location;
	private double maxPrice;
	private double minPrice;
	@Index
	private boolean network;
	private boolean discountAvailable;
	
	private Restaurant(){}
	public Restaurant(long restaurantId,String displayName,double maxPrice,double minPrice,String address)
	{
		this.address = new ArrayList<String>();
		if(address!=null) this.address.add(address);
		this.restaurantId = restaurantId;
		cuisine = new ArrayList<String>();
		this.name = displayName;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
		this.location = new ArrayList<GeoPt>();
		network = false;
	}
	public void networkAvailable(){
		if(this.location.size() > 1 )network = true;
		else network = false;
	}
	public Long getRestaurantId(){
		return this.restaurantId;
	}
	public List<String> getCuisine(){
		return cuisine.size() == 0 ? DEFAULT_CUISINE : ImmutableList.copyOf(cuisine);
	}
	/**
	 * removes Cuisine tag
	 * @param cui
	 * @return
	 */
	public boolean removeCuisineTag(String cui){
		return this.cuisine.remove(cui);
	}
	/**
	 * checks Cuisine tag
	 * @param cui
	 * @return
	 */
	public boolean checkCuisineTag(String cui){
		if(cui.equals("Не визначено")) return false;
		return this.cuisine.contains(cui);
	}
	/**
	 * adds Cuisine tag
	 * @param cui
	 * @return
	 */
	public void addCuisineTag(String cui){
		if(cui.equals("Не визначено") || cuisine.contains(cui)) return;
		cuisine.add(cui);
	}
	/**
	 * adds new GeoPt location of the restaurant
	 * @param newLoc
	 */
	public void addLocation(GeoPt newLoc){
		if(newLoc != null &&!location.contains(newLoc))
			location.add(newLoc);
	}
	/**
	 * checks  GeoPt location of the restaurant
	 * @param newLoc
	 */
	public boolean checkLocation(GeoPt newLoc){
		if(newLoc == null) return false;
		return this.location.contains(newLoc);
	}
	/**
	 * removes GeoPt location of the restaurant
	 * @param newLoc
	 */
	public boolean removeLocation(GeoPt newLoc){
		return this.location.remove(newLoc);
	}
	/**
	 * adds new String location of the restaurant
	 * @param newLoc
	 */
	public void addAddress(String newLoc){
		if(newLoc != null &&!address.contains(newLoc))
			address.add(newLoc);
	}
	/**
	 * checks  String location of the restaurant
	 * @param newLoc
	 */
	public boolean checkAddress(String newLoc){
		if(newLoc == null) return false;
		return this.address.contains(newLoc);
	}
	/**
	 * removes String location of the restaurant
	 * @param newLoc
	 */
	public boolean removeAddress(String newLoc){
		return this.address.remove(newLoc);
	}
	/**
	 * 
	 * @return max price
	 */
	public double getMaxPrice(){
		return this.maxPrice;
	}
	/**
	 * 
	 * @return min price
	 */
	public double getMinPrice(){
		return this.minPrice;
	}
	/**
	 * 
	 * @return network name
	 */
	public boolean getNetwork(){
		return this.network;
	}
	/**
	 * 
	 * @return if discount is available in the restaurant
	 */
	public boolean getDiscountAvailable(){
		return this.discountAvailable;
	}
	/**
	 * 
	 * @return immutable list of locations
	 */
	public List<GeoPt> getLocation() {
		return ImmutableList.copyOf(location);
	}
	/**
	 * 
	 * @return immutable list of address
	 */
	public List<String> getAddress() {
		return ImmutableList.copyOf(address);
	}
	/**
	 * 
	 * @return name of the rastaurant
	 */
	public String getDisplayName() {
		return name;
	}
    public String getWebsafeKey() {
        return Key.create(Restaurant.class, restaurantId).getString();
    }
    /**
     * Updates price and network name
     * @param maxPriceN
     * @param minPriceN
     * @param networkN
     */
    public void update(double maxPriceN, double minPriceN){
    	if(maxPriceN!= this.maxPrice || maxPriceN == 0) this.maxPrice = maxPriceN;
    	if(minPriceN!= this.minPrice || minPriceN == 0) this.minPrice = minPriceN;
    }
    
}
