package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ElementEntity;
import smartspace.data.Location;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.layout.ActionBoundary;
import smartspace.layout.Key;
import smartspace.layout.UserKey;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties="spring.profiles.active=default")
public class ActionPlayerRestIntegrationTest {
	
	@LocalServerPort
	private int port;
	
	private String baseActionUrl;
	private RestTemplate restTemplate;
	
	private UserEntity userPlayerEntity;
	private ElementEntity gameElement;
	private ArrayList<String> citiesName ;
	
	private AdvancedActionDao actionDao;
	private AdvancedUserDao <String> userDao;
	private AdvancedElementDao <String> elementDao;
	
	@Autowired
	public void setActionAndElementAndUserDao(AdvancedActionDao actionDao, AdvancedUserDao <String> userDao, AdvancedElementDao <String> elementDao) {
		this.actionDao = actionDao;
		this.userDao = userDao;
		this.elementDao = elementDao;
	}
	
	@PostConstruct
	public void init() {
		teardown();
		setup();
		this.baseActionUrl = "http://localhost:"+ 
						port+ 
						"/smartspace/actions";
		this.restTemplate = new RestTemplate();
	}

	public void setup() {
		createUserPlayer();
		createGameElement();
	}

	private void createUserPlayer() {
		this.userPlayerEntity = new UserEntity();
		this.userPlayerEntity.setUserEmail("player.invoking.action@de.mo");
		this.userPlayerEntity.setUserSmartspace("2019b.meytal");
		this.userPlayerEntity.setRole(UserRole.PLAYER);
		this.userPlayerEntity.setPoints(50000);
		this.userDao.create(userPlayerEntity);
	}
	
	private void createGameElement() {
		this.gameElement = new ElementEntity();
		this.gameElement.setName("elementTest");
		this.gameElement.setType("game");
		this.gameElement=this.elementDao.create(gameElement);
	}
	
	@After
	public void teardown() {
		this.actionDao.deleteAll();
		this.userDao.deleteAll();
		this.elementDao.deleteAll();
	}
	
	@Test
	public void testInvokeEchoAction() throws Exception{
		// GIVEN the database have user player and element

		// WHEN I post echo ActionBoundary with null key
		ActionBoundary newActionBoundary = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "echo");
		this.restTemplate.postForObject(this.baseActionUrl, newActionBoundary, ActionBoundary.class);
		
		// THEN the database contains the ActionEntity with new key
		assertThat(this.actionDao
				.readAll()
				.get(0)
				.getKey()!= null);
		
