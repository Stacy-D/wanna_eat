package timeToEat.form;




import java.util.logging.Logger;

import timeToEat.domain.Restaurant;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.cmd.Query;

import static timeToEat.service.OfyService.factory;
import static timeToEat.service.OfyService.ofy;

public class RestaurantQueryForm {
	
	private final static Logger LOG = Logger.getLogger(RestaurantQueryForm.class.getName());
	
	
    /**
     * Returns an Objectify Query object for the specified filters.
     *
     * @return an Objectify Query.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Query<Restaurant> getQuery(){
		Query<Restaurant> placesQuery = ofy().load().type(Restaurant.class);
		LOG.info(placesQuery.toString());
		return placesQuery;
	}

}
