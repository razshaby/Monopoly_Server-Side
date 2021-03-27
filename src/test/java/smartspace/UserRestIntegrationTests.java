package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

import smartspace.dao.AdvancedUserDao;

import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.layout.UserBoundary;
import smartspace.layout.UserKey;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class UserRestIntegrationTests {
	@LocalServerPort
	private int port;
	private String baseUrl;
	private RestTemplate restTemplate;
	private UserEntity userEntity;
	private AdvancedUserDao<String> userDao;

	@Autowired
	public void setUserDao(AdvancedUserDao<String> userDao) {
		this.userDao = userDao;
	}

	@PostConstruct
	public void init() {
		setup();
		this.baseUrl = "http://localhost:" + port + "/smartspace/admin/users/" + this.userEntity.getUserSmartspace()
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
		this.userDao.deleteAll();
	}

	@Test
	public void testInsertSinglePlayerUser() throws Exception {
		// GIVEN the database contain one user admin

		baseUrl = "http://localhost:" + port + "/smartspace/admin/users/" + userEntity.getUserSmartspace() + "/"
				+ userEntity.getUserEmail();
		restTemplate = new RestTemplate();

		// WHEN I post a new UserBoundary
		String avatar = "avatar";
		long points = 55;
		UserRole role = UserRole.PLAYER;
		String username = "testUserName";

		UserBoundary newUserBoundary = new UserBoundary();
		newUserBoundary.setAvatar(avatar);
		newUserBoundary.setPoints(points);
		newUserBoundary.setRole(role.name());
		newUserBoundary.setUsername(username);
		newUserBoundary.setKey(new UserKey("otherSmartSpace", "test@mail.com"));

		UserBoundary[] userBoundariesArr = { newUserBoundary };
		UserBoundary[] resultArray = restTemplate.postForObject(baseUrl, userBoundariesArr, UserBoundary[].class);
		UserBoundary result = resultArray[0];

		// THEN the database contains the new element
		String theKey = result.getKey().getSmartspace() + "|" + result.getKey().getEmail();
		assertThat(this.userDao.readById(theKey)).isPresent().get().extracting("username", "role")
				.containsExactly(username, role);
	}

	// Test - Insert user without admin permissions
	@Test(expected = Exception.class)
	public void testInsertSinglePlayerUserWithOutAdminPermissions() throws Exception {
		// GIVEN the database contains user with player role
		this.userEntity = new UserEntity();
		this.userEntity.setUserEmail("player@de.mo");
		this.userEntity.setUserSmartspace("2019b.meytal");
		this.userEntity.setRole(UserRole.PLAYER);
		this.userDao.create(userEntity);

		// WHEN post a new UserBoundary with player role
		String avatar = "avatar";
		long points = 55;
		UserRole role = UserRole.PLAYER;
		String username = "testUserName";

		UserBoundary newUserBoundary = new UserBoundary();
		newUserBoundary.setAvatar(avatar);
		newUserBoundary.setPoints(points);
		newUserBoundary.setRole(role.name());
		newUserBoundary.setUsername(username);

		String url = "http://localhost:" + port + "/smartspace/admin/users/" + "2019b.meytal" + "/" + "player@de.mo";

		UserBoundary[] userBoundariesArr = { newUserBoundary };
		restTemplate.postForObject(url, userBoundariesArr, UserBoundary[].class);

		// THEN throws Exception

	}

	// Test - Insert user with an admin of different smart space
	@Test(expected = Exception.class)
	public void testInsertSinglePlayerUserWithAdminUserThatDoesntBelongToOurSmartSpace() throws Exception {
		// GIVEN the database is empty

		// WHEN i post a user boundary with an ADMIN that does'nt belong to out
		// smartspace
		String avatar = "avatar";
		long points = 55;
		UserRole role = UserRole.PLAYER;
		String username = "testUserName";

		UserBoundary newUserBoundary = new UserBoundary();
		newUserBoundary.setAvatar(avatar);
		newUserBoundary.setPoints(points);
		newUserBoundary.setRole(role.name());
		newUserBoundary.setUsername(username);

		String url = "http://localhost:" + port + "/smartspace/admin/users/" + "anotherSmartspace" + "/"
				+ userEntity.getUserEmail();

		// THEN an exception will occur and the data base is empty
		UserBoundary[] userBoundariesArr = { newUserBoundary };
		restTemplate.postForObject(url, userBoundariesArr, UserBoundary[].class);

	}

	// Test - insert 200 users and check if i get 20 users on page 0
	@Test
	public void testGetUsersWithPagination() throws Exception {
		// Given the database contains 200 users
		int size = 200;

		IntStream.range(1, size + 1).mapToObj(i -> "email@" + i).map(UserEntity::new)
				// .peek(x -> x.setUserSmartspace("2019b.meytal"))
				.forEach(this.userDao::create);

		// When i get users of the page = 0 and size 20
		UserBoundary[] result = restTemplate.getForObject(baseUrl + "?page={page}&size={size}", UserBoundary[].class, 0,
				20);

		// THEN i receive 20 users
		assertThat(result).hasSize(20);
	}

	// Test - insert 20 users and check if page 1 i empty
	@Test
	public void testGetUsersWithPaginationSecondPageShouldBeEmpty() throws Exception {
		// Given the database contains 20 users
		int size = 19;// because admin is a user as well so total of 2

		IntStream.range(1, size + 1).mapToObj(i -> "email@" + i).map(UserEntity::new)
				// .peek(x -> x.setUserSmartspace("2019b.meytal"))
				.forEach(this.userDao::create);

		// When i get users of the page = 1 and size 20
		UserBoundary[] result = restTemplate.getForObject(baseUrl + "?page={page}&size={size}", UserBoundary[].class, 1,
				20);

		// THEN i receive 0 users, page should be empty
		assertThat(result).isEmpty();
	}

	// Test - insert 30 users and check if page 1 conatin users
	@Test
	public void testGetUsersWithPaginationSecondPageThatHaveData() throws Exception {
		// Given the database contains 30 users
		int size = 29;// because admin is a user as well so total of 20

		IntStream.range(1, size + 1).mapToObj(i -> "email@" + i).map(UserEntity::new)
				// .peek(x -> x.setUserSmartspace("2019b.meytal"))
				.forEach(this.userDao::create);

		// When i get users of the page = 1 and size 20
		UserBoundary[] result = restTemplate.getForObject(baseUrl + "?page={page}&size={size}", UserBoundary[].class, 1,
				20);

		// THEN i receive 10 users
		assertThat(result).hasSize(10);
	}

	// Test - Post 3 users and get the same 3 users
	@Test
	public void testPostUsersAndGetTheSameUsers() throws Exception {
		// GIVEN the database only contains an admin that belong to our smartspace

		// WHEN I post 3 users
		// AND I get 2 users on pag num 1
		int size = 3;
		List<UserBoundary> newBoundaries = IntStream.range(1, size + 1).mapToObj(i -> i)
				.map(j -> new UserBoundary("name" + j, "avatar" + j, "PLAYER", 5)).collect(Collectors.toList());

		for (int i = 0; i < newBoundaries.size(); i++)
			newBoundaries.get(i).setKey(new UserKey("2019b.notmeytal", "email@" + i));

		restTemplate.postForObject(baseUrl, newBoundaries, UserBoundary[].class);

		UserBoundary[] getResult = restTemplate.getForObject(baseUrl + "?size={size}&page={page}", UserBoundary[].class,
				2, 0);

		newBoundaries.add(new UserBoundary(userEntity)); // adding the Admin User to the Comparelist

		// .peek(x -> x.setUserSmartspace("2019b.notmeytal"))
		// .forEach(this.userDao::createFromImport);

		// THEN the received elements are similar to 2 of the new users
		assertThat(getResult).hasSize(2).usingElementComparatorOnFields("username", "avatar", "role", "points")
				.containsAnyElementsOf(newBoundaries);
	}

	// Test posting a user with the same smartspace as us
	@Test(expected = Exception.class)
	public void testPostUserWithTheSameSmartspaceAsWeHave() throws Exception {
		// GIVEN the database only contains an admin that belong to our smartspace

		baseUrl = "http://localhost:" + port + "/smartspace/admin/users/" + userEntity.getUserSmartspace() + "/"
				+ userEntity.getUserEmail();
		restTemplate = new RestTemplate();

		// WHEN I post 1 users with the same smartsapce as we have

		UserBoundary userBoundary = new UserBoundary("name", "avatar", "PLAYER", 5);
		userBoundary.setKey(new UserKey("2019b.meytal", "email@"));

		restTemplate.postForObject(baseUrl, userBoundary, UserBoundary.class);

		// THEN the i get exception

	}

	// Test - Post 3 valid users and 1 invalid user
	@Test(expected = Exception.class)
	public void testPostValidAndInValidUsers() throws Exception {
		// GIVEN the database only contains an admin that belong to our smartspace

		// WHEN I post 3 valid users and 1 invalid user

		int size = 3;
		List<UserBoundary> newBoundaries = IntStream.range(1, size + 1).mapToObj(i -> i)
				.map(j -> new UserBoundary("name" + j, "avatar" + j, "PLAYER", 5)).collect(Collectors.toList());

		for (int i = 0; i < newBoundaries.size(); i++)
			newBoundaries.get(i).setKey(new UserKey("2019b.notmeytal", "email@" + i));

		UserBoundary invalidUserBoundary = new UserBoundary("name10", "avatar10", "PLAYER", 5);
		invalidUserBoundary.setKey(new UserKey("2019b.meytal", "@email"));

		newBoundaries.add(invalidUserBoundary);

		restTemplate.postForObject(baseUrl, newBoundaries, UserBoundary[].class);

		// THEN i get exception and transaction is canceled

	}
	
	
	

	
}
