package smartspace;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ElementEntity;
import smartspace.data.Location;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.layout.BoundaryLocation;
import smartspace.layout.ElementBoundary;
import smartspace.layout.UserKey;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class ElementManagerAndPlayerRestIntegrationTest {
	private Random r;
	@LocalServerPort
	private int port;

	private String baseUrl;
	private RestTemplate restTemplate;

	private AdvancedUserDao<String> userDao;
	private UserEntity userEntity;
	private AdvancedElementDao<String> elementDao;
	
	private ArrayList<String> citiesName ;
	
	@Autowired
	public void setElementAndUserDao(AdvancedElementDao<String> elementDao, AdvancedUserDao<String> userDao) {
		this.elementDao = elementDao;
		this.userDao = userDao;
	}

	@PostConstruct
	public void init() {
		teardown();
		setup();
		this.baseUrl = "http://localhost:" + port + "/smartspace/elements/" + this.userEntity.getUserSmartspace()
				+ "/" + this.userEntity.getUserEmail();
		this.restTemplate = new RestTemplate();
	}

	public void setup() {
		createManager();
		this.r = new Random(); 
	}

	private void createManager() {
		this.userEntity = new UserEntity();
		this.userEntity.setUserEmail("manager.creating.element@de.mo");
		this.userEntity.setUserSmartspace("2019b.meytal");
		this.userEntity.setRole(UserRole.MANAGER);
		this.userDao.create(userEntity);
	}
	
	@After
	public void teardown() {
		this.elementDao.deleteAll();
		this.userDao.deleteAll();
	}
	
	@Test
	public void createNewElement() {
		// GIVEN the database is empty
		
		// WHEN I POST new ElementBoundary with null key
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);

		String name = "demoElement";
		String type = "myType";
		ElementBoundary newElementBoundary = new ElementBoundary();
		newElementBoundary.setElementType(type);
		newElementBoundary.setName(name);
		newElementBoundary.setExpired(false);
		newElementBoundary.setLatlng(new BoundaryLocation(32.115, 84.817));
		newElementBoundary.setElementProperties(elementProperties);
		
		ElementBoundary elementResult = this.restTemplate.postForObject(this.baseUrl, newElementBoundary, ElementBoundary.class);
		
		// THEN the database contains the new element with valid key
		assertThat(elementResult.getKey()).isNotNull();
		String elementKey = elementResult.getKey().getSmartspace()+"|"+elementResult.getKey().getId();
		assertThat(this.elementDao.readById(elementKey)).isPresent();
		assertThat(elementResult.getCreator()).isNotNull();
		
		this.elementDao.deleteAll();
		
	}

	@Test
	public void updateElementWithNullKey() {
		// GIVEN the database contains element with null key
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);
		
		ElementEntity element = new ElementEntity("test",
				"myType",
				new Location(32.115, 84.817),
				new Date(),
				this.userEntity.getUserEmail(),
				this.userEntity.getUserSmartspace(),
				false,
				elementProperties);
		ElementEntity elementEntity = this.elementDao.create(element);
		String elementId = elementEntity.getElementId();
		String elementSmartspace  = elementEntity.getElementSmartspace();
		ElementBoundary elementBoundary = new ElementBoundary(elementEntity);
		elementBoundary.setKey(null);
		
		// WHEN manager update the element name
		elementBoundary.setName("newName");
		this.restTemplate.put(this.baseUrl+"/"+elementSmartspace+"/"+elementId, elementBoundary, ElementBoundary.class);
		
		// THEN the element updated
		ElementEntity result = this.elementDao.readAll().get(0);
		assertThat(result.getKey()).isNotEmpty();
		assertThat(result.getName()).isEqualTo("newName");
		
		this.elementDao.deleteAll();
	}
	
	@Test
	public void retrieveSpecificElement() {
		// GIVEN the database contains 3 elements
		this.elementDao.deleteAll();
		int size = 3;
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i).map(ElementEntity::new)
				.forEach(this.elementDao::create);
		
		ElementEntity elementEntity = elementDao.readAll().get(2);
		
		// WHEN manager or player get specific element Id
		ElementBoundary result = this.restTemplate.getForObject(this.baseUrl+"/"+ elementEntity.getElementSmartspace()+"/"+elementEntity.getElementId(), ElementBoundary.class); 
		
		// THEN I receive the specified element
		assertThat(result.getKey().getId()).isEqualTo(elementEntity.getElementId());
		
		this.elementDao.deleteAll();
	}
	
	@Test 
	public void getAllElementsUsingPagination(){
		// GIVEN the database contains 40 elements
			int size = 40;
			IntStream.range(1, size + 1).mapToObj(i -> "element #" + i).map(ElementEntity::new)
			.forEach(this.elementDao::create);
		
		// WHEN I get elements of the page = 1 and size = 30
			ElementBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ElementBoundary[].class, 1, 30);
		
		// THEN I receive 10 elements
			assertThat(result).hasSize(10);
			
			this.elementDao.deleteAll();
	}
	
	@Test
	public void getAllElementsNearLocation() {
		// GIVEN the database contains 4 elements with different locations 
		int size = 4;
		
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i)
		.map(ElementEntity::new)
		.forEach(element->{
			setRandomLocation(element);
			this.elementDao.create(element);});
		
		double x = this.elementDao.readAll().get(0).getLocation().getX();
		double y = this.elementDao.readAll().get(0).getLocation().getY();
		
		// WHEN manager or player get all Elements near a location
		ElementBoundary[] elementsResults = this.restTemplate
				.getForObject(this.baseUrl+"?search=location&x={x}&y={y}", ElementBoundary[].class, x, y);
	
		// THEN the results is not empty
		assertThat(elementsResults).isNotEmpty();
		
		this.elementDao.deleteAll();
	}

	@Test
	public void getAllElementsWithSpecifiedName() {
		// GIVEN the database contains 4 elements with different names		
		int size = 4;
		
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i)
		.map(ElementEntity::new)
		.map(this.elementDao::create)
		.forEach(element->{element.setName("element"+element.getElementId()); 
							this.elementDao.update(element);});
		
		
		// WHEN manager or player get all Elements with specified name
		String name = this.elementDao.readAll().get(2).getName();
		ElementBoundary[] elementsResults = this.restTemplate
				.getForObject(this.baseUrl+"?search=name&value={name}", ElementBoundary[].class, name);
		
		// THEN the results contains the specified element
		assertThat(elementsResults[0].getName()).isEqualTo(name);
		
	}
	
	@Test
	public void getAllElementsWithSpecifiedType() {
		// GIVEN the database contains 10 elements with different types		
		int size = 10;
		
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i)
		.map(ElementEntity::new)
		.map(this.elementDao::create)
		.forEach(element->{element.setType("type"+element.getElementId()); 
							this.elementDao.update(element);});
		
		
		// WHEN manager or player get all Elements with specified type
		String type = this.elementDao.readAll().get(5).getType();
		ElementBoundary[] elementsResults = this.restTemplate
				.getForObject(this.baseUrl+"?search=type&value={type}", ElementBoundary[].class, type);
		
		// THEN the results contains the specified element
		assertThat(elementsResults[0].getElementType()).isEqualTo(type);
		
	}
	
	@Test
	public void getAllElementsWithSpecifiedTypeAndPagination() {
		// GIVEN the database contains 40 elements with type1 and 70 elements with type2		
		int size1 = 100;
		
		IntStream.range(1, size1 + 1).mapToObj(i -> "element #" + i)
		.map(ElementEntity::new)
		.map(this.elementDao::create)
		.forEach(element->{element.setType("type1"); 
							this.elementDao.update(element);});
		int size2 = 100;
		
		IntStream.range(1, size2 + 1).mapToObj(i -> "element #" + i)
		.map(ElementEntity::new)
		.map(this.elementDao::create)
		.forEach(element->{element.setType("type2"); 
							this.elementDao.update(element);});
		
		
		// WHEN manager or player get all Elements with type2
		ElementBoundary[] elementsResults = this.restTemplate
				.getForObject(this.baseUrl+"?search=type&value={type}&page={page}&size={size}", ElementBoundary[].class, "type1", 0, 100);
		
		// THEN the results contains all 100 elements with type = type1
		assertThat(elementsResults).hasSize(100);
		
	}
	
	@Test
	public void getAllElementsWithTypeCity() {
		// GIVEN the database contains elements cities and user player
		createCityElements();
		this.userEntity.setRole(UserRole.PLAYER);
		this.userDao.update(userEntity);
		
		// WHEN manager or player get all Elements with specified type
		ElementBoundary[] elementsResults = this.restTemplate
				.getForObject(this.baseUrl+"?search=type&value={type}", ElementBoundary[].class, "city");
		
		// THEN all cities results sorted by increasing X
	    Comparator<ElementBoundary> increasingX = new Comparator<ElementBoundary>() {
	        public int compare(ElementBoundary element1, ElementBoundary element2) {
	          return Double.compare(element1.getLatlng().getLat(), element2.getLatlng().getLat());
	        }
	      };
	      
		assertThat(elementsResults).isSortedAccordingTo(increasingX);
		
	}
	
	private void setRandomLocation(ElementEntity element) {
		double randomX = 0 + (10 - 0) * this.r.nextDouble();
		double randomY = 0 + (10 - 0) * this.r.nextDouble();
		element.setLocation(new Location(randomX,randomY));		
	}
	
	private void createCityElements() {	
		citiesName = new ArrayList<String>(Arrays.asList("Tel Aviv","Jerusalem","Beer Sheva","Ramat Gan","Netanya",
				"Haifa","Givataym","Eilat","Holon","Petah Tikva","Raanana","ashkelon"));		
		
		long minPrice = 50;
		long maxPrice = 400;
		
		for(int i=0; i<citiesName.size(); i++) {
			Random r = new Random();
			long randomPrice =  minPrice +(long)(r.nextDouble()*(maxPrice-minPrice));

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
