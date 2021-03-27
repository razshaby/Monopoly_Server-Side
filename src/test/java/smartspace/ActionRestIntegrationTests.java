package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.layout.ActionBoundary;
import smartspace.layout.BoundaryLocation;
import smartspace.layout.ElementBoundary;
import smartspace.layout.Key;
import smartspace.layout.UserKey;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties="spring.profiles.active=default")
public class ActionRestIntegrationTests {
	@LocalServerPort
	private int port;
	
	private String baseUrl;
	private RestTemplate restTemplate;
	
	private UserEntity userAdminEntity;
	private ElementEntity elementEntity;
	
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
		setup();
		this.baseUrl = "http://localhost:"+ 
						port+ 
						"/smartspace/admin/actions/"+
						this.userAdminEntity.getUserSmartspace()+"/"+this.userAdminEntity.getUserEmail();
		this.restTemplate = new RestTemplate();
	}
	
	public void setup() {
		createUserAdmin();
		createElement();
	}

	private void createUserAdmin() {
		this.userAdminEntity = new UserEntity();
		this.userAdminEntity.setUserEmail("player.invoking.action@de.mo");
		this.userAdminEntity.setUserSmartspace("2019b.meytal");
		this.userAdminEntity.setRole(UserRole.ADMIN);
		this.userDao.create(userAdminEntity);
	}
	
	private void createElement() {
		this.elementEntity = new ElementEntity();
		this.elementEntity.setName("elementTest");
		this.elementEntity=this.elementDao.create(elementEntity);
	}
	
	@After
	public void teardown() {
		this.actionDao.deleteAll();
		this.userDao.deleteAll();
	}
	
	@Test
	public void testInsertSingleAction() throws Exception{
		// GIVEN the database have user admin and action
		//init();
		// WHEN I post a new ActionBoundary
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		String type = "actionType";
		ActionBoundary newActionBoundary = new ActionBoundary();
		newActionBoundary.setType(type);
		newActionBoundary.setActionKey(new Key("1", "otherSmartSpace"));
		newActionBoundary.setElement(new Key(this.elementEntity.getElementId(),this.elementEntity.getElementSmartspace()));
		newActionBoundary.setPlayer(new UserKey(this.userAdminEntity.getUserSmartspace(), this.userAdminEntity.getUserEmail()));
		newActionBoundary.setProperties(properties);
		
		ActionBoundary[] actionsBoundariesArr = {newActionBoundary};
		//ActionBoundary[] resultArray = this.restTemplate.postForObject(this.baseUrl, actionsBoundariesArr, ActionBoundary[].class);
		this.restTemplate.postForObject(this.baseUrl, actionsBoundariesArr, ActionBoundary[].class);
		//ActionBoundary result = resultArray[0];
		
		// THEN the database contains the ActionEntity
		assertThat(this.actionDao
				.readAll()
				.get(0)
				.getActionType().equals(type));
						
	}
	
	
	@Test
	public void testGetActionWithPagination() throws Exception{
		
		// GIVEN the database contains 10 actions
		//init();
		int size = 10;
		IntStream.range(1, size + 1)
			.mapToObj(i->"actionType #" + i)
			.map(ActionEntity::new)
			.forEach(this.actionDao::create);
//			.peek(action->action.setElementId(this.elementEntity.getElementId()))
//			.peek(action->action.setElementSmartspace(this.elementEntity.getElementSmartspace()))

				
		// WHEN I get action of the page = 0 and size = 5
		ActionBoundary[] result = 
			this.restTemplate
			.getForObject(
					this.baseUrl + "?page={page}&size={size}",
					ActionBoundary[].class,
					0, 5);
		
		// THEN I receive 5 elements
		assertThat(result).hasSize(5);
	}
	
	@Test
	public void testGetActionsWithPaginationOfSecondPage() throws Exception {
		// GIVEN the database contains 40 actions
		int size = 40;
		IntStream.range(1, size + 1).mapToObj(i -> "action #" + i).map(ActionEntity::new)
				.forEach(this.actionDao::create);

		// WHEN I get actions of the page = 1 and size = 100
		ActionBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ActionBoundary[].class, 1, 100);

		// THEN I receive 0 actions
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testGetActionsWithPaginationOfExistingSecondPage() throws Exception {
		// GIVEN the database contains 40 actions
		int size = 40;
		IntStream.range(1, size + 1).mapToObj(i -> "actions #" + i).map(ActionEntity::new)
				.forEach(this.actionDao::create);

		// WHEN I get actions of the page = 1 and size = 30
		ActionBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ActionBoundary[].class, 1, 30);

		// THEN I receive 10 actions
		assertThat(result).hasSize(10);
	}

	@Test
	public void testInsertUsingRestAndGetActionsUsingRest() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST 3 new actions
		// AND I get actions of page 0 of size 2
		UserKey userKey = new UserKey(this.userAdminEntity.getUserSmartspace(), this.userAdminEntity.getUserEmail());
		Key elemenetKey = new Key(this.elementEntity.getElementId(), this.elementEntity.getElementSmartspace());
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		List<ActionBoundary> newBoundaries = IntStream.range(1, 3 + 1).mapToObj(i -> "action #" + i)
				.map(name -> new ActionBoundary("myType", elemenetKey, userKey, properties))
				.collect(Collectors.toList());
		int i=1;
		for (ActionBoundary action : newBoundaries) {
			action.setActionKey(new Key(i+"", "otherSmartSpace"));
			i++;
		}

		this.restTemplate.postForObject(baseUrl, newBoundaries, ActionBoundary[].class);

		ActionBoundary[] getResult = this.restTemplate.getForObject(this.baseUrl + "?size={size}&page={page}",
				ActionBoundary[].class, 2, 0);

		// THEN the received actions are similar to 2 of the new actions
		assertThat(getResult).hasSize(2).usingElementComparatorOnFields("type", "properties")
				.containsAnyElementsOf(newBoundaries);
	}
	
	@Test(expected = Exception.class)
	public void testInsertActionWithLocalSmartspace() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST action with the same smartspace as the project
		UserKey userKey = new UserKey(this.userAdminEntity.getUserSmartspace(), this.userAdminEntity.getUserEmail());
		Key elemenetKey = new Key(this.elementEntity.getElementId(), this.elementEntity.getElementSmartspace());
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		ActionBoundary actionBoundary = new ActionBoundary("myType", elemenetKey, userKey, properties);
		actionBoundary.setActionKey(new Key("1", "2019b.meytal"));
		ActionBoundary[] actionBoundaries = {actionBoundary};

		this.restTemplate.postForObject(baseUrl, actionBoundaries, ActionBoundary[].class);
		// THEN throws exception
	}
	
	@Test(expected = Exception.class)
	public void testInsertValidActionsAndOneInvalidAction() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST 3 valid actions and one invalid action
		UserKey userKey = new UserKey(this.userAdminEntity.getUserSmartspace(), this.userAdminEntity.getUserEmail());
		Key elemenetKey = new Key(this.elementEntity.getElementId(), this.elementEntity.getElementSmartspace());
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		List<ActionBoundary> newBoundaries = IntStream.range(1, 3 + 1).mapToObj(i -> "action #" + i)
				.map(name -> new ActionBoundary("myType", elemenetKey, userKey, properties))
				.collect(Collectors.toList());

		int i=1;
		for (ActionBoundary action : newBoundaries) {
			action.setActionKey(new Key(i+"", "anotherSmartspace"));
			i++;
		}

		ActionBoundary invalidActionBoundary = new ActionBoundary("myType", elemenetKey, userKey, properties);
		invalidActionBoundary.setActionKey(new Key("4", "2019b.meytal"));
		newBoundaries.add(invalidActionBoundary);

		this.restTemplate.postForObject(baseUrl, newBoundaries, ActionBoundary[].class);

		// THEN throws exception

	}
	
	@Test(expected = Exception.class)
	public void testInsertActionsWithNoElement() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST action with no action imported in advanced
		UserKey userKey = new UserKey(this.userAdminEntity.getUserSmartspace(), this.userAdminEntity.getUserEmail());
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");

		ActionBoundary actionBoundary = new ActionBoundary("myType", null, userKey, properties);
		actionBoundary.setActionKey(new Key("1", "2019b.meytal"));
		ActionBoundary[] actionBoundaries = {actionBoundary};

		this.restTemplate.postForObject(baseUrl, actionBoundaries, ActionBoundary[].class);
		// THEN throws exception
	}
	
	
}
