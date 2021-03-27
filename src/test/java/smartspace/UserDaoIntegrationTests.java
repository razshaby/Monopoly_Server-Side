//package smartspace;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.ArrayList;
//
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import smartspace.dao.memory.MemoryUserDao;
//import smartspace.data.UserEntity;
//import smartspace.data.UserRole;
//import smartspace.data.util.EntityFactoryImpl;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class UserDaoIntegrationTests {
//	MemoryUserDao userDao; 
//	
//	@Autowired
//	public UserDaoIntegrationTests() {
//		userDao =  new MemoryUserDao();
//		
//	}
//	
//	@Before
//	public void setup() {
//		userDao.deleteAll();
//	}
//	
//	@After
//	public void teardown() {
//		userDao.deleteAll();
//	}
//	
//	@Test
//	public void create5users() throws Exception{
//		// GIVEN we have a dao
//		
//		//WHEN we create 5 users
//		ArrayList<UserEntity>list = new ArrayList<UserEntity>();
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		for(int i=1 ; i<6 ; i++) {
//			UserEntity user = entityFactory.createNewUser("Mail "+i, "", i + "", "", UserRole.PLAYER, i * 100);
//			userDao.create(user);
//			list.add(user);
//		}
//		//THEN the dao contains those exactly 5 users
//		assertThat(this.userDao.readAll())
//		.usingElementComparatorOnFields("key")
//		.containsExactlyInAnyOrderElementsOf(list);
//	}
//
//	@Test
//	public void testCreateUserAndUpdateAndDeleteAll() throws Exception{
//		//GIVEN User Dao
//		
//		//when create user
//		//and update user
//		//and delete all users
//		//and read users
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		String email = "example@gmail.com";
//		String smartspace = "Monopoly";
//		String name = "Moshe";
//		String avatar = ":)";
//		UserRole role = UserRole.PLAYER;
//		long points = 100;
//		
//		UserEntity userEntity = entityFactory.createNewUser(email, smartspace, name, avatar, role, points);
//		UserEntity insertedEntity = userDao.create(userEntity);
//		
//		
//		insertedEntity.setAvatar("@_@");
//		insertedEntity.setPoints(90);
//		insertedEntity.setKey(userEntity.getKey());
//		userDao.update(insertedEntity);
//		
//		insertedEntity = this.userDao.readById(insertedEntity.getKey())
//				.orElseThrow(()->new RuntimeException("message is not available after update"));
//
//		userDao.deleteAll();
//		
//		// THEN The DAO is Empty 
//		assertThat(this.userDao.readAll())
//		.isEmpty();
//		
//		// AND The generated message has an ID which is not available in the DAO
//		assertThat(insertedEntity)
//		.extracting("key")
//		.isNotNull();
//	
//	}
//	
//
//}
