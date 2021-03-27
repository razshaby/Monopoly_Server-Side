package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import smartspace.dao.UserDao;
import smartspace.data.UserEntity;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoRdbIntegrationTests {

	private UserDao<String> userDao;
	private String userSmartspace;

	@Value("${smartspace.name}")
	public void setUserSmartspace(String userSmartspace) {
		this.userSmartspace = userSmartspace;
	}
	
	
	@Autowired
	public void setUserDao(UserDao<String> userDao) {
		this.userDao = userDao;
	}

//	@Before
//	public void setup() {
//		this.userDao.deleteAll();
//	}

	@After
	public void teardown() {
		this.userDao.deleteAll();
	}

	
	@Test
	public void testCreate5Users() throws Exception {
		// GIVEN we have a dao

		// WHEN we create 5 new users and insert them
		List<UserEntity> list = IntStream.range(1, 6) // int Stream
				.mapToObj(num -> "mail" + num+"@afeka.ac.il")// String Stream
				.map(UserEntity::new) // UserEntity Stream
				.peek(user->user.setUserSmartspace(userSmartspace))// UserEntity Stream
				.map(this.userDao::create) // UserEntity Stream
				.collect(Collectors.toList()); // List<UserEntity>

		// THEN the dao contains 5 users
		// AND the users created available through the dao
		assertThat(this.userDao.readAll()).usingElementComparatorOnFields("userEmail")
				.containsExactlyInAnyOrderElementsOf(list);
	}
	
	@Test
	public void testCreateAndUpdateAndGetByKeyAndDeleteAllAndReadAll () throws Exception{
		// GIVEN we have a dao
				
		// WHEN I Create a User
		// And Update the User
		// And Get User By Id
		// And Delete all Users
		// And Read All Users
		String theName = "Test";
		UserEntity insertedUser =  this.userDao.create(new UserEntity(theName));
		
		String theUpdatedName = "Test2"; 
		UserEntity update = new UserEntity();
		update.setKey(insertedUser.getKey());
		update.setUsername(theUpdatedName);
		this.userDao.update(update);
		
		insertedUser = this.userDao.readById(insertedUser.getKey())
				.orElseThrow(()->new RuntimeException("user is not available after update"));
		
		this.userDao.deleteAll();
		
		
		// THEN The DAO is Empty 
		// AND The generated user has an ID which is not available in the DAO
		assertThat(this.userDao.readAll())
			.isEmpty();
		
		assertThat(insertedUser)
			.extracting("username")
			.containsExactly(theUpdatedName);
		
//		assertThat(insertedUser)
//			.extracting("userKey")
//			.isNotNull();
		
		assertThat(insertedUser.getKey()).isNotNull();

		
		
	}
	
	@Test
	public void testCreatUserWithValidKey() throws Exception{
		//GIVEN nothing
		
		//WHEN create a user 
		String userEmail = "foo@example.com";
		UserEntity user = new UserEntity();
		user.setUserEmail(userEmail);
		user.setUserSmartspace(userSmartspace);
		UserEntity insertedUser = userDao.create(user);
		
		//THEN a valid user key is created
		assertThat(insertedUser.getKey()).matches(Pattern.compile(userSmartspace+"\\|"+userEmail));
	}
	
	@Test(expected=Exception.class)
	public void testGetExceptionWhenReadByInvalidId() throws Exception{
		//GIVEN nothing
		
		//WHEN create a user with id and trying to read with invalid id 
		String theName = "Test";
		UserEntity insertedUser =  this.userDao.create(new UserEntity(theName));
		
		
		//THEN throws exception
		insertedUser = this.userDao.readById("InvalidKey")
				.orElseThrow(()->new RuntimeException("user is not available"));
		
	}
//	@Test
//	public void testCreateUserAndCheckIfSameIdAfterCreate () throws Exception{
//		// GIVEN we have a dao
//				
//		// WHEN I Create a User
//		// And put user in database
//		// I read user by key from database
//		// And check if user ID and Email are the same before and after insertion
//
//		String userEmail = "foo@example.com";
//		UserEntity user = new UserEntity();
//		user.setUserEmail(userEmail);
//		user.setUserSmartspace(userSmartspace);
//		
//		UserEntity insertedUser =  this.userDao.create(user);
//		
//		
//		UserEntity userFromDataBase = this.userDao.readById(insertedUser.getKey())
//				.orElseThrow(()->new RuntimeException("cant get user with key "+insertedUser.getKey()));
//		
//		assertThat(userFromDataBase.getUserSmartspace())
//		.isEqualTo(userSmartspace);
//		
//		assertThat(userFromDataBase.getUserEmail())
//		.isEqualTo(user.getUserEmail());
//		
//
//		this.userDao.deleteAll();
//		
//		
//		// THEN The DAO is Empty 
//		
//	}
	
}
