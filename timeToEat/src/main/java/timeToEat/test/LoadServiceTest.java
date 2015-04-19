package timeToEat.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import timeToEat.service.LoadService;

import com.google.appengine.api.datastore.GeoPt;

public class LoadServiceTest {

	@Test
	public void searchForLocationTest() throws IOException{
		GeoPt geo =new GeoPt(50.452718f,30.52017f);
		assertEquals(geo,LoadService.searchForLocation("Киев, ул Софиевская 14"));
		//System.out.println(("Итальянская, Японская".split("\\s*,\\s*"))[0]);
		//System.out.println("Итальянская,Японская".split("\\s*,\\s*")[1]);
	}

}
