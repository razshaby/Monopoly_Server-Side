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

import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.layout.BoundaryLocation;
import smartspace.layout.ElementBoundary;
import smartspace.layout.Key;
import smartspace.layout.UserKey;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class ElementRestIntegrationTests {
	@LocalServerPort
	private int port;

	private String baseUrl;
	private RestTemplate restTemplate;

	private AdvancedUserDao<String> userDao;
	private UserEntity userEntity;
	private AdvancedElementDao<String> elementDao;

	@Autowired
	public void setElementAndUserDao(AdvancedElementDao<String> elementDao, AdvancedUserDao<String> userDao) {
		this.elementDao = elementDao;
		this.userDao = userDao;
	}

	@PostConstruct
	public void init() {
		setup();
		this.baseUrl = "http://localhost:" + port + "/smartspace/admin/elements/" + this.userEntity.getUserSmartspace()
				+ "/" + this.userEntity.getUserEmail();
		this.restTemplate = new RestTemplate();
	}

	public void setup() {
		this.userEntity = new UserEntity();
		this.userEntity.setUserEmail("manager.creating.element@de.mo");
		this.userEntity.setUserSmartspace("2019b.meytal");
		this.userEntity.setRole(UserRole.ADMIN);
		this.userDao.create(userEntity);
	}

	@After
	public void teardown() {
		this.elementDao.deleteAll();
		this.userDao.deleteAll();
	}

	@Test
	public void testInsertSingleElement() throws Exception {
		// GIVEN the database contains admin

		// WHEN I post a new ElementBoundary with admin
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);

		String name = "demoElement";
		String type = "myType";
		ElementBoundary newElementBoundary = new ElementBoundary();
		newElementBoundary.setKey(new Key("3", "anotherSmartspace"));
		newElementBoundary.setElementType(type);
		newElementBoundary.setName(name);
		newElementBoundary.setExpired(false);
		newElementBoundary.setCreator(new UserKey("bla", this.userEntity.getUserEmail()));
		newElementBoundary.setLatlng(new BoundaryLocation(32.115, 84.817));
		newElementBoundary.setElementProperties(elementProperties);

		ElementBoundary[] elementsBoundariesArr = { newElementBoundary };
		ElementBoundary[] resultArray = this.restTemplate.postForObject(this.baseUrl, elementsBoundariesArr,
				ElementBoundary[].class);
		ElementBoundary result = resultArray[0];

		// THEN the database contains the new element
		String theKey = result.getKey().getSmartspace() + "|" + result.getKey().getId();
		assertThat(this.elementDao.readById(theKey)).isPresent().get().extracting("name", "type").containsExactly(name,
				type);
	}

	@Test
	public void testGetElementWithPagination() throws Exception {
		// GIVEN the database contains 40 elements
		int size = 40;
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i).map(ElementEntity::new)
				.forEach(this.elementDao::create);

		// WHEN I get elements of the page = 0 and size = 10
		ElementBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ElementBoundary[].class, 0, 10);

		// THEN I receive 10 elements
		assertThat(result).hasSize(10);
	}

	@Test
	public void testGetElementsWithPaginationOfSecondPage() throws Exception {
		// GIVEN the database contains 40 elements
		int size = 40;
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i).map(ElementEntity::new)
				.forEach(this.elementDao::create);

		// WHEN I get elements of the page = 1 and size = 100
		ElementBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ElementBoundary[].class, 1, 100);

		// THEN I receive 0 elements
		assertThat(result).isEmpty();
	}

	@Test
	public void testGetElementsWithPaginationOfExistingSecondPage() throws Exception {
		// GIVEN the database contains 40 elements
		int size = 40;
		IntStream.range(1, size + 1).mapToObj(i -> "element #" + i).map(ElementEntity::new)
				.forEach(this.elementDao::create);

		// WHEN I get elements of the page = 1 and size = 30
		ElementBoundary[] result = this.restTemplate.getForObject(this.baseUrl + "?page={page}&size={size}",
				ElementBoundary[].class, 1, 30);

		// THEN I receive 10 elements
		assertThat(result).hasSize(10);
	}

	@Test
	public void testInsertUsingRestAndGetElementsUsingRest() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST 3 new elements
		// AND I get elements of page 0 of size 2
		UserKey userKey = new UserKey(this.userEntity.getUserSmartspace(), this.userEntity.getUserEmail());
		BoundaryLocation boundaryLocation = new BoundaryLocation(32.115, 84.817);
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);

		List<ElementBoundary> newBoundaries = IntStream.range(1, 3 + 1).mapToObj(i -> "element #" + i)
				.map(name -> new ElementBoundary("myType", name, false, userKey, boundaryLocation, elementProperties))
				.collect(Collectors.toList());

		int i = 1;
		for (ElementBoundary element : newBoundaries) {
			element.setKey(new Key(i++ + "" , "anotherSmartspace"));
		}

		this.restTemplate.postForObject(baseUrl, newBoundaries, ElementBoundary[].class);

		ElementBoundary[] getResult = this.restTemplate.getForObject(this.baseUrl + "?size={size}&page={page}",
				ElementBoundary[].class, 2, 0);

		// THEN the received elements are similar to 2 of the new elements
		assertThat(getResult).hasSize(2).usingElementComparatorOnFields("elementType", "name", "elementProperties")
				.containsAnyElementsOf(newBoundaries);
	}

	@Test(expected = Exception.class)
	public void testInsertInvalidElement() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST element with the same smartspace as the project
		UserKey userKey = new UserKey(this.userEntity.getUserSmartspace(), this.userEntity.getUserEmail());
		BoundaryLocation boundaryLocation = new BoundaryLocation(32.115, 84.817);
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);

		ElementBoundary elementBoundary = new ElementBoundary("myType", "demoElement", false, userKey, boundaryLocation,
				elementProperties);
		elementBoundary.setKey(new Key(null, "2019b.meytal"));
		ElementBoundary[] elementBoundaries = { elementBoundary };

		this.restTemplate.postForObject(baseUrl, elementBoundaries, ElementBoundary[].class);
		// THEN throws exception
	}

	@Test(expected = Exception.class)
	public void testInsertValidElementsAndOneInvalidElement() throws Exception {
		// GIVEN the database is empty

		// WHEN I POST 3 valid elements and one invalid element
		UserKey userKey = new UserKey(this.userEntity.getUserSmartspace(), this.userEntity.getUserEmail());
		BoundaryLocation boundaryLocation = new BoundaryLocation(32.115, 84.817);
		Map<String, Object> elementProperties = new HashMap<>();
		elementProperties.put("value", "hello");
		elementProperties.put("value2", 42);
		elementProperties.put("value3", 4.2);

		List<ElementBoundary> newBoundaries = IntStream.range(1, 3 + 1).mapToObj(i -> "element #" + i)
				.map(name -> new ElementBoundary("myType", "demoElement", false, userKey, boundaryLocation,
						elementProperties))
				.collect(Collectors.toList());

		for (ElementBoundary element : newBoundaries) {
			element.setKey(new Key(null, "anotherSmartspace"));
		}

		ElementBoundary invalidElementBoundary = new ElementBoundary("myType", "demoElement", false, userKey,
				boundaryLocation, elementProperties);
		invalidElementBoundary.setKey(new Key(null, "2019b.meytal"));
		newBoundaries.add(invalidElementBoundary);

		this.restTemplate.postForObject(baseUrl, newBoundaries, ElementBoundary[].class);

		// THEN throws exception

	}

}
