package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
import smartspace.data.util.EntityFactory;
import smartspace.layout.ElementBoundary;
import smartspace.layout.NewUserFormBoundary;
import smartspace.layout.UserBoundary;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class UserRest1IntegrationTests {
	@LocalServerPort
	private int port;
	private String baseUrl;
	private RestTemplate restTemplate;
	private AdvancedUserDao<String> userDao;
	private EntityFactory factory;
	private ArrayList<String> avatars;
	
	@Autowired
	public void setUserDao(AdvancedUserDao<String> userDao) {
		this.userDao = userDao;
	}

	
	@Autowired
	public void setFactory(EntityFactory factory) {
		this.factory = factory;
	}
	
	
	@PostConstruct
	public void init() {
		this.userDao.deleteAll();
		this.baseUrl = "http://localhost:" + port + "/smartspace/users";
		this.restTemplate = new RestTemplate();
	}
	
	
	@After
	public void teardown() {
		this.userDao.deleteAll();
	}
	
	
	
	@Test
	public void testPutWithValidId() throws Exception {
		// GIVEN the database contains a user
		long originalPoints;
		String originalEmail;
		String originalSmartspace;
		UserBoundary original = new UserBoundary(
				this.userDao
					.create(this.factory.createNewUser("player@mail.com", 
							"not matter", "player_1", ":-)", UserRole.PLAYER, 5000)));
					
		
		
		// WHEN we PUT an update for this user
		original.setAvatar("after_avatar");
		original.setUsername("after userName");
		original.setRole("MANAGER");
		originalPoints=original.getPoints();
		originalEmail=original.getKey().getEmail();
		originalSmartspace=original.getKey().getSmartspace();
		original.setPoints(5000);
		original.setKey(null);
		this.restTemplate
			.put(this.baseUrl+"/login/{userSmartspace}/{userEmail}", original, originalSmartspace,originalEmail);
		
		// THEN the user is updated in the db
		String key = originalSmartspace+"|"+originalEmail; 
		assertThat(this.userDao.readById(
				key))
			.isNotNull()
			.isPresent()
			.get()
			.extracting("userSmartspace", "username", "avatar", "role", "points","userEmail")
			.containsExactly(originalSmartspace, original.getUsername(), original.getAvatar(), UserRole.MANAGER, originalPoints,originalEmail);
	}
	
	
	@Test(expected=Exception.class)
	public void testPutWithNonExistingUser() throws Exception {
		// GIVEN the database is empty
		
		// WHEN we PUT a non existing user
		this.restTemplate
		.put(this.baseUrl+"/login/{userSmartspace}/{userEmail}", new UserBoundary(), "smartSpace","mail");
		
		// THEN an exception is thrown
	}
	
	
	
	@Test
	public void testInsertUsingRestAndGetUserUsingRest() throws Exception{
		// GIVEN the database is empty
		
		// WHEN I POST user
		
		NewUserFormBoundary newUserFormBoundary= new NewUserFormBoundary("email.test", "PLAYER", "username TEST", "avatar TEST");
		  UserBoundary userBoundry= this.restTemplate
				.postForObject(
						this.baseUrl, 
						newUserFormBoundary, 
						UserBoundary.class);
			
			// THEN the database contains the new user
			UserEntity entity = userBoundry.toEntity();
			assertThat(this.userDao.readById(entity.getKey()))
			.isPresent()
			.get()
			.extracting("userEmail", "role", "username", "avatar")
			.containsExactly("email.test", UserRole.PLAYER,"username TEST","avatar TEST" );
					

		// AND I get the  same user with GET
			UserBoundary newUserBoundary =this.restTemplate
			.getForObject(
					this.baseUrl+"/login/"+entity.getUserSmartspace()+"/"+entity.getUserEmail(), 
					UserBoundary.class);
		
		assertThat(newUserBoundary).extracting("username", "avatar", "role", "points")
		.containsExactly("username TEST","avatar TEST","PLAYER",-1L);
		
	}
	
	@Test
	public void testGetOnlinePlayers() {
		//GIVEN the database contains online players and user manager
		createOnlinePlayers();
		
		//WHEN I get online players with get
		UserBoundary[] onlinePlayers = this.restTemplate
				.getForObject(this.baseUrl+"/getOnlinePlayers/2019b.meytal/player5@test.com", UserBoundary[].class);
		
		//THEN I get all the online players sorting by name
		Comparator<UserBoundary> alphabeticalSortingName = new Comparator<UserBoundary>() {
	        public int compare(UserBoundary user1, UserBoundary user2) {
	          return user1.getUsername().compareTo(user2.getUsername());
	        }
	      };
	      
		assertThat(onlinePlayers).isSortedAccordingTo(alphabeticalSortingName);
	}


	private void createOnlinePlayers() {
		avatars = new ArrayList<String>(Arrays.asList(":-)","^_^",":D","@_@","$,$"));
		for(int i=0 ; i<avatars.size(); i++) {
			this.userDao
			.create(this.factory.createNewUser("player" + (i+1) + "@test.com", "2019b.meytal",
					"player" + (i+1) , avatars.get(i), UserRole.PLAYER, 1000));
		}
		
	}
	
	
	
	
}