		teardown(); 
	}

	@Test(expected = Exception.class)
	public void testInvokeInvalidActionType() throws Exception{
		// GIVEN the database have user player and element

		// WHEN I post a new ActionBoundary with invalid action type
		ActionBoundary newActionBoundary = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "InvalidActionType");		
		this.restTemplate.postForObject(this.baseActionUrl, newActionBoundary, ActionBoundary.class);
		
		// THEN throws exception	
		
		teardown();
	}
	
	
	@Test
	public void testLogin() {
		// GIVEN the database contains user player and element of type game and cities
		createCityElements();
		
		// WHEN I POST login ActionBoundary
		ActionBoundary newActionBoundary = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, newActionBoundary, ActionBoundary.class);

		// THEN player points updated to 1000
		assertThat(this.userDao.readById(this.userPlayerEntity.getKey())
				.get()
				.getPoints())
				.isEqualTo(1000);
		
		teardown();
	}
	
	
	
	@Test
	public void testLogout(){
		// GIVEN the database contains player player logged in and owner of a city
		ElementEntity city = createCityElements();
		
		ActionBoundary loginAction = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginAction, ActionBoundary.class);
		
		ActionBoundary checkInAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "CheckIn");
		this.restTemplate.postForObject(this.baseActionUrl, checkInAction, ActionBoundary.class);	
		
		ActionBoundary buyAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "Buy");
		this.restTemplate.postForObject(this.baseActionUrl, buyAction, ActionBoundary.class);
		
		// WHEN I POST logout ActionBoundary
		ActionBoundary logoutAction = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Logout");		
		this.restTemplate.postForObject(this.baseActionUrl, logoutAction, ActionBoundary.class);
		
		//THEN player's points updated to -1. and the city owner is empty 		
		assertThat(this.userDao.readById(this.userPlayerEntity.getKey()).get()
				.getPoints())
				.isEqualTo(-1);
		
		assertThat(this.elementDao.readById(city.getKey()).get().getMoreAttributes()
				.get("ownerId"))
				.isNotEqualTo(this.userPlayerEntity.getKey());
	}
	
	@Test
	public void testCheckIn() {
		// GIVEN the database contains cities and player logged in 
		ElementEntity cityElement = createCityElements();
		ActionBoundary loginAction = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginAction, ActionBoundary.class);
		
		// WHEN I POST CheckIn ActionBoundary
		ActionBoundary newActionBoundary = createActionWithElementUserAndType(cityElement, this.userPlayerEntity, "CheckIn");
		ActionBoundary action = this.restTemplate.postForObject(this.baseActionUrl, newActionBoundary, ActionBoundary.class);
		
		// THEN the city visitors contains the user key
		Optional<ElementEntity> element = this.elementDao.readById(action.getElement().getSmartspace()+"|"+action.getElement().getId());
		ArrayList<String> visitorsList = (ArrayList<String>) element.get().getMoreAttributes().get("visitors");
		assertThat(visitorsList).contains(this.userPlayerEntity.getKey());
		
	}
	
	@Test
	public void testCheckInToOccupiedCity() {
		// GIVEN the database contains two players logged in, which one of them is owner of a city
		ElementEntity city = createCityElements();
		ActionBoundary loginPlayer1 = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginPlayer1, ActionBoundary.class);
		
		UserEntity player2 = createNewPlayer();
		ActionBoundary loginPlayer2 = createActionWithElementUserAndType(this.gameElement, player2 , "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginPlayer2, ActionBoundary.class);
		
		ActionBoundary checkInPlayer1 = createActionWithElementUserAndType(city, this.userPlayerEntity, "CheckIn");
		this.restTemplate.postForObject(this.baseActionUrl, checkInPlayer1, ActionBoundary.class);		
		
		ActionBoundary buyAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "Buy");
		this.restTemplate.postForObject(this.baseActionUrl, buyAction, ActionBoundary.class);
		
		// WHEN player2 check in to the same city
		ActionBoundary checkPlayer2 = createActionWithElementUserAndType(city, player2, "CheckIn");
		this.restTemplate.postForObject(this.baseActionUrl, checkPlayer2, ActionBoundary.class);		
		
		// THEN the player will be charged a fine
		assertThat(this.userDao.readById(checkPlayer2.getPlayer().getSmartspace()+"|"+checkPlayer2.getPlayer().getEmail())
				.get()
				.getPoints())
				.isLessThan(1000);
	}
	
	@Test
	public void testCheckOut() {
		// GIVEN the database contains cities, player logged in and checked in a city
		ElementEntity city = createCityElements();
		ActionBoundary loginAction = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginAction, ActionBoundary.class);
		ActionBoundary checkInAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "CheckIn");
		this.restTemplate.postForObject(this.baseActionUrl, checkInAction, ActionBoundary.class);		
		
		// WHEN I POST CheckOut ActionBoundary
		ActionBoundary checkOutAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "CheckOut");
		ActionBoundary action = this.restTemplate.postForObject(this.baseActionUrl, checkOutAction, ActionBoundary.class);
		
		// THEN the city visitors not contains the user key
		Optional<ElementEntity> element = this.elementDao.readById(action.getElement().getSmartspace()+"|"+action.getElement().getId());
		ArrayList<String> visitorsList = (ArrayList<String>) element.get().getMoreAttributes().get("visitors");
		assertThat(visitorsList).doesNotContain(this.userPlayerEntity.getKey());
		
	}
	
	@Test
	public void testBuyCity() {
		// GIVEN the database contains cities, player logged in and checked in a city
		ElementEntity city = createCityElements();
		ActionBoundary loginAction = createActionWithElementUserAndType(this.gameElement, this.userPlayerEntity, "Login");		
		this.restTemplate.postForObject(this.baseActionUrl, loginAction, ActionBoundary.class);
		ActionBoundary checkInAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "CheckIn");
		this.restTemplate.postForObject(this.baseActionUrl, checkInAction, ActionBoundary.class);		
		
		// WHEN I POST Buy ActionBoundary
		ActionBoundary buyAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "Buy");
		this.restTemplate.postForObject(this.baseActionUrl, buyAction, ActionBoundary.class);
		
		// THEN the owner name of the city updated
		assertThat(this.elementDao.readById(city.getKey()).get().getMoreAttributes()
				.get("ownerName"))
				.isEqualTo(this.userPlayerEntity.getUsername());
		assertThat(this.elementDao.readById(city.getKey()).get().getMoreAttributes()
				.get("ownerId"))
				.isEqualTo(this.userPlayerEntity.getKey());
		
	}
	
	@Test(expected = Exception.class)
	public void testTryToBuyCityWithoutCheckIn() {
		// GIVEN the database contains city and player which not checked in the city
		ElementEntity city = createCityElements();
		
		// WHEN I POST Buy ActionBoundary
		ActionBoundary buyAction = createActionWithElementUserAndType(city, this.userPlayerEntity, "Buy");
		this.restTemplate.postForObject(this.baseActionUrl, buyAction, ActionBoundary.class);
	
		// THEN throws exception
	}
	
	private  ElementEntity createCityElements() {	
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
		return this.elementDao.readAll().get(citiesName.size()-1);
	}

	private UserEntity createNewPlayer() {
		UserEntity player = new UserEntity();
		player.setUserEmail("player.action@de.mo");
		player.setUserSmartspace("2019b.meytal");
		player.setRole(UserRole.PLAYER);
		player.setPoints(50000);
		return this.userDao.create(player);
	}
	
	
	private ActionBoundary createActionWithElementUserAndType(ElementEntity element, UserEntity user, String type) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		ActionBoundary newActionBoundary = new ActionBoundary();
		newActionBoundary.setType(type);
		newActionBoundary.setElement(new Key(element.getElementId(),element.getElementSmartspace()));
		newActionBoundary.setPlayer(new UserKey(user.getUserSmartspace(), user.getUserEmail()));
		newActionBoundary.setProperties(properties);
		return newActionBoundary;
	}
}
