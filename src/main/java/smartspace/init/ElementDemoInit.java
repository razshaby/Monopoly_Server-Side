package smartspace.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import smartspace.dao.ElementDao;
import smartspace.data.ElementEntity;
import smartspace.data.Location;

@Component
@Profile("production")
public class ElementDemoInit implements CommandLineRunner{
	private final long MIN_PRICE = 50;
	private final long MAX_PRICE = 400;
	private ElementDao<String> elementDao;
	private ArrayList<String> citiesName ;
	
	
	@Autowired
	public ElementDemoInit(ElementDao<String> elementDao) {
		this.elementDao = elementDao;
		citiesName = new ArrayList<String>(Arrays.asList("Tel Aviv","Jerusalem","Beer Sheva","Ramat Gan","Netanya",
				"Haifa","Givataym","Eilat","Holon","Petah Tikva","Raanana","ashkelon"));
	}


	@Override
	public void run(String... args) throws Exception {
//		createElement("element1");
//		createElement("element2");
//		createElement("element3");
//		createGameElement();
//		createCities();
	}
	


	private void createElement(String name) {
		Random r = new Random();
		double randomX = 0 + (10 - 0) * r.nextDouble();
		double randomY = 0 + (10 - 0) * r.nextDouble();
		
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");
        properties.put("myLoc", new Location(10.0, 20.0));

		
		
		ElementEntity elementEntity = new ElementEntity();
		elementEntity.setCreatorSmartspace("2019b.meytal");
		elementEntity.setName(name);
		elementEntity.setExpired(false);
		elementEntity.setType("type");
		elementEntity.setCreatorEmail("test@test.com");
		elementEntity.setLocation(new Location(randomX,randomY));
		elementEntity.setMoreAttributes(properties);

		this.elementDao.create(elementEntity);
	}
	
	private void createGameElement() {
		ElementEntity elementEntity = new ElementEntity();
		elementEntity.setCreatorSmartspace("2019b.meytal");
		elementEntity.setName("monopoly");
		elementEntity.setExpired(false);
		elementEntity.setType("game");
		elementEntity.setCreatorEmail("manager@test.com");
		this.elementDao.create(elementEntity);
	}
	
	private void createCities() {
		
		for(int i=0; i<citiesName.size(); i++) {
			Random r = new Random();
			long randomPrice =  MIN_PRICE +(long)(r.nextDouble()*(MAX_PRICE-MIN_PRICE));

			ElementEntity elementEntity = new ElementEntity();
			elementEntity.setName(citiesName.get(i));
			elementEntity.setExpired(false);
			elementEntity.setType("city");
			elementEntity.setCreatorSmartspace("2019b.meytal");
			elementEntity.setCreatorEmail("manager@test.com");
			elementEntity.setLocation(new Location(i,0));
		

			ArrayList<String> array = new ArrayList<String>();
			array.add("2019b.meytal|player4@test.com");

			
			HashMap<String, Object> moreAttributes= new HashMap<String, Object>();
			moreAttributes.put("price", randomPrice);
			moreAttributes.put("fine", (long)(randomPrice/2));
			moreAttributes.put("ownerId", "");
			moreAttributes.put("ownerName", "");
			moreAttributes.put("visitors", new ArrayList<String>());
			elementEntity.setMoreAttributes(moreAttributes);
			
			this.elementDao.create(elementEntity);
		}
		
	}

	
}
